
var baseDir = document.getElementById("basedir").name;

var consoleElement = document.getElementById("console");

function initSocket() {
    var consoleSocket = new WebSocket(window.location.origin.replace("http", "ws") + "/socket");
    consoleSocket.onmessage = function (event) {
        var strings = event.data.split(":", 2);
        if (strings.length != 2) {
            console.error("Invalid message received: " + event);
            return;
        }
        // TODO: Handle tab completions
        if (strings[0] === "message") {
            var addEl = document.createElement("p");
            addEl.appendChild(mcJsonToHtml(addEl, JSON.parse(strings[1])));
            consoleElement.appendChild(addEl);
        } else if (strings[0] === "close") {
            var addEl = document.createElement("p");
            addEl.innerHTML = "Connection closed by server: ";
            addEl.appendChild(mcJsonToHtml(addEl, JSON.parse(strings[1])))
            consoleElement.appendChild(addEl)
        }
    }

    consoleSocket.onerror = function (event) {
        setTimeout(initSocket, 5);
        var addEl = document.createElement("p");
        addEl.innerHTML("Connection closed due to error, reconnecting in 5 seconds...")
    }
}

initSocket();

function mcJsonToHtml(baseElement, jsonIn) {
    var style = baseElement.style;
    if (jsonIn.bold) {
        style.fontWeight = "bold";
    }

    if (jsonIn.italic) {
        style.fontStyle.italics = true;
    }

    if (jsonIn.underlined) {
        style.textDecoration.append("underline");

    }

    if (jsonIn.strikethrough) {
        style.textDecoration.append("line-through");
    }

    if (jsonIn.obfuscated) {
        // TODO
    }

    if (jsonIn.insertion) {

    }

    if (jsonIn.clickEvent) {

    }

    if (jsonIn.hoverEvent)

    if (jsonIn.text) {
        baseElement.innerHTML = jsonIn.text;
    } else if (jsonIn.translation) {
        var args = jsonIn.with;
    } else if (jsonIn.score) {

    } else if (jsonIn.selector) {

    }


    if (jsonIn.extra) {
        for (var i = 0; i < jsonIn.extra.length; ++i) {
            var childEl = document.createElement("span");
            mcJsonToHtml(childEl, jsonIn.extra[i]);
            baseElement.appendChild(childEl);
        }
    }

}
