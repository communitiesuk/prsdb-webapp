package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyState
import kotlin.test.assertEquals

class RemoveOrgUserConfirmationStepConfigTests {
    @Test
    fun `chooses the TODO template and identifies the removal confirmation`() {
        val config = RemoveOrgUserConfirmationStepConfig()

        assertEquals("forms/todo", config.chooseTemplate(mock()))
        assertEquals(
            "Confirm removal from the organisation (TODO PDJB-1764)",
            config.getStepSpecificContent(mock<ChangeOrgTeamMemberAccessLevelJourneyState>())["todoComment"],
        )
    }
}
