package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.OrgLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.OrgLookupAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.OrgManualAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.OrgSelectAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgNameFormPageUpdateLandlordDetails
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

        val lookupAddressPage = assertPageIs(page, OrgLookupAddressFormPageUpdateLandlordDetails::class)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val newSelectedAddress = "1 PRSDB Square, EG1 2AA"
        val selectAddressPage = assertPageIs(page, OrgSelectAddressFormPageUpdateLandlordDetails::class)
        BaseComponent.assertThat(selectAddressPage.warning).isVisible()
        selectAddressPage.selectAddressAndSubmit(newSelectedAddress)

        val updatedDetailsPage = assertPageIs(page, OrgLandlordDetailsPage::class)
        assertThat(updatedDetailsPage.mainContent).containsText(newSelectedAddress)
    }

    @Test
    fun `An organisation landlord can update their organisation address (manual) and return to details page`(page: Page) {
        val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
        orgLandlordDetailsPage.clickOrganisationAddressChangeLinkAndWait()

        val lookupAddressPage = assertPageIs(page, OrgLookupAddressFormPageUpdateLandlordDetails::class)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")

        val selectAddressPage = assertPageIs(page, OrgSelectAddressFormPageUpdateLandlordDetails::class)
        selectAddressPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)

        val newFirstLine = "3 Example Road"
        val newTown = "Vilton"
        val newPostcode = "AB1 9YZ"
        val manualAddressPage = assertPageIs(page, OrgManualAddressFormPageUpdateLandlordDetails::class)
        BaseComponent.assertThat(manualAddressPage.warning).isVisible()
        manualAddressPage.submitAddress(newFirstLine, townOrCity = newTown, postcode = newPostcode)

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
}
