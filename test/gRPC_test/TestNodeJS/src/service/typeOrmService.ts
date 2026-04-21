import { Inject, Service } from "typedi";
import { TestRepository } from "../repository/testRepository";
import { TestEntity } from './../model/entity/testEntity';
import TypeOrmDTO from "../model/dto/typeOrmDTO";


@Service()
export default class TypeOrmService{
    constructor(
        @Inject() private testRepository : TestRepository
    ){}

    public async saveData(typeOrmDto : TypeOrmDTO){

        const testEntity = new TestEntity;
        
        // 지금은 일일이 집어넣었지만 실제론 mapper를 만들어서 쓸것  
        testEntity.name = typeOrmDto.name;
        testEntity.description = typeOrmDto.description;

        return await this.testRepository.save(testEntity);
    }
}