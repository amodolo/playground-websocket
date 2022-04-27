<!DOCTYPE html>
<%@ page session="false" %>
<html lang="en" class="h-100">

    <head>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>websocket playground</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.8.1/font/bootstrap-icons.css">
    </head>

    <body class="d-flex flex-column h-100">
        <nav class="navbar navbar-light bg-light mb-3">
            <div class="w-100 d-flex px-3 align-items-center">
                <div class="flex-fill">
                    <a class="navbar-brand" href="#">
                        Welcome <%=request.getAttribute("name")%>
                            <%=request.getAttribute("surname")%>
                    </a>
                    <br>
                    <span class="navbar-text">
                        <%=request.getAttribute("node")%>
                    </span>
                </div>
                <form method="post" class="m-0">
                    <button class="btn btn-success" type="button" id="connect-btn" onclick="connect()" disabled>
                        Connect
                    </button>
                    <button class="btn btn-secondary" type="button" id="disconnect-btn" onclick="disconnect()" disabled>
                        Disconnect
                    </button>
                    <button class="btn btn-danger" type="submit" onclick="logout()">
                        <i class="bi bi-box-arrow-left"></i>
                    </button>
                </form>
            </div>
        </nav>
        <div class="container-fluid flex-fill d-flex flex-column">
            <div class="container-fluid">
                <div class="row mb-3">
                    <div class="col-sm-6">
                        <select class="form-select" id="user">
                            <option value="1">user1</option>
                            <option value="2">user2</option>
                            <option value="3">user3</option>
                            <option value="4">user4</option>
                            <option value="5">user5</option>
                        </select>
                    </div>
                    <div class="col-sm-6">
                        <input type="text" id="wm" class="form-control" placeholder="view id...">
                    </div>
                </div>

                <div class="input-group mb-3">
                    <input type="text" class="form-control" placeholder="message..." id="msg">
                    <button type="button" class="btn btn-primary" id="send" onclick="send();">
                        <i class="bi bi-send"></i>
                    </button>
                    <button type="button" class="btn btn-success" id="call" onclick="call();">
                        <i class="bi bi-telephone"></i>
                    </button>
                    <button type="button" class="btn btn-danger" id="call" onclick="closeCall();">
                        <i class="bi bi-telephone"></i>
                    </button>
                </div>
            </div>
            <div class="flex-fill mb-3">
                <textarea name="log" id="log" class="form-control h-100" readonly></textarea>
            </div>
        </div>

        <div class="modal fade" id="call-modal" data-bs-backdrop="static" data-bs-keyboard="false">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="call-modal-title" />
                    </div>
                    <div class="modal-body">
                        <span id="call-modal-text" />
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-danger" data-bs-dismiss="modal" onclick="rejectCall()">Reject</button>
                        <button type="button" class="btn btn-success" data-bs-dismiss="modal" onclick="acceptCall()">Accept</button>
                    </div>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
        <script src="../ClientEndpoint.js"></script>
        <script src="../main.js"></script>
        <script>
            userId = <%=request.getAttribute("id") %>
            wmId = '<%=request.getAttribute("wmId") %>'
            logEnabled = <%=request.getAttribute("logEnabled") %>;
            document.title += " "+wmId;
            connect();
        </script>
    </body>

    </html>