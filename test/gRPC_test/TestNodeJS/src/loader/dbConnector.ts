import {Pool} from 'pg'
import config from '../config'

const pool =  new Pool ({
    user: config.dbUser,
    host: config.dbHost,
    database: config.dbDatabase,
    password: config.dbPassword,
    port: config.dbPort,
    // max: 20,  // 커넥션 수 
});

export default async (): Promise<Pool> =>{
    try{
        const client  = await pool.connect();
        console.log('postgre 연결 성공')
        client.release();

        return pool;
    }catch(error){
        console.error('postgre 연결 실패 : ', error);
        throw error;
    }
}