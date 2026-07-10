import express from 'express';
import { Server as SocketIOServer } from 'socket.io';
import { customSocketServer } from './socket/customServer';
import { setupRedis } from './redisAdapter';

async function startServer() {
    const app = express();
    
    const port = 3012

    // loader 호출
    await require('./loader').default({expressApp : app})

    // 웹 어플리케이션 시작
    const server = app.listen(port , () => {
        console.log("%d 번 포트로 웹 시작" , port);}
    );

    // socket io 서버 객체 생성
    const io : customSocketServer = new SocketIOServer(server, { 
        path: '/socket.io',
        cors : {
            origin : "*",
            methods : ["GET", "POST"]
        }
        
    })

    // redis 어뎁터 로드
    console.log(" redis 어뎁터 로드 시작");
    await setupRedis(io);
    console.log(" redis 어뎁터 로드 종료");


    // socket io 로드
    console.log("socket io 로드 시작");
    await require('./socket/server').default({io : io});
    console.log("socket io 로드 종료");

}

startServer();