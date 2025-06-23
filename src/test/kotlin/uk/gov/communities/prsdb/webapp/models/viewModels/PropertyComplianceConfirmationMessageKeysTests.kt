package uk.gov.communities.prsdb.webapp.models.viewModels

import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import kotlin.test.assertEquals

class PropertyComplianceConfirmationMessageKeysTests {
    @ParameterizedTest(name = "when {0}")
    @MethodSource("providePropertyCompliancesWithNonCompliantMsgKeys")
    fun `nonCompliantMsgKeys align with the PropertyCompliance's status`(
        propertyCompliance: PropertyCompliance,
        expectedNonCompliantMsgKeys: List<String>,
    ) {
        val viewModel = PropertyComplianceConfirmationMessageKeys(propertyCompliance)
        assertEquals(expectedNonCompliantMsgKeys, viewModel.nonCompliantMsgKeys)
    }

    @ParameterizedTest(name = "when {0} certificates are compliant")
    @MethodSource("providePropertyCompliancesWithCompliantMsgKeys")
    fun `compliantMsgKeys align with the PropertyCompliance's status`(
        propertyCompliance: PropertyCompliance,
        expectedCompliantMsgKeys: List<String>,
    ) {
        val viewModel = PropertyComplianceConfirmationMessageKeys(propertyCompliance)
        assertEquals(expectedCompliantMsgKeys, viewModel.compliantMsgKeys)
    }

    companion object {
        @JvmStatic
        private fun providePropertyCompliancesWithNonCompliantMsgKeys() =
            arrayOf(
                arguments(
                    named("certificates are in date", PropertyComplianceBuilder.createWithInDateCerts()),
                    emptyList<String>(),
                ),
                arguments(
                    named("certificates are expired", PropertyComplianceBuilder.createWithExpiredCerts()),
                    expiredCertMsgKeys,
                ),
                arguments(
                    named("certificates have exemptions", PropertyComplianceBuilder.createWithCertExemptions()),
                    emptyList<String>(),
                ),
                arguments(
                    named("certificates are missing", PropertyComplianceBuilder.createWithMissingCerts()),
                    missingCertMsgKeys,
                ),
                arguments(
                    named("EPC is low rated", lowRatingEpcPropertyCompliance),
                    lowRatingEpcMsgKeys,
                ),
                arguments(
                    named("EPC is low rated and expired", expiredAndLowRatingEpcPropertyCompliance),
                    expiredAndLowRatingEpcMsgKeys,
                ),
            )

        @JvmStatic
        private fun providePropertyCompliancesWithCompliantMsgKeys() =
            arrayOf(
                arguments(named("all", PropertyComplianceBuilder.createWithInDateCerts()), allCompliantCertMsgKeys),
                arguments(named("some", lowRatingEpcPropertyCompliance), someCompliantCertMsgKeys),
                arguments(named("no", PropertyComplianceBuilder().build()), noCompliantCertMsgKeys),
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

        private val expiredCertMsgKeys =
            listOf(
                "propertyCompliance.confirmation.nonCompliant.bullet.gasSafety.expired",
                "propertyCompliance.confirmation.nonCompliant.bullet.eicr.expired",
                "propertyCompliance.confirmation.nonCompliant.bullet.epc.expired",
            )

        private val missingCertMsgKeys =
            listOf(
                "propertyCompliance.confirmation.nonCompliant.bullet.gasSafety.missing",
                "propertyCompliance.confirmation.nonCompliant.bullet.eicr.missing",
                "propertyCompliance.confirmation.nonCompliant.bullet.epc.missing",
            )

        private val lowRatingEpcMsgKeys = listOf("propertyCompliance.confirmation.nonCompliant.bullet.epc.lowRating")

        private val expiredAndLowRatingEpcMsgKeys = listOf("propertyCompliance.confirmation.nonCompliant.bullet.epc.expiredAndLowRating")

        private val allCompliantCertMsgKeys =
            listOf(
                "propertyCompliance.confirmation.compliant.bullet.gasSafety",
                "propertyCompliance.confirmation.compliant.bullet.eicr",
                "propertyCompliance.confirmation.compliant.bullet.epc",
                "propertyCompliance.confirmation.compliant.bullet.responsibilities",
            )

        private val someCompliantCertMsgKeys = allCompliantCertMsgKeys - "propertyCompliance.confirmation.compliant.bullet.epc"

        private val noCompliantCertMsgKeys = listOf("propertyCompliance.confirmation.compliant.bullet.responsibilities")
    }
}
