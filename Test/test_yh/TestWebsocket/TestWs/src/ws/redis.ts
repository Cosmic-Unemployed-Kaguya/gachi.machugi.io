import Redis from 'ioredis';

export const pubClient = new Redis("redis://redis-service:6379");
export const subClient = new Redis("redis://redis-service:6379");