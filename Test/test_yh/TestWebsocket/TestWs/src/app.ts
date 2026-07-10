import express from 'express';
import socket from 'ws';

async function startServer() {
    const app = express();
    
    const port = 3013

    // loader 호출
    // await require('./loader').default({expressApp : app})

    // 웹 어플리케이션 시작
    const server = app.listen(port , () => {
        console.log("%d 번 포트로 웹 시작" , port);}
    );

    const ws = new socket.Server({
        server : server,
        path : "/chat"
    });

    console.log("ws 로드 시작")
    // await require('./ws.ts').default({ws : ws})
    await require('./ws/ws_redis').default({ws : ws})
    console.log("ws 로드 종료")


}

startServer();