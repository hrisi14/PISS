const ws = new WebSocket('ws://localhost:8081');

ws.onopen = function() {
    console.log('Connected to WebSocket server');
    ws.send('Hello from client!');
};

ws.onmessage = function(event) {
    console.log('Push received:', event.data);
    const messagesDiv = document.getElementById('push-messages');
    if (messagesDiv) {
        const msgElem = document.createElement('div');
        let msg = event.data;
        let cssClass = 'push-msg';

        if (msg.startsWith('[info]')) {
            cssClass += ' info';
            msg = msg.replace('[info]', '').trim();
        } else if (msg.startsWith('[error]')) {
            cssClass += ' error';
            msg = msg.replace('[error]', '').trim();
        } else if (msg.startsWith('[success]')) {
            cssClass += ' success';
            msg = msg.replace('[success]', '').trim();
        }

        msgElem.textContent = msg;
        msgElem.className = cssClass;
        messagesDiv.appendChild(msgElem);
    }
};

ws.onclose = function() {
    console.log('WebSocket connection closed');
};
