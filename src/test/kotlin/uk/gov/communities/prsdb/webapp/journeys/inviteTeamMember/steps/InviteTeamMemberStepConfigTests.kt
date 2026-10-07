package uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember.InviteTeamMemberJourneyState
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class InviteTeamMemberStepConfigTests {
    @Mock
    lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    lateinit var mockState: InviteTeamMemberJourneyState

    @Test
    fun `getStepSpecificContent returns the current user's organisation name`() {
        val organisation = createOrgLandlord(name = "Test Organisation")
        whenever(mockUserToLandlordService.getCurrentOrganisationLandlordForUser()).thenReturn(organisation)

        val content = InviteTeamMemberStepConfig(mockUserToLandlordService).getStepSpecificContent(mockState)

        assertEquals("Test Organisation", content["organisationName"])
    }
}
