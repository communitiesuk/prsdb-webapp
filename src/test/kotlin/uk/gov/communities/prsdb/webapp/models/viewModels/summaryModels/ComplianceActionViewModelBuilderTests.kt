package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import kotlin.test.assertEquals

class ComplianceActionViewModelBuilderTests {
    @Test
    fun `fromDataModel returns a SummaryCardViewModel with the correct title and summary list for an occupied property`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Test Street",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.ADDED,
                eicrStatus = ComplianceCertStatus.NOT_ADDED,
                epcStatus = ComplianceCertStatus.EXPIRED,
                isComplete = true,
                isOccupied = true,
            )

        val viewModel = ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(dataModel)

        assertEquals(dataModel.singleLineAddress, viewModel.title)

        val expectedSummaryList =
            listOf(
                SummaryListRowViewModel(
                    "complianceActions.summaryRow.may26redesign.registrationNumber",
                    dataModel.registrationNumber,
                ),
                SummaryListRowViewModel(
                    fieldHeading = "complianceActions.summaryRow.may26redesign.status",
                    fieldValue = "complianceActions.summaryRow.may26redesign.occupied",
                    tagColour = "pink",
                ),
                SummaryListRowViewModel(
                    "complianceActions.summaryRow.may26redesign.electricalSafety",
                    MessageKeyConverter.convert(dataModel.eicrStatus),
                ),
                SummaryListRowViewModel(
                    "complianceActions.summaryRow.may26redesign.energyPerformance",
                    MessageKeyConverter.convert(dataModel.epcStatus),
                ),
            )
        assertEquals(expectedSummaryList, viewModel.summaryList)
    }

    @Test
    fun `fromDataModel includes status row with pink tag when includeStatusRow is true and property is occupied`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Test Street",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.ADDED,
                eicrStatus = ComplianceCertStatus.NOT_ADDED,
                epcStatus = ComplianceCertStatus.EXPIRED,
                isComplete = true,
                isOccupied = true,
            )

        val viewModel = ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(dataModel)

        val statusRow = viewModel.summaryList[1]
        assertEquals("complianceActions.summaryRow.may26redesign.status", statusRow.fieldHeading)
        assertEquals("complianceActions.summaryRow.may26redesign.occupied", statusRow.fieldValue)
        assertEquals("pink", statusRow.tagColour)
    }

    @Test
    fun `fromDataModel includes status row with grey tag when includeStatusRow is true and property is unoccupied`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Test Street",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.EXPIRED,
                eicrStatus = ComplianceCertStatus.NOT_ADDED,
                epcStatus = ComplianceCertStatus.ADDED,
                isComplete = true,
                isOccupied = false,
            )

        val viewModel = ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(dataModel)

        val statusRow = viewModel.summaryList[1]
        assertEquals("complianceActions.summaryRow.may26redesign.status", statusRow.fieldHeading)
        assertEquals("complianceActions.summaryRow.may26redesign.unoccupied", statusRow.fieldValue)
        assertEquals("grey", statusRow.tagColour)
    }

    @Test
    fun `fromDataModel only shows expired cert rows for vacant properties`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Test Street",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.EXPIRED,
                eicrStatus = ComplianceCertStatus.NOT_ADDED,
                epcStatus = ComplianceCertStatus.ADDED,
                isComplete = true,
                isOccupied = false,
            )

        val viewModel = ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(dataModel)

        assertEquals(3, viewModel.summaryList.size)
        assertEquals("complianceActions.summaryRow.may26redesign.registrationNumber", viewModel.summaryList[0].fieldHeading)
        assertEquals("complianceActions.summaryRow.may26redesign.status", viewModel.summaryList[1].fieldHeading)
        assertEquals("complianceActions.summaryRow.may26redesign.gasSafety", viewModel.summaryList[2].fieldHeading)
    }

    @Test
    fun `fromDataModel returns a SummaryCardViewModel with goToProperty action`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Test Street",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.EXPIRED,
                eicrStatus = ComplianceCertStatus.ADDED,
                epcStatus = ComplianceCertStatus.ADDED,
                isComplete = true,
                isOccupied = true,
            )

        val viewModel = ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(dataModel)

        assertEquals(1, viewModel.actions?.size)
        assertEquals("complianceActions.action.goToProperty", viewModel.actions?.first()?.text)
        assertEquals(
            PropertyDetailsController.getPropertyCompliancePath(dataModel.propertyOwnershipId),
            viewModel.actions?.first()?.url,
        )
    }
}
