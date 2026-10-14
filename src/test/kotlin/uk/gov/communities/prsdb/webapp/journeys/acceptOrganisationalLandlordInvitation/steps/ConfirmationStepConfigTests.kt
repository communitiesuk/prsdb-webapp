package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.CurrentUserService
import uk.gov.communities.prsdb.webapp.services.OrganisationPermissionsProvider
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class ConfirmationStepConfigTests {
    @Mock
    private lateinit var userToLandlordService: UserToLandlordService

    @Mock
    private lateinit var currentUserService: CurrentUserService

    @Mock
    private lateinit var organisationPermissionsProvider: OrganisationPermissionsProvider

    @Mock
    private lateinit var state: AcceptOrganisationalLandlordUserInvitationJourneyState

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `getStepSpecificContent returns joined organisation details and role permissions`(isAdmin: Boolean) {
        whenever(userToLandlordService.getCurrentOrganisationLandlordForUser())
            .thenReturn(MockLandlordData.createOrgLandlord(name = "Test Organisation"))
        whenever(currentUserService.getCurrentEmail()).thenReturn("invitee@example.com")
        whenever(organisationPermissionsProvider.canCurrentUserPerformOrgAdminActions()).thenReturn(isAdmin)

        val content = createStepConfig().getStepSpecificContent(state)

        assertEquals("Test Organisation", content["organisationName"])
        assertEquals("invitee@example.com", content["emailAddress"])
        assertEquals(isAdmin, content["isAdmin"])
        assertEquals(LANDLORD_DASHBOARD_URL, content["dashboardUrl"])
    }

    @Test
    fun `chooseTemplate returns the invitation acceptance confirmation template`() {
        assertEquals("acceptOrganisationInvitationConfirmation", createStepConfig().chooseTemplate(state))
    }

    @Test
    fun `mode returns complete without submitted confirmation form data`() {
        assertEquals(Complete.COMPLETE, createStepConfig().mode(state))
    }

    private fun createStepConfig() =
        ConfirmationStepConfig(userToLandlordService, currentUserService, organisationPermissionsProvider)
}
