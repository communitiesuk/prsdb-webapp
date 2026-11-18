package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseholdsAndTenantsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentFrequencyAndAmountTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentIncludesBillsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.TenancyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.ComplianceDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class ReactivationCheckAnswersStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockComplianceDetailsHelper: ComplianceDetailsHelper

    @Mock
    lateinit var mockOccupancyDetailsHelper: OccupancyDetailsHelper

    @Mock
    lateinit var mockMessageSource: MessageSource

    @Mock
    lateinit var mockState: PropertyReactivationJourneyState

    @Mock
    lateinit var mockGasSafetyTask: GasSafetyTask

    @Mock
    lateinit var mockTenancyDetailsTask: TenancyDetailsTask

    @Mock
    lateinit var mockHouseholdsAndTenantsTask: HouseholdsAndTenantsTask

    @Mock
    lateinit var mockFurnishedStatusStep: FurnishedStatusStep

    @Mock
    lateinit var mockRentIncludesBillsTask: RentIncludesBillsTask

    @Mock
    lateinit var mockRentFrequencyAndAmountTask: RentFrequencyAndAmountTask

    private val propertyOwnershipId = 123L

    @Test
    fun `chooseTemplate returns the reactivation check answers template`() {
        val stepConfig = setupStepConfig()

        assertEquals("forms/reactivatePropertyCheckAnswersForm", stepConfig.chooseTemplate(mockState))
    }

    @Test
    fun `getStepSpecificContent includes the property's single line address as the property name`() {
        val stepConfig = setupStepConfig()
        mockInactiveProperty()
        whenever(mockState.requiredTaskTypes).thenReturn(emptyList())

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals("1 Inactive Road, IN1 1AA", content["propertyName"])
    }

    @Test
    fun `getStepSpecificContent only includes sections for the required tasks`() {
        val stepConfig = setupStepConfig()
        mockInactiveProperty()
        whenever(mockState.requiredTaskTypes)
            .thenReturn(listOf(ReactivationTaskType.GAS_SAFETY, ReactivationTaskType.TENANCY_DETAILS))
        val gasSupplyRows = listOf(mock<SummaryListRowViewModel>())
        whenever(mockState.gasSafetyTask).thenReturn(mockGasSafetyTask)
        whenever(mockComplianceDetailsHelper.getGasSafetyCyaContent(mockState, mockGasSafetyTask))
            .thenReturn(mapOf("gasSupplyRows" to gasSupplyRows))
        val tenancyRows = mockTenancyRows()

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(true, content["showGasSafety"])
        assertEquals(false, content["showElectricalSafety"])
        assertEquals(false, content["showEpc"])
        assertEquals(true, content["showTenancyDetails"])
        assertEquals(gasSupplyRows, content["gasSupplyRows"])
        assertEquals(tenancyRows, content["tenancyRows"])
        verify(mockComplianceDetailsHelper, never()).getElectricalSafetyCyaContent(any(), any())
        verify(mockComplianceDetailsHelper, never()).getEpcCyaContent(any(), any())
    }

    private fun mockTenancyRows(): List<SummaryListRowViewModel> {
        whenever(mockState.tenancyDetailsTask).thenReturn(mockTenancyDetailsTask)
        whenever(mockTenancyDetailsTask.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        whenever(mockTenancyDetailsTask.furnishedStatus).thenReturn(mockFurnishedStatusStep)
        whenever(mockTenancyDetailsTask.rentIncludesBillsTask).thenReturn(mockRentIncludesBillsTask)
        whenever(mockTenancyDetailsTask.rentFrequencyAndAmountTask).thenReturn(mockRentFrequencyAndAmountTask)
        val householdsAndTenantsRows = listOf(mock<SummaryListRowViewModel>(), mock<SummaryListRowViewModel>())
        val furnishedStatusRow = mock<SummaryListRowViewModel>()
        val rentIncludesBillsRows = listOf(mock<SummaryListRowViewModel>())
        val rentFrequencyAndAmountRows = listOf(mock<SummaryListRowViewModel>(), mock<SummaryListRowViewModel>())
        whenever(mockOccupancyDetailsHelper.getCheckYourHouseHoldsAndTenantsAnswersSummaryList(mockState, mockHouseholdsAndTenantsTask))
            .thenReturn(householdsAndTenantsRows)
        whenever(mockOccupancyDetailsHelper.getFurnishedStatusRow(mockState, mockFurnishedStatusStep)).thenReturn(furnishedStatusRow)
        whenever(
            mockOccupancyDetailsHelper.getCheckYourRentIncludesBillsAnswersSummaryList(
                mockState,
                mockRentIncludesBillsTask,
                mockMessageSource,
            ),
        ).thenReturn(rentIncludesBillsRows)
        whenever(
            mockOccupancyDetailsHelper.getCheckYourRentFrequencyAndAmountAnswersSummaryList(
                mockState,
                mockRentFrequencyAndAmountTask,
                mockMessageSource,
            ),
        ).thenReturn(rentFrequencyAndAmountRows)
        return householdsAndTenantsRows + furnishedStatusRow + rentIncludesBillsRows + rentFrequencyAndAmountRows
    }

    private fun mockInactiveProperty() {
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                isActive = false,
                address = MockLandlordData.createAddress(singleLineAddress = "1 Inactive Road, IN1 1AA"),
            )
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
            .thenReturn(propertyOwnership)
    }

    private fun setupStepConfig(): ReactivationCheckAnswersStepConfig {
        val stepConfig =
            ReactivationCheckAnswersStepConfig(
                mockPropertyOwnershipService,
                mockComplianceDetailsHelper,
                mockOccupancyDetailsHelper,
                mockMessageSource,
            )
        stepConfig.urlPath = ReactivationCheckAnswersStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
