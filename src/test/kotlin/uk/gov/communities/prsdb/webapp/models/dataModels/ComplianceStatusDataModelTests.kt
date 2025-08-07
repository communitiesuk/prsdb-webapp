package uk.gov.communities.prsdb.webapp.models.dataModels

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComplianceStatusDataModelTests {
    private val objectMapper = ObjectMapper()

    @ParameterizedTest(name = "{1} when {0}")
    @MethodSource("provideComplianceStatusDataModelsAndStates")
    fun `isInProgress returns`(
        complianceStatusDataModel: ComplianceStatusDataModel,
        expectedIsInProgress: Boolean,
    ) {
        val returnedIsComplianceInProgress = complianceStatusDataModel.isInProgress
        assertEquals(expectedIsInProgress, returnedIsComplianceInProgress)
    }

    @Test
    fun `fromIncompleteComplianceForm returns a ComplianceStatusDataModel with correct non-status values`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val propertyOwnershipRegNum = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString()

        // Act
        val complianceStatusDataModel = ComplianceStatusDataModel.fromIncompleteComplianceForm(propertyOwnership)

        // Assert
        assertEquals(propertyOwnership.id, complianceStatusDataModel.propertyOwnershipId)
        assertEquals(propertyOwnership.property.address.singleLineAddress, complianceStatusDataModel.singleLineAddress)
        assertEquals(propertyOwnershipRegNum, complianceStatusDataModel.registrationNumber)
        assertFalse(complianceStatusDataModel.isComplete)
    }

    @ParameterizedTest(name = "when {0}")
    @MethodSource("providePropertyOwnershipsAndStatuses")
    fun `fromIncompleteComplianceForm returns a ComplianceStatusDataModel with correct status values`(
        complianceFormData: JourneyData,
        expectedCertStatus: ComplianceCertStatus,
    ) {
        // Arrange
        val complianceForm =
            MockLandlordData.createPropertyComplianceFormContext(
                context = objectMapper.writeValueAsString(complianceFormData),
            )
        val propertyOwnership = MockLandlordData.createPropertyOwnership(incompleteComplianceForm = complianceForm)

        // Act
        val complianceStatusDataModel = ComplianceStatusDataModel.fromIncompleteComplianceForm(propertyOwnership)

        assertEquals(expectedCertStatus, complianceStatusDataModel.gasSafetyStatus)
        assertEquals(expectedCertStatus, complianceStatusDataModel.eicrStatus)
        assertEquals(expectedCertStatus, complianceStatusDataModel.epcStatus)
    }

    @Test
    fun `fromIncompleteComplianceForm throws an error if the property doesn't have an incomplete compliance form`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(incompleteComplianceForm = null)

        assertThrows<IllegalArgumentException> { ComplianceStatusDataModel.fromIncompleteComplianceForm(propertyOwnership) }
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
        assertEquals(propertyCompliance.propertyOwnership.property.address.singleLineAddress, complianceStatusDataModel.singleLineAddress)
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
        private fun provideComplianceStatusDataModelsAndStates() =
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
        private fun providePropertyOwnershipsAndStatuses() =
            listOf(
                arguments(
                    named(
                        "when tasks haven't been started",
                        JourneyDataBuilder().build(),
                    ),
                    ComplianceCertStatus.NOT_STARTED,
                ),
                arguments(
                    named(
                        "when in-date certs have been added",
                        JourneyDataBuilder()
                            .withGasSafetyCertUploadConfirmation()
                            .withEicrUploadConfirmation()
                            .withEpcExemptionConfirmationStep()
                            .build(),
                    ),
                    ComplianceCertStatus.ADDED,
                ),
                arguments(
                    named(
                        "when certs are missing",
                        JourneyDataBuilder()
                            .withMissingGasSafetyExemption()
                            .withMissingEicrExemption()
                            .withEpcNotFoundStep()
                            .build(),
                    ),
                    ComplianceCertStatus.NOT_ADDED,
                ),
                arguments(
                    named(
                        "when certs are expired",
                        JourneyDataBuilder()
                            .withGasSafetyIssueDate(LocalDate.now().minusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong()))
                            .withGasSafetyOutdatedConfirmation()
                            .withEicrIssueDate(LocalDate.now().minusYears(EICR_VALIDITY_YEARS.toLong()))
                            .withEpcExpiredStep()
                            .build(),
                    ),
                    ComplianceCertStatus.EXPIRED,
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
