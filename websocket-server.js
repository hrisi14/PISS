const WebSocket = require('ws');
const express = require('express');
const bodyParser = require('body-parser');

const wss = new WebSocket.Server({ port: 8081 });
const app = express();
app.use(bodyParser.json());

app.post('/push', (req, res) => {
    const msg = req.body.message || 'No message';
    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN) {
            client.send(msg);
        }
    });
    res.sendStatus(200);
});

app.listen(8080, () => {
    console.log('HTTP push endpoint started on http://localhost:8080/push');
});

wss.on('connection', function connection(ws) {
    console.log('WebSocket client connected');
    ws.send('Welcome!');
    ws.on('message', function incoming(message) {
        console.log('received: %s', message);
    });
});

// Примерен push от сървъра към всички клиенти на всеки 10 секунди
// setInterval(() => {
//     wss.clients.forEach(client => {
//         if (client.readyState === WebSocket.OPEN) {
//             client.send('Push message from server at ' + new Date());
//         }
//     });
// }, 10000);

console.log('WebSocket server started on ws://localhost:8081');
