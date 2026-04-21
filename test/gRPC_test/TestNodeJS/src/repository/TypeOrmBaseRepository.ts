import { DataSource,Repository,EntityTarget } from "typeorm";

/**
 * typeorm의 Repository를 통째로 받아와서 정의까지 해줄거임
 * TypeOrmRepository를 상속받는 클래스는 무조건 Repository이며 아래의 db 조회 메서드를 모두 사용할 수 있는것을 원했음
 * DataSource와 entity를 주입받아 Repository를 구현
 */
export abstract class TypeOrmRepository<T extends Object> extends Repository<T>{

    constructor(
        appDataSource :DataSource,

        entity : EntityTarget<T>
    ){
        super(entity, appDataSource.createEntityManager())
    }


}