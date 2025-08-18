package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.constants.ONE_LOGIN_INFO_URL
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.EmailTemplateMetadataFactory
import java.net.URI

class EmailTemplateModelsTests {
    companion object {
        @JvmStatic
        private fun templateList() =
            listOf(
                EmailTemplateTestData(
                    LocalAuthorityInvitationEmail(
                        localAuthority = createLocalAuthority(1, "name"),
                        invitationUri = URI("https://example.com"),
                        prsdUrl = "https://example.com",
                        oneLoginUrl = ONE_LOGIN_INFO_URL,
                    ),
                    "/emails/LocalAuthorityInvitation.md",
                ),
                EmailTemplateTestData(
                    LocalAuthorityInvitationCancellationEmail(createLocalAuthority(1, "name")),
                    "/emails/LocalAuthorityInvitationCancellation.md",
                ),
                EmailTemplateTestData(
                    LocalAuthorityAdminInvitationEmail(createLocalAuthority(1, "name"), URI("https://example.com")),
                    "/emails/LocalAuthorityAdminInvitation.md",
                ),
                EmailTemplateTestData(
                    LandlordRegistrationConfirmationEmail("L-CCCC_CCCC", "https://emample.com"),
                    "/emails/LandlordRegistrationConfirmation.md",
                ),
                EmailTemplateTestData(
                    PropertyRegistrationConfirmationEmail(
                        "P-XXX-YYY",
                        "1 Street Name, Town, Country, AB1 2CD",
                        "www.example.com",
                    ),
                    "/emails/PropertyRegistrationConfirmation.md",
                ),
                EmailTemplateTestData(
                    PropertyDeregistrationConfirmationEmail(
                        "P-XXX-YYY",
                        "1 Street Name, Town, Country, AB1 2CD",
                    ),
                    "/emails/PropertyDeregistrationConfirmation.md",
                ),
                EmailTemplateTestData(
                    LandlordNoPropertiesDeregistrationConfirmationEmail(),
                    "/emails/LandlordNoPropertiesDeregistrationConfirmation.md",
                ),
                EmailTemplateTestData(
                    LandlordWithPropertiesDeregistrationConfirmationEmail(
                        PropertyDetailsEmailSectionList(
                            listOf(PropertyDetailsEmailSection(1, "P-WWW-XXX", "1 Fake Street, Mirageville")),
                        ),
                    ),
                    "/emails/LandlordWithPropertiesDeregistrationConfirmation.md",
                ),
                EmailTemplateTestData(
                    FullPropertyComplianceConfirmationEmail(
                        "1 Street Name, Town, Country, AB1 2CD",
                        EmailBulletPointList("certificate 1", "certificate 2"),
                        "https://emample.com",
                    ),
                    "/emails/FullPropertyComplianceConfirmation.md",
                ),
                EmailTemplateTestData(
                    PartialPropertyComplianceConfirmationEmail(
                        "1 Street Name, Town, Country, AB1 2CD",
                        EmailBulletPointList("certificate 1", "certificate 2"),
                        EmailBulletPointList("certificate 3", "certificate 4"),
                        "https://emample.com",
                    ),
                    "/emails/PartialPropertyComplianceConfirmation.md",
                ),
                EmailTemplateTestData(
                    VirusScanUnsuccessfulEmail(
                        "Subject for certificate",
                        "Heading for certificate",
                        "Body for certificate",
                        "1 Street Name, Town, Country, AB1 2CD",
                        RegistrationNumberDataModel(type = RegistrationNumberType.PROPERTY, number = 12345L).toString(),
                        URI("https://example.com/property/12345"),
                    ),
                    "/emails/VirusScanUnsuccessful.md",
                ),
                EmailTemplateTestData(
                    PropertyUpdateConfirmation(
                        "1 Street Name, Town, Country, AB1 2CD",
                        "P-XXX-YYY",
                        URI("https://example.com"),
                        EmailBulletPointList("Thing you changed"),
                    ),
                    "/emails/PropertyUpdateConfirmation.md",
                ),
                EmailTemplateTestData(
                    LandlordUpdateConfirmation(
                        "1 Street Name, Town, Country, AB1 2CD",
                        URI("https://example.com"),
                        "Thing you changed",
                    ),
                    "/emails/LandlordUpdateConfirmation.md",
                ),
            )

        private fun createLocalAuthority(
            id: Int,
            name: String,
        ): LocalAuthority {
            val localAuthority = LocalAuthority()
            ReflectionTestUtils.setField(localAuthority, "id", id)
            ReflectionTestUtils.setField(localAuthority, "name", name)

            return localAuthority
        }
    }

    data class EmailTemplateTestData(
        val model: EmailTemplateModel,
        val markdownLocation: String,
    ) {
        override fun toString(): String = model.javaClass.simpleName
    }

    @ParameterizedTest(name = "{0} keys match markdown")
    @MethodSource("templateList")
    fun `EmailTemplateModels hashmaps have keys that match the parameters in their markdown templates`(testData: EmailTemplateTestData) {
        val emailTemplateMetadata = EmailTemplateMetadataFactory(null)
        // Arrange
        val storedBody = javaClass.getResource(testData.markdownLocation)?.readText() ?: ""
        val storedMetadata =
            emailTemplateMetadata.metadataList.single { metadata ->
                metadata.enumName == testData.model.templateId.name
            }

        val subjectParameters = extractParameters(storedMetadata.subject)
        val bodyParameters = extractParameters(storedBody)
        val parameters = (subjectParameters + bodyParameters).distinct()

        // Act
        val modelHashMap = testData.model.toHashMap()

        // Assert
        Assertions.assertEquals(parameters.size, modelHashMap.size)
        for (parameter in parameters) {
            modelHashMap.keys.single { key -> key == parameter }
        }
    }

    private fun extractParameters(body: String): List<String> {
        val parameterRegex = Regex("\\(\\((.*?)\\)\\)")
        return parameterRegex.findAll(body).map { result -> result.value.trim(')').trim('(') }.toList()
    }
}
