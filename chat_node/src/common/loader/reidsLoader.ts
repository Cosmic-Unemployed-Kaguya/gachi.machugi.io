import Container from "typedi";
import { RedisClient } from "../redis/redisClient";



export default async () =>{
    const redisClient = Container.get(RedisClient);
    await redisClient.onSubscribe();
    
}
