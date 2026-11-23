package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType

data class LicensingDetailsDataModel(
    val licensingType: LicensingType,
    val licenceNumber: String?,
)
