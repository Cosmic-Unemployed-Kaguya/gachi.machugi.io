import Container from "typedi";
// import { Pool } from "pg";
import { DataSource } from "typeorm";


// export default (dbPool : Pool) =>{
//     Container.set('dbPool', dbPool)
// }

// DI Container에서 관리 할 객체들을 여기서 추가

export default (appDataSource :DataSource) => {
    // appDataSource : typeORM의 DB 연결 풀
    Container.set('AppDataSource', appDataSource)
}