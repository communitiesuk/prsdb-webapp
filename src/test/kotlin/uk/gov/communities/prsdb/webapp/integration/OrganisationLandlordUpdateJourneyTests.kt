package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.OrgLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgLookupAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgManualAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgNameFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgNoAddressFoundFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgSelectAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

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
        assertThat(updatedDetailsPage.mainContent).containsText(newSelectedAddress)
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
        assertThat(updatedDetailsPage.mainContent).containsText(newSingleLineAddress)
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
}
