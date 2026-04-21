

import { TypeOrmRepository } from './TypeOrmBaseRepository';
import { TestEntity } from '../model/entity/testEntity';
import { Repository } from '../decorator/repository';

@Repository(TestEntity)
export class TestRepository extends TypeOrmRepository<TestEntity>{

    
}