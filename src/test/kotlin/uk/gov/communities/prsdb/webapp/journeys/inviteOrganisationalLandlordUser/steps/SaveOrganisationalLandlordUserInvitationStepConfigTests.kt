package uk.gov.communities.prsdb.webapp.journeys.inviteOrganisationalLandlordUser.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.inviteOrganisationalLandlordUser.InviteOrganisationalLandlordUserJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.InviteOrganisationalLandlordUserFormModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class SaveOrganisationalLandlordUserInvitationStepConfigTests {
    @Mock
    lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    lateinit var mockInvitationService: OrganisationalLandlordInvitationService

    @Mock
    lateinit var mockState: InviteOrganisationalLandlordUserJourneyState

    @Mock
    lateinit var mockInviteOrganisationalLandlordUserStep: InviteOrganisationalLandlordUserStep

    private fun createStepConfig() = SaveOrganisationalLandlordUserInvitationStepConfig(mockUserToLandlordService, mockInvitationService)

    @Test
    fun `afterStepIsReached creates an invitation for the current organisation from the submitted answers`() {
        // Arrange
        val organisation = createOrgLandlord()
        val formModel =
            InviteOrganisationalLandlordUserFormModel().apply {
                emailAddress = "invitee@example.com"
                role = OrganisationalLandlordUserRole.ADMIN
            }
        whenever(mockState.inviteOrganisationalLandlordUserStep).thenReturn(mockInviteOrganisationalLandlordUserStep)
        whenever(mockInviteOrganisationalLandlordUserStep.formModel).thenReturn(formModel)
        whenever(mockUserToLandlordService.getCurrentOrganisationLandlordForUser()).thenReturn(organisation)

        // Act
        createStepConfig().afterStepIsReached(mockState)

        // Assert
        verify(mockInvitationService).createInvitation(
            invitedEmail = "invitee@example.com",
            role = OrganisationalLandlordUserRole.ADMIN,
            organisationalLandlord = organisation,
        )
    }

    @Test
    fun `resolveNextDestination deletes the journey and returns the default destination`() {
        val defaultDestination = Destination.ExternalUrl("/test")

        val result = createStepConfig().resolveNextDestination(mockState, defaultDestination)

        verify(mockState).deleteJourney()
        assertEquals(defaultDestination, result)
    }
}
