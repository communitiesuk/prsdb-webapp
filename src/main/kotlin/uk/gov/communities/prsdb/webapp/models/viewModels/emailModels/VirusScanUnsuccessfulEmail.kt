package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyFileNameInfo.FileCategory
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

@ConsistentCopyVisibility
data class VirusScanUnsuccessfulEmail private constructor(
    val headingCertificateType: String,
    val bodyCertificateType: String,
    val singleLineAddress: String,
    val registrationNumber: String,
    val propertyUrl: String,
) : EmailTemplateModel {
    private val headingCertificateTypeKey = "heading certificate type"
    private val bodyCertificateTypeKey = "body certificate type"
    private val singleLineAddressKey = "single line address"
    private val registrationNumberKey = "registration number"
    private val propertyUrlKey = "property url"

    override val templateId = EmailTemplateId.VIRUS_SCAN_UNSUCCESSFUL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            headingCertificateTypeKey to headingCertificateType,
            bodyCertificateTypeKey to bodyCertificateType,
            singleLineAddressKey to singleLineAddress,
            registrationNumberKey to registrationNumber,
            propertyUrlKey to propertyUrl,
        )

    companion object {
        fun fromPropertyOwnershipAndFileCategory(
            propertyOwnership: PropertyOwnership,
            fileCategory: FileCategory,
        ): VirusScanUnsuccessfulEmail =
            VirusScanUnsuccessfulEmail(
                headingForCertificateType(fileCategory),
                bodyForCertificateType(fileCategory),
                propertyOwnership.property.address.singleLineAddress,
                RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
                // TODO 1284: Add id tag to open correct tab in property details page
                PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id),
            )

        private fun headingForCertificateType(category: FileCategory): String =
            when (category) {
                FileCategory.GasSafetyCert -> "gas safety certificate"
                FileCategory.Eirc -> "Electrical Installation Condition Report (EICR)"
            }

        private fun bodyForCertificateType(category: FileCategory): String =
            when (category) {
                FileCategory.GasSafetyCert -> "gas compliance certificate"
                FileCategory.Eirc -> "EICR"
            }
    }
}
