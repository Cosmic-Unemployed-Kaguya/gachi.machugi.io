import { Pool } from "pg";

import config from "../config";
import logger from "../utils/logger";

/**
 * typeORM을 사용하지 않고 DB와 직접 연결하기 위한 모듈
 * typeORM을 사용함으로서 지금은 사용 X
 * 일단 코드를 만들어놓기만 함
 */

const pool = new Pool({
  user: config.dbUser,
  host: config.dbHost,
  database: config.dbDatabase,
  password: config.dbPassword,
  port: config.dbPort,
  // max: 20,  // 커넥션 수
});

export default async (): Promise<Pool> => {
  try {
    const client = await pool.connect();
    logger.info("postgre 연결 성공");
    client.release();

    return pool;
  } catch (error) {
    logger.error("postgre 연결 실패 : ", error);
    throw error;
  }
};
