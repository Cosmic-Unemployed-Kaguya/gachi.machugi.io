package kaguya.user.domain.user.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kaguya.user.domain.user.model.dto.request.BlockSearchReq;
import kaguya.user.domain.user.model.entity.QUserEntity;
import kaguya.user.domain.user.model.entity.UserManagementEntity;
import kaguya.user.domain.user.model.enums.ManagementType;
import kaguya.user.domain.user.model.enums.SearchType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.List;

import static kaguya.user.domain.user.model.entity.QUserManagementEntity.userManagementEntity;

@RequiredArgsConstructor
public class UserManagementRepositoryCustomImpl implements UserManagementRepositoryCustom {

    // user, admin 둘 다 UserEntity라서 별칭을 나눈 Q인스턴스를 따로 만듦
    private static final QUserEntity targetUser = new QUserEntity("targetUser");  // 제재 대상 회원
    private static final QUserEntity admin = new QUserEntity("admin");  // 처리한 관리자

    private final JPAQueryFactory queryFactory;

    /**
     * 제재 목록 조회 (검색 + 필터 + 정렬 + 페이징)
     * - 검색: keyword를 searchType에 따라 제재 대상 회원의 이메일/닉네임에서 부분 일치
     * - 필터: managementType (null이면 해당 조건 제외)
     * - 정렬: Pageable의 Sort + idx tiebreaker
     * - user, admin은 fetch join으로 함께 조회 (N+1 방지)
     */
    @Override
    public Page<UserManagementEntity> findByCondition(BlockSearchReq condition, Pageable pageable) {

        List<UserManagementEntity> content = queryFactory
                .selectFrom(userManagementEntity)
                .join(userManagementEntity.user, targetUser).fetchJoin()  // 대상 회원 함께 조회
                .join(userManagementEntity.admin, admin).fetchJoin()  // 관리자 함께 조회
                .where(
                        keywordContains(condition.searchType(), condition.keyword()),
                        managementTypeEq(condition.managementType())
                )
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회용 쿼리. fetch join 없이 검색 조건에 필요한 user 조인만
        JPAQuery<Long> countQuery = queryFactory
                .select(userManagementEntity.count())
                .from(userManagementEntity)
                .join(userManagementEntity.user, targetUser)
                .where(
                        keywordContains(condition.searchType(), condition.keyword()),
                        managementTypeEq(condition.managementType())
                );
        // 실행 메서드(fetch/fetchOne) 미호출 -> 아직 DB에 안 보냄. getPage가 필요할 때만 실행

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }


    /**
     * 헬퍼 메서드
     */
    // 제재 대상 회원의 이메일/닉네임 부분 일치 (LIKE %keyword%)
    private BooleanExpression keywordContains(SearchType searchType, String keyword) {

        // 키워드가 null 이거나 공백이면 조건 없음 (전부 조회)
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        // 검색 타입이 null이면 email, nickname 둘 다 검색 (OR)
        if (searchType == null) {
            return targetUser.email.contains(keyword)
                    .or(targetUser.nickname.contains(keyword));
        }

        // 타입이 있으면 해당 필드만
        return switch (searchType) {
            case EMAIL -> targetUser.email.contains(keyword);
            case NICKNAME -> targetUser.nickname.contains(keyword);
        };
    }

    // 제재 종류 필터. null이면 조건 제외 (모든 종류)
    private BooleanExpression managementTypeEq(ManagementType managementType) {
        return managementType != null ? userManagementEntity.managementType.eq(managementType) : null;
    }

    // 정렬: Sort.Order(Spring) -> OrderSpecifier(QueryDSL) + idx tiebreaker
    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {

        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order o : sort) {
            Order direction = o.isAscending() ? Order.ASC : Order.DESC;

            OrderSpecifier<?> spec = switch (o.getProperty()) {
                case "startDate" -> new OrderSpecifier<>(direction, userManagementEntity.startDate);
                case "endDate" -> new OrderSpecifier<>(direction, userManagementEntity.endDate);
                default -> throw new IllegalStateException("정렬 매핑 누락: " + o.getProperty());
            };

            // Spring 정렬에 NULLS_LAST 표시가 있으면 -> QueryDSL 정렬에도 nullsLast 적용
            orders.add(o.getNullHandling() == Sort.NullHandling.NULLS_LAST ? spec.nullsLast() : spec);
        }

        Order tiebreakerDirection = sort.stream()
                .findFirst()
                .map(o -> o.isAscending() ? Order.ASC : Order.DESC)  // 첫 번째 정렬 항목의 방향을 따름
                .orElse(Order.DESC);  // 정렬 항목이 없으면 DESC

        // tiebreaker: 정렬 값이 같은 행끼리 순서가 바뀌지 않도록 맨 끝에 idx 정렬 추가
        orders.add(new OrderSpecifier<>(tiebreakerDirection, userManagementEntity.idx));

        return orders.toArray(OrderSpecifier[]::new);
    }
}