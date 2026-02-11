package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.ONE_LOGIN_INFO_URL
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
                        "prsdUrl",
                        ONE_LOGIN_INFO_URL,
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
                    PropertyRegistrationConfirmationEmail("P-XXX-YYY", "1 Street Name, AB1 2CD", "prsdUrl", isOccupied = true),
                    "/emails/PropertyRegistrationConfirmation.md",
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
                    FullPropertyComplianceConfirmationEmail(
                        "1 Street Name, Town, Country, AB1 2CD",
                        EmailBulletPointList("certificate 1", "certificate 2"),
                        "prsdUrl",
                    ),
                    "/emails/FullPropertyComplianceConfirmation.md",
                ),
                EmailTemplateTestData(
                    PartialPropertyComplianceConfirmationEmail(
                        "1 Street Name, Town, Country, AB1 2CD",
                        RegistrationNumberDataModel(RegistrationNumberType.PROPERTY, 12345L),
                        EmailBulletPointList("certificate 3", "certificate 4"),
                        "updateComplianceUrl",
                    ),
                    "/emails/PartialPropertyComplianceConfirmation.md",
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
                        "1 Street Name, Town, Country, AB1 2CD",
                        "P-XXX-YYY",
                        URI("prsdUrl"),
                        EmailBulletPointList("Thing you changed"),
                    ),
                    "/emails/PropertyUpdateConfirmation.md",
                ),
                EmailTemplateTestData(
                    LandlordUpdateConfirmation("L-XXXX-XXXX", URI("dashboardUrl"), "Thing you changed"),
                    "/emails/LandlordUpdateConfirmation.md",
                ),
                EmailTemplateTestData(
                    GiveFeedbackLaterEmail(),
                    "/emails/GiveFeedbackLater.md",
                ),
                EmailTemplateTestData(
                    BetaFeedbackEmail("feedback", "email@test.com", "referrer"),
                    "/emails/BetaFeedbackEmail.md",
                ),
                EmailTemplateTestData(
                    ComplianceUpdateConfirmationEmail(
                        "propertyAddress",
                        RegistrationNumberDataModel(type = RegistrationNumberType.PROPERTY, number = 123456L),
                        URI("dashboardUrl"),
                        ComplianceUpdateConfirmationEmail.UpdateType.VALID_GAS_SAFETY_INFORMATION,
                    ),
                    "/emails/GasSafetyUpdateConfirmation.md",
                    allowExtraKeys = true,
                ),
                EmailTemplateTestData(
                    ComplianceUpdateConfirmationEmail(
                        "propertyAddress",
                        RegistrationNumberDataModel(type = RegistrationNumberType.PROPERTY, number = 123456L),
                        URI("dashboardUrl"),
                        ComplianceUpdateConfirmationEmail.UpdateType.EXPIRED_GAS_SAFETY_INFORMATION,
                    ),
                    "/emails/GasSafetyExpiredUpdateConfirmation.md",
                    allowExtraKeys = true,
                ),
                EmailTemplateTestData(
                    ComplianceUpdateConfirmationEmail(
                        "propertyAddress",
                        RegistrationNumberDataModel(type = RegistrationNumberType.PROPERTY, number = 123456L),
                        URI("dashboardUrl"),
                        ComplianceUpdateConfirmationEmail.UpdateType.VALID_ELECTRICAL_INFORMATION,
                    ),
                    "/emails/ElectricalSafetyUpdateConfirmation.md",
                    allowExtraKeys = true,
                ),
                EmailTemplateTestData(
                    ComplianceUpdateConfirmationEmail(
                        "propertyAddress",
                        RegistrationNumberDataModel(type = RegistrationNumberType.PROPERTY, number = 123456L),
                        URI("dashboardUrl"),
                        ComplianceUpdateConfirmationEmail.UpdateType.EXPIRED_ELECTRICAL_INFORMATION,
                    ),
                    "/emails/ElectricalSafetyExpiredUpdateConfirmation.md",
                    allowExtraKeys = true,
                ),
                EmailTemplateTestData(
                    ComplianceUpdateConfirmationEmail(
                        "propertyAddress",
                        RegistrationNumberDataModel(type = RegistrationNumberType.PROPERTY, number = 123456L),
                        URI("dashboardUrl"),
                        ComplianceUpdateConfirmationEmail.UpdateType.VALID_EPC_INFORMATION,
                    ),
                    "/emails/EnergyPerformanceUpdateConfirmation.md",
                    allowExtraKeys = true,
                ),
                EmailTemplateTestData(
                    ComplianceUpdateConfirmationEmail(
                        "propertyAddress",
                        RegistrationNumberDataModel(type = RegistrationNumberType.PROPERTY, number = 123456L),
                        URI("dashboardUrl"),
                        ComplianceUpdateConfirmationEmail.UpdateType.LOW_RATED_EPC_INFORMATION,
                    ),
                    "/emails/EnergyPerformanceLowUpdateConfirmation.md",
                    allowExtraKeys = true,
                ),
                EmailTemplateTestData(
                    ComplianceUpdateConfirmationEmail(
                        "propertyAddress",
                        RegistrationNumberDataModel(type = RegistrationNumberType.PROPERTY, number = 123456L),
                        URI("dashboardUrl"),
                        ComplianceUpdateConfirmationEmail.UpdateType.EXPIRED_EPC_INFORMATION,
                    ),
                    "/emails/EnergyPerformanceExpiredUpdateConfirmation.md",
                    allowExtraKeys = true,
                ),
                EmailTemplateTestData(
                    ComplianceUpdateConfirmationEmail(
                        "propertyAddress",
                        RegistrationNumberDataModel(type = RegistrationNumberType.PROPERTY, number = 123456L),
                        URI("dashboardUrl"),
                        ComplianceUpdateConfirmationEmail.UpdateType.REMOVED_MEES_EPC_INFORMATION,
                    ),
                    "/emails/EnergyPerformanceMeesRemovedUpdateConfirmation.md",
                    allowExtraKeys = true,
                ),
                EmailTemplateTestData(
                    ComplianceUpdateConfirmationEmail(
                        "propertyAddress",
                        RegistrationNumberDataModel(type = RegistrationNumberType.PROPERTY, number = 123456L),
                        URI("dashboardUrl"),
                        ComplianceUpdateConfirmationEmail.UpdateType.NO_EPC_INFORMATION,
                    ),
                    "/emails/EnergyPerformanceRemovedUpdateConfirmation.md",
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
