import { createAdapter } from "@socket.io/redis-adapter";
import { createClient } from "redis";
import { customSocketServer } from "./socket/customServer";


export async function setupRedis(io: customSocketServer) {
    // Redis 클라이언트 2개 생성 
    const pubClient = createClient({ url: "redis://redis-service:6379" });
    const subClient = pubClient.duplicate();

    // 3. Redis 연결
    await Promise.all([
        pubClient.connect(),
        subClient.connect()
    ]);

    // Socket.IO에 Redis 어댑터 장착
    io.adapter(createAdapter(pubClient, subClient));
    
}