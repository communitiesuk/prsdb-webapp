package uk.gov.communities.prsdb.webapp.constants.enums

enum class ComplianceCertStatus {
    NOT_REQUIRED,
    ADDED,
    HAS_FAULTS,
    PROVIDE_LATER,
    EXPIRED,
    ;

    companion object {
        val NEEDS_COMPLIANCE_IF_OCCUPIED_STATUSES = listOf(HAS_FAULTS, PROVIDE_LATER)
        val COUNCIL_WILL_SEE_STATUSES = listOf(HAS_FAULTS, EXPIRED)
        val VALID_STATUSES = listOf(ADDED, NOT_REQUIRED)
    }
}
