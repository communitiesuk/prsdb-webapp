package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

data class PartialPropertyComplianceConfirmationEmail(
    private val propertyAddress: String,
    private val registrationNumber: RegistrationNumberDataModel,
    private val nonCompliantBulletPoints: EmailBulletPointList,
    val updateComplianceUrl: String,
) : EmailTemplateModel {
    override val template = EmailTemplate.PARTIAL_PROPERTY_COMPLIANCE_CONFIRMATION

    override fun toHashMap() =
        hashMapOf(
            "property address" to propertyAddress,
            "property registration number" to registrationNumber.toString(),
            "non-compliant bullets" to nonCompliantBulletPoints.toString(),
            "update compliance URL" to updateComplianceUrl,
        )
}
