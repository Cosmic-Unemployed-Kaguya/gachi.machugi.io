import { DataSource } from "typeorm";

import config from "../config";

/**
 * Type ORM을 통한 postgre와의 통신 연결
 */
export const AppDataSource = new DataSource({
  type: "postgres",
  host: config.dbHost,
  port: config.dbPort,
  username: config.dbUser,
  password: config.dbPassword,
  database: config.dbDatabase,
  synchronize: true, // == ddlAuto, db랑 자동 연동 할 것인가?

  entities: ["src/common/model/entity/*.ts"],

  /** 기본값false, JS는 큰 수를 메모리에 못담아 큰 수를 db에 넣을 때 문자열로 바꿔서 넣음.
   *  true로 할 시 숫자로 들어가지만 큰 수의경우 깨질 위험이 있어 false로 사용  */
  // parseInt8 : false

  // ssl : ~~     // 암호화를 위한 옵션

  // poolErrorHandler : pool에서 에러 발생 시 뭔가뭔가 하는 옵션...???

  // uuidExtension :   // PK를 1,2,3 등 알기 쉬운 숫자가 아닌 복잡한 UUID를 사용하는 옵션, postgre 기능

  // connectTimeoutMS : // db와 연결이 끊길 시 몇초 기다릴거임?

  // extra: {
  //     max: 30,                         // 최대 커넥션 개수 (기본값 10)
  //     idleTimeoutMillis: 30000,        // 커넥션 타임아웃 시간 설정
  //     connectionTimeoutMillis: 5000,  // db 연결 타임아웃 시간 설정
  // }
});
