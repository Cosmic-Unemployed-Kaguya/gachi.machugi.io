import config from '@common/config';
import logger from '@common/util/logger';
import { loadService, startServer } from '@cosmic-unemployed-kaguya/grpc-express';
import express from 'express';
import Container from 'typedi';
import socket from 'ws';
import { grpcServers } from '../../grpc-server';
import reidsLoader from './reidsLoader';
import { WsLoader } from './wsLoader';

export default async ({ expressApp }: { expressApp: express.Application } ) => {

    const port = config.port;

    logger.info("express 로드 시작");
    // 웹 어플리케이션 시작
    const server = expressApp.listen(port , () => {
        console.log("%d 번 포트로 웹 시작" , port);}
    );

    logger.info("express 로드 완료");


    logger.info("ws 로드 시작");
    // 웹소켓 서버 로드
    const ws = new socket.Server({
        server : server,
        path : "/chat"
    });

    Container.set(socket.Server , ws);

    const wsLoader = Container.get(WsLoader);
    wsLoader.startLoadWs();

    logger.info("ws 로드 완료");


    logger.info("redis 로드 시작");
    // redis 로드
    await reidsLoader()
    logger.info("redis 로드 완료");

    // gRPC 서버 로드
    logger.info("grpc 서비스 로드 시작");
    await loadService(grpcServers, logger);
    logger.info("grpc 서비스 로드 완료");

    //  gRPC 서버 시작
    logger.info("grpc 서버 로드 시작");
    await startServer(config.grpcServerAddress, logger);
    logger.info("grpc 서버 로드 완료");

}