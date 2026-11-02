package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class AcceptOrRejectJointLandlordInvitationJourneyStateTests {
    private lateinit var mockInvitationService: JointLandlordInvitationService
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService
    private lateinit var journey: AcceptOrRejectJointLandlordInvitationJourney

    private val journeyId = "test-journey-id"
    private val token = "aaaabbbb-cccc-dddd-eeee-ffff00001111"
    private val propertyOwnershipId = 42L

    @BeforeEach
    fun setup() {
        mockInvitationService = mock()
        mockPropertyOwnershipService = mock()
        val mockJourneyStateService = mock<JourneyStateService>()
        whenever(mockJourneyStateService.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)

        journey =
            AcceptOrRejectJointLandlordInvitationJourney(
                validateTokenStep = mock(),
                acceptOrRejectStep = mock(),
                checkUserRoleStep = mock(),
                markLandlordRegistrationCompleteStep = mock(),
                confirmYouAreALandlordForThisPropertyStep = mock(),
                sendRejectionEmailsStep = mock(),
                deleteInvitationAndTokenStep = mock(),
                inviteUnavailableStep = mock(),
                landlordRegistrationTask = mock(),
                invitationService = mockInvitationService,
                propertyOwnershipService = mockPropertyOwnershipService,
                journeyStateService = mockJourneyStateService,
            )
    }

    @Test
    fun `getInvitedPropertyOwnershipIdIfCurrentUserIsLandlordOnIt returns the id when the user is a landlord on it`() {
        setupValidTokenForProperty(userIsLandlordOnProperty = true)

        assertEquals(propertyOwnershipId, journey.getInvitedPropertyOwnershipIdIfCurrentUserIsLandlordOnIt())
        assertTrue(journey.isCurrentUserLandlordOnInvitedPropertyOwnership())
    }

    @Test
    fun `getInvitedPropertyOwnershipIdIfCurrentUserIsLandlordOnIt returns null when the user is not a landlord on it`() {
        setupValidTokenForProperty(userIsLandlordOnProperty = false)

        assertNull(journey.getInvitedPropertyOwnershipIdIfCurrentUserIsLandlordOnIt())
        assertFalse(journey.isCurrentUserLandlordOnInvitedPropertyOwnership())
    }

    @Test
    fun `getInvitedPropertyOwnershipIdIfCurrentUserIsLandlordOnIt returns null when the token is invalid`() {
        whenever(mockInvitationService.getTokenIsValid(token)).thenReturn(false)

        assertNull(journey.getInvitedPropertyOwnershipIdIfCurrentUserIsLandlordOnIt())
        assertFalse(journey.isCurrentUserLandlordOnInvitedPropertyOwnership())
        verifyNoInteractions(mockPropertyOwnershipService)
    }

    private fun setupValidTokenForProperty(userIsLandlordOnProperty: Boolean) {
        val invitation =
            MockJointLandlordData.createJointLandlordInvitation(
                propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId),
            )
        whenever(mockInvitationService.getTokenIsValid(token)).thenReturn(true)
        whenever(mockInvitationService.getInvitationFromToken(token)).thenReturn(invitation)
        whenever(mockPropertyOwnershipService.isCurrentUserLandlord(propertyOwnershipId)).thenReturn(userIsLandlordOnProperty)
    }
}
