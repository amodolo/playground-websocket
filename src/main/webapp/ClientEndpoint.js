/**
 * TODO
 */
class ClientEndpoint {

     #options = {
         autoReconnect: true,
         maxRetries: 10
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
        if (!!this.socket) {
            this.#connectionOpen = false;
            this.socket.close();
            window.sessionStorage.setItem('CONNECTION_STATUS', this.#status.DISCONNECTED);
        }
    }

    /**
     * TODO
     */
    #reconnect() {
        if (this.#retry++ < this.#options.maxRetries) {
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
        this.#send("CANCEL_CALL", user, wm, null);
    }

    /**
     * TODO
     * @param {*} target 
     * @param {*} accepted 
     */
    sendCallResponse(user, wm, accepted) {
        this.#send("CALL_RESPONSE", user, wm, accepted);
    }

    /**
     * TODO
     * @param {*} target 
     */
    call(user, wm) {
        this.#send("CALL", user, wm, null);
    }

    /**
     * TODO
     * @param {*} target 
     */
    sendMessage(user, wm, msg) {
        this.#send("TEXT", user, wm, msg);
    }

    /**
     * TODO
     * @param {string} data 
     */
    #send(type, user, wm, content) {
        if (this.isOpen()) {
            let target = user;
            if (wm) target += "_" + wm;
            this.socket.send(JSON.stringify({
                "type": type,
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
        return `${protocol}${location.host}${contextName}${channel}`;
    }

    #onOpen(event) {
        this.#retry = 0;
        this.#currentStatus = this.#status.CONNECTED;
        if (!!this.onOpenHandler) this.onOpenHandler(event);
    }

    #onClose(event) {
        debugger;
        this.socket = null;
        this.#currentStatus = this.#status.DISCONNECTED;
        if (!!this.onCloseHandler) this.onCloseHandler(event);
        if (this.#connectionOpen) this.reconnect();
    }

    #onMessage(event) {
        let type = JSON.parse(event.data).type;
        if (type === 'callResponse') {
            this.#currentStatus = this.#status.CONNECTED;
        }
        if (!!this.onMessageHandler) this.onMessageHandler(event);
    }

    #onError(event) {
        // TODO log
        if (!!this.onErrorHandler) this.onErrorHandler(event);

    }
}
