import { Server as SocketIOServer } from 'socket.io';
import { ClientToServerEvents } from './listenEvents';
import { ServerToClientEvents } from './emitEvents';
import { InterServerEvents } from './serverSideEvents';
import { DefaultSocketData } from './socketData';

export type customSocketServer = SocketIOServer<
    ClientToServerEvents,
    ServerToClientEvents,
    InterServerEvents,
    DefaultSocketData>