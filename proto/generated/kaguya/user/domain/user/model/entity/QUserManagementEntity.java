package kaguya.user.domain.user.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserManagementEntity is a Querydsl query type for UserManagementEntity
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserManagementEntity extends EntityPathBase<UserManagementEntity> {

    private static final long serialVersionUID = -1658538883L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserManagementEntity userManagementEntity = new QUserManagementEntity("userManagementEntity");

    public final QUserEntity admin;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> endDate = createDateTime("endDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> idx = createNumber("idx", Long.class);

    public final EnumPath<kaguya.user.domain.user.model.enums.ManagementType> managementType = createEnum("managementType", kaguya.user.domain.user.model.enums.ManagementType.class);

    public final StringPath reason = createString("reason");

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    public final QUserEntity user;

    public QUserManagementEntity(String variable) {
        this(UserManagementEntity.class, forVariable(variable), INITS);
    }

    public QUserManagementEntity(Path<? extends UserManagementEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserManagementEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserManagementEntity(PathMetadata metadata, PathInits inits) {
        this(UserManagementEntity.class, metadata, inits);
    }

    public QUserManagementEntity(Class<? extends UserManagementEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.admin = inits.isInitialized("admin") ? new QUserEntity(forProperty("admin")) : null;
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user")) : null;
    }

}

