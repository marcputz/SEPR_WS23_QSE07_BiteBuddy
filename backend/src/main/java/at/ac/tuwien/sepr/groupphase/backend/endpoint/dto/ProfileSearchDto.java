package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public record ProfileSearchDto(
    String creator,
    String name,
    boolean ownProfiles,
    int page,
    int entriesPerPage
) {
}
