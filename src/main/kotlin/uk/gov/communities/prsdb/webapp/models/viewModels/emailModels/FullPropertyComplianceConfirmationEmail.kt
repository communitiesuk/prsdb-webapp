package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class FullPropertyComplianceConfirmationEmail(
    private val propertyAddress: String,
    private val compliantBulletPoints: EmailBulletPointList,
    val dashboardUrl: String,
) : EmailTemplateModel {
    override val templateId = EmailTemplateId.FULL_PROPERTY_COMPLIANCE_CONFIRMATION

    override fun toHashMap() =
        hashMapOf(
            "property address" to propertyAddress,
            "compliant bullets" to compliantBulletPoints.toString(),
            "dashboard URL" to dashboardUrl,
        )
}
