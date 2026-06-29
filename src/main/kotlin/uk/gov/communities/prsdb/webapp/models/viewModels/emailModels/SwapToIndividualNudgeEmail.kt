package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class SwapToIndividualNudgeEmail(
    val recipientName: String,
    val propertyAddress: String,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val propertyAddressKey = "property address"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.SWAP_TO_INDIVIDUAL_NUDGE_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            propertyAddressKey to propertyAddress,
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
