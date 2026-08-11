package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.ORGANISATION_CONTACTS_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.OrgLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.LeadTrusteeAddressLookupPageUpdateLeadTrustee
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.LeadTrusteeCyaPageUpdateLeadTrustee
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.LeadTrusteeDobFormPageUpdateLeadTrustee
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.LeadTrusteeEmailFormPageUpdateLeadTrustee
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.LeadTrusteeNameFormPageUpdateLeadTrustee
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.LeadTrusteePhoneFormPageUpdateLeadTrustee
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.LeadTrusteeSelectAddressPageUpdateLeadTrustee
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgCharityCyaPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgCharityNumberEnglandAndWalesFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgCharityRegisteredWithFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgEmailFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgIsRegisteredCharityFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgMainContactFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgNameFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgPhoneNumberFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.LeadTrusteeAddressFormPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.LeadTrusteeDobFormPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.LeadTrusteeEmailFormPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.LeadTrusteeNameFormPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.LeadTrusteePhoneFormPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.LeadTrusteeSelectAddressFormPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.OrgTypeCyaPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.OrgTypeFormPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.OrgTypeTrustInterruptionPageUpdateOrganisationType
import kotlin.test.assertEquals

@WithOrgLandlordProfile
class OrganisationLandlordUpdateJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @BeforeEach
    fun setup() {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)
    }

    @Test
    fun `An organisation landlord can update organisation name from landlord details and return to details page`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationNameChangeLinkAndWait()

        val updateOrgNamePage = assertPageIs(page, OrgNameFormPageUpdateLandlordDetails::class)
        updateOrgNamePage.submitName("Updated Organisation Name")

        val updatedDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(updatedDetailsPage.mainContent).containsText("Updated Organisation Name")
    }

    @Test
    fun `An organisation landlord can update organisation email from landlord details and return to details page`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationEmailChangeLinkAndWait()

        val updateOrgEmailPage = assertPageIs(page, OrgEmailFormPageUpdateLandlordDetails::class)
        updateOrgEmailPage.submitEmail("updated-organisation@example.com")

        val updatedDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(updatedDetailsPage.mainContent).containsText("updated-organisation@example.com")
    }

    @Test
    fun `Main contact change link opens the main contact update page`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.tabs.goToOrganisationContacts()
        orgLandlordDetailsPage.clickMainContactChangeLinkAndWait()

        assertPageIs(page, OrgMainContactFormPageUpdateLandlordDetails::class)
    }

    @Test
    fun `Main contact update page shows the update warning`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.tabs.goToOrganisationContacts()
        orgLandlordDetailsPage.clickMainContactChangeLinkAndWait()

        val updatePage = assertPageIs(page, OrgMainContactFormPageUpdateLandlordDetails::class)
        BaseComponent.assertThat(updatePage.warning).isVisible()
    }

    @Test
    fun `An organisation landlord can update the main contact and return to the contacts tab`(page: Page) {
        var orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.tabs.goToOrganisationContacts()
        orgLandlordDetailsPage.clickMainContactChangeLinkAndWait()

        val updatePage = assertPageIs(page, OrgMainContactFormPageUpdateLandlordDetails::class)
        updatePage.submit("New Main Contact", "new.main.contact@example.com", "07222222222")

        orgLandlordDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertEquals(ORGANISATION_CONTACTS_FRAGMENT, orgLandlordDetailsPage.tabs.activeTabPanelId)
        assertThat(orgLandlordDetailsPage.mainContactCard.summaryList.nameRow.value).containsText("New Main Contact")
        assertThat(orgLandlordDetailsPage.mainContactCard.summaryList.emailRow.value).containsText("new.main.contact@example.com")
        assertThat(orgLandlordDetailsPage.mainContactCard.summaryList.phoneNumberRow.value).containsText("07222222222")
    }

    @Test
    fun `An organisation landlord can update their charity registration details and return to the details page`(page: Page) {
        startCharityUpdateJourney(page).submitYes()
        assertPageIs(page, OrgCharityRegisteredWithFormPageUpdateLandlordDetails::class)
            .submitCharityRegisteredWith(CharityRegulator.ENGLAND_AND_WALES)
        assertPageIs(page, OrgCharityNumberEnglandAndWalesFormPageUpdateLandlordDetails::class)
            .submitCharityNumber(CHARITY_NUMBER)

        submitCyaPage(page)

        val summaryList = assertPageIs(page, OrgLandlordDetailsPage::class).organisationDetailsSummaryList
        assertThat(summaryList.registeredCharityRow.value).containsText("Yes")
        assertThat(summaryList.charityCommissionRow.value).containsText("Charities Commission of England and Wales")
        assertThat(summaryList.charityNumberRow.value).containsText(CHARITY_NUMBER)
    }

    @Test
    fun `Selecting no charity regulator records the charity without a charity number`(page: Page) {
        startCharityUpdateJourney(page).submitYes()
        assertPageIs(page, OrgCharityRegisteredWithFormPageUpdateLandlordDetails::class)
            .submitCharityRegisteredWith(CharityRegulator.NONE)

        submitCyaPage(page)

        val detailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(detailsPage.organisationDetailsSummaryList.registeredCharityRow.value).containsText("Yes")
        assertThat(detailsPage.organisationDetailsSummaryList.charityCommissionRow.value).containsText("None of these")
        assertThat(detailsPage.mainContent).not().containsText("Charity number")
    }

    @Test
    fun `Changing the charity regulator after going back does not record the abandoned charity number`(page: Page) {
        startCharityUpdateJourney(page).submitYes()
        assertPageIs(page, OrgCharityRegisteredWithFormPageUpdateLandlordDetails::class)
            .submitCharityRegisteredWith(CharityRegulator.ENGLAND_AND_WALES)
        assertPageIs(page, OrgCharityNumberEnglandAndWalesFormPageUpdateLandlordDetails::class)
            .submitCharityNumber(CHARITY_NUMBER)

        BackLink.default(page).clickAndWait()
        assertPageIs(page, OrgCharityNumberEnglandAndWalesFormPageUpdateLandlordDetails::class)
        BackLink.default(page).clickAndWait()
        assertPageIs(page, OrgCharityRegisteredWithFormPageUpdateLandlordDetails::class)
            .submitCharityRegisteredWith(CharityRegulator.NONE)

        submitCyaPage(page)

        val detailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(detailsPage.organisationDetailsSummaryList.registeredCharityRow.value).containsText("Yes")
        assertThat(detailsPage.organisationDetailsSummaryList.charityCommissionRow.value).containsText("None of these")
        assertThat(detailsPage.mainContent).not().containsText("Charity number")
    }

    @Test
    fun `Answering no to registered charity clears previously recorded charity details`(page: Page) {
        startCharityUpdateJourney(page).submitYes()
        assertPageIs(page, OrgCharityRegisteredWithFormPageUpdateLandlordDetails::class)
            .submitCharityRegisteredWith(CharityRegulator.ENGLAND_AND_WALES)
        assertPageIs(page, OrgCharityNumberEnglandAndWalesFormPageUpdateLandlordDetails::class)
            .submitCharityNumber(CHARITY_NUMBER)
        submitCyaPage(page)

        startCharityUpdateJourney(page).submitNo()
        submitCyaPage(page)

        val detailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(detailsPage.organisationDetailsSummaryList.registeredCharityRow.value).containsText("No")
        assertThat(detailsPage.mainContent).not().containsText("Charity commission")
        assertThat(detailsPage.mainContent).not().containsText("Charity number")
    }

    // The is-registered-charity URL is a prefix of the registered-with URL, so the heading is asserted to confirm
    // which step the journey is actually on
    private fun startCharityUpdateJourney(page: Page): OrgIsRegisteredCharityFormPageUpdateLandlordDetails {
        navigator.goToOrgLandlordDetails().clickOrganisationCharityChangeLinkAndWait()

        val charityPage = assertPageIs(page, OrgIsRegisteredCharityFormPageUpdateLandlordDetails::class)
        assertThat(charityPage.heading).containsText("Is your organisation a registered charity?")
        return charityPage
    }

    // TODO: PDJB-1463: this page is a placeholder that does not yet list the submitted answers
    private fun submitCyaPage(page: Page) = assertPageIs(page, OrgCharityCyaPageUpdateLandlordDetails::class).submit()

    @Test
    fun `An organisation landlord can update the organisation phone number and return to details page`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationPhoneNumberChangeLinkAndWait()

        val updatePhonePage = assertPageIs(page, OrgPhoneNumberFormPageUpdateLandlordDetails::class)
        updatePhonePage.submitPhoneNumber("07999123456")

        val updatedDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(updatedDetailsPage.mainContent).containsText("07999123456")
    }

    @Test
    fun `An organisation landlord can update organisation type when trust status is unchanged`(page: Page) {
        var orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationTypeChangeLinkAndWait()

        val orgTypePage = assertPageIs(page, OrgTypeFormPageUpdateOrganisationType::class)
        orgTypePage.selectCharity()
        orgTypePage.form.submit()

        val cyaPage = assertPageIs(page, OrgTypeCyaPageUpdateOrganisationType::class)
        assertThat(cyaPage.summaryList.organisationTypeRow.value).containsText("Charity")
        BaseComponent.assertThat(cyaPage.leadTrusteeCard).isHidden()
        cyaPage.submit()

        orgLandlordDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(orgLandlordDetailsPage.mainContent).containsText("Charity")
        assertThat(orgLandlordDetailsPage.mainContent).not().containsText("Trust")
    }

    @Test
    fun `An organisation landlord can add trust to their organisation type and provide lead trustee details`(page: Page) {
        var orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationTypeChangeLinkAndWait()

        val orgTypePage = assertPageIs(page, OrgTypeFormPageUpdateOrganisationType::class)
        orgTypePage.selectCompany()
        orgTypePage.selectTrust()
        orgTypePage.form.submit()

        val interruptionPage = assertPageIs(page, OrgTypeTrustInterruptionPageUpdateOrganisationType::class)
        assertThat(interruptionPage.heading).containsText("You must provide trustee details")
        assertThat(interruptionPage.body).containsText("your organisation is a trust")
        interruptionPage.submit()

        val leadTrusteeNamePage = assertPageIs(page, LeadTrusteeNameFormPageUpdateOrganisationType::class)
        leadTrusteeNamePage.submitName(LEAD_TRUSTEE_NAME)

        val leadTrusteeDobPage = assertPageIs(page, LeadTrusteeDobFormPageUpdateOrganisationType::class)
        leadTrusteeDobPage.submitDate("15", "6", "1980")

        val leadTrusteeEmailPage = assertPageIs(page, LeadTrusteeEmailFormPageUpdateOrganisationType::class)
        leadTrusteeEmailPage.submitEmail(LEAD_TRUSTEE_EMAIL)

        val leadTrusteePhonePage = assertPageIs(page, LeadTrusteePhoneFormPageUpdateOrganisationType::class)
        leadTrusteePhonePage.submitPhoneNumber(LEAD_TRUSTEE_PHONE)

        val leadTrusteeLookupAddressPage = assertPageIs(page, LeadTrusteeAddressFormPageUpdateOrganisationType::class)
        leadTrusteeLookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val leadTrusteeSelectAddressPage = assertPageIs(page, LeadTrusteeSelectAddressFormPageUpdateOrganisationType::class)
        leadTrusteeSelectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

        val cyaPage = assertPageIs(page, OrgTypeCyaPageUpdateOrganisationType::class)
        assertThat(cyaPage.summaryList.organisationTypeRow.value).containsText("Company")
        assertThat(cyaPage.summaryList.organisationTypeRow.value).containsText("Trust")
        assertThat(cyaPage.leadTrusteeCard.summaryList.nameRow.value).containsText(LEAD_TRUSTEE_NAME)
        assertThat(cyaPage.leadTrusteeCard.summaryList.emailRow.value).containsText(LEAD_TRUSTEE_EMAIL)
        assertThat(cyaPage.leadTrusteeCard.summaryList.phoneRow.value).containsText(LEAD_TRUSTEE_PHONE)
        cyaPage.submit()

        orgLandlordDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(orgLandlordDetailsPage.mainContent).containsText("Company")
        assertThat(orgLandlordDetailsPage.mainContent).containsText("Trust")
        assertThat(orgLandlordDetailsPage.mainContent).containsText(LEAD_TRUSTEE_NAME)
        assertThat(orgLandlordDetailsPage.mainContent).containsText(LEAD_TRUSTEE_EMAIL)
        assertThat(orgLandlordDetailsPage.mainContent).containsText(LEAD_TRUSTEE_PHONE)
    }

    @Test
    fun `Go back link on adding trust interruption page returns to the organisation type page`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationTypeChangeLinkAndWait()

        val orgTypePage = assertPageIs(page, OrgTypeFormPageUpdateOrganisationType::class)
        orgTypePage.selectCompany()
        orgTypePage.selectTrust()
        orgTypePage.form.submit()

        val interruptionPage = assertPageIs(page, OrgTypeTrustInterruptionPageUpdateOrganisationType::class)
        interruptionPage.goBackLink.clickAndWait()

        assertPageIs(page, OrgTypeFormPageUpdateOrganisationType::class)
    }

    @Nested
    inner class RemovingTrustUpdates : NestedIntegrationTestWithMutableData("data-org-landlord-trust.sql") {
        @BeforeEach
        fun setup() {
            featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)
        }

        @Test
        fun `An organisation landlord can remove trust from their organisation type without re-walking trustee details`(page: Page) {
            var orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
            orgLandlordDetailsPage.clickOrganisationTypeChangeLinkAndWait()

            val orgTypePage = assertPageIs(page, OrgTypeFormPageUpdateOrganisationType::class)
            orgTypePage.deselectTrust()
            orgTypePage.selectCompany()
            orgTypePage.form.submit()

            val interruptionPage = assertPageIs(page, OrgTypeTrustInterruptionPageUpdateOrganisationType::class)
            assertThat(interruptionPage.heading).containsText("Are you sure you want to change this?")
            assertThat(interruptionPage.body).containsText("company")
            assertThat(interruptionPage.body).containsText("lead trustee details will be removed")
            interruptionPage.submit()

            val cyaPage = assertPageIs(page, OrgTypeCyaPageUpdateOrganisationType::class)
            assertThat(cyaPage.summaryList.organisationTypeRow.value).containsText("Company")
            BaseComponent.assertThat(cyaPage.leadTrusteeCard).isHidden()
            cyaPage.submit()

            orgLandlordDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
            assertEquals(ORGANISATION_CONTACTS_FRAGMENT, orgLandlordDetailsPage.tabs.activeTabPanelId)
            assertThat(orgLandlordDetailsPage.mainContent).containsText("Company")
            assertThat(orgLandlordDetailsPage.mainContent).not().containsText("Trust")
            assertThat(orgLandlordDetailsPage.mainContent).not().containsText("Existing Lead Trustee")
            assertThat(orgLandlordDetailsPage.mainContent).not().containsText("Lead trustee")
        }
    }

    companion object {
        private const val LEAD_TRUSTEE_NAME = "Test Lead Trustee Name"
        private const val LEAD_TRUSTEE_EMAIL = "trustee@test.com"
        private const val LEAD_TRUSTEE_PHONE = "07123456789"
        private const val CHARITY_NUMBER = "1234567"
    }

    @Nested
    inner class LeadTrusteeUpdates : NestedIntegrationTestWithMutableData("data-mockuser-org-landlord-trust.sql") {
        @Test
        fun `A trust org landlord can complete the full lead trustee update journey`(page: Page) {
            val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
            orgLandlordDetailsPage.tabs.goToOrganisationContacts()
            orgLandlordDetailsPage.leadTrusteeCard
                .getAction("Change")
                .link
                .clickAndWait()

            // Lead Trustee Name
            val namePage = assertPageIs(page, LeadTrusteeNameFormPageUpdateLeadTrustee::class)
            val newName = "Updated Trustee Name"
            namePage.submitName(newName)

            // Lead Trustee DOB
            val dobPage = assertPageIs(page, LeadTrusteeDobFormPageUpdateLeadTrustee::class)
            dobPage.submitDate("15", "6", "1985")

            // Lead Trustee Email
            val emailPage = assertPageIs(page, LeadTrusteeEmailFormPageUpdateLeadTrustee::class)
            emailPage.submitEmail("updated.trustee@example.com")

            // Lead Trustee Phone
            val phonePage = assertPageIs(page, LeadTrusteePhoneFormPageUpdateLeadTrustee::class)
            phonePage.submitPhoneNumber("07999888777")

            // Lead Trustee Address Lookup
            val addressLookupPage = assertPageIs(page, LeadTrusteeAddressLookupPageUpdateLeadTrustee::class)
            addressLookupPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

            // Select Address
            val selectAddressPage = assertPageIs(page, LeadTrusteeSelectAddressPageUpdateLeadTrustee::class)
            selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

            // CYA page
            // TODO: PDJB-1470: Implement this
            val cyaPage = assertPageIs(page, LeadTrusteeCyaPageUpdateLeadTrustee::class)
            cyaPage.submit()

            // Back to landlord details
            val updatedDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
            updatedDetailsPage.tabs.goToOrganisationContacts()
            assertThat(updatedDetailsPage.leadTrusteeCard.summaryList.nameRow.value).containsText(newName)
        }
    }
}
