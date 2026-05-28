package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComplianceStatusDataModelTests {
    @ParameterizedTest(name = "shouldShowCert returns {2} for status {0} when isOccupied is {1}")
    @MethodSource("provideShouldShowCertCases")
    fun `shouldShowCert returns expected value based on occupancy and cert status`(
        status: ComplianceCertStatus,
        isOccupied: Boolean,
        expectedResult: Boolean,
    ) {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Example St",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = status,
                eicrStatus = ComplianceCertStatus.ADDED,
                epcStatusOld = ComplianceCertStatus.ADDED,
                epcStatusMay2026Redesign = ComplianceCertStatus.ADDED,
                isComplete = true,
                isOccupied = isOccupied,
            )

        assertEquals(expectedResult, dataModel.shouldShowCert(status))
    }

    @Test
    fun `shouldShowOnMay2026RedesignComplianceActionsPage returns true for vacant property with expired cert`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Example St",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.EXPIRED,
                eicrStatus = ComplianceCertStatus.HAS_FAULTS,
                epcStatusOld = ComplianceCertStatus.ADDED,
                epcStatusMay2026Redesign = ComplianceCertStatus.ADDED,
                isComplete = true,
                isOccupied = false,
            )
        assertTrue(dataModel.shouldShowOnMay2026RedesignComplianceActionsPage)
    }

    @Test
    fun `shouldShowOnMay2026RedesignComplianceActionsPage returns false for vacant property with only non-added certs`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Example St",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.HAS_FAULTS,
                eicrStatus = ComplianceCertStatus.HAS_FAULTS,
                epcStatusOld = ComplianceCertStatus.HAS_FAULTS,
                epcStatusMay2026Redesign = ComplianceCertStatus.HAS_FAULTS,
                isComplete = true,
                isOccupied = false,
            )
        assertFalse(dataModel.shouldShowOnMay2026RedesignComplianceActionsPage)
    }

    @Test
    fun `shouldShowOnMay2026RedesignComplianceActionsPage returns true for occupied property with non-added certs`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Example St",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.HAS_FAULTS,
                eicrStatus = ComplianceCertStatus.HAS_FAULTS,
                epcStatusOld = ComplianceCertStatus.HAS_FAULTS,
                epcStatusMay2026Redesign = ComplianceCertStatus.HAS_FAULTS,
                isComplete = true,
                isOccupied = true,
            )
        assertTrue(dataModel.shouldShowOnMay2026RedesignComplianceActionsPage)
    }

    @Test
    fun `shouldShowOnMay2026RedesignComplianceActionsPage returns false when all certs are ADDED`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Example St",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.ADDED,
                eicrStatus = ComplianceCertStatus.ADDED,
                epcStatusOld = ComplianceCertStatus.ADDED,
                epcStatusMay2026Redesign = ComplianceCertStatus.ADDED,
                isComplete = true,
                isOccupied = true,
            )
        assertFalse(dataModel.shouldShowOnMay2026RedesignComplianceActionsPage)
    }

    @Test
    fun `fromPropertyCompliance returns NOT_REQUIRED gas safety status when property has no gas supply`() {
        // Arrange
        val propertyCompliance =
            PropertyComplianceBuilder()
                .withOccupiedPropertyOwnership()
                .withHasGasSupply(false)
                .withElectricalCertType()
                .build()

        // Act
        val complianceStatusDataModel = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance)

        // Assert
        assertEquals(ComplianceCertStatus.NOT_REQUIRED, complianceStatusDataModel.gasSafetyStatus)
    }

    @Test
    fun `fromPropertyCompliance returns a ComplianceStatusDataModel with correct non-status values`() {
        // Arrange
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()
        val propertyOwnershipRegNum =
            RegistrationNumberDataModel
                .fromRegistrationNumber(propertyCompliance.propertyOwnership.registrationNumber)
                .toString()

        // Act
        val complianceStatusDataModel = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance)

        // Assert
        assertEquals(propertyCompliance.propertyOwnership.id, complianceStatusDataModel.propertyOwnershipId)
        assertEquals(propertyCompliance.propertyOwnership.address.singleLineAddress, complianceStatusDataModel.singleLineAddress)
        assertEquals(propertyOwnershipRegNum, complianceStatusDataModel.registrationNumber)
        assertTrue(complianceStatusDataModel.isComplete)
        assertEquals(propertyCompliance.propertyOwnership.isOccupied, complianceStatusDataModel.isOccupied)
    }

    @ParameterizedTest(name = "when {0}")
    @MethodSource("providePropertyCompliancesAndStatuses")
    fun `fromPropertyCompliance returns a ComplianceStatusDataModel with correct status values`(
        propertyCompliance: PropertyCompliance,
        expectedCertStatus: ComplianceCertStatus,
    ) {
        // Act
        val complianceStatusDataModel = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance)

        // Assert
        assertEquals(expectedCertStatus, complianceStatusDataModel.gasSafetyStatus)
        assertEquals(expectedCertStatus, complianceStatusDataModel.eicrStatus)
        assertEquals(expectedCertStatus, complianceStatusDataModel.epcStatusMay2026Redesign)
    }

    companion object {
        @JvmStatic
        private fun provideShouldShowCertCases() =
            listOf(
                // EXPIRED always shows
                arguments(ComplianceCertStatus.EXPIRED, true, true),
                arguments(ComplianceCertStatus.EXPIRED, false, true),
                // NOT_ADDED only shows when occupied
                arguments(ComplianceCertStatus.HAS_FAULTS, true, true),
                arguments(ComplianceCertStatus.HAS_FAULTS, false, false),
                // NOT_STARTED only shows when occupied
                arguments(ComplianceCertStatus.NOT_STARTED, true, true),
                arguments(ComplianceCertStatus.NOT_STARTED, false, false),
                // ADDED never shows
                arguments(ComplianceCertStatus.ADDED, true, false),
                arguments(ComplianceCertStatus.ADDED, false, false),
                // NOT_REQUIRED never shows
                arguments(ComplianceCertStatus.NOT_REQUIRED, true, false),
                arguments(ComplianceCertStatus.NOT_REQUIRED, false, false),
            )

        @JvmStatic
        private fun providePropertyCompliancesAndStatuses() =
            listOf(
                arguments(
                    named("when in-date certs have been added", PropertyComplianceBuilder.createWithInDateCerts()),
                    ComplianceCertStatus.ADDED,
                ),
                arguments(
                    named("when exemptions have been added", PropertyComplianceBuilder.createWithCertExemptions()),
                    ComplianceCertStatus.ADDED,
                ),
                arguments(
                    named("when certs are missing", PropertyComplianceBuilder.createWithMissingCerts(true)),
                    ComplianceCertStatus.HAS_FAULTS,
                ),
                arguments(
                    named(
                        "when gas and electric and missing and epc has a low energy rating",
                        PropertyComplianceBuilder.createWithGasElectricMissingAndEpcLowEnergy(true),
                    ),
                    ComplianceCertStatus.HAS_FAULTS,
                ),
                arguments(
                    named("when certs are expired", PropertyComplianceBuilder.createWithExpiredCerts()),
                    ComplianceCertStatus.EXPIRED,
                ),
                arguments(
                    named(
                        "when certs are expired and epc has a low energy rating",
                        PropertyComplianceBuilder.createWithExpiredCertsAndLowEpcRating(),
                    ),
                    ComplianceCertStatus.EXPIRED,
                ),
            )
    }
}
