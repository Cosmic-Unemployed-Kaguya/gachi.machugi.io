import {
  DataSource,
  EntityTarget,
  Repository,
  SelectQueryBuilder,
} from "typeorm";

import { Page, PagingReqType } from "@dto/paging";

/**
 * typeorm의 Repository를 통째로 받아와서 정의까지 해줄거임
 * TypeOrmRepository를 상속받는 클래스는 무조건 Repository이며 아래의 db 조회 메서드를 모두 사용할 수 있는것을 원했음
 * DataSource와 entity를 주입받아 Repository를 구현
 */
export abstract class TypeOrmRepository<
  T extends Object,
> extends Repository<T> {
  constructor(
    appDataSource: DataSource,

    entity: EntityTarget<T>,
  ) {
    super(entity, appDataSource.createEntityManager());
  }

  //-----------------------------------------------
  // 이하로는 repository들이 공통으로 사용 할 함수
  //-----------------------------------------------

  /**
   * 페이징 처리
   * - 여기서 처리하는것은 sort, page, size 뿐
   * - search나 filter의 경우 예외가 많기에 따로 처리해줘야함.
   *
   * @param qb
   * @param pagingDTO
   * @returns
   */
  protected async paginate(
    qb: SelectQueryBuilder<T>,
    pagingDTO: PagingReqType,
  ): Promise<Page<T>> {
    const { page, size, sort } = pagingDTO;

    if (sort) {
      const sortOptions = sort.split(",").map((s) => s.trim());

      // forEach > condition : 현재 값,  index : 번호
      sortOptions.forEach((condition, index) => {
        const isDesc = condition.startsWith("-");
        const sortField = isDesc ? condition.substring(1) : condition;
        const sortDirection = isDesc ? "DESC" : "ASC";

        if (index === 0) {
          qb.orderBy(`${qb.alias}.${sortField}`, sortDirection);
        } else {
          // 두번째 부터는 addOrderBy
          qb.addOrderBy(`${qb.alias}.${sortField}`, sortDirection);
        }
      });
    } else {
      // 기본값: 최신순
      qb.orderBy(`${qb.alias}.createdAt`, "DESC");
    }

    const skip = (page > 0 ? page - 1 : 0) * size;
    // 특정 지점부터 size만큼 데이터 조회
    qb.skip(skip).take(size);

    // getManyAndCount : 데이터 조회 및 데이터의 개수
    const [items, totalCount] = await qb.getManyAndCount();

    return {
      items,
      totalCount,
      currentPage: page,
      totalPages: Math.ceil(totalCount / size),
      hasNext: page * size < totalCount,
    };
  }
}
