package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateRentFrequencyAndAmountController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLettingAgentView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateRentFrequencyAndAmountJourneyPages.CheckRentFrequencyAndAmountAnswersPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateRentFrequencyAndAmountJourneyPages.RentAmountFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateRentFrequencyAndAmountJourneyPages.RentFrequencyFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import java.util.UUID

class LettingAgentUpdateRentFrequencyAndAmountJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val token = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222b")
    private val urlArguments = mapOf("token" to token.toString())

    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)
    }

    @Test
    fun `A letting agent can update a property's rent frequency and amount`(page: Page) {
        val newRentFrequency = RentFrequency.WEEKLY
        val newRentFrequencyDisplayName = "Weekly"
        val newRentAmount = "200"

        var propertyDetailsPage = navigator.goToPropertyDetailsLettingAgentView(token)
        assertThat(propertyDetailsPage.summaryList.rentFrequencyRow.value).not().containsText(newRentFrequencyDisplayName)
        assertThat(propertyDetailsPage.summaryList.rentAmountRow.value).not().containsText(newRentAmount)

        propertyDetailsPage.summaryList.rentFrequencyRow.clickFirstActionLinkAndWait()
        val rentFrequencyPage =
            assertPageIs(page, RentFrequencyFormPageLettingAgentUpdate::class, urlArguments)

        assertThat(rentFrequencyPage.header).containsText("Update when you charge rent")
        rentFrequencyPage.selectRentFrequency(newRentFrequency)
        rentFrequencyPage.form.submit()
        val rentAmountPage =
            assertPageIs(page, RentAmountFormPageLettingAgentUpdate::class, urlArguments)

        assertThat(rentAmountPage.header).containsText("Update your weekly rent")
        rentAmountPage.submitRentAmount(newRentAmount)
        val checkYourAnswersPage =
            assertPageIs(page, CheckRentFrequencyAndAmountAnswersPageLettingAgentUpdate::class, urlArguments)

        assertThat(checkYourAnswersPage.summaryList.rentFrequencyRow).containsText(newRentFrequencyDisplayName)
        assertThat(checkYourAnswersPage.summaryList.rentAmountRow).containsText(newRentAmount)
        checkYourAnswersPage.confirm()

        propertyDetailsPage =
            assertPageIs(page, PropertyDetailsPageLettingAgentView::class, urlArguments)
        assertThat(propertyDetailsPage.summaryList.rentFrequencyRow).containsText(newRentFrequencyDisplayName)
        assertThat(propertyDetailsPage.summaryList.rentAmountRow).containsText(newRentAmount)
    }

    @Test
    fun `Changing the rent amount from the CYA page updates the property with the correct value`(page: Page) {
        val newRentAmount = "350"

        val propertyDetailsPage = navigator.goToPropertyDetailsLettingAgentView(token)
        propertyDetailsPage.summaryList.rentFrequencyRow.clickFirstActionLinkAndWait()

        val rentFrequencyPage =
            assertPageIs(page, RentFrequencyFormPageLettingAgentUpdate::class, urlArguments)
        rentFrequencyPage.selectRentFrequency(RentFrequency.MONTHLY)
        rentFrequencyPage.form.submit()

        val initialRentAmountPage =
            assertPageIs(page, RentAmountFormPageLettingAgentUpdate::class, urlArguments)
        initialRentAmountPage.submitRentAmount("100")

        var checkYourAnswersPage =
            assertPageIs(page, CheckRentFrequencyAndAmountAnswersPageLettingAgentUpdate::class, urlArguments)

        checkYourAnswersPage.summaryList.rentAmountRow.clickFirstActionLinkAndWait()
        val rentAmountPage =
            assertPageIs(page, RentAmountFormPageLettingAgentUpdate::class, urlArguments)
        rentAmountPage.submitRentAmount(newRentAmount)
        checkYourAnswersPage =
            assertPageIs(page, CheckRentFrequencyAndAmountAnswersPageLettingAgentUpdate::class, urlArguments)

        assertThat(checkYourAnswersPage.summaryList.rentAmountRow).containsText(newRentAmount)
        checkYourAnswersPage.confirm()

        val updatedPropertyDetailsPage =
            assertPageIs(page, PropertyDetailsPageLettingAgentView::class, urlArguments)
        assertThat(updatedPropertyDetailsPage.summaryList.rentAmountRow).containsText(newRentAmount)
    }

    @Test
    fun `a not found page is returned when the delegate to letting agent flag is disabled`(page: Page) {
        featureFlagManager.disable(DELEGATE_TO_LETTING_AGENT)

        navigator.navigate(
            LettingAgentUpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(token) +
                "/${RentFrequencyStep.ROUTE_SEGMENT}",
        )

        val errorPage = createValidPage(page, ErrorPage::class)
        assertThat(errorPage.heading).containsText("Page not found")
    }
}
