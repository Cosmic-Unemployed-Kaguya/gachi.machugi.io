import { WebSocket } from 'ws';

// socket 객체가 저장 해야할 데이터
export interface CustomSocket extends WebSocket{
    userIdx : number;
    userNickname: string;
    roomIdx? : number;
}
