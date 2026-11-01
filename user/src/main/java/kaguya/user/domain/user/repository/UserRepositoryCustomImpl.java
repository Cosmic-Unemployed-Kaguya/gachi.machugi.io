package kaguya.user.domain.user.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kaguya.user.domain.user.model.dto.request.UserSearchReq;
import kaguya.user.domain.user.model.entity.UserEntity;
import kaguya.user.domain.user.model.enums.Role;
import kaguya.user.domain.user.model.enums.SearchType;
import kaguya.user.domain.user.model.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.List;

import static kaguya.user.domain.user.model.entity.QUserEntity.userEntity;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 회원 목록 조회 (검색 + 필터 + 정렬 + 페이징)
     * - 검색: keyword를 searchType에 따라 이메일/닉네임에서 부분 일치
     * - 필터: role, status (null이면 해당 조건 제외)
     * - 정렬: Pageable의 Sort + idx tiebreaker
     */
    @Override
    public Page<UserEntity> findByCondition(UserSearchReq condition, Pageable pageable) {

        List<UserEntity> content = queryFactory
                .selectFrom(userEntity)  // SELECT u FROM UserEntity u
                .where(  // WHERE ... AND ...
                        keywordContains(condition.searchType(), condition.keyword()),
                        roleEq(condition.role()),
                        statusEq(condition.status())
                )
                .orderBy(toOrderSpecifiers(pageable.getSort()))  // ORDER BY ...
                .offset(pageable.getOffset())  // 앞에서 몇 개 건너뛸지 (page × size)
                .limit(pageable.getPageSize())  // 몇 개 가져올지 (size)
                .fetch();  // SQL 실행 후 List로 받기

        // 전체 개수 조회용 쿼리 (목록과 같은 조건, 정렬/페이징 없음)
        JPAQuery<Long> countQuery = queryFactory
                .select(userEntity.count())  // SELECT COUNT(u)
                .from(userEntity)
                .where(
                        keywordContains(condition.searchType(), condition.keyword()),
                        roleEq(condition.role()),
                        statusEq(condition.status())
                );
                // 실행 메서드(fetch/fetchOne) 미호출 -> 아직 DB에 안 보냄. getPage가 필요할 때만 실행

        /*
            PageableExecutionUtils.getPage: 목록 결과만으로 전체 개수를 알 수 있으면 countQuery를 실행하지 않음
            ex) 전체 회원이 25명이고 size=10 일 때
            (page / 가져온 개수 / 전체 개수)
            - page=0 / 10 (꽉 참) / 25 (countQuery 실행)
            - page=1 / 10 (꽉 참) / 25 (countQuery 실행)
            - page=2 / 5 (덜 참 = 마지막 페이지) / 20 + 5 = 25 (countQuery 생략)
            - 검색 결과가 3명, page=0 / 3 (첫 페이지인데 덜 참) / 3 (countQuery 생략)
        */
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }


    /**
     * 헬퍼 메서드
     */
    // 키워드 부분 일치(LIKE %keyword%)
    // ex. keyword "ser" -> "user1", "maser" 등 포함된 값 모두
    private BooleanExpression keywordContains(SearchType searchType, String keyword) {

        // 키워드가 null 이거나 공백이면 조건 없음 (전부 조회)
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        // 검색 타입이 null이면 email, nickname 둘 다 검색 (OR)
        if (searchType == null) {
            return userEntity.email.contains(keyword)
                    .or(userEntity.nickname.contains(keyword));
        }

        // 타입이 있으면 해당 필드만
        return switch (searchType) {
            case EMAIL -> userEntity.email.contains(keyword);
            case NICKNAME -> userEntity.nickname.contains(keyword);
        };
    }
    // 권한 필터. null이면 조건 제외 (모든 권한)
    private BooleanExpression roleEq(Role role) {
        return role != null ? userEntity.role.eq(role) : null;
    }
    // 상태 필터. null이면 조건 제외 (모든 상태)
    private BooleanExpression statusEq(Status status) {
        return status != null ? userEntity.status.eq(status) : null;
    }

    // 정렬: Sort.Order(Spring) -> OrderSpecifier(QueryDSL) + idx tiebreaker
    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {

        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order o : sort) {
            Order direction = o.isAscending() ? Order.ASC : Order.DESC;

            OrderSpecifier<?> spec = switch (o.getProperty()) {
                case "joinDate" -> new OrderSpecifier<>(direction, userEntity.joinDate);
                case "lastLoginDate" -> new OrderSpecifier<>(direction, userEntity.lastLoginDate);
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
        orders.add(new OrderSpecifier<>(tiebreakerDirection, userEntity.idx));

        return orders.toArray(OrderSpecifier[]::new);
    }
}