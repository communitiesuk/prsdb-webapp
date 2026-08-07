package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.OrgLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages.CompaniesHouseUpdateCheckAnswersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages.CompaniesHouseUpdateInterruptionPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages.OrgCompanyNumberFormPageUpdateCompaniesHouse
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages.OrgIsRegisteredCompanyFormPageUpdateCompaniesHouse
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyDetailsStep

@WithOrgLandlordProfile
class UpdateCompaniesHouseJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @BeforeEach
    fun setup() {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)
    }

    @Test
    fun `Companies House change link opens the is-registered-company update page`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickCompaniesHouseChangeLinkAndWait()

        assertPageIs(page, OrgIsRegisteredCompanyFormPageUpdateCompaniesHouse::class)
    }

    @Test
    fun `An organisation landlord can update the company number and return to the details page`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickCompaniesHouseChangeLinkAndWait()

        val isRegisteredCompanyPage = assertPageIs(page, OrgIsRegisteredCompanyFormPageUpdateCompaniesHouse::class)
        isRegisteredCompanyPage.submitYes()

        val interruptionPage = assertPageIs(page, CompaniesHouseUpdateInterruptionPage::class)
        interruptionPage.clickContinue()

        val companyNumberPage = assertPageIs(page, OrgCompanyNumberFormPageUpdateCompaniesHouse::class)
        companyNumberPage.submitCompanyNumber("87654321")

        val checkAnswersPage = assertPageIs(page, CompaniesHouseUpdateCheckAnswersPage::class)
        checkAnswersPage.confirmAndSubmit()

        val updatedDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(updatedDetailsPage.mainContent).containsText("87654321")
    }

    @Test
    fun `Answering no routes to the governing body members flow`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickCompaniesHouseChangeLinkAndWait()

        val isRegisteredCompanyPage = assertPageIs(page, OrgIsRegisteredCompanyFormPageUpdateCompaniesHouse::class)
        isRegisteredCompanyPage.submitNo()

        val interruptionPage = assertPageIs(page, CompaniesHouseUpdateInterruptionPage::class)
        interruptionPage.clickContinue()

        assertTrue(page.url().contains(OrgGovBodyDetailsStep.ROUTE_SEGMENT))
    }
}
