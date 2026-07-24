import { Service } from "typedi";

/**
 * @TODO
 */
@Service()
export class UserGrpcClient{

    constructor(){}

    public async getUserNickname(userIdx : number) : Promise<string>{
        return "test" + userIdx
    }
}