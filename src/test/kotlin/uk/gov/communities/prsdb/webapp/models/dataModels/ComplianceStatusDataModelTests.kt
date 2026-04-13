package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
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
                epcStatus = ComplianceCertStatus.ADDED,
                isComplete = true,
                isOccupied = isOccupied,
            )

        assertEquals(expectedResult, dataModel.shouldShowCert(status))
    }

    @Test
    fun `shouldShowOnComplianceActionsPage returns true for vacant property with expired cert`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Example St",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.EXPIRED,
                eicrStatus = ComplianceCertStatus.NOT_ADDED,
                epcStatus = ComplianceCertStatus.ADDED,
                isComplete = true,
                isOccupied = false,
            )
        assertTrue(dataModel.shouldShowOnComplianceActionsPage)
    }

    @Test
    fun `shouldShowOnComplianceActionsPage returns false for vacant property with only non-added certs`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Example St",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.NOT_ADDED,
                eicrStatus = ComplianceCertStatus.NOT_ADDED,
                epcStatus = ComplianceCertStatus.ADDED,
                isComplete = true,
                isOccupied = false,
            )
        assertFalse(dataModel.shouldShowOnComplianceActionsPage)
    }

    @Test
    fun `shouldShowOnComplianceActionsPage returns true for occupied property with non-added certs`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Example St",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.NOT_ADDED,
                eicrStatus = ComplianceCertStatus.ADDED,
                epcStatus = ComplianceCertStatus.ADDED,
                isComplete = true,
                isOccupied = true,
            )
        assertTrue(dataModel.shouldShowOnComplianceActionsPage)
    }

    @Test
    fun `shouldShowOnComplianceActionsPage returns false when all certs are ADDED`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Example St",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.ADDED,
                eicrStatus = ComplianceCertStatus.ADDED,
                epcStatus = ComplianceCertStatus.ADDED,
                isComplete = true,
                isOccupied = true,
            )
        assertFalse(dataModel.shouldShowOnComplianceActionsPage)
    }

    @Test
    fun `fromPropertyOwnershipWithoutCompliance returns a ComplianceStatusDataModel with correct values`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val propertyOwnershipRegNum = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString()

        // Act
        val complianceStatusDataModel = ComplianceStatusDataModel.fromPropertyOwnershipWithoutCompliance(propertyOwnership)

        // Assert
        assertEquals(propertyOwnership.id, complianceStatusDataModel.propertyOwnershipId)
        assertEquals(propertyOwnership.address.singleLineAddress, complianceStatusDataModel.singleLineAddress)
        assertEquals(propertyOwnershipRegNum, complianceStatusDataModel.registrationNumber)
        assertFalse(complianceStatusDataModel.isComplete)
        assertEquals(ComplianceCertStatus.NOT_STARTED, complianceStatusDataModel.gasSafetyStatus)
        assertEquals(ComplianceCertStatus.NOT_STARTED, complianceStatusDataModel.eicrStatus)
        assertEquals(ComplianceCertStatus.NOT_STARTED, complianceStatusDataModel.epcStatus)
        assertEquals(propertyOwnership.isOccupied, complianceStatusDataModel.isOccupied)
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
        assertEquals(expectedCertStatus, complianceStatusDataModel.epcStatus)
    }

    companion object {
        @JvmStatic
        private fun provideShouldShowCertCases() =
            listOf(
                // EXPIRED always shows
                arguments(ComplianceCertStatus.EXPIRED, true, true),
                arguments(ComplianceCertStatus.EXPIRED, false, true),
                // NOT_ADDED only shows when occupied
                arguments(ComplianceCertStatus.NOT_ADDED, true, true),
                arguments(ComplianceCertStatus.NOT_ADDED, false, false),
                // NOT_STARTED only shows when occupied
                arguments(ComplianceCertStatus.NOT_STARTED, true, true),
                arguments(ComplianceCertStatus.NOT_STARTED, false, false),
                // ADDED never shows
                arguments(ComplianceCertStatus.ADDED, true, false),
                arguments(ComplianceCertStatus.ADDED, false, false),
            )

        @JvmStatic
        private fun providePropertyCompliancesAndStatuses() =
            listOf(
                arguments(
                    named("when in-date certs or exemptions have been added", PropertyComplianceBuilder.createWithInDateCerts()),
                    ComplianceCertStatus.ADDED,
                ),
                arguments(
                    named("when certs are missing", PropertyComplianceBuilder.createWithMissingCerts()),
                    ComplianceCertStatus.NOT_ADDED,
                ),
                arguments(
                    named("when certs are expired", PropertyComplianceBuilder.createWithExpiredCerts()),
                    ComplianceCertStatus.EXPIRED,
                ),
            )
    }
}
