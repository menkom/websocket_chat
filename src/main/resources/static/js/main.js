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
    stompClient.subscribe('/user/' + sessionId + '/message', onMessageReceived);
    headerLabel.textContent = 'Chat user: ' + username;

    // Tell your username to the server
    stompClient.send("/app/newConnection",
        {},
        JSON.stringify({username: username})
    )
    connectingElement.classList.add('hidden');
}


function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}


function onMessageReceived(payload) {
    console.log('onMessageReceived', payload);
    const message = JSON.parse(payload.body);
    if (message.type === 'LOGOUT') {
        disconnect();
    }
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
disconnectButton.onclick = disconnect;
