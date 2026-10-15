package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrganisationalLandlordUserInvitationController.Companion.ACCEPT_INVITATION_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AcceptOrganisationInvitationEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.services.UserRolesService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper.Companion.setMockOidcPrincipal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class CompleteInvitationAcceptanceStepConfigTests {
    @Mock
    lateinit var mockInvitationService: OrganisationalLandlordInvitationService

    @Mock
    lateinit var mockUserRolesService: UserRolesService

    @Mock
    lateinit var mockSecurityContextService: SecurityContextService

    @Mock
    lateinit var mockState: AcceptOrganisationalLandlordUserInvitationJourneyState

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `mode returns COMPLETE`() {
        assertEquals(Complete.COMPLETE, setupStepConfig().mode(mockState))
    }

    @Test
    fun `beforeAttemptingToReachStep returns true when the invitation is pending and the user is not a landlord`() {
        setMockOidcPrincipal(USERNAME)
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getPendingInvitationForJourneyIdOrNull(JOURNEY_ID))
            .thenReturn(mock<OrganisationalLandlordInvitation>())
        whenever(mockUserRolesService.getUserHasLandlordRole(USERNAME)).thenReturn(false)

        assertTrue(setupStepConfig().beforeAttemptingToReachStep(mockState))
    }

    @Test
    fun `beforeAttemptingToReachStep returns false when there is no pending invitation`() {
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getPendingInvitationForJourneyIdOrNull(JOURNEY_ID)).thenReturn(null)

        assertFalse(setupStepConfig().beforeAttemptingToReachStep(mockState))
    }

    @Test
    fun `beforeAttemptingToReachStep returns false when the user is already a landlord`() {
        setMockOidcPrincipal(USERNAME)
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getPendingInvitationForJourneyIdOrNull(JOURNEY_ID))
            .thenReturn(mock<OrganisationalLandlordInvitation>())
        whenever(mockUserRolesService.getUserHasLandlordRole(USERNAME)).thenReturn(true)

        assertFalse(setupStepConfig().beforeAttemptingToReachStep(mockState))
    }

    @Test
    fun `afterStepIsReached accepts the invitation and stores the email before refreshing the security context`() {
        setMockOidcPrincipal(USERNAME)
        val mockFullNameStep = mock<FullNameStep>()
        val mockEmailAddressStep = mock<EmailAddressStep>()
        whenever(mockFullNameStep.formModel).thenReturn(NameFormModel().apply { name = NAME })
        whenever(mockEmailAddressStep.formModel).thenReturn(AcceptOrganisationInvitationEmailFormModel().apply { emailAddress = EMAIL })
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockState.fullNameStep).thenReturn(mockFullNameStep)
        whenever(mockState.emailAddressStep).thenReturn(mockEmailAddressStep)
        whenever(mockInvitationService.acceptInvitation(JOURNEY_ID, USERNAME, NAME, EMAIL)).thenReturn(TOKEN)

        setupStepConfig().afterStepIsReached(mockState)

        verify(mockInvitationService).acceptInvitation(JOURNEY_ID, USERNAME, NAME, EMAIL)
        verify(mockInvitationService).clearJourneyIdInvitationTokenPairsForTokenFromSession(TOKEN)
        verify(mockInvitationService).storeAcceptedInvitationEmailInSession(EMAIL)
        verify(mockSecurityContextService).refreshContext()
    }

    @Test
    fun `resolveNextDestination deletes the journey before returning the confirmation destination`() {
        val destination = Destination.ExternalUrl(ACCEPT_INVITATION_CONFIRMATION_ROUTE)

        val result = setupStepConfig().resolveNextDestination(mockState, destination)

        verify(mockState).deleteJourney()
        assertSame(destination, result)
    }

    private fun setupStepConfig(): CompleteInvitationAcceptanceStepConfig =
        CompleteInvitationAcceptanceStepConfig(mockInvitationService, mockUserRolesService, mockSecurityContextService)

    companion object {
        private const val JOURNEY_ID = "test-journey-id"
        private const val USERNAME = "test-user"
        private const val NAME = "Jane Smith"
        private const val EMAIL = "invitee@example.com"
        private const val TOKEN = "1234abcd-5678-abcd-1234-567abcd2222a"
    }
}
