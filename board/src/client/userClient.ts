import { CallOptions, Client, ClientUnaryCall, credentials, Metadata, ServiceError } from "@grpc/grpc-js";
import { UserInfoListReply, UserInfoReply, UserInfoRequest, UserServiceClient } from "../generated/user";
import { Service } from "typedi";
import { UserInfoListRequset } from './../generated/user';


/**
 * UserClient 클래스 내부에서 UserServiceClient 객체를 생성 후 사용
 *  
 * UserClient 자체가 UserServiceClient를 상속/구현 하면 안되냐?
 *  > 통신 메서드가 Req 말고도 callback 함수를 파라미터로 받아야함.
 *  > 이에 외부 service 등에서 사용 할 때 callback함수를 정의하여 넣어줘야하는데 이게 너무 마음에 안들음
 *  > 따라서 해당 클래스 내에서 callback을 정의하고, callback을 넣어주는 새로운 메서드를 만들어서 외부에서는 Req만 알아도 문제 없도록 해봄
 * 
 */
@Service()
export default class UserClient {
    
    private readonly grpcClient: UserServiceClient;
    /** @TODO 하드코딩 XXXXXXXXXXXXXXXXX */
    constructor(){
        this.grpcClient = new UserServiceClient(
            'localhost:3001', 
            credentials.createInsecure())
    }


    public async getUserInfo(userInfoReq : UserInfoRequest) : Promise<UserInfoReply> {

        return await this.grpcCall<UserInfoReply> ((cb) =>{
            return this.grpcClient.getUserInfo(userInfoReq,cb)
        })

        // return await new Promise<UserInfoReply>((resolve,reject) =>{

        //     this.grpcClient.getUserInfo(userInfoReq, (err, res) =>{
        //         if(err){
        //             reject(err);

        //         }else{
        //             resolve(res);
        //         }
        //     }) ;
        // });
    }




    public async getUserListInfo(userInfoListReq :UserInfoListRequset) :Promise<UserInfoListReply> {
        return await this.grpcCall<UserInfoListReply>((cb) => {
             return this.grpcClient.getUserListInfo(userInfoListReq,cb)
        })
        
        // this.grpcClient.getUserListInfo(userInfoListReq , this.callback<UserInfoListReply>)
    }
        


    /**
     * 중복 코드를 줄이기 위한 제네릭 함수. 
     * gRPC 요청 메서드의 구조는 get~~(req, callback 함수) 로 되어있음
     * 메서드를 사용 할 때마다 callback 함수를 정의해서 넣어줘야하는 불편함이 있음.
     * 
     * 이를 해소하기위해 callback 함수를 정의하고, Promise 객체를 반환하는 함수 생성
     * 
     * @param callFunction : UserServiceClient 메서드
     * @returns Promise <res> 
     */
    private async grpcCall<T>(callFunction :(callback: (error: ServiceError | null, response: T) => void ) => ClientUnaryCall ):Promise<T>{
        return new Promise<T>((resolve, reject) =>{
            callFunction((error,response)=> {
                if(error){
                    reject(error);
                }else{
                    resolve(response);}
            })

        })
    }

}