package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class LettingAgentComplianceUpdateNotificationEmail(
    val recipientName: String,
    val multiLineAddress: String,
    val registrationNumber: String,
    val certificateType: String,
    val certificateTypeLabel: String,
    val expiryDate: String,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    override val template = EmailTemplate.LETTING_AGENT_COMPLIANCE_UPDATE_NOTIFICATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            "recipient name" to recipientName,
            "multi line address" to multiLineAddress,
            "registration number" to registrationNumber,
            "certificate type" to certificateType,
            "certificate type label" to certificateTypeLabel,
            "expiry date" to expiryDate,
            "property record url" to propertyRecordUrl,
        )
}
