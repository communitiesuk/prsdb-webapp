package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import java.time.LocalDate

data class EpcDetailsDataModel(
    val certificateUrl: String?,
    val expiryDate: LocalDate?,
    val energyRating: String?,
    val tenancyStartedBeforeExpiry: Boolean?,
    val exemptionReason: EpcExemptionReason?,
    val meesExemptionReason: MeesExemptionReason?,
)
