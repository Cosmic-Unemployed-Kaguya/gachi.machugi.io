import winston from "winston";
import config from "../config";
const transports = [];
// 배포환경일 경우 처리없이 기본 콘솔 출력
if (config.profile !== "dev"){
    transports.push(
        new winston.transports.Console()
    )


} else {
    // 개발 환경의 경우
    transports.push(
        new winston.transports.Console({
            format: winston.format.combine(
                // 색 입히기
                winston.format.cli(),
            )
        })
  )
}


export const logger = winston.createLogger({
    level: config.logLevel,
    levels: winston.config.npm.levels,
    format: winston.format.combine(
        winston.format.timestamp({
        format: 'YYYY-MM-DD HH:mm:ss'
        }),
        winston.format.errors({ stack: true }),
        winston.format.splat(),
        winston.format.json()
    ),
    transports

});