package kaguya.user.domain.user.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserProfileEntity is a Querydsl query type for UserProfileEntity
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserProfileEntity extends EntityPathBase<UserProfileEntity> {

    private static final long serialVersionUID = 1653192661L;

    public static final QUserProfileEntity userProfileEntity = new QUserProfileEntity("userProfileEntity");

    public final DatePath<java.time.LocalDate> birth = createDate("birth", java.time.LocalDate.class);

    public final EnumPath<kaguya.user.domain.user.model.enums.Gender> gender = createEnum("gender", kaguya.user.domain.user.model.enums.Gender.class);

    public final StringPath name = createString("name");

    public final StringPath phone = createString("phone");

    public final NumberPath<Long> userIdx = createNumber("userIdx", Long.class);

    public QUserProfileEntity(String variable) {
        super(UserProfileEntity.class, forVariable(variable));
    }

    public QUserProfileEntity(Path<? extends UserProfileEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserProfileEntity(PathMetadata metadata) {
        super(UserProfileEntity.class, metadata);
    }

}

