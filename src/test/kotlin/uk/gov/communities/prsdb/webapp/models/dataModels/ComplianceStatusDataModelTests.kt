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
    @ParameterizedTest(name = "{1} when {0}")
    @MethodSource("provideComplianceStatusDataModelsInProgressStates")
    fun `isInProgress returns`(
        complianceStatusDataModel: ComplianceStatusDataModel,
        expectedIsInProgress: Boolean,
    ) {
        val returnedIsComplianceInProgress = complianceStatusDataModel.isInProgress
        assertEquals(expectedIsInProgress, returnedIsComplianceInProgress)
    }

    @Test
    fun `isNonCompliant returns true if any cert's status isn't ADDED`() {
        // Arrange
        val complianceStatusDataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Example St",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.EXPIRED,
                eicrStatus = ComplianceCertStatus.ADDED,
                epcStatus = ComplianceCertStatus.ADDED,
                isComplete = false,
            )

        // Act & Assert
        assertTrue(complianceStatusDataModel.isNonCompliant)
    }

    @Test
    fun `isNonCompliant returns false if all cert statuses are ADDED`() {
        // Arrange
        val complianceStatusDataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Example St",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.ADDED,
                eicrStatus = ComplianceCertStatus.ADDED,
                epcStatus = ComplianceCertStatus.ADDED,
                isComplete = false,
            )

        // Act & Assert
        assertFalse(complianceStatusDataModel.isNonCompliant)
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
        private fun provideComplianceStatusDataModelsInProgressStates() =
            listOf(
                arguments(
                    named(
                        "isComplete is false and any cert's task has been completed",
                        ComplianceStatusDataModel(
                            propertyOwnershipId = 1L,
                            singleLineAddress = "123 Example St",
                            registrationNumber = "REG123",
                            gasSafetyStatus = ComplianceCertStatus.ADDED,
                            eicrStatus = ComplianceCertStatus.NOT_STARTED,
                            epcStatus = ComplianceCertStatus.NOT_STARTED,
                            isComplete = false,
                        ),
                    ),
                    true,
                ),
                arguments(
                    named(
                        "isComplete is true",
                        ComplianceStatusDataModel(
                            propertyOwnershipId = 1L,
                            singleLineAddress = "123 Example St",
                            registrationNumber = "REG123",
                            gasSafetyStatus = ComplianceCertStatus.NOT_ADDED,
                            eicrStatus = ComplianceCertStatus.EXPIRED,
                            epcStatus = ComplianceCertStatus.NOT_ADDED,
                            isComplete = true,
                        ),
                    ),
                    false,
                ),
                arguments(
                    named(
                        "all cert tasks have not been started",
                        ComplianceStatusDataModel(
                            propertyOwnershipId = 1L,
                            singleLineAddress = "123 Example St",
                            registrationNumber = "REG123",
                            gasSafetyStatus = ComplianceCertStatus.NOT_STARTED,
                            eicrStatus = ComplianceCertStatus.NOT_STARTED,
                            epcStatus = ComplianceCertStatus.NOT_STARTED,
                            isComplete = false,
                        ),
                    ),
                    false,
                ),
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
