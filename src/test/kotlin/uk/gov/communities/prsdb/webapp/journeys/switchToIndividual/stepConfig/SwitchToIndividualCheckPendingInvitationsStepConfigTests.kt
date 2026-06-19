package uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PropertyOwnershipJourneyState
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class SwitchToIndividualCheckPendingInvitationsStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockJointLandlordInvitationService: JointLandlordInvitationService

    @Mock
    lateinit var mockState: PropertyOwnershipJourneyState

    @Test
    fun `mode returns null when form model is not present in state`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(SwitchToIndividualCheckPendingInvitationsStep.ROUTE_SEGMENT)).thenReturn(null)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Test
    fun `mode returns COMPLETE when form model is present in state`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(SwitchToIndividualCheckPendingInvitationsStep.ROUTE_SEGMENT)).thenReturn(emptyMap())

        val result = stepConfig.mode(mockState)

        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `chooseTemplate returns checkInvitationsForm`() {
        val stepConfig = setupStepConfig()

        val result = stepConfig.chooseTemplate(mockState)

        assertEquals("forms/checkInvitationsForm", result)
    }

    @Test
    fun `getStepSpecificContent returns correct model attributes`() {
        val stepConfig = setupStepConfig()
        val propertyOwnershipId = 1L
        val address = "123 Test Street, AB1 2CD"
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                address = MockLandlordData.createAddress(singleLineAddress = address),
            )
        val invitation = MockJointLandlordData.createJointLandlordInvitation()
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockJointLandlordInvitationService.getPendingInvitations(propertyOwnership))
            .thenReturn(listOf(invitation))

        val result = stepConfig.getStepSpecificContent(mockState)

        assertEquals("switchToIndividual", result["messagePrefix"])
        assertEquals(address, result["address"])
        assertEquals(1, result["invitationCount"])
        assertEquals(PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId), result["cancelUrl"])
    }

    private fun setupStepConfig(): SwitchToIndividualCheckPendingInvitationsStepConfig {
        val stepConfig =
            SwitchToIndividualCheckPendingInvitationsStepConfig(mockPropertyOwnershipService, mockJointLandlordInvitationService)
        stepConfig.routeSegment = SwitchToIndividualCheckPendingInvitationsStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
