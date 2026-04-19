import { Service,Container } from 'typedi';
import { DataSource, EntityTarget } from 'typeorm';


/**
 * entity를 받아 해당 repository가 컨테이너에 등록되면서 
 * AppDataSource까지 주입되도록 하는 데코레이터
 * JPA랑 비슷한 기능을 원했음
 * @TODO 코드에 대한 이해가 완벽하게 된게 아니라서 다시 봐야함!!!
 * @param entity 
 */
export function Repository(entity: EntityTarget<any>) :ClassDecorator{
    
    return function (constructor: Function) {
        // typedi의 @Service를 재정의
        // 컨테이너가 해당 클래스를 생성 할 때 아래 과정을 진행하도록.
        // 이게 되네
        Service({
            factory: () => {
                // 컨테이너에서 'AppDataSource'로 등록 된 객체를 가져옴
                const dataSource : DataSource = Container.get<DataSource>('AppDataSource');

                // 해당 클래스를 생성하면서 인자 넣어줌
                return new (constructor as any)(dataSource, entity);
            }

        })(constructor); 
    };
}