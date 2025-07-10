package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import kotlin.test.assertEquals

class PropertyComplianceViewModelTests {
    @ParameterizedTest(name = "{0}")
    @MethodSource("providePropertyComplianceAndExpectedMessages")
    fun `notificationMessages returns correctly populated list when`(
        propertyCompliance: PropertyCompliance,
        expectedNotificationMessages: List<String>,
    ) {
        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    companion object {
        @JvmStatic
        private fun providePropertyComplianceAndExpectedMessages() =
            arrayOf(
                arguments(
                    named("property is compliant", PropertyComplianceBuilder.createWithInDateCerts()),
                    emptyList<String>(),
                ),
                arguments(
                    named("all certs are expired", PropertyComplianceBuilder.createWithExpiredCerts()),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.gasCert.expired",
                        "propertyDetails.complianceInformation.notificationMessage.eicr.expired",
                        "propertyDetails.complianceInformation.notificationMessage.epc.expired",
                    ),
                ),
                arguments(
                    named(
                        "gas and eicr certs are expired",
                        PropertyComplianceBuilder.createWithGasAndEicrExpiredCerts(),
                    ),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.gasCert.expired",
                        "propertyDetails.complianceInformation.notificationMessage.eicr.expired",
                    ),
                ),
                arguments(
                    named(
                        "gas and epc certs are expired",
                        PropertyComplianceBuilder.createWithGasAndEpcExpiredCerts(),
                    ),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.gasCert.expired",
                        "propertyDetails.complianceInformation.notificationMessage.epc.expired",
                    ),
                ),
                arguments(
                    named(
                        "eicr and epc certs are expired",
                        PropertyComplianceBuilder.createWithEicrAndEpcExpiredCerts(),
                    ),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.eicr.expired",
                        "propertyDetails.complianceInformation.notificationMessage.epc.expired",
                    ),
                ),
                arguments(
                    named(
                        "gas cert is expired",
                        PropertyComplianceBuilder.createWithGasCertExpiredAfterUpload(),
                    ),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.gasCert.expired",
                    ),
                ),
                arguments(
                    named(
                        "eicr cert is expired",
                        PropertyComplianceBuilder.createWithEicrExpiredAfterUpload(),
                    ),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.eicr.expired",
                    ),
                ),
                arguments(
                    named(
                        "epc cert is expired",
                        PropertyComplianceBuilder.createWithOnlyEpcExpiredCert(),
                    ),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.epc.expired",
                    ),
                ),
                arguments(
                    named("all certs are missing", PropertyComplianceBuilder.createWithMissingCerts()),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.gasCert.missing",
                        "propertyDetails.complianceInformation.notificationMessage.eicr.missing",
                        "propertyDetails.complianceInformation.notificationMessage.epc.missing",
                    ),
                ),
                arguments(
                    named(
                        "gas and eicr certs are missing",
                        PropertyComplianceBuilder.createWithGasAndEicrMissingCerts(),
                    ),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.gasCert.missing",
                        "propertyDetails.complianceInformation.notificationMessage.eicr.missing",
                    ),
                ),
                arguments(
                    named(
                        "gas and epc certs are missing",
                        PropertyComplianceBuilder.createWithGasAndEpcMissingCerts(),
                    ),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.gasCert.missing",
                        "propertyDetails.complianceInformation.notificationMessage.epc.missing",
                    ),
                ),
                arguments(
                    named(
                        "eicr and epc certs are missing",
                        PropertyComplianceBuilder.createWithEicrAndEpcMissingCerts(),
                    ),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.eicr.missing",
                        "propertyDetails.complianceInformation.notificationMessage.epc.missing",
                    ),
                ),
                arguments(
                    named(
                        "gas cert is missing",
                        PropertyComplianceBuilder.createWithOnlyGasMissingCert(),
                    ),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.gasCert.missing",
                    ),
                ),
                arguments(
                    named(
                        "eicr cert is missing",
                        PropertyComplianceBuilder.createWithOnlyEicrMissingCert(),
                    ),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.eicr.missing",
                    ),
                ),
                arguments(
                    named(
                        "epc cert is missing",
                        PropertyComplianceBuilder.createWithOnlyEpcMissingCert(),
                    ),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.epc.missing",
                    ),
                ),
                arguments(
                    named(
                        "epc rating is low",
                        PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating(),
                    ),
                    listOf(
                        "propertyDetails.complianceInformation.notificationMessage.epc.lowRating",
                    ),
                ),
            )
    }
}
