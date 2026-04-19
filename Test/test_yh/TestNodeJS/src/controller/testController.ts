import Container from "typedi";
import catchAsync from "../utils/catchAsync";
import { Request, Response} from 'express';
import TestService from "../service/testService";
import TestDTO from './../model/dto/ItestDTO';
import TypeOrmService from "../service/typeOrmService";
import TypeOrmDTO from "../model/dto/typeOrmDTO";
import { HelloReply, HelloRequest, HelloServiceClient } from "../generated/test";
import { credentials } from "@grpc/grpc-js";

export const testAPI = catchAsync(async(req :Request, res : Response) =>{
    const message : String= "첫번째 테스트!!!!";
    return res.status(200).json(message);
});

export const testAPI2 = catchAsync(async(req :Request, res : Response) =>{
    const message : String= "두번쨰 테스트! : " + req.params.name;

    const testServiceInstance =  Container.get(TestService);

    const data : TestDTO = {
        id : 1,
        content : req.params.name as string
    }

    testServiceInstance.testInsert(data)

    return res.status(200).json(message);
})

export const testAPI3 = catchAsync(async(req :Request, res : Response) =>{
    const {name, description } = req.query;
    const message : String= "type orm 테스트! : " + name;

    const typeOrmService =  Container.get(TypeOrmService);
    
    // 우선은 as string으로 타입을 강제하기는 했지만 실제로는 middlewares 에서 타입검사를 하고 넘어오는식이면 될듯?
    const data : TypeOrmDTO = new TypeOrmDTO(name as string, description  as string)

    typeOrmService.saveData(data)

    return res.status(200).json(message);
})

export const testAPI4 = catchAsync(async(req :Request, res : Response) =>{
    const name = req.query.name as string;
    const message : String= "gRPC nodejs<-->spring 테스트! : " + name;

    const client = new HelloServiceClient(
        'service-b:9090',
        credentials.createInsecure());

    const helloReq = {name: name};
    const response = await new Promise<HelloReply>((resolve, reject) =>{

        // sayHello ( 요청 데이터 , 콜백함수)
        client.sayHello(helloReq, (err, grpcRes) =>{

            if(err){
                console.error("gRPC 통신 에러:", err);
                reject(err);
            } else {
                resolve(grpcRes);
            }
        });
    } );
        
    return res.status(200).json(response.message);
})