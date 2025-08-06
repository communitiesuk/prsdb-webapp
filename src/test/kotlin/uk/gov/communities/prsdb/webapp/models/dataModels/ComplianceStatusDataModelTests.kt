package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import kotlin.test.assertEquals

class ComplianceStatusDataModelTests {
    @ParameterizedTest(name = "{1} when {0}")
    @MethodSource("provideComplianceStatusDataModelsAndStates")
    fun `isInProgress returns`(
        complianceStatusDataModel: ComplianceStatusDataModel,
        expectedIsInProgress: Boolean,
    ) {
        val returnedIsComplianceInProgress = complianceStatusDataModel.isInProgress
        assertEquals(expectedIsInProgress, returnedIsComplianceInProgress)
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
    }
}
