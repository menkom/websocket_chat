'use strict';

const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const usernameForm = document.querySelector('#usernameForm');
const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const messageArea = document.querySelector('#messageArea');
const connectingElement = document.querySelector('.connecting');
const disconnectButton = document.querySelector('#disconnect');
const headerLabel = document.querySelector('#chat-header-label');
let stompClient = null;
let username = null;
let sessionId = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

// Использует SockJS  и клиент Stomp, чтобы подключиться к конечной точке /user-control, которую мы настроили в Spring Boot
function connect(event) {
    username = document.querySelector('#name').value.trim();
    if (username) {
        const socket = new SockJS('/user-control');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}

// После успешного подключения, клиент подписывается на адрес /topic/public и сообщает серверу имя пользователя по адресу /app/chat.addUser.
function onConnected() {
    // При подключении к серверу клиенту выдаётся sessionId. Правильнее использовать именно sessionId, а не username,
    // т.к. под одним именем может быть несколько пользователей.
    sessionId = /\/([^\/]+)\/websocket/.exec(stompClient.ws._transport.url)[1];
    usernamePage.classList.add('hidden');
    chatPage.classList.remove('hidden');
    // Функция stompClient.subscribe() принимает аргументом callback-функцию,
    // которая вызывается каждый раз, когда в тему приходит сообщение.
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);
    stompClient.subscribe('/user/' + sessionId + '/message', onMessageReceived);
    headerLabel.textContent = 'Chat user: ' + username;

    // Tell your username to the server
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    )
    connectingElement.classList.add('hidden');
}


function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}


function sendMessage(event) {
    var messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}


function onMessageReceived(payload) {
    console.log('onMessageReceived', payload);
    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else if (message.type === 'COMMAND') {
        if (message.sender === sessionId) {
            disconnect();
        }
        messageElement.classList.add('event-message');
        message.content = 'Command "' + message.content + '" for ' + message.sender + ' send!';
    } else {
        messageElement.classList.add('chat-message');

        const avatarElement = document.createElement('i');
        const avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}


function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }

    var index = Math.abs(hash % colors.length);
    return colors[index];
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    usernamePage.classList.remove('hidden');
    chatPage.classList.add('hidden');
    messageArea.textContent = '';

    console.log("Disconnected");
}

usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)
disconnectButton.onclick = disconnect;
