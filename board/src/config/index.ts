import dotenv from 'dotenv';

const envFound = dotenv.config();

function getEnv(key : string, defaultValue? : string) : string{
    const value = process.env[key];

    // 값이 undefined 이거나 빈 문자열일 때
    if (!value) {
        if (defaultValue !== undefined) {
            return defaultValue;
        }
        // .env에도, 기본값도 없을 경우 에러
        throw new Error(`'${key}' 값이 존재하지 않습니다!`);
    }

    return value;
}

export default{

    port:  parseInt(getEnv("PORT", "3000"), 10),
    
    dbHost : getEnv("DB_HOST"),

    dbDatabase : getEnv("DB_DATABASE"),

    dbPassword : getEnv("DB_PASSWORD"),

    dbPort : parseInt(getEnv("DB_PORT"),10),

    dbUser : getEnv("DB_USER"),

    profile : getEnv("PROFILE"),

    logLevel : getEnv("LOG_LEVEL", "info"),


    userService : getEnv("USER_SERVICE", 'localhost:3001'),
    quizService : getEnv("QUIZ_SERVICE", 'localhost:3001'),

    grpcServerAddress : getEnv("GRPC_SERVER_ADDRESS", 'localhost:3003')
}