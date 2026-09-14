package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateFurnishedStatusController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLettingAgentView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateFurnishedStatusJourneyPages.FurnishedStatusFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import java.util.UUID

class LettingAgentUpdateFurnishedStatusJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val token = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222b")
    private val urlArguments = mapOf("token" to token.toString())

    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)
    }

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

    @Test
    fun `a not found page is returned when the delegate to letting agent flag is disabled`(page: Page) {
        featureFlagManager.disable(DELEGATE_TO_LETTING_AGENT)

        navigator.navigate(
            LettingAgentUpdateFurnishedStatusController.getUpdateFurnishedStatusRoute(token) +
                "/${FurnishedStatusStep.ROUTE_SEGMENT}",
        )

        val errorPage = createValidPage(page, ErrorPage::class)
        assertThat(errorPage.heading).containsText("Page not found")
    }
}
