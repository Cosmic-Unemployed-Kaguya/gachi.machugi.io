import config from '@common/config';
import express from 'express';
import Container from 'typedi';
import socket from 'ws';
import reidsLoader from './reidsLoader';
import { WsLoader } from './wsLoader';

export default async ({ expressApp }: { expressApp: express.Application } ) => {

    const port = config.port;

    // 웹 어플리케이션 시작
    const server = expressApp.listen(port , () => {
        console.log("%d 번 포트로 웹 시작" , port);}
    );

    // 웹소켓 서버 로드
    const ws = new socket.Server({
        server : server,
        path : "/chat"
    });

    Container.set(socket.Server , ws);

    const wsLoader = Container.get(WsLoader);

    await reidsLoader()
}