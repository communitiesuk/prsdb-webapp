package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.ORGANISATION_CONTACTS_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.OrgLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.LeadTrusteeAddressLookupPageUpdateLeadTrustee
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.LeadTrusteeCyaPageUpdateLeadTrustee
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.LeadTrusteeDobFormPageUpdateLeadTrustee
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.LeadTrusteeEmailFormPageUpdateLeadTrustee
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.LeadTrusteeNameFormPageUpdateLeadTrustee
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.LeadTrusteePhoneFormPageUpdateLeadTrustee
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.LeadTrusteeSelectAddressPageUpdateLeadTrustee
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgNameFormPageUpdateLandlordDetails
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
    fun `Organisation name change link opens the organisation name update page`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationNameChangeLinkAndWait()

        assertPageIs(page, OrgNameFormPageUpdateLandlordDetails::class)
    }

    @Test
    fun `Submitting an empty organisation name on update shows a validation error`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationNameChangeLinkAndWait()

        val updateOrgNamePage = assertPageIs(page, OrgNameFormPageUpdateLandlordDetails::class)
        updateOrgNamePage.submitName("")
        assertThat(updateOrgNamePage.form.getErrorMessage()).containsText("Enter an organisation name")
    }

    @Test
    fun `An organisation landlord can update organisation type when trust status is unchanged`(page: Page) {
        var orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationTypeChangeLinkAndWait()

        val orgTypePage = assertPageIs(page, OrgTypeFormPageUpdateOrganisationType::class)
        orgTypePage.selectCharity()
        orgTypePage.form.submit()

        val cyaPage = assertPageIs(page, OrgTypeCyaPageUpdateOrganisationType::class)
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
        cyaPage.submit()

        orgLandlordDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(orgLandlordDetailsPage.mainContent).containsText("Company")
        assertThat(orgLandlordDetailsPage.mainContent).containsText("Trust")
        assertThat(orgLandlordDetailsPage.mainContent).containsText(LEAD_TRUSTEE_NAME)
        assertThat(orgLandlordDetailsPage.mainContent).containsText(LEAD_TRUSTEE_EMAIL)
        assertThat(orgLandlordDetailsPage.mainContent).containsText(LEAD_TRUSTEE_PHONE)
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
            interruptionPage.submit()

            val cyaPage = assertPageIs(page, OrgTypeCyaPageUpdateOrganisationType::class)
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
    }

    @Nested
    inner class LeadTrusteeUpdates : NestedIntegrationTestWithMutableData("data-mockuser-org-landlord-trust.sql") {
        @Test
        fun `Lead trustee change link opens the lead trustee name update page`(page: Page) {
            val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
            orgLandlordDetailsPage.tabs.goToOrganisationContacts()
            orgLandlordDetailsPage.leadTrusteeCard.getAction("Change").link.clickAndWait()

            assertPageIs(page, LeadTrusteeNameFormPageUpdateLeadTrustee::class)
        }

        @Test
        fun `A trust org landlord can complete the full lead trustee update journey`(page: Page) {
            val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
            orgLandlordDetailsPage.tabs.goToOrganisationContacts()
            orgLandlordDetailsPage.leadTrusteeCard.getAction("Change").link.clickAndWait()

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

            // CYA TODO page
            val cyaPage = assertPageIs(page, LeadTrusteeCyaPageUpdateLeadTrustee::class)
            cyaPage.submitAndContinue()

            // Back to landlord details
            val updatedDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
            updatedDetailsPage.tabs.goToOrganisationContacts()
            assertThat(updatedDetailsPage.leadTrusteeCard.summaryList.nameRow.value).containsText(newName)
        }
    }
}
