package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersState
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class OrgGovBodyMemberListStepConfigTests {
    @Mock
    lateinit var mockState: OrgGovBodyMembersState

    @Mock
    lateinit var urlParameterService: CollectionKeyParameterService

    @Mock
    lateinit var orgGovBodyWhoToProvideStep: OrgGovBodyWhoToProvideStep

    @Mock
    lateinit var setStateForGovBodyMemberEditStep: SetStateForGovBodyMemberEditStep

    @Mock
    lateinit var removeGovBodyMemberStep: RemoveGovBodyMemberStep

    @Test
    fun `afterStepIsReached resets editingGovBodyMemberId to null`() {
        val stepConfig = OrgGovBodyMemberListStepConfig(urlParameterService)
        stepConfig.urlPath = OrgGovBodyMemberListStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()

        stepConfig.afterStepIsReached(mockState)

        verify(mockState).editingGovBodyMemberId = null
    }

    @Test
    fun `member row keeps Change and hides Remove when list is at minimum size`() {
        val stepConfig = setupStepConfig(stubRemoveStep = false)
        whenever(mockState.governingBodyMembersMap).thenReturn(mapOf(1 to createMember("Alex Example")))
        whenever(mockState.allowRemovingLastMember).thenReturn(false)

        val content = stepConfig.getStepSpecificContent(mockState)
        val rows = content["summaryListData"] as List<SummaryListRowViewModel>

        assertEquals(listOf("forms.links.change"), rows.first().actions.map { it.text })
    }

    @Test
    fun `member row includes Remove when list is above minimum size`() {
        val stepConfig = setupStepConfig(stubRemoveStep = true)
        whenever(mockState.governingBodyMembersMap).thenReturn(
            mapOf(
                1 to createMember("Alex Example"),
                2 to createMember("Jamie Example"),
            ),
        )

        val content = stepConfig.getStepSpecificContent(mockState)
        val rows = content["summaryListData"] as List<SummaryListRowViewModel>

        assertEquals(listOf("forms.links.change", "forms.links.remove"), rows.first().actions.map { it.text })
    }

    private fun setupStepConfig(stubRemoveStep: Boolean = false): OrgGovBodyMemberListStepConfig {
        val stepConfig = OrgGovBodyMemberListStepConfig(urlParameterService)
        stepConfig.urlPath = OrgGovBodyMemberListStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()

        whenever(mockState.orgGovBodyWhoToProvideStep).thenReturn(orgGovBodyWhoToProvideStep)
        whenever(orgGovBodyWhoToProvideStep.urlPath).thenReturn(OrgGovBodyWhoToProvideStep.ROUTE_SEGMENT)
        whenever(orgGovBodyWhoToProvideStep.currentJourneyId).thenReturn("journey-123")
        whenever(orgGovBodyWhoToProvideStep.isStepReachable).thenReturn(true)
        whenever(mockState.setStateForGovBodyMemberEditStep).thenReturn(setStateForGovBodyMemberEditStep)
        whenever(setStateForGovBodyMemberEditStep.urlPath).thenReturn(SetStateForGovBodyMemberEditStep.ROUTE_SEGMENT)
        whenever(setStateForGovBodyMemberEditStep.currentJourneyId).thenReturn("journey-123")
        whenever(setStateForGovBodyMemberEditStep.isStepReachable).thenReturn(true)
        if (stubRemoveStep) {
            whenever(mockState.removeGovBodyMemberStep).thenReturn(removeGovBodyMemberStep)
            whenever(removeGovBodyMemberStep.urlPath).thenReturn(RemoveGovBodyMemberStep.ROUTE_SEGMENT)
            whenever(removeGovBodyMemberStep.currentJourneyId).thenReturn("journey-123")
            whenever(removeGovBodyMemberStep.isStepReachable).thenReturn(true)
        }
        whenever(urlParameterService.createParameterPair(any())).thenAnswer {
            "memberId" to it.getArgument<Int>(0).toString()
        }

        return stepConfig
    }

    private fun createMember(name: String) =
        GoverningBodyMemberDataModel(
            name = name,
            type = GoverningBodyMemberType.DIRECTOR,
            dateOfBirth = LocalDate(1980, 1, 2),
            address = AddressDataModel(singleLineAddress = "1 Test Street, Test Town, TT1 1TT", postcode = "TT1 1TT"),
        )
}
