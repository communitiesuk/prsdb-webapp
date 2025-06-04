package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.datetime.LocalDate

data class IncompleteComplianceDataModel(
    val propertyOwnershipId: Long,
    val singleLineAddress: String,
    val localAuthorityName: String,
    val certificatesDueDate: LocalDate,
    val gasSafety: Boolean,
    val electricalSafety: Boolean,
    val energyPerformance: Boolean,
    val landlordsResponsibilities: Boolean,
) {
    fun isComplianceInProgress(): Boolean = gasSafety || electricalSafety || energyPerformance || landlordsResponsibilities
}
