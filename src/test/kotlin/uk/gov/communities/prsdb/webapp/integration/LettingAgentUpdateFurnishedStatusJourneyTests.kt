package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLettingAgentView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateFurnishedStatusJourneyPages.FurnishedStatusFormPageLettingAgentUpdate
import java.util.UUID

class LettingAgentUpdateFurnishedStatusJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val token = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222b")
    private val urlArguments = mapOf("token" to token.toString())

    @Test
    fun `A letting agent can update a property's furnished status`(page: Page) {
        val newFurnishedStatusValue = "Partly furnished"

        var propertyDetailsPage = navigator.goToPropertyDetailsLettingAgentView(token)
        assertThat(propertyDetailsPage.summaryList.furnishedStatusRow.value).not().containsText(newFurnishedStatusValue)

        propertyDetailsPage.summaryList.furnishedStatusRow.clickFirstActionLinkAndWait()
        val furnishedStatusFormPage =
            assertPageIs(page, FurnishedStatusFormPageLettingAgentUpdate::class, urlArguments)

        assertThat(furnishedStatusFormPage.form.fieldsetHeading).containsText("Update is the property furnished")
        furnishedStatusFormPage.submitFurnishedStatus(FurnishedStatus.PART_FURNISHED)

        propertyDetailsPage =
            assertPageIs(page, PropertyDetailsPageLettingAgentView::class, urlArguments)
        assertThat(propertyDetailsPage.summaryList.furnishedStatusRow.value).containsText(newFurnishedStatusValue)
    }
}
