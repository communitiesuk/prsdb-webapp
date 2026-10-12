package uk.gov.communities.prsdb.webapp.journeys.inviteOrganisationalLandlordUser.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole.ADMIN
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.journeys.inviteOrganisationalLandlordUser.InviteOrganisationalLandlordUserJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.InviteOrganisationalLandlordUserFormModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class InviteOrganisationalLandlordUserStepConfigTests {
    @Mock
    lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    lateinit var mockInvitationService: OrganisationalLandlordInvitationService

    @Mock
    lateinit var mockOrganisationalLandlordUserService: OrganisationalLandlordUserService

    @Mock
    lateinit var mockState: InviteOrganisationalLandlordUserJourneyState

    private fun createStepConfig() =
        InviteOrganisationalLandlordUserStepConfig(
            mockUserToLandlordService,
            mockInvitationService,
            mockOrganisationalLandlordUserService,
        )

    @Test
    fun `getStepSpecificContent returns the current user's organisation name`() {
        val organisation = createOrgLandlord(name = "Test Organisation")
        whenever(mockUserToLandlordService.getCurrentOrganisationLandlordForUser()).thenReturn(organisation)

        val content = createStepConfig().getStepSpecificContent(mockState)

        assertEquals("Test Organisation", content["organisationName"])
    }

    @Test
    fun `enrichSubmittedDataBeforeValidation adds the organisation's invited and member email addresses`() {
        // Arrange
        val organisation = createOrgLandlord()
        whenever(mockUserToLandlordService.getCurrentOrganisationLandlordForUser()).thenReturn(organisation)
        whenever(mockInvitationService.getInvitedEmails(organisation)).thenReturn(listOf("invited@example.com"))
        whenever(mockOrganisationalLandlordUserService.getOrganisationalLandlordUsers(organisation)).thenReturn(
            listOf(OrganisationalLandlordUser(organisation, PrsdbUser("user-123"), "Team Member", "member@example.com", ADMIN)),
        )

        // Act
        val enrichedData =
            createStepConfig().enrichSubmittedDataBeforeValidation(mockState, mapOf("emailAddress" to "new@example.com"))

        // Assert
        assertEquals("new@example.com", enrichedData["emailAddress"])
        assertEquals(
            listOf("invited@example.com"),
            enrichedData[InviteOrganisationalLandlordUserFormModel::invitedEmailAddresses.name],
        )
        assertEquals(
            listOf("member@example.com"),
            enrichedData[InviteOrganisationalLandlordUserFormModel::memberEmailAddresses.name],
        )
    }
}
