let client;
function connect() {
    client = new ClientEndpoint(`/notification/${user}/${wmId}`);
    client.on('message', (event) => {
        var log = document.getElementById("log");
        console.log(event.data);
        var message = JSON.parse(event.data);
        if (message.type === "CALL") {
            document.getElementById('call-modal-text').innerHTML = message.content;
            var modal = new bootstrap.Modal(document.getElementById('call-modal'), {})
            modal.show();
        } else {
            log.innerHTML += "user" + message.sender + " : " + message.content + "\n";
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
    var to = document.getElementById("user").value;
    var wm = document.getElementById("wm").value;
    if (wm) to += "@" + wm;

    client.send("MSG", content, to)
    return true;
}

function call() {
    var content = document.getElementById("msg").value;
    var to = document.getElementById("user").value;
    var wm = document.getElementById("wm").value;
    if (wm) to += "@" + wm;

    client.send("CALL", content, to)
    return true;
}

function logout() {
    client.disconnect();
}