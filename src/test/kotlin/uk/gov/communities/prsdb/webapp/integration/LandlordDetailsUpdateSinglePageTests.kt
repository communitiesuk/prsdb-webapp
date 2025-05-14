package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LookupAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManualAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.NoAddressFoundFormPageUpdateLandlordDetails

@Sql("/data-local.sql")
class LandlordDetailsUpdateSinglePageTests : IntegrationTest() {
    @Nested
    inner class NameUpdates {
        @Test
        fun `A verified landlord cannot update their name`(page: Page) {
            // Check change link is hidden on details page
            val landlordDetailsPage = navigator.goToLandlordDetails()
            BaseComponent.assertThat(landlordDetailsPage.personalDetailsSummaryList.nameRow.actions.actionLink).isHidden()

            // Check update name page can't be reached
            navigator.navigateToLandlordDetailsUpdateNamePage()
            BasePage.assertPageIs(page, LandlordDetailsPage::class)
        }
    }

    @Nested
    inner class DateOfBirthUpdates {
        @Test
        fun `A verified landlord cannot update their date of birth`(page: Page) {
            // Check change link is hidden on details page
            val landlordDetailsPage = navigator.goToLandlordDetails()
            BaseComponent.assertThat(landlordDetailsPage.personalDetailsSummaryList.dateOfBirthRow.actions.actionLink).isHidden()

            // Check update date of birth page can't be reached
            navigator.navigateToLandlordDetailsUpdateDateOfBirthPage()
            BasePage.assertPageIs(page, LandlordDetailsPage::class)
        }
    }

    // TODO PRSD-355: Re-enable and update to match flow
    @Disabled
    @Nested
    inner class AddressUpdates {
        @Test
        fun `A landlord can search again via the Select Address page`(page: Page) {
            val selectAddressPage = navigator.skipToLandlordDetailsUpdateSelectAddressPage()
            selectAddressPage.searchAgain.clickAndWait()
            BasePage.assertPageIs(page, LookupAddressFormPageUpdateLandlordDetails::class)
        }

        @Test
        fun `A landlord can search again or choose manual address via the No Address Found page if no addresses are found`(page: Page) {
            // Arrange for no addresses to be found
            val houseNumber = "15"
            val postcode = "AB1 2CD"
            whenever(osPlacesClient.search(houseNumber, postcode)).thenReturn("{}")

            // Lookup Address page
            val lookupAddressPage = navigator.goToUpdateLandlordDetailsUpdateLookupAddressPage()
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber(postcode, houseNumber)
            var noAddressFoundPage = BasePage.assertPageIs(page, NoAddressFoundFormPageUpdateLandlordDetails::class)

            // No Address Found page
            BaseComponent.assertThat(noAddressFoundPage.heading).containsText(houseNumber)
            BaseComponent.assertThat(noAddressFoundPage.heading).containsText(postcode)

            // Search again
            noAddressFoundPage.searchAgain.clickAndWait()
            val lookupAddressPageAgain = BasePage.assertPageIs(page, LookupAddressFormPageUpdateLandlordDetails::class)
            lookupAddressPageAgain.submitPostcodeAndBuildingNameOrNumber(postcode, houseNumber)
            noAddressFoundPage = BasePage.assertPageIs(page, NoAddressFoundFormPageUpdateLandlordDetails::class)

            // Choose Manual Address
            noAddressFoundPage.form.submit()
            BasePage.assertPageIs(page, ManualAddressFormPageUpdateLandlordDetails::class)
        }
    }
}
