import { Inject, Service } from "typedi";
import TestDTO from "../model/dto/ItestDTO";
import { Pool } from "pg";

@Service()
export default class TestService{
    constructor(
        @Inject('dbPool') private dbPool : Pool
    ){}

    public testInsert (data :TestDTO ) {
        
        const query = {
            text: "Insert INTO test VALUES ($1, $2)",
            values: [data.id, data.content]
        }

        this.dbPool
            .query(query)
            .then((res) => {
                console.log(res);
                this.dbPool.end();
            })
            .catch((e) => console.error("오류 발생!! ", e))
        
    }
}