package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

public record ProfileSearchResultDto(
    int page,
    int entriesPerPage,
    int numberOfPages,
    List<ProfileDetailDto> profiles
) {
}