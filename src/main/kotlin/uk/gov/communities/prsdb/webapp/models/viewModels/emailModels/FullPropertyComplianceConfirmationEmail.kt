package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

// TODO PDJB-770: Remove this email template — the old full compliance journey no longer exists.
data class FullPropertyComplianceConfirmationEmail(
    private val propertyAddress: String,
    private val compliantBulletPoints: EmailBulletPointList,
    val dashboardUrl: String,
) : EmailTemplateModel {
    override val template = EmailTemplate.FULL_PROPERTY_COMPLIANCE_CONFIRMATION

    override fun toHashMap() =
        hashMapOf(
            "property address" to propertyAddress,
            "compliant bullets" to compliantBulletPoints.toString(),
            "dashboard URL" to dashboardUrl,
        )
}
