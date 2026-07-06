package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class SwitchToIndividualConfirmationEmail(
    val landlordName: String,
    val propertyAddress: String,
) : EmailTemplateModel {
    private val landlordNameKey = "landlord name"
    private val propertyAddressKey = "property address"

    override val template = EmailTemplate.SWITCH_TO_INDIVIDUAL_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            landlordNameKey to landlordName,
            propertyAddressKey to propertyAddress,
        )
}
