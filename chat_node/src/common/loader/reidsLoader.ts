import { RedisSubClient } from "@common/redis/redisSubClient";
import Container from "typedi";
import { } from "../redis/redisPubClient";



export default async () =>{
    const channels :string[] = ['global_chat_channel', 'kick_user_channel']
    const redisSubClient = Container.get(RedisSubClient);
    await redisSubClient.onSubscribe(channels);
    
}
