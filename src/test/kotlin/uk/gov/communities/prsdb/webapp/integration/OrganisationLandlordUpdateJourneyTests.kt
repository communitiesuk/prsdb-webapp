package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.ORGANISATION_CONTACTS_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.OrgLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGoverningBodyJourneyPages.GoverningBodyCyaPageUpdateGoverningBody
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGoverningBodyJourneyPages.OrgGovBodyMemberDobFormPageUpdateGoverningBody
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGoverningBodyJourneyPages.OrgGovBodyMemberListFormPageUpdateGoverningBody
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGoverningBodyJourneyPages.OrgGovBodyMemberLookupAddressFormPageUpdateGoverningBody
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGoverningBodyJourneyPages.OrgGovBodyMemberNameFormPageUpdateGoverningBody
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGoverningBodyJourneyPages.OrgGovBodyMemberSelectAddressFormPageUpdateGoverningBody
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGoverningBodyJourneyPages.OrgGovBodyWhoToProvideFormPageUpdateGoverningBody
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgLookupAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgMainContactFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgManualAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgNameFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgNoAddressFoundFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgPhoneNumberFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgSelectAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.LeadTrusteeAddressFormPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.LeadTrusteeDobFormPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.LeadTrusteeEmailFormPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.LeadTrusteeNameFormPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.LeadTrusteePhoneFormPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.LeadTrusteeSelectAddressFormPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.OrgTypeCyaPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.OrgTypeFormPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.OrgTypeTrustInterruptionPageUpdateOrganisationType
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
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
    fun `Submitting an empty organisation name on update shows a validation error`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationNameChangeLinkAndWait()

        val updateOrgNamePage = assertPageIs(page, OrgNameFormPageUpdateLandlordDetails::class)
        updateOrgNamePage.submitName("")
        assertThat(updateOrgNamePage.form.getErrorMessage()).containsText("Enter an organisation name")
    }

    @Test
    fun `An organisation landlord can update their organisation address (selected) and return to details page`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationAddressChangeLinkAndWait()

        val lookupOrgAddressPage = assertPageIs(page, OrgLookupAddressFormPageUpdateLandlordDetails::class)
        lookupOrgAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val newSelectedAddress = "1 PRSDB Square, EG1 2AA"
        val selectOrgAddressPage = assertPageIs(page, OrgSelectAddressFormPageUpdateLandlordDetails::class)
        BaseComponent.assertThat(selectOrgAddressPage.warning).isVisible()
        selectOrgAddressPage.selectAddressAndSubmit(newSelectedAddress)

        val updatedDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(updatedDetailsPage.mainContent).containsText(newSelectedAddress.replace(", ", " "))
    }

    @Test
    fun `An organisation landlord can update their organisation address (manual) and return to details page`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationAddressChangeLinkAndWait()

        val lookupOrgAddressPage = assertPageIs(page, OrgLookupAddressFormPageUpdateLandlordDetails::class)
        lookupOrgAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val selectOrgAddressPage = assertPageIs(page, OrgSelectAddressFormPageUpdateLandlordDetails::class)
        selectOrgAddressPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)

        val newFirstLine = "3 Example Road"
        val newTown = "Vilton"
        val newPostcode = "AB1 9YZ"
        val manualOrgAddressPage = assertPageIs(page, OrgManualAddressFormPageUpdateLandlordDetails::class)
        BaseComponent.assertThat(manualOrgAddressPage.warning).isVisible()
        manualOrgAddressPage.submitAddress(newFirstLine, townOrCity = newTown, postcode = newPostcode)

        val newSingleLineAddress = AddressDataModel.manualAddressDataToSingleLineAddress(newFirstLine, newTown, newPostcode)
        val updatedDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(updatedDetailsPage.mainContent).containsText(newSingleLineAddress.replace(", ", " "))
    }

    @Test
    fun `Organisation address change link opens the address lookup page`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationAddressChangeLinkAndWait()

        assertPageIs(page, OrgLookupAddressFormPageUpdateLandlordDetails::class)
    }

    @Test
    fun `Submitting an empty organisation house number and postcode on the address lookup page shows validation errors`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationAddressChangeLinkAndWait()

        val lookupOrgAddressPage = assertPageIs(page, OrgLookupAddressFormPageUpdateLandlordDetails::class)
        lookupOrgAddressPage.submitPostcodeAndBuildingNameOrNumber("", "")
        assertThat(lookupOrgAddressPage.form.getErrorMessage("houseNameOrNumber")).containsText("Enter a house name or number")
        assertThat(lookupOrgAddressPage.form.getErrorMessage("postcode")).containsText("Enter a postcode")
    }

    @Test
    fun `Submitting an empty organisation address, city and postcode on the manual address page shows validation errors`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationAddressChangeLinkAndWait()

        val lookupOrgAddressPage = assertPageIs(page, OrgLookupAddressFormPageUpdateLandlordDetails::class)
        lookupOrgAddressPage.submitPostcodeAndBuildingNameOrNumber("1", "1")

        val noMatchingOrgAddressPage = assertPageIs(page, OrgNoAddressFoundFormPageUpdateLandlordDetails::class)
        noMatchingOrgAddressPage.form.submit()

        val manualOrgAddressPage = assertPageIs(page, OrgManualAddressFormPageUpdateLandlordDetails::class)
        manualOrgAddressPage.submitAddress(addressLineOne = "", townOrCity = "", postcode = "")

        assertThat(manualOrgAddressPage.form.getErrorMessage("addressLineOne"))
            .containsText("Enter the first line of an address, typically the building and street")
        assertThat(manualOrgAddressPage.form.getErrorMessage("townOrCity")).containsText("Enter town or city")
        assertThat(manualOrgAddressPage.form.getErrorMessage("postcode")).containsText("Enter postcode")
    }

    @Test
    fun `Submitting the form with no option selected on organisation select address page shows a validation error`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationAddressChangeLinkAndWait()

        val lookupOrgAddressPage = assertPageIs(page, OrgLookupAddressFormPageUpdateLandlordDetails::class)
        lookupOrgAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val selectOrgAddressPage = assertPageIs(page, OrgSelectAddressFormPageUpdateLandlordDetails::class)
        selectOrgAddressPage.form.submit()

        assertThat(selectOrgAddressPage.form.getErrorMessage("address")).containsText("Select an address")
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
        assertThat(detailsPage.organisationDetailsSummaryList.charityCommissionRow.value).containsText("Other")
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
        assertThat(detailsPage.organisationDetailsSummaryList.charityCommissionRow.value).containsText("Other")
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

    @Test
    fun `Changing org type on CYA to add then remove trust shows interruption pages correctly`(page: Page) {
        // Start: non-trust org
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationTypeChangeLinkAndWait()

        var orgTypePage = assertPageIs(page, OrgTypeFormPageUpdateOrganisationType::class)
        orgTypePage.selectCharity()
        orgTypePage.form.submit()

        var cyaPage = assertPageIs(page, OrgTypeCyaPageUpdateOrganisationType::class)
        assertThat(cyaPage.summaryList.organisationTypeRow.value).containsText("Charity")
        BaseComponent.assertThat(cyaPage.leadTrusteeCard).isHidden()

        // CYA change: set type to include trust
        cyaPage.summaryList.organisationTypeRow.clickFirstActionLinkAndWait()

        orgTypePage = assertPageIs(page, OrgTypeFormPageUpdateOrganisationType::class)
        orgTypePage.selectCompany()
        orgTypePage.selectTrust()
        orgTypePage.form.submit()

        var interruptionPage = assertPageIs(page, OrgTypeTrustInterruptionPageUpdateOrganisationType::class)
        assertThat(interruptionPage.heading).containsText("You must provide trustee details")
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

        cyaPage = assertPageIs(page, OrgTypeCyaPageUpdateOrganisationType::class)
        assertThat(cyaPage.summaryList.organisationTypeRow.value).containsText("Company")
        assertThat(cyaPage.summaryList.organisationTypeRow.value).containsText("Trust")
        assertThat(cyaPage.leadTrusteeCard.summaryList.nameRow.value).containsText(LEAD_TRUSTEE_NAME)

        // CYA change again: remove trust
        cyaPage.summaryList.organisationTypeRow.clickFirstActionLinkAndWait()

        orgTypePage = assertPageIs(page, OrgTypeFormPageUpdateOrganisationType::class)
        orgTypePage.deselectTrust()
        orgTypePage.form.submit()

        interruptionPage = assertPageIs(page, OrgTypeTrustInterruptionPageUpdateOrganisationType::class)
        assertThat(interruptionPage.heading).containsText("Are you sure you want to change this?")
        interruptionPage.submit()

        cyaPage = assertPageIs(page, OrgTypeCyaPageUpdateOrganisationType::class)
        assertThat(cyaPage.summaryList.organisationTypeRow.value).containsText("Company")
        assertThat(cyaPage.summaryList.organisationTypeRow.value).not().containsText("Trust")
        BaseComponent.assertThat(cyaPage.leadTrusteeCard).isHidden()
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

    @Nested
    inner class GoverningBodyUpdateTests : NestedIntegrationTestWithMutableData("data-mockuser-org-landlord-trust.sql") {
        @Test
        fun `A trust org landlord can update governing body members`(page: Page) {
            var orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
            orgLandlordDetailsPage.tabs.goToOrganisationContacts()
            orgLandlordDetailsPage.governingBodyMembersLink.clickAndWait()

            var memberListPage = assertPageIs(page, OrgGovBodyMemberListFormPageUpdateGoverningBody::class)
            BaseComponent.assertThat(memberListPage.heading).containsText("added 2 people")
            assertThat(memberListPage.summaryList.getRowByIndex(0).value).containsText("David Director")
            assertThat(memberListPage.summaryList.getRowByIndex(1).value).containsText("Omar Hassan")
            BaseComponent.assertThat(memberListPage.getChangeActionLink(0)).isVisible()
            BaseComponent.assertThat(memberListPage.getRemoveActionLink(0)).isVisible()

            memberListPage.getChangeActionLink(0).clickAndWait()

            val whoToProvidePage = assertPageIs(page, OrgGovBodyWhoToProvideFormPageUpdateGoverningBody::class)
            assertEquals("DIRECTOR", whoToProvidePage.form.radios.selectedValue)
            whoToProvidePage.submitWhoToProvide(GoverningBodyMemberType.TRUSTEE)

            val namePage = assertPageIs(page, OrgGovBodyMemberNameFormPageUpdateGoverningBody::class)
            BaseComponent.assertThat(namePage.form.nameInput).hasValue("David Director")
            namePage.submitName("Updated Trustee Name")

            val dobPage = assertPageIs(page, OrgGovBodyMemberDobFormPageUpdateGoverningBody::class)
            BaseComponent.assertThat(dobPage.form.dayInput).hasValue("18")
            BaseComponent.assertThat(dobPage.form.monthInput).hasValue("3")
            BaseComponent.assertThat(dobPage.form.yearInput).hasValue("1974")
            dobPage.submitDate("19", "4", "1980")

            val lookupAddressPage = assertPageIs(page, OrgGovBodyMemberLookupAddressFormPageUpdateGoverningBody::class)
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

            val selectAddressPage = assertPageIs(page, OrgGovBodyMemberSelectAddressFormPageUpdateGoverningBody::class)
            selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

            memberListPage = assertPageIs(page, OrgGovBodyMemberListFormPageUpdateGoverningBody::class)
            BaseComponent.assertThat(memberListPage.heading).containsText("added 2 people")
            assertThat(memberListPage.summaryList.getRowByIndex(0).key).containsText("Person 1")
            assertThat(memberListPage.summaryList.getRowByIndex(0).value).containsText("Updated Trustee Name")

            memberListPage.form.submit()

            val cyaPage = assertPageIs(page, GoverningBodyCyaPageUpdateGoverningBody::class)
            BaseComponent.assertThat(cyaPage.warning).isVisible()
            val firstCard = cyaPage.governingBodyMemberCard(0)
            BaseComponent.assertThat(firstCard.title).containsText("Trustee")
            val secondCard = cyaPage.governingBodyMemberCard(1)
            BaseComponent.assertThat(secondCard.title).containsText("Partner")

            // check that pressing Change on the CYA page can update the landlord
            firstCard.getAction("Change").link.clickAndWait()
            val changeMemberListPage = assertPageIs(page, OrgGovBodyMemberListFormPageUpdateGoverningBody::class)

            changeMemberListPage.getChangeActionLink(1).clickAndWait()
            val whoToProvideChangePage = assertPageIs(page, OrgGovBodyWhoToProvideFormPageUpdateGoverningBody::class)
            whoToProvideChangePage.submitWhoToProvide(GoverningBodyMemberType.DIRECTOR)

            val nameChangePage = assertPageIs(page, OrgGovBodyMemberNameFormPageUpdateGoverningBody::class)
            nameChangePage.submitName("Renamed Partner")

            val dobChangePage = assertPageIs(page, OrgGovBodyMemberDobFormPageUpdateGoverningBody::class)
            dobChangePage.submitDate("1", "1", "1990")

            val lookupAddressChangePage = assertPageIs(page, OrgGovBodyMemberLookupAddressFormPageUpdateGoverningBody::class)
            lookupAddressChangePage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

            val selectAddressChangePage = assertPageIs(page, OrgGovBodyMemberSelectAddressFormPageUpdateGoverningBody::class)
            selectAddressChangePage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")

            val memberListAfterEdit = assertPageIs(page, OrgGovBodyMemberListFormPageUpdateGoverningBody::class)
            assertThat(memberListAfterEdit.summaryList.getRowByIndex(1).value).containsText("Renamed Partner")
            memberListAfterEdit.form.submit()

            val cyaPageAfterChange = assertPageIs(page, GoverningBodyCyaPageUpdateGoverningBody::class)
            val updatedCyaCard = cyaPageAfterChange.governingBodyMemberCard(1)
            BaseComponent.assertThat(updatedCyaCard.title).containsText("Director")
            BaseComponent.assertThat(updatedCyaCard).containsText("Renamed Partner")
            cyaPageAfterChange.submit()

            orgLandlordDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
            orgLandlordDetailsPage.tabs.goToOrganisationContacts()
            assertEquals(2, orgLandlordDetailsPage.governingBodyMemberCardCount())
            val updatedCard = orgLandlordDetailsPage.governingBodyMemberCard("1. Trustee")
            assertThat(updatedCard.summaryList.roleRow.value).containsText("Trustee")
            assertThat(updatedCard.summaryList.nameRow.value).containsText("Updated Trustee Name")
            assertThat(updatedCard.summaryList.dateOfBirthRow.value).containsText("19 April 1980")
            assertThat(updatedCard.summaryList.addressRow.value).containsText("1 PRSDB Square")
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
            val cyaPage = assertPageIs(page, LeadTrusteeCyaPageUpdateLeadTrustee::class)
            assertThat(cyaPage.leadTrusteeCard.summaryList.nameRow.value).containsText(newName)
            assertThat(cyaPage.leadTrusteeCard.summaryList.dateOfBirthRow.value).containsText("15 June 1985")
            assertThat(cyaPage.leadTrusteeCard.summaryList.emailRow.value).containsText("updated.trustee@example.com")
            assertThat(cyaPage.leadTrusteeCard.summaryList.phoneRow.value).containsText("07999888777")
            assertThat(cyaPage.leadTrusteeCard.summaryList.addressRow.value).containsText("1 PRSDB Square")
            cyaPage.submit()

            // Back to landlord details
            val updatedDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
            updatedDetailsPage.tabs.goToOrganisationContacts()
            assertThat(updatedDetailsPage.leadTrusteeCard.summaryList.nameRow.value).containsText(newName)
        }
    }
}
