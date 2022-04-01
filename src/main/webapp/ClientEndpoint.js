/**
 * TODO
 */
class ClientEndpoint {

    /**
     * TODO
     * @param {string} channel 
     */
    constructor(channel, autoReconnect) {
        this.channel = channel;
        this.autoReconnect = autoReconnect;
        this.eventHandlers = {}
    }

    /**
     * TODO
     */
    connect() {
        this.socket = new WebSocket(this.#getUrl());
        this.connectionOpen = true;
        this.#attachHandlers();
    }

    /**
     * TODO
     */
    disconnect() {
        if (!!this.socket) {
            this.connectionOpen = false;
            this.socket.close();
        }
    }

    /**
     * TODO
     */
    reconnect() {
        this.disconnect();
        this.connect();
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
     * @param {string} data 
     */
    send(type, user, wm, content) {
        if (this.isOpen()) {
            let target = user;
            if (wm) target += "|" + wm;
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
                if (!!this.socket) this.socket.onopen = handler;
                else this.onOpenHandler = handler;
                break;
            case 'error':
                if (!!this.socket) this.socket.onerror = handler;
                else this.onErrorHandler = handler;
                break;
            case 'message':
                if (!!this.socket) this.socket.onmessage = handler;
                else this.onMessageHandler = handler;
                break;
            case 'close':
                if (!!this.socket) this.socket.onclose = (event) => this.#innerOnClose(event, handler);
                else this.onCloseHandler = (event) => this.#innerOnClose(event, handler);
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
        let channel = this.channel;
        if (!channel.startsWith('/')) channel = `/${channel}`;
        return `${protocol}${location.host}${contextName}${channel}`;
    }

    #attachHandlers() {
        if (!!this.onOpenHandler) this.socket.onopen = this.onOpenHandler;
        if (!!this.onErrorHandler) this.socket.onerror = this.onErrorHandler;
        if (!!this.onMessageHandler) this.socket.onmessage = this.onMessageHandler;
        if (!!this.onCloseHandler) this.socket.onclose = this.onCloseHandler;
    }

    #innerOnClose(event, handler) {
        handler(event);
        if (this.connectionOpen) this.reconnect();
    }
}