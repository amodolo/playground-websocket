let client;

function connect() {
    let modal;
    client = new ClientEndpoint(`/web-channel/${userId}/${wmId}`);
    client.on('message', (event) => {
        var log = document.getElementById("log");
        console.log(event.data);
        var message = event.data.startsWith("{") ? JSON.parse(event.data) : event.data;
        if (message.type === "CALL") {
            modal = new bootstrap.Modal(document.getElementById('call-modal'), {})
            showCallModal(modal, message);
        } else if (message.type === "CALL_MISSED") {
            hideCallModal(modal);
            logMissedCall(message);
        } else if (message.type === "CALL_ACCEPTED" || message.type === "CALL_REJECTED") {
            hideCallModal(modal);
        } else if (message.type === "MSG") {
            logMessage(message);
        } else {
             log.innerHTML += message + "\n";
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

    client.send("MSG", user, wm, content)
    return true;
}

function call() {
    var content = document.getElementById("msg").value;
    var user = document.getElementById("user").value;
    var wm = document.getElementById("wm").value;

    client.send("CALL", user, wm, content)
    return true;
}

function closeCall() {
    var user = document.getElementById("user").value;
    var wm = document.getElementById("wm").value;

    client.send("CALL_MISSED", user, wm, null)
    return true;
}

function acceptCall() {
    client.send("CALL_ACCEPTED", userId, null, null)
}

function rejectCall() {
    client.send("CALL_REJECTED", userId, null, null)
}

function logout() {
    client.disconnect();
}

function showCallModal(modal, message) {
    let log = document.getElementById("log");
    let sender = message.sender.split('|');
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
    let sender = message.sender.split('|');
    let user = sender[0];
    let wm = sender[1];
    log.innerHTML += `missed call from user ${user}\n`;
}

function logMessage(message) {
    let sender = message.sender.split('|');
    let user = sender[0];
    let wm = sender[1];
    log.innerHTML += `user ${user} from ${wm} says: ${message.content}\n`;
}