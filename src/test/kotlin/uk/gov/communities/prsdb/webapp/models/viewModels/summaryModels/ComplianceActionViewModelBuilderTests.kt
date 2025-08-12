package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import kotlin.test.assertEquals

class ComplianceActionViewModelBuilderTests {
    @Test
    fun `fromDataModel returns a SummaryCardViewModel with the correct title and summary list`() {
        // Arrange
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Test Street",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.ADDED,
                eicrStatus = ComplianceCertStatus.NOT_ADDED,
                epcStatus = ComplianceCertStatus.EXPIRED,
                isComplete = true,
            )
        val anyCurrentUrlKey = 1

        val expectedSummaryList =
            listOf(
                SummaryListRowViewModel(
                    "complianceActions.summaryRow.registrationNumber",
                    dataModel.registrationNumber,
                ),
                SummaryListRowViewModel(
                    "complianceActions.summaryRow.gasSafety",
                    MessageKeyConverter.convert(dataModel.gasSafetyStatus),
                ),
                SummaryListRowViewModel(
                    "complianceActions.summaryRow.electricalSafety",
                    MessageKeyConverter.convert(dataModel.eicrStatus),
                ),
                SummaryListRowViewModel(
                    "complianceActions.summaryRow.energyPerformance",
                    MessageKeyConverter.convert(dataModel.epcStatus),
                ),
            )

        // Act
        val viewModel = ComplianceActionViewModelBuilder.fromDataModel(dataModel, anyCurrentUrlKey)

        // Assert
        assertEquals(dataModel.singleLineAddress, viewModel.title)
        assertEquals(expectedSummaryList, viewModel.summaryList)
    }

    @ParameterizedTest(name = "{1} action when {0}")
    @MethodSource("provideDataModelsAndActions")
    fun `fromDataModel returns a SummaryCardViewModel with`(
        dataModel: ComplianceStatusDataModel,
        expectedActionText: String,
    ) {
        // Arrange
        val anyCurrentUrlKey = 1

        // Act
        val viewModel = ComplianceActionViewModelBuilder.fromDataModel(dataModel, anyCurrentUrlKey)
        val returnedActionText = viewModel.actions?.first()?.text

        // Assert
        assertEquals(expectedActionText, returnedActionText)
    }

    companion object {
        @JvmStatic
        fun provideDataModelsAndActions() =
            listOf(
                arguments(
                    named(
                        "the compliance form is complete",
                        ComplianceStatusDataModel(
                            propertyOwnershipId = 1L,
                            singleLineAddress = "123 Test Street",
                            registrationNumber = "P-XXXX-XXXX",
                            gasSafetyStatus = ComplianceCertStatus.ADDED,
                            eicrStatus = ComplianceCertStatus.NOT_ADDED,
                            epcStatus = ComplianceCertStatus.EXPIRED,
                            isComplete = true,
                        ),
                    ),
                    named("an update", "complianceActions.action.update"),
                ),
                arguments(
                    named(
                        "the compliance form is in progress",
                        ComplianceStatusDataModel(
                            propertyOwnershipId = 1L,
                            singleLineAddress = "123 Test Street",
                            registrationNumber = "P-XXXX-XXXX",
                            gasSafetyStatus = ComplianceCertStatus.ADDED,
                            eicrStatus = ComplianceCertStatus.NOT_ADDED,
                            epcStatus = ComplianceCertStatus.NOT_STARTED,
                            isComplete = false,
                        ),
                    ),
                    named("a continue", "complianceActions.action.continue"),
                ),
                arguments(
                    named(
                        "the compliance form has not been started",
                        ComplianceStatusDataModel(
                            propertyOwnershipId = 1L,
                            singleLineAddress = "123 Test Street",
                            registrationNumber = "P-XXXX-XXXX",
                            gasSafetyStatus = ComplianceCertStatus.NOT_STARTED,
                            eicrStatus = ComplianceCertStatus.NOT_STARTED,
                            epcStatus = ComplianceCertStatus.NOT_STARTED,
                            isComplete = false,
                        ),
                    ),
                    named("a start", "complianceActions.action.start"),
                ),
            )
    }
}
