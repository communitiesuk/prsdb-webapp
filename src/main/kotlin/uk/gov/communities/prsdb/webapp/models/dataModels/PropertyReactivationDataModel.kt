package uk.gov.communities.prsdb.webapp.models.dataModels

data class PropertyReactivationDataModel(
    val licensing: LicensingDetailsDataModel? = null,
    val gasSafety: GasSafetyDetailsDataModel? = null,
    val electricalSafety: ElectricalSafetyDetailsDataModel? = null,
    val epc: EpcDetailsDataModel? = null,
    val tenancyDetails: TenancyDetailsDataModel? = null,
)
