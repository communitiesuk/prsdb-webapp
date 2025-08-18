package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class PropertyDeregistrationConfirmationEmail(
    val prn: String,
    val singleLineAddress: String,
) : EmailTemplateModel {
    private val prnKey = "prn number"
    private val addressKey = "property address"

    override val template = EmailTemplate.PROPERTY_DEREGISTRATION_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            prnKey to prn,
            addressKey to singleLineAddress,
        )
}
