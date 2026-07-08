import { Page } from "@dto/paging";

/**
 * T : Page<Entity> , R : mapper 함수의 반환 타입
 * @param entityPage
 * @param mapper
 * @returns
 */
export const toPageDTO = <T extends object, R>(
  entityPage: Page<T>,
  mapper: (item: T) => R, // T를 받아 R 타입의 반환값을 뱉는 함수
): Page<R> => {
  const dtoItems: R[] = entityPage.items.map((item) => mapper(item));

  const dtoPage: Page<R> = {
    items: dtoItems,
    currentPage: entityPage.currentPage,
    hasNext: entityPage.hasNext,
    totalCount: entityPage.totalCount,
    totalPages: entityPage.totalPages,
  };

  return dtoPage;
};

// ================================================================
// 아래로는 사용 안하는 함수
//=================================================================

/**
 * Entity를 DTO로 바꿔주는 mapper, dto에 들어갈 속성을 배열로 입력해줘야함.
 * DTO의 속성 이름이 Entity와 같다는 가정 하에 사용
 * DTO의 속성을 가져와서 사용 못하냐? >> dto를 interface로 만들어서 불가능......,..,,,
 * @param entity
 * @param keys
 * @returns
 *
 * @deprecated 코드가 너무 구려서 사용 안함
 */
export const toDtoMapper = <T extends object, K extends keyof T>(
  // T 타입의 Entity와 해당 Entity 속성중 뽑아낼 키 K
  entity: T,
  keys: K[],
): Pick<T, K> => {
  // Pick : Js 유틸리티 타입, T 객체에서 K 속성만 뽑아 새로운 객체(타입)를 생성

  // dto의 형태 고정(?)
  const dto = {} as Pick<T, K>;

  //
  keys.forEach((key) => {
    dto[key] = entity[key];
  });

  return dto;
};

/**
 * Page<Entity> 객체를 받아서 Page<DTO>로 반환해주는 함수
 * 위와 마찬가지로 DTO의 속성이 Entity와 이름, 속성 등 다 같아야함
 * @param entityPage
 * @param keys
 * @returns
 *
 *  @deprecated 코드가 너무 구려서 사용 안함
 */
export const createDTO = <T extends object, K extends keyof T>(
  entityPage: Page<T>,
  keys: K[],
): Page<Pick<T, K>> => {
  /**  
    const dtoItems : Pick<T,K> [] = []

    entityPage.items.forEach((item)=>{
        dtoItems.push(toDtoMapper(item,keys))
    })
    */

  // == 위 코드와 동일한 역할. map을 사용함
  const dtoItems = entityPage.items.map((item) => toDtoMapper(item, keys));

  const dtoPage: Page<Pick<T, K>> = {
    items: dtoItems,
    currentPage: entityPage.currentPage,
    hasNext: entityPage.hasNext,
    totalCount: entityPage.totalCount,
    totalPages: entityPage.totalPages,
  };

  return dtoPage;
};
