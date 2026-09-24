package uk.gov.communities.prsdb.webapp.models.viewModels

data class PropertyRegistrationConfirmationViewModel(
    val provideMissingDetails: Boolean,
    val gasSafetyRequired: Boolean,
    val electricalSafetyRequired: Boolean,
    val epcRequired: Boolean,
    val licenseProvideLater: Boolean,
    val tenancyProvideLater: Boolean,
)
