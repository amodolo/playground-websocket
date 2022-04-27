/**
 * TODO
 */
class ClientEndpoint {

     #options = {
         autoReconnect: true,
         maxRetries: 10,
         logEnabled: true
     };
     #connectionOpen = false;
     #retry = 0;

     #status = { DISCONNECTED: 0, CONNECTED: 1, AWAITING_RESPONSE: 2 };
     #currentStatus = this.#status.DISCONNECTED;

    /**
     * TODO
     * @param {string} channel 
     */
    constructor(options = {}) {
        this.#options = {...this.#options, ...options};
        this.eventHandlers = {}
        const savedStatus = parseInt(window.sessionStorage.getItem('CONNECTION_STATUS'));
        window.addEventListener('beforeunload', this.disconnect);
        if (savedStatus !== this.#status.DISCONNECTED) {
            this.connect();
        }
    }

    /**
     * TODO
     */
    connect() {
        this.#log("connect()");
        this.disconnect();
        this.socket = new WebSocket(this.#getUrl());
        this.socket.onopen = this.#onOpen.bind(this);
        this.socket.onmessage = this.#onMessage.bind(this);
        this.socket.onclose = this.#onClose.bind(this);
        this.socket.onerror = this.#onError.bind(this);
        this.#connectionOpen = true;
        window.sessionStorage.setItem('CONNECTION_STATUS', this.#status.CONNECTED);
    }

    /**
     * TODO
     */
    disconnect() {
        this.#log("disconnect()");
        if (!!this.socket) {
            this.#connectionOpen = false;
            this.socket.close();
            this.#currentStatus = this.#status.DISCONNECTED;
            window.sessionStorage.setItem('CONNECTION_STATUS', this.#status.DISCONNECTED);
        }
    }

    /**
     * TODO
     */
    #reconnect() {
        this.#log("reconnect()");
        if (this.#retry++ < this.#options.maxRetries) {
            this.#log(`retry is ${this.#retry}`);
            this.connect();
        }
    }

    /**
     * TODO
     * @returns 
     */
    getReadyState() {
        return !!this.socket ? this.socket.readyState : null;
    }

    /**
     * TODO
     * @returns
     */
    isOpen() {
        return this.getReadyState() === 1;
    }

    /**
     * TODO
     * @param {*} target 
     */
    cancelCall(user, wm) {
        this.#log(`cancelCall(user=${user}, wm=${wm})`);
        this.#send("CANCEL_CALL", user, wm, null);
        this.#currentStatus = this.#status.CONNECTED;
    }

    /**
     * TODO
     * @param {*} target 
     * @param {*} accepted 
     */
    sendCallResponse(user, wm, accepted) {
        this.#log(`sendCallResponse(user=${user}, wm=${wm}, accepted=${accepted})`);
        this.#send("CALL_RESPONSE", user, wm, accepted);
    }

    /**
     * TODO
     * @param {*} target 
     */
    call(user, wm) {
        this.#log(`call(user=${user}, wm=${wm})`);
        this.#send("CALL", user, wm, null);
        this.#currentStatus = this.#status.AWAITING_RESPONSE;
    }

    /**
     * TODO
     * @param {*} target 
     */
    sendMessage(user, wm, msg) {
        this.#log(`sendMessage(user=${user}, wm=${wm}, msg=${msg})`);
        this.#send("TEXT", user, wm, msg);
    }

    /**
     * TODO
     * @param {string} data 
     */
    #send(action, user, wm, content) {
        this.#log(`send(action=${action}, user=${user}, wm=${wm}, content=${content})`);
        if (this.isOpen()) {
            let target = `${user}`;
            if (wm) target += "_" + wm;
            this.socket.send(JSON.stringify({
                "action": action,
                "content": content,
                "target": target
            }));
        }
    }

    /**
     * 
     * @param {string} event 
     * @param {function} handler 
     */
    on(event, handler) {
        switch (event) {
            case 'open':
                this.onOpenHandler = handler;
                break;
            case 'error':
                this.onErrorHandler = handler;
                break;
            case 'message':
                this.onMessageHandler = handler;
                break;
            case 'close':
                this.onCloseHandler = handler;
                break;
            default:
            // nothing to do
        }
    }

    #getUrl() {
        let protocol = 'https:' === location.protocol.toLowerCase() ? 'wss://' : 'ws://';
        let contextName = location.pathname.slice(1);
        let i = contextName.lastIndexOf('/');
        contextName = i !==-1 ? `/${contextName.substring(0,i)}` : `/${contextName}`
        let channel = `/pipe/${wmId}`;
        let url = `${protocol}${location.host}${contextName}${channel}`;
        this.#log(`Web-socket URL is ${url}`);
        return url;
    }

    #onOpen(event) {
        this.#log(`onOpen(event=${event.data})`);
        this.#retry = 0;
        this.#currentStatus = this.#status.CONNECTED;
        if (!!this.onOpenHandler) this.onOpenHandler(event);
    }

    #onClose(event) {
        this.#log(`onClose(event=${event.data})`);
        this.socket = null;
        this.#currentStatus = this.#status.DISCONNECTED;
        if (!!this.onCloseHandler) this.onCloseHandler(event);
        if (this.#connectionOpen) this.#reconnect();
    }

    #onMessage(event) {
        this.#log(`onMessage(event=${event.data})`);
        let action = JSON.parse(event.data).action;
        if (action === 'CALL_RESPONSE') {
            this.#currentStatus = this.#status.CONNECTED;
        }
        if (!!this.onMessageHandler) this.onMessageHandler(event);
    }

    #onError(event) {
        this.#log(`onError(event=${event.data})`);
        if (!!this.onErrorHandler) this.onErrorHandler(event);
    }

    #log(message) {
        if (!!this.#options.logEnabled) console.log(message);
    }
}
