import express from 'express';
import 'reflect-metadata';

async function startServer() {

    const app = express();

    // loader 호출
    await require('./common/loader').default({expressApp : app})

}

startServer();