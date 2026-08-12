package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.OrgLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages.CompaniesHouseUpdateCheckAnswersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages.CompaniesHouseUpdateInterruptionPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages.OrgCompanyNumberFormPageUpdateCompaniesHouse
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages.OrgIsRegisteredCompanyFormPageUpdateCompaniesHouse
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyWhoToProvideStep

@WithOrgLandlordProfile
class UpdateCompaniesHouseJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var organisationLandlordRepository: OrganisationLandlordRepository

    @Autowired
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository

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
    fun `Keeping the same registration answer routes through the company number page before check answers`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickCompaniesHouseChangeLinkAndWait()

        val isRegisteredCompanyPage = assertPageIs(page, OrgIsRegisteredCompanyFormPageUpdateCompaniesHouse::class)
        // The seeded landlord is already registered with Companies House, so re-answering Yes is unchanged. There's a
        // single change link for the whole Companies House section, so the journey still routes through the company
        // number page (skipping the interruption) rather than straight to check answers.
        isRegisteredCompanyPage.submitYes()

        val companyNumberPage = assertPageIs(page, OrgCompanyNumberFormPageUpdateCompaniesHouse::class)
        companyNumberPage.submitCompanyNumber("12345678")

        val checkAnswersPage = assertPageIs(page, CompaniesHouseUpdateCheckAnswersPage::class)
        checkAnswersPage.confirmAndSubmit()

        val updatedDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(updatedDetailsPage.mainContent).containsText("12345678")
    }

    @Test
    fun `Changing the registration answer to no shows the interruption then routes to the governing body flow`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickCompaniesHouseChangeLinkAndWait()

        val isRegisteredCompanyPage = assertPageIs(page, OrgIsRegisteredCompanyFormPageUpdateCompaniesHouse::class)
        // The seeded landlord is registered with Companies House, so answering No is a change and shows the interruption.
        isRegisteredCompanyPage.submitNo()

        val interruptionPage = assertPageIs(page, CompaniesHouseUpdateInterruptionPage::class)
        interruptionPage.submit()

        assertTrue(page.url().contains(OrgGovBodyWhoToProvideStep.ROUTE_SEGMENT))
    }

    @Test
    fun `Keeping the non-company answer routes straight to the governing body flow without the interruption`(page: Page) {
        val landlord =
            organisationalLandlordUserRepository
                .findByBaseUser_Id("urn:fdc:gov.uk:2022:ORG01")
                .single()
                .organisationalLandlord
        landlord.companyNumber = null
        organisationLandlordRepository.save(landlord)

        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickCompaniesHouseChangeLinkAndWait()

        val isRegisteredCompanyPage = assertPageIs(page, OrgIsRegisteredCompanyFormPageUpdateCompaniesHouse::class)
        // The landlord is now not registered with Companies House, so answering No is unchanged and routes straight to
        // the governing body flow (skipping the interruption) because of the single Companies House change link.
        isRegisteredCompanyPage.submitNo()

        assertTrue(page.url().contains(OrgGovBodyWhoToProvideStep.ROUTE_SEGMENT))
    }

    @Test
    fun `Changing the registration answer to yes shows the interruption then asks for the company number`(page: Page) {
        val landlord =
            organisationalLandlordUserRepository
                .findByBaseUser_Id("urn:fdc:gov.uk:2022:ORG01")
                .single()
                .organisationalLandlord
        landlord.companyNumber = null
        organisationLandlordRepository.save(landlord)

        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickCompaniesHouseChangeLinkAndWait()

        val isRegisteredCompanyPage = assertPageIs(page, OrgIsRegisteredCompanyFormPageUpdateCompaniesHouse::class)
        isRegisteredCompanyPage.submitYes()

        val interruptionPage = assertPageIs(page, CompaniesHouseUpdateInterruptionPage::class)
        interruptionPage.submit()

        val companyNumberPage = assertPageIs(page, OrgCompanyNumberFormPageUpdateCompaniesHouse::class)
        companyNumberPage.submitCompanyNumber("87654321")

        val checkAnswersPage = assertPageIs(page, CompaniesHouseUpdateCheckAnswersPage::class)
        checkAnswersPage.confirmAndSubmit()

        val updatedDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(updatedDetailsPage.mainContent).containsText("87654321")
    }

    @Test
    fun `The check answers page shows the company details rows with change links`(page: Page) {
        val checkAnswersPage = navigateToCompanyCheckAnswers(page, companyNumber = "12345678")

        assertThat(checkAnswersPage.companyDetails.registeredWithCompaniesHouseRow.value).containsText("Yes")
        assertThat(checkAnswersPage.companyDetails.companiesHouseNumberRow.value).containsText("12345678")
    }

    @Test
    fun `Changing the company number from the check answers page updates the record`(page: Page) {
        val checkAnswersPage = navigateToCompanyCheckAnswers(page, companyNumber = "12345678")

        checkAnswersPage.companyDetails.companiesHouseNumberRow.clickFirstActionLinkAndWait()

        val companyNumberChangePage = assertPageIs(page, OrgCompanyNumberFormPageUpdateCompaniesHouse::class)
        companyNumberChangePage.submitCompanyNumber("87654321")

        val updatedCheckAnswersPage = assertPageIs(page, CompaniesHouseUpdateCheckAnswersPage::class)
        assertThat(updatedCheckAnswersPage.companyDetails.companiesHouseNumberRow.value).containsText("87654321")
        updatedCheckAnswersPage.confirmAndSubmit()

        val updatedDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(updatedDetailsPage.mainContent).containsText("87654321")
    }

    @Test
    fun `Changing the registration answer from the check answers page re-runs the companies house questions`(page: Page) {
        val checkAnswersPage = navigateToCompanyCheckAnswers(page, companyNumber = "12345678")

        checkAnswersPage.companyDetails.registeredWithCompaniesHouseRow.clickFirstActionLinkAndWait()

        assertPageIs(page, OrgIsRegisteredCompanyFormPageUpdateCompaniesHouse::class)
    }

    private fun navigateToCompanyCheckAnswers(
        page: Page,
        companyNumber: String,
    ): CompaniesHouseUpdateCheckAnswersPage {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickCompaniesHouseChangeLinkAndWait()

        assertPageIs(page, OrgIsRegisteredCompanyFormPageUpdateCompaniesHouse::class).submitYes()
        assertPageIs(page, OrgCompanyNumberFormPageUpdateCompaniesHouse::class).submitCompanyNumber(companyNumber)

        return assertPageIs(page, CompaniesHouseUpdateCheckAnswersPage::class)
    }
}
