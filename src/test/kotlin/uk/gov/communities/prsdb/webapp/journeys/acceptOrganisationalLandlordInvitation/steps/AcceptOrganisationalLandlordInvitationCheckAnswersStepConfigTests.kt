package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.JourneyIdProvider
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AcceptOrganisationInvitationEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class AcceptOrganisationalLandlordInvitationCheckAnswersStepConfigTests {
    @Mock
    lateinit var mockInvitationService: OrganisationalLandlordInvitationService

    @Mock
    lateinit var mockState: AcceptOrganisationalLandlordUserInvitationJourneyState

    @Test
    fun `beforeAttemptingToReachStep returns true when a pending invitation is found`() {
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getPendingInvitationForJourneyIdOrNull(JOURNEY_ID))
            .thenReturn(mock<OrganisationalLandlordInvitation>())

        assertTrue(
            AcceptOrganisationalLandlordInvitationCheckAnswersStepConfig(mockInvitationService)
                .beforeAttemptingToReachStep(mockState),
        )
    }

    @Test
    fun `beforeAttemptingToReachStep returns false when no pending invitation is found`() {
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getPendingInvitationForJourneyIdOrNull(JOURNEY_ID)).thenReturn(null)

        assertFalse(
            AcceptOrganisationalLandlordInvitationCheckAnswersStepConfig(mockInvitationService)
                .beforeAttemptingToReachStep(mockState),
        )
    }

    @Test
    fun `getStepSpecificContent returns summary rows with invitation details`() {
        stubPendingInvitationAndSteps()

        val content = createStepConfig().getStepSpecificContent(mockState)
        val rows = content["summaryListData"] as List<SummaryListRowViewModel>

        assertEquals(3, rows.size)

        assertEquals("acceptOrganisationInvitation.checkAnswers.rowHeading.organisation", rows[0].fieldHeading)
        assertEquals(ORGANISATION_NAME, rows[0].fieldValue)
        assertTrue(rows[0].actions.isEmpty())

        assertEquals("acceptOrganisationInvitation.checkAnswers.rowHeading.name", rows[1].fieldHeading)
        assertEquals(INVITEE_NAME, rows[1].fieldValue)
        assertEquals(1, rows[1].actions.size)
        assertEquals("forms.links.change", rows[1].actions.single().text)
        assertEquals(
            "$FULL_NAME_STEP_ROUTE?${JourneyIdProvider.PARAMETER_NAME}=$FULL_NAME_CYA_JOURNEY_ID",
            rows[1].actions.single().url,
        )

        assertEquals("acceptOrganisationInvitation.checkAnswers.rowHeading.emailAddress", rows[2].fieldHeading)
        assertEquals(INVITEE_EMAIL, rows[2].fieldValue)
        assertEquals(1, rows[2].actions.size)
        assertEquals("forms.links.change", rows[2].actions.single().text)
        assertEquals(
            "$EMAIL_ADDRESS_STEP_ROUTE?${JourneyIdProvider.PARAMETER_NAME}=$EMAIL_ADDRESS_CYA_JOURNEY_ID",
            rows[2].actions.single().url,
        )
    }

    @Test
    fun `getStepSpecificContent returns correct summary name and submit button text`() {
        stubPendingInvitationAndSteps()

        val content = createStepConfig().getStepSpecificContent(mockState)

        assertEquals("acceptOrganisationInvitation.checkAnswers.summaryName", content["summaryName"])
        assertEquals("forms.buttons.confirmAndContinue", content["submitButtonText"])
    }

    @Test
    fun `getStepSpecificContent throws when the pending invitation disappears`() {
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getPendingInvitationForJourneyIdOrNull(JOURNEY_ID)).thenReturn(null)

        assertThrows<PrsdbWebException> {
            createStepConfig().getStepSpecificContent(mockState)
        }
    }

    companion object {
        private const val JOURNEY_ID = "test-journey-id"
        private const val ORGANISATION_NAME = "Test Organisation"
        private const val INVITEE_NAME = "Jane Smith"
        private const val INVITEE_EMAIL = "invitee@example.com"
        private const val FULL_NAME_STEP_ROUTE = "full-name"
        private const val EMAIL_ADDRESS_STEP_ROUTE = "email-address"
        private const val FULL_NAME_CYA_JOURNEY_ID = "full-name-cya-journey"
        private const val EMAIL_ADDRESS_CYA_JOURNEY_ID = "email-address-cya-journey"
    }

    private fun createStepConfig() = AcceptOrganisationalLandlordInvitationCheckAnswersStepConfig(mockInvitationService)

    private fun stubPendingInvitationAndSteps() {
        val invitation = MockLandlordData.createOrganisationalLandlordInvitation(
            organisationalLandlord = MockLandlordData.createOrgLandlord(name = ORGANISATION_NAME),
        )
        val nameFormModel = NameFormModel().apply { name = INVITEE_NAME }
        val emailFormModel = AcceptOrganisationInvitationEmailFormModel().apply { emailAddress = INVITEE_EMAIL }
        val fullNameStep = mock<FullNameStep>()
        val emailAddressStep = mock<EmailAddressStep>()

        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockState.fullNameStep).thenReturn(fullNameStep)
        whenever(mockState.emailAddressStep).thenReturn(emailAddressStep)
        whenever(mockState.getCyaJourneyId(fullNameStep)).thenReturn(FULL_NAME_CYA_JOURNEY_ID)
        whenever(mockState.getCyaJourneyId(emailAddressStep)).thenReturn(EMAIL_ADDRESS_CYA_JOURNEY_ID)
        whenever(fullNameStep.urlPath).thenReturn(FULL_NAME_STEP_ROUTE)
        whenever(emailAddressStep.urlPath).thenReturn(EMAIL_ADDRESS_STEP_ROUTE)
        whenever(fullNameStep.isStepReachable).thenReturn(true)
        whenever(emailAddressStep.isStepReachable).thenReturn(true)
        whenever(fullNameStep.formModel).thenReturn(nameFormModel)
        whenever(emailAddressStep.formModel).thenReturn(emailFormModel)
        whenever(mockInvitationService.getPendingInvitationForJourneyIdOrNull(JOURNEY_ID)).thenReturn(invitation)
    }
}
