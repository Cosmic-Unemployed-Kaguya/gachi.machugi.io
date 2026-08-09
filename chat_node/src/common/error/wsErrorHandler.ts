import { BaseError, BaseRes } from "@common/model/base";
import logger from "@common/util/logger";
import { WebSocket } from "ws";
import { WsError } from "./wsError";
import { wsErrorConverter } from "./wsErrorConvertor";

export async function sendError (ws : WebSocket, error : WsError){
    ws.send(JSON.stringify({
        event : 'ERROR',
        data : {
            code : error.code,
            message : error.message
        } as BaseError
    } as BaseRes))
}

export const catchAsyncEvent = (fn : Function) =>{
    return async ( socket : WebSocket , ...args: any[] ) =>{
        try {
            await fn (socket, ...args);
        }catch (error) {
            logger.error(error);
            const wsError : WsError = wsErrorConverter(error);
            await sendError(socket, wsError)

        }
    }
}