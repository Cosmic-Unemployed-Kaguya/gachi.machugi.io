import { Container, Service } from "typedi";
import { DataSource, EntityTarget } from "typeorm";

/**
 *
 * 1. DI컨테이너에 등록 될 것 > 기존 Service 데코레이터 활용
 * 2. 1번 과정 중 객체를 생성 해야하는데, 생성자에 들어가는 필요한 파라미터를 주입
 *   > entity, AppDataSource(DB)
 *
 * 이로서 @Repository 만 붙이면 DB 연결이 깔끔하게 해결
 *
 * @param entity
 */
export function Repository(entity: EntityTarget<any>): ClassDecorator {
  return function (constructor: Function) {
    // typedi의 @Service를 재정의
    // 컨테이너가 해당 클래스를 생성 할 때 아래 과정을 진행하도록.
    // 이게 되네
    Service({
      factory: () => {
        // 컨테이너에서 'AppDataSource'로 등록 된 객체를 가져옴
        const dataSource: DataSource = Container.get<DataSource>("AppDataSource");

        // 해당 클래스를 생성하면서 인자 넣어줌
        return new (constructor as any)(dataSource, entity);
      },
    })(constructor);
  };
}
