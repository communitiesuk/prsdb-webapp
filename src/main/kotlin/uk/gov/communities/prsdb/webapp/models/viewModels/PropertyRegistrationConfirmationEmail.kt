package uk.gov.communities.prsdb.webapp.models.viewModels

data class PropertyRegistrationConfirmationEmail(
    val prn: String,
    val singleLineAddress: String,
    val cost: String,
) : EmailTemplateModel {
    private val prnKey = "prn number"
    private val addressKey = "property address"
    private val costKey = "cost"

    override val templateId = EmailTemplateId.PROPERTY_REGISTRATION_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            prnKey to prn,
            addressKey to singleLineAddress,
            costKey to cost,
        )
}
