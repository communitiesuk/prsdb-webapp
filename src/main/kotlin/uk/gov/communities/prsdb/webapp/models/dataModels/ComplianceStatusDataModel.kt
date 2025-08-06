package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus

data class ComplianceStatusDataModel(
    val propertyOwnershipId: Long,
    val singleLineAddress: String,
    val registrationNumber: String,
    val gasSafetyStatus: ComplianceCertStatus,
    val eicrStatus: ComplianceCertStatus,
    val epcStatus: ComplianceCertStatus,
    val isComplete: Boolean,
) {
    val isInProgress: Boolean
        get() = !isComplete && certStatuses.any { it != ComplianceCertStatus.NOT_STARTED }

    private val certStatuses = listOf(gasSafetyStatus, eicrStatus, epcStatus)
}
