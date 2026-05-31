import { Response } from "express";

export const sendSuccess = (res: Response, data : any ,statusCode :number = 200 ) =>{
    return res.status(statusCode).json({
        success: true,
        data,
    });
};

export const sendError = (res: Response, statusCode: number = 500, message: string) => {
    return res.status(statusCode).json({
        success: false,
        message : message
    });
};