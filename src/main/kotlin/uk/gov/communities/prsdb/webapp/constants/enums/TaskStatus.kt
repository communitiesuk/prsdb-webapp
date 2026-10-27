package uk.gov.communities.prsdb.webapp.constants.enums

enum class TaskStatus {
    CANNOT_START,
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,

    // Letting agent provides these details, so they're never needed from the landlord
    NOT_REQUIRED,

    // Not applicable until the property becomes occupied
    NOT_NEEDED_YET,
}
