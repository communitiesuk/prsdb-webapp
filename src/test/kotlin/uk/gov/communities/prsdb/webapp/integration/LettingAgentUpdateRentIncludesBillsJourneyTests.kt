package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateRentIncludesBillsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLettingAgentView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateRentIncludesBillsJourneyPages.BillsIncludedFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateRentIncludesBillsJourneyPages.CheckRentIncludesBillsAnswersPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateRentIncludesBillsJourneyPages.RentIncludesBillsFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import java.util.UUID

class LettingAgentUpdateRentIncludesBillsJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val token = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222b")
    private val urlArguments = mapOf("token" to token.toString())

    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)
    }

    @Test
    fun `A letting agent can update a property's rent-includes-bills status`(page: Page) {
        var propertyDetailsPage = navigator.goToPropertyDetailsLettingAgentView(token)
        assertThat(propertyDetailsPage.summaryList.rentIncludesBillsRow.value).not().containsText("Yes")

        propertyDetailsPage.summaryList.rentIncludesBillsRow.clickFirstActionLinkAndWait()
        val rentIncludesBillsFormPage =
            assertPageIs(page, RentIncludesBillsFormPageLettingAgentUpdate::class, urlArguments)

        assertThat(rentIncludesBillsFormPage.form.fieldsetHeading).containsText("Update whether the rent includes bills")
        rentIncludesBillsFormPage.submitIsIncluded()
        val billsIncludedFormPage =
            assertPageIs(page, BillsIncludedFormPageLettingAgentUpdate::class, urlArguments)

        val expectedBillsIncluded = "Gas, Electricity, Water"
        assertThat(billsIncludedFormPage.form.fieldsetHeading).containsText("Update which of these you include in the rent")
        billsIncludedFormPage.selectGasElectricityWater()
        billsIncludedFormPage.form.submit()
        val checkYourAnswersPage =
            assertPageIs(page, CheckRentIncludesBillsAnswersPageLettingAgentUpdate::class, urlArguments)

        assertThat(checkYourAnswersPage.summaryList.rentIncludesBillsRow).containsText("Yes")
        assertThat(checkYourAnswersPage.summaryList.billsIncludedRow).containsText(expectedBillsIncluded)
        checkYourAnswersPage.confirm()

        propertyDetailsPage =
            assertPageIs(page, PropertyDetailsPageLettingAgentView::class, urlArguments)
        assertThat(propertyDetailsPage.summaryList.rentIncludesBillsRow.value).containsText("Yes")
        assertThat(propertyDetailsPage.summaryList.billsIncludedRow).containsText(expectedBillsIncluded)
    }

    @Test
    fun `Changing the bills included answer from the CYA page updates the property with the correct values`(page: Page) {
        val propertyDetailsPage = navigator.goToPropertyDetailsLettingAgentView(token)
        propertyDetailsPage.summaryList.rentIncludesBillsRow.clickFirstActionLinkAndWait()

        val rentIncludesBillsFormPage =
            assertPageIs(page, RentIncludesBillsFormPageLettingAgentUpdate::class, urlArguments)
        rentIncludesBillsFormPage.submitIsIncluded()

        val initialBillsIncludedPage =
            assertPageIs(page, BillsIncludedFormPageLettingAgentUpdate::class, urlArguments)
        initialBillsIncludedPage.selectGasElectricityWater()
        initialBillsIncludedPage.form.submit()

        var checkYourAnswersPage =
            assertPageIs(page, CheckRentIncludesBillsAnswersPageLettingAgentUpdate::class, urlArguments)

        checkYourAnswersPage.summaryList.billsIncludedRow.clickFirstActionLinkAndWait()
        val billsIncludedPage =
            assertPageIs(page, BillsIncludedFormPageLettingAgentUpdate::class, urlArguments)
        billsIncludedPage.form.billsIncludedCheckboxes.checkCheckbox(BillsIncluded.COUNCIL_TAX.toString())
        billsIncludedPage.form.submit()
        checkYourAnswersPage =
            assertPageIs(page, CheckRentIncludesBillsAnswersPageLettingAgentUpdate::class, urlArguments)

        val expectedBillsIncluded = "Gas, Electricity, Water, Council Tax"
        assertThat(checkYourAnswersPage.summaryList.billsIncludedRow).containsText(expectedBillsIncluded)
        checkYourAnswersPage.confirm()

        val updatedPropertyDetailsPage =
            assertPageIs(page, PropertyDetailsPageLettingAgentView::class, urlArguments)
        assertThat(updatedPropertyDetailsPage.summaryList.billsIncludedRow).containsText(expectedBillsIncluded)
    }

    @Test
    fun `a not found page is returned when the delegate to letting agent flag is disabled`(page: Page) {
        featureFlagManager.disable(DELEGATE_TO_LETTING_AGENT)

        navigator.navigate(
            LettingAgentUpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(token) +
                "/${RentIncludesBillsStep.ROUTE_SEGMENT}",
        )

        val errorPage = createValidPage(page, ErrorPage::class)
        assertThat(errorPage.heading).containsText("Page not found")
    }
}
