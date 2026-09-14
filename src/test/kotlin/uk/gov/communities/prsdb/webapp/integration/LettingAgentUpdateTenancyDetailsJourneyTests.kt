package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateTenancyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLettingAgentView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateTenancyDetailsJourneyPages.BillsIncludedFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateTenancyDetailsJourneyPages.CheckTenancyDetailsAnswersPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateTenancyDetailsJourneyPages.FurnishedStatusFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateTenancyDetailsJourneyPages.HouseholdsNumberOfPeopleFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateTenancyDetailsJourneyPages.NumberOfHouseholdsFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateTenancyDetailsJourneyPages.RentAmountFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateTenancyDetailsJourneyPages.RentFrequencyFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateTenancyDetailsJourneyPages.RentIncludesBillsFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import java.util.UUID

class LettingAgentUpdateTenancyDetailsJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    // PO 43 (token ...2222c) has tenancy marked "provide later", so the tenancy row links to the full tenancy journey.
    private val tenancyProvideLaterToken = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222c")
    private val tenancyProvideLaterUrlArguments = mapOf("token" to tenancyProvideLaterToken.toString())

    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)
    }

    @Test
    fun `the tenancy provide-later change link runs the full tenancy details journey and returns to the property record`(page: Page) {
        val detailsPage = navigator.goToPropertyDetailsLettingAgentView(tenancyProvideLaterToken)
        detailsPage.summaryList.tenancyRow.clickFirstActionLinkAndWait()

        val newNumberOfHouseholds = 2
        val numberOfHouseholdsPage =
            assertPageIs(page, NumberOfHouseholdsFormPageLettingAgentUpdate::class, tenancyProvideLaterUrlArguments)
        numberOfHouseholdsPage.submitNumberOfHouseholds(newNumberOfHouseholds)

        val newNumberOfPeople = 4
        val numberOfPeoplePage =
            assertPageIs(page, HouseholdsNumberOfPeopleFormPageLettingAgentUpdate::class, tenancyProvideLaterUrlArguments)
        numberOfPeoplePage.submitNumOfPeople(newNumberOfPeople)

        val rentIncludesBillsPage =
            assertPageIs(page, RentIncludesBillsFormPageLettingAgentUpdate::class, tenancyProvideLaterUrlArguments)
        rentIncludesBillsPage.submitIsIncluded()

        val expectedBillsIncluded = "Gas, Electricity, Water"
        val billsIncludedPage =
            assertPageIs(page, BillsIncludedFormPageLettingAgentUpdate::class, tenancyProvideLaterUrlArguments)
        billsIncludedPage.selectGasElectricityWater()
        billsIncludedPage.form.submit()

        val newFurnishedStatus = FurnishedStatus.FURNISHED
        val furnishedStatusPage =
            assertPageIs(page, FurnishedStatusFormPageLettingAgentUpdate::class, tenancyProvideLaterUrlArguments)
        furnishedStatusPage.submitFurnishedStatus(newFurnishedStatus)

        val newRentFrequency = RentFrequency.MONTHLY
        val rentFrequencyPage =
            assertPageIs(page, RentFrequencyFormPageLettingAgentUpdate::class, tenancyProvideLaterUrlArguments)
        rentFrequencyPage.selectRentFrequency(newRentFrequency)
        rentFrequencyPage.form.submit()

        val newRentAmount = "750"
        val rentAmountPage =
            assertPageIs(page, RentAmountFormPageLettingAgentUpdate::class, tenancyProvideLaterUrlArguments)
        rentAmountPage.submitRentAmount(newRentAmount)

        val checkAnswersPage =
            assertPageIs(page, CheckTenancyDetailsAnswersPageLettingAgentUpdate::class, tenancyProvideLaterUrlArguments)
        assertThat(checkAnswersPage.summaryList.numberOfHouseholdsRow).containsText(newNumberOfHouseholds.toString())
        assertThat(checkAnswersPage.summaryList.numberOfPeopleRow).containsText(newNumberOfPeople.toString())
        assertThat(checkAnswersPage.summaryList.billsIncludedRow).containsText(expectedBillsIncluded)
        assertThat(checkAnswersPage.summaryList.rentAmountRow).containsText(newRentAmount)
        checkAnswersPage.confirm()

        val updatedDetailsPage =
            assertPageIs(page, PropertyDetailsPageLettingAgentView::class, tenancyProvideLaterUrlArguments)
        assertThat(updatedDetailsPage.summaryList.numberOfHouseholdsRow.value).containsText(newNumberOfHouseholds.toString())
        assertThat(updatedDetailsPage.summaryList.numberOfTenantsRow.value).containsText(newNumberOfPeople.toString())
        assertThat(updatedDetailsPage.summaryList.billsIncludedRow.value).containsText(expectedBillsIncluded)
        assertThat(updatedDetailsPage.summaryList.rentAmountRow.value).containsText(newRentAmount)
    }

    @Test
    fun `a not found page is returned for the tenancy details update route when the flag is disabled`(page: Page) {
        featureFlagManager.disable(DELEGATE_TO_LETTING_AGENT)

        navigator.navigate(
            LettingAgentUpdateTenancyDetailsController.getUpdateTenancyDetailsRoute(tenancyProvideLaterToken) +
                "/${HouseholdStep.ROUTE_SEGMENT}",
        )

        val errorPage = createValidPage(page, ErrorPage::class)
        assertThat(errorPage.heading).containsText("Page not found")
    }
}
