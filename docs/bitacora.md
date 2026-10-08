# Bitácora · Práctica 12: Charla

## Ejercicio 0 — ¿HTTP o WebSocket?

- **Pedir entrar:** HTTP `POST /solicitudes`. El invitado aún no tiene token para abrir el WebSocket.
- **Aceptar o rechazar:** WebSocket `/chat` del anfitrión, que ya está autenticado. El anfitrión envía la orden; el invitado consulta por HTTP hasta recibir el resultado y, si se aprueba, su token.
- **Enviar mensajes:** WebSocket. Mantiene el canal abierto en ambos sentidos y reparte cada mensaje al instante.
- **Consultar la dirección del túnel:** HTTP `GET /invitacion`. Es una consulta puntual que devuelve una dirección.

## Ejercicio D1 — Evitar que una conexión vieja cambie el estado

Cada callback de OkHttp recibe el WebSocket que lo originó. Antes de modificar el estado, publicar un evento o programar un reintento, comparo ese socket con `actual`, que representa la conexión vigente:

```kotlin
if (webSocket !== actual) return
```

Así, si el cierre de una conexión anterior llega tarde, no puede marcar como desconectada ni reiniciar la conexión nueva. El callback `onClosing` completa el cierre del socket que se despide.
