import { Client } from '@stomp/stompjs';

export function createSessionStateClient(roomCode, onState, onStatusChange) {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
  const wsUrl = `${protocol}://${window.location.host}/ws`;

  const client = new Client({
    brokerURL: wsUrl,
    reconnectDelay: 3000,
    onConnect: () => {
      onStatusChange?.('connected');
      client.subscribe(`/topic/sessions/${roomCode}/state`, (message) => {
        onState(JSON.parse(message.body));
      });
    },
    onDisconnect: () => onStatusChange?.('disconnected'),
    onStompError: () => onStatusChange?.('error'),
    onWebSocketError: () => onStatusChange?.('error'),
  });

  return client;
}
