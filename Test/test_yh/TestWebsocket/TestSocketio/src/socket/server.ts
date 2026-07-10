
import { MessageRes } from '../dto/message';
import { customSocketServer } from './customServer';
import os from 'os';

/** @TODO 유저가 방에 출입 시 roomservice에 알림? 필요? */
export default async ({io} : {io:customSocketServer}) => {

    const podName = os.hostname(); 
    console.log(`🚀 [서버 시작] 현재 파드 이름: ${podName}`);

    io.on('connection', (socket) => {
        
        socket.emit('server_info', { 
            message: "연결 성공!", 
            connectedPod: podName 
        });
        
        // 연결 될 때 임의의 socket id 발급
        console.log(`[접속] ${socket.id}`);

        const headers = socket.handshake.headers;

        const userIdxStr = headers['x-user-idx']; 
        const userRoleStr = headers['x-user-role'];

        socket.data.userIdx = Number(userIdxStr)

        /**  @TODO 유저 닉네임 가져오기 (gRPC 추가해야함;;;;) */
        socket.data.userNickname = "temp"+ userIdxStr

        // // 공지
        // socket.on('notice', (data) => {

        //     io.emit('notice', data);
        // });
        
        // 방 입장
        socket.on('join_room', (data) => {

            socket.join(data.roomId)

            socket.data.currentRoom = data.roomId;

            io.to(socket.data.currentRoom).emit('join_success', {userNickname: socket.data.userNickname})

            console.log(`입장 성공 ${socket.data.userIdx }, ${socket.data.currentRoom} }` )
        });

        // 방 단위 메시지 수신 및 방 단위 송신
        socket.on('message', (data) => {

            const reply : MessageRes = {
                msg : data.msg,
                userNickname : socket.data.userNickname
            }

            io.to(socket.data.currentRoom).emit('message', reply);

        });

        // // 메시지 수신 및 전체 브로드캐스팅
        // socket.on('message', (data) => {

        //     io.emit('message', data);
        // });


        // 방 나가기
        socket.on('exit_room', () => {

            socket.to(socket.data.currentRoom).emit('exit_room', {userNickname :socket.data.userNickname })
            
            socket.leave(socket.data.currentRoom);

            socket.data.currentRoom = "null";
        });

        // 연결 종료 시 알림
        socket.on('disconnect', () => {

            socket.to(socket.data.currentRoom).emit('exit_room', {userNickname :socket.data.userNickname })

        });



    });

    
}
