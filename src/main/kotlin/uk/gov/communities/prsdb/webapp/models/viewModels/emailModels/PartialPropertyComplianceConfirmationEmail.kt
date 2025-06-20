package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

class PartialPropertyComplianceConfirmationEmail(
    private val propertyAddress: String,
    private val compliantBulletPoints: EmailBulletPointList,
    private val nonCompliantBulletPoints: EmailBulletPointList,
    private val updateComplianceUrl: String,
) : EmailTemplateModel {
    override val templateId = EmailTemplateId.PARTIAL_PROPERTY_COMPLIANCE_CONFIRMATION

    override fun toHashMap() =
        hashMapOf(
            "property address" to propertyAddress,
            "compliant bullets" to compliantBulletPoints.toString(),
            "non-compliant bullets" to nonCompliantBulletPoints.toString(),
            "update compliance URL" to updateComplianceUrl,
        )
}
