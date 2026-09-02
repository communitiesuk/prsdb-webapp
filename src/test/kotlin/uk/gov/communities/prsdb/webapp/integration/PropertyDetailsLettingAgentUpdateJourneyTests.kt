package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateHouseholdsAndTenantsController
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateTenancyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLettingAgentView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CheckHouseholdsAnswersPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HouseholdsNumberOfPeopleFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfHouseholdsFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import java.util.UUID
import kotlin.test.assertContains

class PropertyDetailsLettingAgentUpdateJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    // PO 40 (token ...2222b) has all details provided, so the households row shows an editable change link.
    private val allDetailsProvidedToken = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222b")
    private val allDetailsProvidedUrlArguments = mapOf("token" to allDetailsProvidedToken.toString())

    // PO 43 (token ...2222c) has tenancy marked "provide later", so the tenancy row links to the full tenancy journey.
    private val tenancyProvideLaterToken = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222c")

    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)
    }

    @Test
    fun `the households change link runs the households and tenants journey and returns to the property record`(page: Page) {
        var detailsPage = navigator.goToPropertyDetailsLettingAgentView(allDetailsProvidedToken)
        detailsPage.summaryList.numberOfHouseholdsRow.clickFirstActionLinkAndWait()

        val updateNumberOfHouseholdsPage =
            assertPageIs(page, NumberOfHouseholdsFormPageLettingAgentUpdate::class, allDetailsProvidedUrlArguments)
        val newNumberOfHouseholds = 1
        updateNumberOfHouseholdsPage.submitNumberOfHouseholds(newNumberOfHouseholds)

        val updateNumberOfPeoplePage =
            assertPageIs(page, HouseholdsNumberOfPeopleFormPageLettingAgentUpdate::class, allDetailsProvidedUrlArguments)
        val newNumberOfPeople = 3
        updateNumberOfPeoplePage.submitNumOfPeople(newNumberOfPeople)

        val checkAnswersPage =
            assertPageIs(page, CheckHouseholdsAnswersPageLettingAgentUpdate::class, allDetailsProvidedUrlArguments)
        assertThat(checkAnswersPage.summaryList.numberOfHouseholdsRow).containsText(newNumberOfHouseholds.toString())
        assertThat(checkAnswersPage.summaryList.numberOfPeopleRow).containsText(newNumberOfPeople.toString())
        checkAnswersPage.confirm()

        detailsPage = assertPageIs(page, PropertyDetailsPageLettingAgentView::class, allDetailsProvidedUrlArguments)
        assertThat(detailsPage.summaryList.numberOfHouseholdsRow.value).containsText(newNumberOfHouseholds.toString())
        assertThat(detailsPage.summaryList.numberOfTenantsRow.value).containsText(newNumberOfPeople.toString())
    }

    @Test
    fun `the tenancy provide-later change link starts the full tenancy details journey`(page: Page) {
        val detailsPage = navigator.goToPropertyDetailsLettingAgentView(tenancyProvideLaterToken)
        detailsPage.summaryList.tenancyRow.clickFirstActionLinkAndWait()

        assertContains(page.url(), LettingAgentUpdateTenancyDetailsController.getBaseRoute(tenancyProvideLaterToken))
    }

    @Test
    fun `a not found page is returned for the households update route when the flag is disabled`(page: Page) {
        featureFlagManager.disable(DELEGATE_TO_LETTING_AGENT)

        navigator.navigate(
            LettingAgentUpdateHouseholdsAndTenantsController.getRoute(allDetailsProvidedToken, HouseholdStep.ROUTE_SEGMENT),
        )

        val errorPage = createValidPage(page, ErrorPage::class)
        assertThat(errorPage.heading).containsText("Page not found")
    }
}
