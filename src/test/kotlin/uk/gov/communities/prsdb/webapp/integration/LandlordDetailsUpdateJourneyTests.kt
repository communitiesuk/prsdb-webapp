package uk.gov.communities.prsdb.webapp.integration

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.LocatorAssertions
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LookupAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManualAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SelectAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.DateOfBirthFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.EmailFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.NameFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.NoAddressFoundFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.PhoneNumberFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.extensions.getFormattedUkPhoneNumber

@Sql("/data-local.sql")
class LandlordDetailsUpdateJourneyTests : IntegrationTest() {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()
    val addressFound = "Entirely new test address"

    @BeforeEach
    fun setup() {
        val addressJson2 =
            "{'DPA':{'ADDRESS':'2, Example Road, EG1 2AB'," +
                "'LOCAL_CUSTODIAN_CODE':114,'UPRN':'22','BUILDING_NUMBER':2,'POSTCODE':'EG1 2AB'}}"
        val addressJson3 =
            "{'DPA':{'ADDRESS':'3, Example Road, EG1 2AB'," +
                "'LOCAL_CUSTODIAN_CODE':116,'UPRN':'973','BUILDING_NUMBER':3,'POSTCODE':'EG1 2AB'}}"
        whenever(
            osPlacesClient.search(any(), any()),
        ).thenReturn(
            "{'results':[" +
                "{'DPA':{'ADDRESS':'$addressFound','LOCAL_CUSTODIAN_CODE':28,'UPRN':'7923','BUILDING_NUMBER':9,'POSTCODE':'EG1 2AB'}}," +
                "$addressJson2,$addressJson3,]}",
        )
    }

    @Nested
    inner class NameUpdates {
        @Sql("/data-unverified-landlord.sql")
        @Test
        fun `An unverified landlord can update their name`(page: Page) {
            // Details page
            var landlordDetailsPage = navigator.goToLandlordDetails()
            landlordDetailsPage.personalDetailsSummaryList.nameRow.actions.actionLink
                .clickAndWait()
            val updateNamePage = assertPageIs(page, NameFormPageUpdateLandlordDetails::class)

            // Update Name page
            val newName = "new landlord name"
            updateNamePage.submitName(newName)
            landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

            // Check changes have occurred
            assertThat(landlordDetailsPage.personalDetailsSummaryList.nameRow.value).containsText(newName)
        }

        @Test
        fun `A verified landlord cannot update their name`(page: Page) {
            // Check change link is hidden on details page
            val landlordDetailsPage = navigator.goToLandlordDetails()
            assertThat(landlordDetailsPage.personalDetailsSummaryList.nameRow.actions.actionLink).isHidden()

            // Check update name page can't be reached
            navigator.skipToLandlordDetailsUpdateNamePage()
            assertPageIs(page, LandlordDetailsPage::class)
        }
    }

    @Nested
    inner class DateOfBirthUpdates {
        @Sql("/data-unverified-landlord.sql")
        @Test
        fun `An unverified landlord can update their date of birth`(page: Page) {
            // Details page
            var landlordDetailsPage = navigator.goToLandlordDetails()
            landlordDetailsPage.personalDetailsSummaryList.dateOfBirthRow.actions.actionLink
                .clickAndWait()
            val updateDateOfBirthPage = assertPageIs(page, DateOfBirthFormPageUpdateLandlordDetails::class)

            // Update DOB page
            val newDateOfBirth = LocalDate(1990, 1, 1)
            updateDateOfBirthPage.submitDate(newDateOfBirth)
            landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

            // Check changes have occurred
            assertThat(landlordDetailsPage.personalDetailsSummaryList.dateOfBirthRow.value)
                .containsText(formatDateOfBirth(newDateOfBirth), LocatorAssertions.ContainsTextOptions().setIgnoreCase(true))
        }

        @Test
        fun `A verified landlord cannot update their date of birth`(page: Page) {
            // Check change link is hidden on details page
            val landlordDetailsPage = navigator.goToLandlordDetails()
            assertThat(landlordDetailsPage.personalDetailsSummaryList.dateOfBirthRow.actions.actionLink).isHidden()

            // Check update date of birth page can't be reached
            navigator.skipToLandlordDetailsUpdateDateOfBirthPage()
            assertPageIs(page, LandlordDetailsPage::class)
        }
    }

    @Nested
    inner class EmailUpdates {
        @Test
        fun `A landlord can update their email address`(page: Page) {
            // Details page
            var landlordDetailsPage = navigator.goToLandlordDetails()
            landlordDetailsPage.personalDetailsSummaryList.emailRow.actions.actionLink
                .clickAndWait()
            val updateEmailPage = assertPageIs(page, EmailFormPageUpdateLandlordDetails::class)

            // Update Email page
            val newEmail = "newEmail@test.com"
            updateEmailPage.submitEmail(newEmail)
            landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

            // Check changes have occurred
            assertThat(landlordDetailsPage.personalDetailsSummaryList.emailRow.value).containsText(newEmail)
        }
    }

    // TODO PRSD-1105: Re-enable and update to match flow
    @Disabled
    @Nested
    inner class PhoneNumberUpdates {
        @Test
        fun `A landlord can update their phone number`(page: Page) {
            // Details page
            var landlordDetailsPage = navigator.goToLandlordDetails()
            landlordDetailsPage.personalDetailsSummaryList.phoneNumberRow.actions.actionLink
                .clickAndWait()
            val updatePhoneNumberPage = assertPageIs(page, PhoneNumberFormPageUpdateLandlordDetails::class)

            // Update Phone Number page
            val newPhoneNumber = phoneNumberUtil.getFormattedUkPhoneNumber()
            updatePhoneNumberPage.submitPhoneNumber(newPhoneNumber)
            landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

            // Check changes have occurred
            assertThat(landlordDetailsPage.personalDetailsSummaryList.phoneNumberRow.value).containsText(newPhoneNumber)
        }
    }

    // TODO PRSD-355: Re-enable and update to match flow
    @Disabled
    @Nested
    inner class AddressUpdates {
        @Test
        fun `A landlord can update their address (selected)`(page: Page) {
            // Details page
            var landlordDetailsPage = navigator.goToLandlordDetails()
            landlordDetailsPage.personalDetailsSummaryList.addressRow.actions.actionLink
                .clickAndWait()
            val lookupAddressPage = assertPageIs(page, LookupAddressFormPageUpdateLandlordDetails::class)

            // Lookup Address page
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG", "5")
            val selectAddressPage = assertPageIs(page, SelectAddressFormPageUpdateLandlordDetails::class)

            // Select Address page
            val newSelectedAddress = addressFound
            selectAddressPage.selectAddressAndSubmit(newSelectedAddress)
            landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

            // Check changes have occurred
            assertThat(landlordDetailsPage.personalDetailsSummaryList.addressRow.value).containsText(newSelectedAddress)
        }

        @Test
        fun `A landlord can update their address (manual)`(page: Page) {
            // Details page
            var landlordDetailsPage = navigator.goToLandlordDetails()
            landlordDetailsPage.personalDetailsSummaryList.addressRow.actions.actionLink
                .clickAndWait()
            val lookupAddressPage = assertPageIs(page, LookupAddressFormPageUpdateLandlordDetails::class)

            // Lookup Address page
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG", "5")
            val selectAddressPage = assertPageIs(page, SelectAddressFormPageUpdateLandlordDetails::class)

            // Select Address page
            selectAddressPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)
            val manualAddressPage = assertPageIs(page, ManualAddressFormPageUpdateLandlordDetails::class)

            // Manual Address page
            val newFirstLine = "3 Example Road"
            val newTown = "Vilton"
            val newPostcode = "AB1 9YZ"
            manualAddressPage.submitAddress(newFirstLine, townOrCity = newTown, postcode = newPostcode)
            landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

            // Check changes have occurred
            val newSingleLineAddress = AddressDataModel.manualAddressDataToSingleLineAddress(newFirstLine, newTown, newPostcode)
            assertThat(landlordDetailsPage.personalDetailsSummaryList.addressRow.value).containsText(newSingleLineAddress)
        }

        @Test
        fun `A landlord can search again via the Select Address page`(page: Page) {
            val selectAddressPage = navigator.goToLandlordRegistrationSelectAddressPage()
            selectAddressPage.searchAgain.clickAndWait()
            assertPageIs(page, LookupAddressFormPageUpdateLandlordDetails::class)
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
            var noAddressFoundPage = assertPageIs(page, NoAddressFoundFormPageUpdateLandlordDetails::class)

            // No Address Found page
            assertThat(noAddressFoundPage.heading).containsText(houseNumber)
            assertThat(noAddressFoundPage.heading).containsText(postcode)

            // Search again
            noAddressFoundPage.searchAgain.clickAndWait()
            val lookupAddressPageAgain = assertPageIs(page, LookupAddressFormPageUpdateLandlordDetails::class)
            lookupAddressPageAgain.submitPostcodeAndBuildingNameOrNumber(postcode, houseNumber)
            noAddressFoundPage = assertPageIs(page, NoAddressFoundFormPageUpdateLandlordDetails::class)

            // Choose Manual Address
            noAddressFoundPage.form.submit()
            assertPageIs(page, ManualAddressFormPageUpdateLandlordDetails::class)
        }
    }

    private fun formatDateOfBirth(date: LocalDate): String = "${date.dayOfMonth} ${date.month} ${date.year}"
}
