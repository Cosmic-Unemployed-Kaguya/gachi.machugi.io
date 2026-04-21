import Container from "typedi";
// import { Pool } from "pg";
import { DataSource } from "typeorm";

;

// export default (dbPool : Pool) =>{
//     Container.set('dbPool', dbPool)
// }

export default (appDataSource :DataSource) => {
    Container.set('AppDataSource', appDataSource)
}