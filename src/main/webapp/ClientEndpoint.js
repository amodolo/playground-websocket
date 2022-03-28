/**
 * TODO
 */
class ClientEndpoint {

    /**
     * TODO
     * @param {string} channel 
     */
    constructor(channel) {
        this.channel = channel;
        this.eventHandlers = {}
    }

    /**
     * TODO
     */
    connect() {
        this.socket = new WebSocket(this.#getUrl());
        this.#attachHandlers();
    }

    /**
     * TODO
     */
    disconnect() {
        clearInterval(this.heartBeatInterval);
        if (!!this.socket) this.socket.close();
    }

    /**
     * TODO
     */
    reconnect() {
        // TODO
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
    send(type, content, to) {
        if (this.isOpen()) this.socket.send(JSON.stringify({
            "type": type,
            "content": content,
            "sender": user,
            "target": to
        }));
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
                if (!!this.socket) this.socket.onclose = handler;
                else this.onCloseHandler = handler;
                break;
            default:
            // nothing to do
        }
    }

    #getUrl() {
        let protocol = 'https:' === location.protocol.toLowerCase() ? 'wss://' : 'ws://';
        let contextName = location.pathname.slice(1).split('/')[0];
        return `${protocol}${location.host}/${contextName}/${this.channel}`;
    }

    #attachHandlers() {
        if (!!this.onOpenHandler) this.socket.onopen = this.onOpenHandler;
        if (!!this.onErrorHandler) this.socket.onerror = this.onErrorHandler;
        if (!!this.onMessageHandler) this.socket.onmessage = this.onMessageHandler;
        if (!!this.onCloseHandler) this.socket.onclose = this.onCloseHandler;
    }
}