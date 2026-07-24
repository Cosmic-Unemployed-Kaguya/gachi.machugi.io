
export const ServerEvent = {
    CONNECTION: 'connection',
    ERROR: 'error',
    HEADERS: 'headers',
    CLOSE: 'close',
    LISTENING: 'listening',
    WS_CLIENT_ERROR: 'wsClientError'
} as const;

export type ServerEventType = typeof ServerEvent[keyof typeof ServerEvent];


export const SocketEvent = {
    CLOSE: 'close',
    ERROR: 'error',
    UPGRADE: 'upgrade',
    MESSAGE: 'message',
    OPEN: 'open',
    PING: 'ping',
    PONG: 'pong',
    REDIRECT: 'redirect',
    UNEXPECTED_RESPONSE: 'unexpected-response'
} as const;

export type SocketEventType = typeof SocketEvent[keyof typeof SocketEvent];

export interface EventMetadata{
    event: string;
    methodName: string | symbol;
}

export type EventMap = Map<string, string|symbol>;