package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public record ProfileSearchDto(
    String creator,
    String name,
    Long userId,
    int page,
    int entriesPerPage
) {
}
