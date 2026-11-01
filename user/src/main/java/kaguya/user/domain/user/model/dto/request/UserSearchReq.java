package kaguya.user.domain.user.model.dto.request;

import kaguya.user.domain.user.model.enums.Role;
import kaguya.user.domain.user.model.enums.SearchType;
import kaguya.user.domain.user.model.enums.Status;

public record UserSearchReq (
        SearchType searchType,
        String keyword,
        Role role,
        Status status
) {}