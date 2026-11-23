package uk.gov.communities.prsdb.webapp.models.dataModels

data class PropertyReactivationDataModel(
    val licensing: LicensingDetailsDataModel? = null,
    val tenancyDetails: TenancyDetailsDataModel? = null,
)
