package uk.gov.communities.prsdb.webapp.models.viewModels

import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import kotlin.test.assertEquals

class PropertyComplianceConfirmationMessagesTests {
    @ParameterizedTest(name = "when {0}")
    @MethodSource("providePropertyCompliancesWithNonCompliantMsgs")
    fun `nonCompliantMsgs align with the PropertyCompliance's status`(
        propertyCompliance: PropertyCompliance,
        expectedNonCompliantMsgs: List<String>,
    ) {
        val viewModel = PropertyComplianceConfirmationMessages(propertyCompliance)
        assertEquals(expectedNonCompliantMsgs, viewModel.nonCompliantMsgs)
    }

    @ParameterizedTest(name = "when {0} certificates are compliant")
    @MethodSource("providePropertyCompliancesWithCompliantMsgs")
    fun `compliantMsgs align with the PropertyCompliance's status`(
        propertyCompliance: PropertyCompliance,
        expectedCompliantMsgs: List<String>,
    ) {
        val viewModel = PropertyComplianceConfirmationMessages(propertyCompliance)
        assertEquals(expectedCompliantMsgs, viewModel.compliantMsgs)
    }

    companion object {
        @JvmStatic
        private fun providePropertyCompliancesWithNonCompliantMsgs() =
            arrayOf(
                Arguments.arguments(
                    Named.named(
                        "certificates are in date",
                        PropertyComplianceBuilder.createWithInDateCerts(),
                    ),
                    emptyList<String>(),
                ),
                Arguments.arguments(
                    Named.named(
                        "certificates are expired",
                        PropertyComplianceBuilder.createWithExpiredCerts(),
                    ),
                    expiredCertMsgs,
                ),
                Arguments.arguments(
                    Named.named(
                        "certificates have exemptions",
                        PropertyComplianceBuilder.createWithCertExemptions(),
                    ),
                    emptyList<String>(),
                ),
                Arguments.arguments(
                    Named.named(
                        "certificates are missing",
                        PropertyComplianceBuilder.createWithMissingCerts(),
                    ),
                    missingCertMsgs,
                ),
                Arguments.arguments(Named.named("EPC is low rated", lowRatingEpcPropertyCompliance), lowRatingEpcMsgs),
                Arguments.arguments(
                    Named.named(
                        "EPC is low rated and expired",
                        expiredAndLowRatingEpcPropertyCompliance,
                    ),
                    expiredAndLowRatingEpcMsgs,
                ),
            )

        @JvmStatic
        private fun providePropertyCompliancesWithCompliantMsgs() =
            arrayOf(
                Arguments.arguments(
                    Named.named("all", PropertyComplianceBuilder.createWithInDateCerts()),
                    allCompliantCertMsgs,
                ),
                Arguments.arguments(Named.named("some", lowRatingEpcPropertyCompliance), someCompliantCertMsgs),
                Arguments.arguments(Named.named("no", PropertyComplianceBuilder().build()), noCompliantCertMsgs),
            )

        private val lowRatingEpcPropertyCompliance =
            PropertyComplianceBuilder()
                .withGasSafetyCert()
                .withEicr()
                .withEpc()
                .withLowEpcRating()
                .build()

        private val expiredAndLowRatingEpcPropertyCompliance =
            PropertyComplianceBuilder()
                .withGasSafetyCert()
                .withEicr()
                .withExpiredEpc()
                .withLowEpcRating()
                .build()

        private val expiredCertMsgs =
            listOf(
                "propertyCompliance.confirmation.nonCompliant.bullet.gasSafety.expired",
                "propertyCompliance.confirmation.nonCompliant.bullet.eicr.expired",
                "propertyCompliance.confirmation.nonCompliant.bullet.epc.expired",
            )

        private val missingCertMsgs =
            listOf(
                "propertyCompliance.confirmation.nonCompliant.bullet.gasSafety.missing",
                "propertyCompliance.confirmation.nonCompliant.bullet.eicr.missing",
                "propertyCompliance.confirmation.nonCompliant.bullet.epc.missing",
            )

        private val lowRatingEpcMsgs = listOf("propertyCompliance.confirmation.nonCompliant.bullet.epc.lowRating")

        private val expiredAndLowRatingEpcMsgs = listOf("propertyCompliance.confirmation.nonCompliant.bullet.epc.expiredAndLowRating")

        private val allCompliantCertMsgs =
            listOf(
                "propertyCompliance.confirmation.compliant.bullet.gasSafety",
                "propertyCompliance.confirmation.compliant.bullet.eicr",
                "propertyCompliance.confirmation.compliant.bullet.epc",
                "propertyCompliance.confirmation.compliant.bullet.responsibilities",
            )

        private val someCompliantCertMsgs = allCompliantCertMsgs - "propertyCompliance.confirmation.compliant.bullet.epc"

        private val noCompliantCertMsgs = listOf("propertyCompliance.confirmation.compliant.bullet.responsibilities")
    }
}
