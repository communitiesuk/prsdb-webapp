package uk.gov.communities.prsdb.webapp.constants.enums

enum class TaskStatus {
    CANNOT_START,
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,

    // TODO PDJB-1617: remove NOT_REQUIRED (in favour of NOT_NEEDED_YET) this when we remove the delegation feature flag
    NOT_REQUIRED,
    NOT_NEEDED_YET,
}
