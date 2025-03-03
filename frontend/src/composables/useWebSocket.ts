import { ref, onMounted, onBeforeUnmount } from 'vue';

interface Body {
  x: number;
  y: number;
  z: number;
  blackHole: boolean;
  mass: number;
  vx: number;
  vy: number;
  vz: number;
}

// Variables singleton partagées
let ws: WebSocket | null = null;
const bodies = ref<Body[]>([]);
let connectionAttempts = 0;
const maxAttempts = 5;

function connect() {
  if (ws?.readyState === WebSocket.OPEN) {
    console.log('WebSocket already connected');
    return;
  }

  if (connectionAttempts >= maxAttempts) {
    console.error('Max connection attempts reached');
    return;
  }

  ws = new WebSocket('ws://localhost:8080/nbody');

  ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    if (data.bodies) {
      bodies.value = data.bodies;
    }
  };

  ws.onclose = () => {
    console.log('WebSocket closed, attempting to reconnect...');
    ws = null;
    connectionAttempts++;
    setTimeout(connect, 1000);
  };

  ws.onerror = (error) => {
    console.error('WebSocket error:', error);
  };

  ws.onopen = () => {
    console.log('WebSocket connected');
    connectionAttempts = 0;
    sendMessage({ type: 'fps', fps: 60 });
  };
}

function sendMessage(message: any) {
  if (ws?.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(message));
  } else {
    console.warn('WebSocket is not open, message not sent:', message);
  }
}

export function useWebSocket() {
  onMounted(() => {
    if (!ws) {
      connect();
    }
  });

  onBeforeUnmount(() => {
    // Ne ferme la connexion que si c'est le dernier composant à utiliser le WebSocket
    if (ws && document.querySelectorAll('[data-v-usewebsocket]').length <= 1) {
      ws.close();
      ws = null;
      connectionAttempts = 0;
    }
  });

  return {
    sendMessage,
    bodies
  };
}
