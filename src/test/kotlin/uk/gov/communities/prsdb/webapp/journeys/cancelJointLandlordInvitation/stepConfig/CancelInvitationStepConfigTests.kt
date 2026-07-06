package uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.stepConfig

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.CancelJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationCancellationCancellerEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationCancellationInviteeEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationCancellationOtherLandlordEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.SwapToIndividualNudgeEmailService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
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
    lateinit var mockLandlordService: LandlordService

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

    private val baseUserId = "test-user-id"
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
    fun `afterStepIsReached sends confirmation email to canceller`() {
        val stepConfig = setupStepConfig()
        setupMocks()

        stepConfig.afterStepIsReached(mockState)

        verify(mockCancellerEmailSender).sendEmail(
            eq(cancellerEmail),
            any<JointLandlordInvitationCancellationCancellerEmail>(),
        )
    }

    @Test
    fun `afterStepIsReached sends notification email to other landlords`() {
        val stepConfig = setupStepConfig()
        setupMocks()

        stepConfig.afterStepIsReached(mockState)

        verify(mockOtherLandlordEmailSender).sendEmail(
            eq(otherLandlordEmail),
            any<JointLandlordInvitationCancellationOtherLandlordEmail>(),
        )
    }

    @Test
    fun `afterStepIsReached does not send other landlord email to canceller`() {
        val stepConfig = setupStepConfig()
        setupMocks()

        stepConfig.afterStepIsReached(mockState)

        verify(mockOtherLandlordEmailSender, never()).sendEmail(
            eq(cancellerEmail),
            any<JointLandlordInvitationCancellationOtherLandlordEmail>(),
        )
    }

    @Test
    fun `afterStepIsReached does not send other landlord emails when canceller is the only landlord`() {
        val stepConfig = setupStepConfig()
        setupMocks(includeOtherLandlord = false)

        stepConfig.afterStepIsReached(mockState)

        verify(mockOtherLandlordEmailSender, never()).sendEmail(
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
        JourneyTestHelper.setMockUser(baseUserId)

        val cancellerLandlord = MockLandlordData.createLandlord(name = cancellerName, email = cancellerEmail)
        ReflectionTestUtils.setField(cancellerLandlord, "id", 1L)

        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(cancellerLandlord), id = propertyOwnershipId)

        if (includeOtherLandlord) {
            val otherLandlord = MockLandlordData.createLandlord(name = otherLandlordName, email = otherLandlordEmail)
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
        whenever(mockJointLandlordInvitationService.getPendingInvitationIfAuthorizedLandlord(invitationId, baseUserId))
            .thenReturn(invitation)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(cancellerLandlord)
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(propertyOwnershipId)).thenReturn(URI("example.com"))

        return invitation
    }

    private fun setupStepConfig(): CancelInvitationStepConfig =
        CancelInvitationStepConfig(
            mockJointLandlordInvitationService,
            mockSwapToIndividualNudgeEmailService,
            mockLandlordService,
            mockAbsoluteUrlProvider,
            mockInviteeEmailSender,
            mockCancellerEmailSender,
            mockOtherLandlordEmailSender,
        )
}
