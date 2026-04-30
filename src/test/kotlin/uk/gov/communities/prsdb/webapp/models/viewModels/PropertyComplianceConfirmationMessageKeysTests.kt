package uk.gov.communities.prsdb.webapp.models.viewModels

import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
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
                arguments(
                    named("EIC certificate is expired", expiredEicPropertyCompliance),
                    expiredEicMsgKeys,
                ),
                arguments(
                    named("EIC certificate is missing", missingEicPropertyCompliance),
                    missingEicMsgKeys,
                ),
                arguments(
                    named("electrical safety certificate is missing with no cert type", missingWithNoCertTypePropertyCompliance),
                    missingWithNoCertTypeMsgKeys,
                ),
            )

        @JvmStatic
        private fun providePropertyCompliancesWithCompliantMsgKeys() =
            arrayOf(
                arguments(named("all", PropertyComplianceBuilder.createWithInDateCerts()), allCompliantCertMsgKeys),
                arguments(named("some", lowRatingEpcPropertyCompliance), someCompliantCertMsgKeys),
                arguments(
                    named(
                        "no",
                        PropertyComplianceBuilder().withElectricalCertType().build(),
                    ),
                    noCompliantCertMsgKeys,
                ),
            )

        private val lowRatingEpcPropertyCompliance =
            PropertyComplianceBuilder()
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withEpc()
                .withLowEpcRating()
                .build()

        private val expiredAndLowRatingEpcPropertyCompliance =
            PropertyComplianceBuilder()
                .withGasSafetyCert()
                .withElectricalSafety()
                .withElectricalCertType()
                .withExpiredEpc()
                .withLowEpcRating()
                .build()

        private val expiredCertMsgKeys =
            listOf(
                "propertyCompliance.confirmation.nonCompliant.bullet.gasSafety.expired",
                "propertyCompliance.confirmation.nonCompliant.bullet.electricalSafety.eicr.expired",
                "propertyCompliance.confirmation.nonCompliant.bullet.epc.expired",
            )

        private val missingCertMsgKeys =
            listOf(
                "propertyCompliance.confirmation.nonCompliant.bullet.gasSafety.missing",
                "propertyCompliance.confirmation.nonCompliant.bullet.electricalSafety.eicr.missing",
                "propertyCompliance.confirmation.nonCompliant.bullet.epc.missing",
            )

        private val lowRatingEpcMsgKeys = listOf("propertyCompliance.confirmation.nonCompliant.bullet.epc.lowRating")

        private val expiredAndLowRatingEpcMsgKeys = listOf("propertyCompliance.confirmation.nonCompliant.bullet.epc.expiredAndLowRating")

        private val expiredEicPropertyCompliance =
            PropertyComplianceBuilder()
                .withGasSafetyCert()
                .withExpiredElectricalSafety()
                .withElectricalCertType(CertificateType.Eic)
                .withEpc()
                .build()

        private val expiredEicMsgKeys =
            listOf("propertyCompliance.confirmation.nonCompliant.bullet.electricalSafety.eic.expired")

        private val missingEicPropertyCompliance =
            PropertyComplianceBuilder()
                .withGasSafetyCert()
                .withElectricalCertType(CertificateType.Eic)
                .withEpc()
                .build()

        private val missingEicMsgKeys =
            listOf("propertyCompliance.confirmation.nonCompliant.bullet.electricalSafety.eic.missing")

        private val missingWithNoCertTypePropertyCompliance =
            PropertyComplianceBuilder()
                .withPropertyOwnershipWithOccupancy(false)
                .withGasSafetyCert()
                .withEpc()
                .build()

        private val missingWithNoCertTypeMsgKeys =
            listOf("propertyCompliance.confirmation.nonCompliant.bullet.electricalSafety.missing")

        private val allCompliantCertMsgKeys =
            listOf(
                "propertyCompliance.confirmation.compliant.bullet.gasSafety",
                "propertyCompliance.confirmation.compliant.bullet.electricalSafety",
                "propertyCompliance.confirmation.compliant.bullet.epc",
                "propertyCompliance.confirmation.compliant.bullet.responsibilities",
            )

        private val someCompliantCertMsgKeys = allCompliantCertMsgKeys - "propertyCompliance.confirmation.compliant.bullet.epc"

        private val noCompliantCertMsgKeys = listOf("propertyCompliance.confirmation.compliant.bullet.responsibilities")
    }
}
