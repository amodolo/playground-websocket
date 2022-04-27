let client;
let callerUser;
let callerWM;

function connect() {
    let modal;
    client = new ClientEndpoint({logEnabled: logEnabled});
    client.on('message', (event) => {
        console.log(`Received message: ${event.data}`);
        const message = JSON.parse(event.data);

        switch (message.action) {
            case 'CANCEL_CALL':
                hideCallModal(modal);
                logMissedCall(message);
                break;
            case 'CALL_RESPONSE':
                logCallResponse(message);
                hideCallModal(modal); // to dismiss all panel of the receiver user
                break;
            case 'CALL':
                modal = new bootstrap.Modal(document.getElementById('call-modal'), {})
                let sender = message.sender.split('_');
                callerUser = sender[0];
                callerWM = sender[1];
                console.log(`caller user is ${callerUser}; caller WM is ${callerWM}`);
                showCallModal(modal, message);
                break;
            case 'TEXT':
                logMessage(message);
                break;
        }
    });
    client.on("open", (event) => {
        document.getElementById("disconnect-btn").disabled = false;
        document.getElementById("connect-btn").disabled = true;
    });
    client.on("close", (event) => {
        document.getElementById("disconnect-btn").disabled = true;
        document.getElementById("connect-btn").disabled = false;
    });
    client.connect();
}

function disconnect() {
    if (client) {
        client.disconnect();
        delete client;
    }
}

function send() {
    var content = document.getElementById("msg").value;
    var user = document.getElementById("user").value;
    var wm = document.getElementById("wm").value;

    client.sendMessage(user, wm, content)
    return true;
}

function call() {
    var user = document.getElementById("user").value;
    var wm = document.getElementById("wm").value;

    client.call(user, wm)
    return true;
}

function closeCall() {
    var user = document.getElementById("user").value;
    var wm = document.getElementById("wm").value;

    client.cancelCall(user, wm)
    return true;
}

function acceptCall() {
    client.sendCallResponse(callerUser, callerWM, true)
    client.sendCallResponse(userId, null, true)
}

function rejectCall() {
    client.sendCallResponse(callerUser, callerWM, false)
    client.sendCallResponse(userId, null, false)
}

function logout() {
    client.disconnect();
}

function showCallModal(modal, message) {
    let log = document.getElementById("log");
    let sender = message.sender.split('_');
    let user = sender[0];
    document.getElementById('call-modal-title').innerHTML = "Incoming call from user " + user;
    document.getElementById('call-modal-text').innerHTML = message.content;
    modal.show();
    log.innerHTML += `incoming call from user ${user}\n`;
}

function hideCallModal(modal) {
    if (!!modal) modal.hide();
}

function logMissedCall(message) {
    let log = document.getElementById("log");
    let sender = message.sender.split('_');
    let user = sender[0];
    let wm = sender[1];
    log.innerHTML += `missed call from user ${user}\n`;
}

function logMessage(message) {
    let sender = message.sender.split('_');
    let user = sender[0];
    let wm = sender[1];
    log.innerHTML += `user ${user} from ${wm} says: ${message.content}\n`;
}

function logCallResponse(message) {
    let sender = message.sender.split('_');
    let user = sender[0];
    let wm = sender[1];
    let accepted = message.content;

    log.innerHTML += `user ${user} from ${wm} has ${accepted ? 'accepted' : 'rejected'} the call\n`;
}