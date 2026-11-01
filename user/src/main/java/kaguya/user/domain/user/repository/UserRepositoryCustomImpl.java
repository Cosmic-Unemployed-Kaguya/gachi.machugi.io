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

    @Override
    public Page<UserEntity> findByCondition(UserSearchReq condition, Pageable pageable) {

        // 1. 목록 조회 (검색/필터 + 정렬 + 페이징)
        List<UserEntity> content = queryFactory
                .selectFrom(userEntity)
                .where(
                        keywordContains(condition.searchType(), condition.keyword()),
                        roleEq(condition.role()),
                        statusEq(condition.status())
                )
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 개수 조회 (같은 where 조건, 정렬/페이징 없음)
        JPAQuery<Long> countQuery = queryFactory
                .select(userEntity.count())
                .from(userEntity)
                .where(
                        keywordContains(condition.searchType(), condition.keyword()),
                        roleEq(condition.role()),
                        statusEq(condition.status())
                );

        // 3. 첫 페이지인데 결과가 size보다 적으면 count 쿼리 생략
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 검색 / 필터 조건. null 반환 시 where절에서 무시됨 (= 필터 미적용)
     */
    // searchType 없이 keyword만 오면 이메일 + 닉네임 둘 다 검색
    private BooleanExpression keywordContains(SearchType searchType, String keyword) {
        if (keyword == null) {
            return null;
        }
        if (searchType == null) {
            return userEntity.email.contains(keyword)
                    .or(userEntity.nickname.contains(keyword));
        }
        return switch (searchType) {
            case EMAIL -> userEntity.email.contains(keyword);
            case NICKNAME -> userEntity.nickname.contains(keyword);
        };
    }

    private BooleanExpression roleEq(Role role) {
        return role != null ? userEntity.role.eq(role) : null;
    }

    private BooleanExpression statusEq(Status status) {
        return status != null ? userEntity.status.eq(status) : null;
    }

    /**
     * 정렬. Sort(엔티티 프로퍼티명) → OrderSpecifier + idx tiebreaker
     */
    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order o : sort) {
            Order direction = o.isAscending() ? Order.ASC : Order.DESC;

            OrderSpecifier<?> spec = switch (o.getProperty()) {
                case "joinDate" -> new OrderSpecifier<>(direction, userEntity.joinDate);
                case "lastLoginDate" -> new OrderSpecifier<>(direction, userEntity.lastLoginDate);
                // 상위 계층 검증을 거치지 않은 호출에 대한 방어선
                default -> throw new IllegalArgumentException("정렬할 수 없는 필드: " + o.getProperty());
            };

            // Sort.NullHandling은 QueryDSL에 자동 반영되지 않으므로 직접 적용
            orders.add(o.getNullHandling() == Sort.NullHandling.NULLS_LAST ? spec.nullsLast() : spec);
        }

        // tiebreaker: 첫 정렬 방향을 따라 idx 추가 (정렬이 없으면 DESC)
        Order tiebreakerDirection = sort.stream()
                .findFirst()
                .map(o -> o.isAscending() ? Order.ASC : Order.DESC)
                .orElse(Order.DESC);
        orders.add(new OrderSpecifier<>(tiebreakerDirection, userEntity.idx));

        return orders.toArray(OrderSpecifier[]::new);
    }
}