package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class CompleteInviteJointLandlordStepConfigTests {
    @Mock
    private lateinit var mockJointLandlordInvitationService: JointLandlordInvitationService

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockLandlordService: LandlordService

    @Mock
    private lateinit var mockState: InviteJointLandlordJourneyState

    private val propertyId = 123L
    private val invitedEmails = listOf("first@example.com", "second@example.com")

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `afterStepIsReached calls sendInvitationEmails with correct parameters`() {
        val baseUserId = "test-user"
        val mockLandlord = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser(baseUserId))
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId, primaryLandlord = mockLandlord)
        val stepConfig =
            CompleteInviteJointLandlordStepConfig(
                mockJointLandlordInvitationService,
                mockPropertyOwnershipService,
                mockLandlordService,
            )
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.invitedJointLandlords).thenReturn(invitedEmails)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        setMockPrincipal(baseUserId)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(mockLandlord)

        stepConfig.afterStepIsReached(mockState)

        verify(mockJointLandlordInvitationService).sendInvitationEmails(
            jointLandlordEmails = eq(invitedEmails),
            propertyOwnership = eq(propertyOwnership),
            invitingLandlord = eq(mockLandlord),
        )
    }

    @Test
    fun `resolveNextDestination deletes the journey and returns the default destination`() {
        val stepConfig =
            CompleteInviteJointLandlordStepConfig(
                mockJointLandlordInvitationService,
                mockPropertyOwnershipService,
                mockLandlordService,
            )
        val defaultDestination = Destination.ExternalUrl("/redirect")

        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        verify(mockState).deleteJourney()
        assertEquals(defaultDestination, result)
    }

    @Test
    fun `mode always returns COMPLETE`() {
        val stepConfig =
            CompleteInviteJointLandlordStepConfig(
                mockJointLandlordInvitationService,
                mockPropertyOwnershipService,
                mockLandlordService,
            )

        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `afterStepIsReached throws PrsdbWebException when logged in user is not found in landlord database`() {
        val baseUserId = "unknown-user"
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val stepConfig =
            CompleteInviteJointLandlordStepConfig(
                mockJointLandlordInvitationService,
                mockPropertyOwnershipService,
                mockLandlordService,
            )
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        setMockPrincipal(baseUserId)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(null)

        assertThrows<PrsdbWebException> {
            stepConfig.afterStepIsReached(mockState)
        }
    }

    private fun setMockPrincipal(name: String = "test-user") {
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn(name)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }
}
