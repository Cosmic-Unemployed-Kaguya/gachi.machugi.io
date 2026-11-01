package kaguya.user.domain.user.model.dto.request;

import kaguya.user.domain.user.model.enums.ManagementType;
import kaguya.user.domain.user.model.enums.SearchType;

public record BlockSearchReq(
        SearchType searchType,
        String keyword,
        ManagementType managementType
) {}