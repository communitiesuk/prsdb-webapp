package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.TaskListPagePropertyCompliance

@Sql("/data-local.sql")
class PropertyComplianceJourneyTests : IntegrationTest() {
    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @Test
    fun `User can navigate whole journey if pages are filled in correctly`(page: Page) {
        // Start page
        val startPage = navigator.goToPropertyComplianceStartPage(propertyOwnershipId)
        assertThat(startPage.heading).containsText("Compliance certificates")
        startPage.startButton.clickAndWait()
        assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

        // TODO PRSD-942: Continue journey tests
    }
}
