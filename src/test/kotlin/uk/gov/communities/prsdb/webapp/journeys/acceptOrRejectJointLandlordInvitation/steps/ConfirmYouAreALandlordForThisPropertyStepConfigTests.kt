package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ConfirmYouAreALandlordForThisPropertyStepConfigTests {
    @Mock
    lateinit var mockInvitationService: JointLandlordInvitationService

    @Mock
    lateinit var mockInvitationRepository: JointLandlordInvitationRepository

    @Mock
    lateinit var mockLandlordService: LandlordService

    @Mock
    lateinit var mockState: AcceptOrRejectJointLandlordInvitationJourneyState

    private val journeyId = "test-journey-id"
    private val token = "aaaabbbb-cccc-dddd-eeee-ffff00001111"
    private val baseUserId = "test-user"

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `getStepSpecificContent returns property address from invitation`() {
        val stepConfig = setupStepConfig()
        val invitation = setupMockInvitation()
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)
        whenever(mockInvitationRepository.findByToken(UUID.fromString(token))).thenReturn(invitation)
        whenever(mockState.userCompletedLandlordRegistrationThisJourney).thenReturn(false)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(listOf("1 Fake Street", "Faketown", "FK1 2AB"), content["propertyAddress"])
    }

    @Test
    fun `getStepSpecificContent shows success banner when user completed landlord registration this journey`() {
        val stepConfig = setupStepConfig()
        val invitation = setupMockInvitation()
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)
        whenever(mockInvitationRepository.findByToken(UUID.fromString(token))).thenReturn(invitation)
        whenever(mockState.userCompletedLandlordRegistrationThisJourney).thenReturn(true)
        setMockPrincipal(baseUserId)
        val mockLandlord = setupMockLandlord()
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(mockLandlord)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(true, content["showSuccessBanner"])
    }

    @Test
    fun `getStepSpecificContent hides success banner when user was already registered`() {
        val stepConfig = setupStepConfig()
        val invitation = setupMockInvitation()
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)
        whenever(mockInvitationRepository.findByToken(UUID.fromString(token))).thenReturn(invitation)
        whenever(mockState.userCompletedLandlordRegistrationThisJourney).thenReturn(false)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(false, content["showSuccessBanner"])
        assertNull(content["registrationNumber"])
    }

    @Test
    fun `afterStepDataIsAdded deletes invitation and clears session`() {
        val stepConfig = setupStepConfig()
        val invitation = mock<JointLandlordInvitation>()
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)
        whenever(mockInvitationRepository.findByToken(UUID.fromString(token))).thenReturn(invitation)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockInvitationRepository).delete(invitation)
        verify(mockInvitationService).clearJourneyIdInvitationTokenPairsForTokenFromSession(token)
    }

    @Test
    fun `afterStepDataIsAdded throws when token not found in session`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(null)

        assertThrows<PrsdbWebException> { stepConfig.afterStepDataIsAdded(mockState) }
    }

    @Test
    fun `afterStepDataIsAdded throws when invitation not found in database`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)
        whenever(mockInvitationRepository.findByToken(UUID.fromString(token))).thenReturn(null)

        assertThrows<PrsdbWebException> { stepConfig.afterStepDataIsAdded(mockState) }
    }

    private fun setupStepConfig() =
        ConfirmYouAreALandlordForThisPropertyStepConfig(
            mockInvitationService,
            mockInvitationRepository,
            mockLandlordService,
        )

    private fun setupMockInvitation(): JointLandlordInvitation {
        val mockAddress = mock<Address>()
        whenever(mockAddress.toMultiLineAddress()).thenReturn("1 Fake Street\nFaketown\nFK1 2AB")
        val mockOwnership = mock<PropertyOwnership>()
        whenever(mockOwnership.address).thenReturn(mockAddress)
        val invitation = mock<JointLandlordInvitation>()
        whenever(invitation.registeredOwnership).thenReturn(mockOwnership)
        return invitation
    }

    private fun setupMockLandlord(): Landlord {
        val mockRegNumber = mock<RegistrationNumber>()
        whenever(mockRegNumber.number).thenReturn(12833726L)
        whenever(mockRegNumber.type).thenReturn(uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType.LANDLORD)
        val mockLandlord = mock<Landlord>()
        whenever(mockLandlord.registrationNumber).thenReturn(mockRegNumber)
        return mockLandlord
    }

    private fun setMockPrincipal(name: String) {
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn(name)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }
}
