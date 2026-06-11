package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.EmailTemplateMetadataFactory
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData
import java.net.URI
import kotlin.String

class EmailTemplateModelsTests {
    companion object {
        @JvmStatic
        private fun templateList() =
            listOf(
                EmailTemplateTestData(
                    LocalCouncilInvitationEmail(
                        MockLocalCouncilData.createLocalCouncil(),
                        URI("invitationUri"),
                    ),
                    "/emails/LocalCouncilInvitation.md",
                ),
                EmailTemplateTestData(
                    LocalCouncilInvitationCancellationEmail(MockLocalCouncilData.createLocalCouncil()),
                    "/emails/LocalCouncilInvitationCancellation.md",
                ),
                EmailTemplateTestData(
                    LocalCouncilAdminInvitationEmail(MockLocalCouncilData.createLocalCouncil(), URI("invitationUri")),
                    "/emails/LocalCouncilAdminInvitation.md",
                ),
                EmailTemplateTestData(
                    LandlordRegistrationConfirmationEmail("L-CCCC_CCCC", "prsdUrl"),
                    "/emails/LandlordRegistrationConfirmation.md",
                ),
                EmailTemplateTestData(
                    PropertyRegistrationConfirmationEmail(
                        "P-XXX-YYY",
                        "1 Street Name, AB1 2CD",
                        "prsdUrl",
                        isOccupied = true,
                        jointLandlordEmails = listOf("joint1@example.com", "joint2@example.com"),
                    ),
                    "/emails/PropertyRegistrationConfirmation.md",
                    allowExtraKeys = true,
                ),
                EmailTemplateTestData(
                    PropertyDeregistrationConfirmationEmail("P-XXX-YYY", "1 Street Name, Town, Country, AB1 2CD"),
                    "/emails/PropertyDeregistrationConfirmation.md",
                ),
                EmailTemplateTestData(
                    LandlordNoPropertiesDeregistrationConfirmationEmail(),
                    "/emails/LandlordNoPropertiesDeregistrationConfirmation.md",
                ),
                EmailTemplateTestData(
                    LandlordWithPropertiesDeregistrationConfirmationEmail(
                        PropertyDetailsEmailSectionList(
                            listOf(PropertyDetailsEmailSection(propertyNumber = 1, "P-WWW-XXX", "1 Fake Street, Mirageville")),
                        ),
                    ),
                    "/emails/LandlordWithPropertiesDeregistrationConfirmation.md",
                ),
                EmailTemplateTestData(
                    VirusScanUnsuccessfulEmail(
                        "Subject for certificate",
                        "Heading for certificate",
                        "Body for certificate",
                        "1 Street Name, Town, Country, AB1 2CD",
                        "P-XXXX-XXXX",
                        URI("propertyUrl"),
                    ),
                    "/emails/VirusScanUnsuccessful.md",
                ),
                EmailTemplateTestData(
                    PropertyUpdateConfirmation(
                        "1 Street Name, Town, AB1 2CD",
                        "P-XXXX-XXXX",
                        listOf("Thing you changed"),
                        URI("dashboardUrl"),
                    ),
                    "/emails/PropertyUpdateConfirmation.md",
                ),
                EmailTemplateTestData(
                    LandlordUpdateConfirmation("L-XXXX-XXXX", URI("dashboardUrl"), "Thing you changed"),
                    "/emails/LandlordUpdateConfirmation.md",
                ),
                EmailTemplateTestData(
                    BetaFeedbackEmail("feedback", "email@test.com", "referrer"),
                    "/emails/BetaFeedbackEmail.md",
                ),
                EmailTemplateTestData(
                    ComplianceUpdateConfirmationEmail(
                        landlordName = "landlordName",
                        multiLineAddress = "multi\nline\naddress",
                        registrationNumber = RegistrationNumberDataModel(type = RegistrationNumberType.PROPERTY, number = 123456L),
                        dashboardUrl = URI("dashboardUrl"),
                        newCertificateUrl = URI("newCertificateUrl"),
                        complianceUpdateType = ComplianceUpdateConfirmationEmail.UpdateType.CERTIFICATE_ADDED,
                        certificateType = "gas safety certificate",
                        certificateTypeLabel = "Gas safety certificate",
                        expiryDate = "1 January 2027",
                    ),
                    "/emails/ComplianceUpdatedConfirmation.md",
                    allowExtraKeys = true,
                ),
                EmailTemplateTestData(
                    ComplianceUpdateConfirmationEmail(
                        landlordName = "landlordName",
                        multiLineAddress = "multi\nline\naddress",
                        registrationNumber = RegistrationNumberDataModel(type = RegistrationNumberType.PROPERTY, number = 123456L),
                        dashboardUrl = URI("dashboardUrl"),
                        newCertificateUrl = URI("newCertificateUrl"),
                        complianceUpdateType = ComplianceUpdateConfirmationEmail.UpdateType.EXPIRED_CERTIFICATE_OCCUPIED,
                        certificateType = "gas safety certificate",
                        certificateTypeLabel = "Gas safety certificate",
                        deadlineDate = "1 January 2027",
                    ),
                    "/emails/ComplianceExpiredOccupiedConfirmation.md",
                    allowExtraKeys = true,
                ),
                EmailTemplateTestData(
                    ComplianceUpdateConfirmationEmail(
                        landlordName = "landlordName",
                        multiLineAddress = "multi\nline\naddress",
                        registrationNumber = RegistrationNumberDataModel(type = RegistrationNumberType.PROPERTY, number = 123456L),
                        dashboardUrl = URI("dashboardUrl"),
                        newCertificateUrl = URI("newCertificateUrl"),
                        complianceUpdateType = ComplianceUpdateConfirmationEmail.UpdateType.EXPIRED_CERTIFICATE_UNOCCUPIED,
                        certificateType = "gas safety certificate",
                        certificateTypeLabel = "Gas safety certificate",
                    ),
                    "/emails/ComplianceExpiredUnoccupiedConfirmation.md",
                    allowExtraKeys = true,
                ),
                EmailTemplateTestData(
                    ComplianceUpdateConfirmationEmail(
                        landlordName = "landlordName",
                        multiLineAddress = "multi\nline\naddress",
                        registrationNumber = RegistrationNumberDataModel(type = RegistrationNumberType.PROPERTY, number = 123456L),
                        dashboardUrl = URI("dashboardUrl"),
                        newCertificateUrl = URI("newCertificateUrl"),
                        complianceUpdateType = ComplianceUpdateConfirmationEmail.UpdateType.EXPIRED_EPC_OCCUPIED,
                        certificateType = "energy performance certificate (EPC)",
                        certificateTypeLabel = "Energy performance certificate (EPC)",
                    ),
                    "/emails/ComplianceExpiredOccupiedEpcConfirmation.md",
                    allowExtraKeys = true,
                ),
                EmailTemplateTestData(
                    LocalCouncilRegistrationConfirmationEmail("councilName", "prsdUrl", isAdmin = true),
                    "/emails/LocalCouncilRegistrationConfirmation.md",
                    allowExtraKeys = true,
                ),
                EmailTemplateTestData(
                    LocalCouncilUserDeletionEmail("councilName"),
                    "/emails/LocalCouncilUserDeletion.md",
                ),
                EmailTemplateTestData(
                    LocalCouncilUserDeletionInformAdminEmail("councilName", "email", "userName", "prsdUrl"),
                    "/emails/LocalCouncilUserDeletionAdminEmail.md",
                ),
                EmailTemplateTestData(
                    LocalCouncilUserInvitationInformAdminEmail("councilName", "email", "prsdURL"),
                    "/emails/LocalCouncilUserInvitationInformAdminEmail.md",
                ),
                EmailTemplateTestData(
                    IncompletePropertyReminderEmail("propertyAddress", 7, "prsdUrl"),
                    "/emails/IncompletePropertyReminder.md",
                ),
                EmailTemplateTestData(
                    JointLandlordInvitationEmail("John Smith", "1 Fake Street, London", URI("invitationUrl")),
                    "/emails/JointLandlordInvitation.md",
                ),
                EmailTemplateTestData(
                    JointLandlordInvitationExpiryEmail(
                        recipientName = "Lois",
                        invitedEmail = "invited@example.com",
                        propertyAddress = "1 Fake Street\nLondon\nSW1A 1AA",
                        propertyRecordUri = URI("propertyRecordUrl"),
                        expiryDays = 28,
                    ),
                    "/emails/JointLandlordInvitationExpiry.md",
                ),
                EmailTemplateTestData(
                    JointLandlordInvitationCancellationInviteeEmail(
                        propertyAddress = "1 Fake Street, London",
                    ),
                    "/emails/JointLandlordInvitationCancellationInvitee.md",
                ),
                EmailTemplateTestData(
                    JointLandlordInvitationCancellationCancellerEmail(
                        recipientName = "John Smith",
                        invitedEmail = "invitee@example.com",
                        propertyAddress = "1 Fake Street, London",
                        propertyRecordUrl = "https://example.com/property",
                    ),
                    "/emails/JointLandlordInvitationCancellationCanceller.md",
                ),
                EmailTemplateTestData(
                    JointLandlordInvitationCancellationOtherLandlordEmail(
                        recipientName = "Jane Doe",
                        invitedEmail = "invitee@example.com",
                        propertyAddress = "1 Fake Street, London",
                        propertyRecordUrl = "https://example.com/property",
                    ),
                    "/emails/JointLandlordInvitationCancellationOtherLandlord.md",
                ),
                EmailTemplateTestData(
                    JointLandlordInvitationRejectionEmail(
                        recipientName = "Lois Lane",
                        inviteeEmail = "invitee@example.com",
                        propertyAddress = "Flat 1\n11 Elm Drive\nLondon\nNW8 2DK",
                        propertyRecordUrl = "https://example.com/property/42",
                    ),
                    "/emails/JointLandlordInvitationRejection.md",
                ),
            )
    }

    data class EmailTemplateTestData(
        val model: EmailTemplateModel,
        val markdownLocation: String,
        val allowExtraKeys: Boolean = false,
    ) {
        override fun toString(): String = model.javaClass.simpleName
    }

    @ParameterizedTest(name = "{0} keys match markdown")
    @MethodSource("templateList")
    fun `EmailTemplateModels hashmaps have keys that match the parameters in their markdown templates`(testData: EmailTemplateTestData) {
        // Arrange
        val emailTemplateMetadata = EmailTemplateMetadataFactory(null)
        val storedBody = javaClass.getResource(testData.markdownLocation)?.readText() ?: ""
        val storedMetadata =
            emailTemplateMetadata.metadataList.single { metadata ->
                metadata.enumName == testData.model.template.name
            }

        val subjectParameters = extractParameters(storedMetadata.subject)
        val bodyParameters = extractParameters(storedBody)
        val parameters = (subjectParameters + bodyParameters).distinct()

        // Act
        val modelHashMap = testData.model.toHashMap()

        // Assert
        if (!testData.allowExtraKeys) {
            Assertions.assertEquals(parameters.size, modelHashMap.size)
        }

        for (parameter in parameters) {
            if (isOptionalContentParameter(parameter)) {
                modelHashMap[extractOptionalContentParameter(parameter)] in listOf("yes", "no")
            } else {
                modelHashMap.keys.single { key -> key == parameter }
            }
        }
    }

    private fun isOptionalContentParameter(parameter: String): Boolean = extractOptionalContentParameter(parameter).isNotEmpty()

    private fun extractOptionalContentParameter(parameter: String): String = parameter.substringBefore("??", "")

    private fun extractParameters(body: String): List<String> {
        val parameterRegex = Regex("\\(\\((.*?)\\)\\)")
        return parameterRegex.findAll(body).map { result -> result.value.trim(')').trim('(') }.toList()
    }
}
