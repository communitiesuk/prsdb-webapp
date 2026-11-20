package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import java.math.BigDecimal

data class PropertyReactivationDataModel(
    val licensing: ReactivationLicensingDataModel? = null,
    val tenancyDetails: ReactivationTenancyDetailsDataModel? = null,
)

data class ReactivationLicensingDataModel(
    val licensingType: LicensingType,
    val licenceNumber: String?,
)

data class ReactivationTenancyDetailsDataModel(
    val numberOfHouseholds: Int,
    val numberOfPeople: Int,
    val billsIncludedList: String?,
    val customBillsIncluded: String?,
    val furnishedStatus: FurnishedStatus,
    val rentFrequency: RentFrequency,
    val customRentFrequency: String?,
    val rentAmount: BigDecimal,
)
