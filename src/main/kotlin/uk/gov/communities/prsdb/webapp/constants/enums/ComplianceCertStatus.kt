package uk.gov.communities.prsdb.webapp.constants.enums

enum class ComplianceCertStatus {
    NOT_REQUIRED,
    NOT_STARTED,
    ADDED,
    NOT_ADDED,
    PROVIDE_LATER,
    EXPIRED,
    ;

    companion object {
        val MISSING_STATUSES = listOf(NOT_ADDED, PROVIDE_LATER)
        val VALID_STATUSES = listOf(ADDED, NOT_REQUIRED)
    }
}
