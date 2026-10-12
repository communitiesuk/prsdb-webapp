package uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.stepConfig

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.CancelJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationCancellationCancellerEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationCancellationInviteeEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationCancellationOtherLandlordEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.CurrentUserService
import uk.gov.communities.prsdb.webapp.services.CurrentUserService.CurrentUserDetails
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.SwapToIndividualNudgeEmailService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class CancelInvitationStepConfigTests {
    @Mock
    lateinit var mockJointLandlordInvitationService: JointLandlordInvitationService

    @Mock
    lateinit var mockSwapToIndividualNudgeEmailService: SwapToIndividualNudgeEmailService

    @Mock
    lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    lateinit var mockCurrentUserService: CurrentUserService

    @Mock
    lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    lateinit var mockInviteeEmailSender: EmailNotificationService<JointLandlordInvitationCancellationInviteeEmail>

    @Mock
    lateinit var mockCancellerEmailSender: EmailNotificationService<JointLandlordInvitationCancellationCancellerEmail>

    @Mock
    lateinit var mockOtherLandlordEmailSender: EmailNotificationService<JointLandlordInvitationCancellationOtherLandlordEmail>

    @Mock
    lateinit var mockState: CancelJointLandlordInvitationJourneyState

    private val invitedEmail = "invitee@example.com"
    private val cancellerEmail = "canceller@example.com"
    private val cancellerName = "Canceller Name"
    private val otherLandlordEmail = "other@example.com"
    private val otherLandlordName = "Other Landlord"
    private val invitationId = 1L
    private val propertyOwnershipId = 2L

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `afterStepIsReached sends cancellation email to invitee`() {
        val stepConfig = setupStepConfig()
        setupMocks()

        stepConfig.afterStepIsReached(mockState)

        verify(mockInviteeEmailSender).sendEmail(
            eq(invitedEmail),
            any<JointLandlordInvitationCancellationInviteeEmail>(),
        )
    }

    @Test
    fun `afterStepIsReached sends confirmation email to canceller using current user details`() {
        val stepConfig = setupStepConfig()
        setupMocks()

        stepConfig.afterStepIsReached(mockState)

        val emailCaptor = argumentCaptor<JointLandlordInvitationCancellationCancellerEmail>()
        verify(mockCancellerEmailSender).sendEmail(eq(cancellerEmail), emailCaptor.capture())
        assertEquals(cancellerName, emailCaptor.firstValue.recipientName)
    }

    @Test
    fun `afterStepIsReached sends notification email to each other landlord`() {
        val stepConfig = setupStepConfig()
        setupMocks()

        stepConfig.afterStepIsReached(mockState)

        val landlordCaptor = argumentCaptor<Landlord>()
        verify(mockOtherLandlordEmailSender, times(1)).sendEmailToLandlord(
            landlordCaptor.capture(),
            any<JointLandlordInvitationCancellationOtherLandlordEmail>(),
        )
        assertEquals(otherLandlordEmail, (landlordCaptor.firstValue as IndividualLandlord).email)
    }

    @Test
    fun `afterStepIsReached does not send other landlord email to canceller`() {
        val stepConfig = setupStepConfig()
        setupMocks()

        stepConfig.afterStepIsReached(mockState)

        val landlordCaptor = argumentCaptor<Landlord>()
        verify(mockOtherLandlordEmailSender, times(1)).sendEmailToLandlord(
            landlordCaptor.capture(),
            any<JointLandlordInvitationCancellationOtherLandlordEmail>(),
        )
        assertNotEquals(cancellerEmail, (landlordCaptor.firstValue as IndividualLandlord).email)
    }

    @Test
    fun `afterStepIsReached does not send other landlord emails when canceller is the only landlord`() {
        val stepConfig = setupStepConfig()
        setupMocks(includeOtherLandlord = false)

        stepConfig.afterStepIsReached(mockState)

        verify(mockOtherLandlordEmailSender, never()).sendEmailToLandlord(
            any(),
            any<JointLandlordInvitationCancellationOtherLandlordEmail>(),
        )
    }

    @Test
    fun `afterStepIsReached cancels the invitation`() {
        val stepConfig = setupStepConfig()
        val invitation = setupMocks()

        stepConfig.afterStepIsReached(mockState)

        verify(mockJointLandlordInvitationService).removeInvitation(invitation)
    }

    @Test
    fun `afterStepIsReached stores cancelled email in session`() {
        val stepConfig = setupStepConfig()
        setupMocks()

        stepConfig.afterStepIsReached(mockState)

        verify(mockJointLandlordInvitationService).addOrUpdateCancelledInvitationEmailInSession(invitedEmail)
    }

    @Test
    fun `resolveNextDestination deletes the journey`() {
        val stepConfig = setupStepConfig()

        stepConfig.resolveNextDestination(mockState, Destination.ExternalUrl("/test"))

        verify(mockState).deleteJourney()
    }

    private fun setupMocks(includeOtherLandlord: Boolean = true): uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation {
        val cancellerLandlord = MockLandlordData.createIndividualLandlord(name = cancellerName, email = cancellerEmail)
        ReflectionTestUtils.setField(cancellerLandlord, "id", 1L)

        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(cancellerLandlord),
                id = propertyOwnershipId,
            )

        if (includeOtherLandlord) {
            val otherLandlord =
                MockLandlordData.createIndividualLandlord(name = otherLandlordName, email = otherLandlordEmail)
            ReflectionTestUtils.setField(otherLandlord, "id", 2L)
            propertyOwnership.addLandlord(otherLandlord)
        }

        val invitation =
            MockJointLandlordData.createJointLandlordInvitation(
                id = invitationId,
                email = invitedEmail,
                propertyOwnership = propertyOwnership,
            )

        whenever(mockState.invitationId).thenReturn(invitationId)
        whenever(mockState.invitedEmail).thenReturn(invitedEmail)
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockJointLandlordInvitationService.getPendingInvitationIfAuthorizedLandlord(invitationId))
            .thenReturn(invitation)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(cancellerLandlord)
        whenever(mockCurrentUserService.getCurrentUserDetails())
            .thenReturn(CurrentUserDetails(name = cancellerName, email = cancellerEmail))
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(propertyOwnershipId)).thenReturn(URI("example.com"))

        return invitation
    }

    private fun setupStepConfig(): CancelInvitationStepConfig =
        CancelInvitationStepConfig(
            mockJointLandlordInvitationService,
            mockSwapToIndividualNudgeEmailService,
            mockUserToLandlordService,
            mockCurrentUserService,
            mockAbsoluteUrlProvider,
            mockInviteeEmailSender,
            mockCancellerEmailSender,
            mockOtherLandlordEmailSender,
        )
}
