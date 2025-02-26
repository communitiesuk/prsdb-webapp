package uk.gov.communities.prsdb.webapp.integration

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.helpers.getFormattedUkPhoneNumber
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordUpdateDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LookupAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManualAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SelectAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.EmailFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.NameFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.PhoneNumberFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@Sql("/data-local.sql")
class UpdateLandlordDetailsJourneyTests : IntegrationTest() {
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

    @Test
    fun `A Landlord can update all of their details on the Update Details Journey`(page: Page) {
        // Update details page
        var landlordDetailsUpdatePage = navigator.goToUpdateLandlordDetailsPage()
        assertThat(landlordDetailsUpdatePage.heading).containsText("Alexander Smith")

        val landlordName = "landlord name"
        landlordDetailsUpdatePage = updateLandlordNameAndReturn(landlordDetailsUpdatePage, landlordName)

        val landlordEmail = "new@email.test"
        landlordDetailsUpdatePage = updateLandlordEmailAndReturn(landlordDetailsUpdatePage, landlordEmail)

        val landlordPhoneNumber = phoneNumberUtil.getFormattedUkPhoneNumber()
        landlordDetailsUpdatePage = updateLandlordPhoneNumberAndReturn(landlordDetailsUpdatePage, landlordPhoneNumber)

        val selectedAddress = addressFound
        landlordDetailsUpdatePage = updateLandlordAddressAndReturn(landlordDetailsUpdatePage, selectedAddress)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        landlordDetailsUpdatePage.submitButton.clickAndWait()
        val landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

        // Check changes have occurred
        assertThat(landlordDetailsPage.personalDetailsSummaryList.nameRow.value).containsText(landlordName)
        assertThat(landlordDetailsPage.personalDetailsSummaryList.emailRow.value).containsText(landlordEmail)
        assertThat(landlordDetailsPage.personalDetailsSummaryList.phoneNumberRow.value).containsText(landlordPhoneNumber)
        assertThat(landlordDetailsPage.personalDetailsSummaryList.addressRow.value).containsText(selectedAddress)
    }

    @Test
    fun `A Landlord can update just their name on the Update Details Journey`(page: Page) {
        // Update details page
        var landlordDetailsUpdatePage = navigator.goToUpdateLandlordDetailsPage()
        assertThat(landlordDetailsUpdatePage.heading).containsText("Alexander Smith")

        val landlordName = "landlord name"
        landlordDetailsUpdatePage = updateLandlordNameAndReturn(landlordDetailsUpdatePage, landlordName)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        landlordDetailsUpdatePage.submitButton.clickAndWait()
        val landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

        // Check changes have occurred
        assertThat(landlordDetailsPage.personalDetailsSummaryList.nameRow.value).containsText(landlordName)
    }

    @Test
    fun `A Landlord can update just their email on the Update Details Journey`(page: Page) {
        // Update details page
        var landlordDetailsUpdatePage = navigator.goToUpdateLandlordDetailsPage()
        assertThat(landlordDetailsUpdatePage.heading).containsText("Alexander Smith")
        val landlordEmail = "new@email.test"
        landlordDetailsUpdatePage = updateLandlordEmailAndReturn(landlordDetailsUpdatePage, landlordEmail)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        landlordDetailsUpdatePage.submitButton.clickAndWait()
        val landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

        // Check changes have occurred
        assertThat(landlordDetailsPage.personalDetailsSummaryList.emailRow.value).containsText(landlordEmail)
    }

    @Test
    fun `A Landlord can update just their phone number on the Update Details Journey`(page: Page) {
        // Update details page
        var landlordDetailsUpdatePage = navigator.goToUpdateLandlordDetailsPage()
        assertThat(landlordDetailsUpdatePage.heading).containsText("Alexander Smith")
        val landlordPhoneNumber = phoneNumberUtil.getFormattedUkPhoneNumber()
        landlordDetailsUpdatePage = updateLandlordPhoneNumberAndReturn(landlordDetailsUpdatePage, landlordPhoneNumber)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        landlordDetailsUpdatePage.submitButton.clickAndWait()
        val landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

        // Check changes have occurred
        assertThat(landlordDetailsPage.personalDetailsSummaryList.phoneNumberRow.value).containsText(landlordPhoneNumber)
    }

    @Nested
    inner class AddressUpdates {
        @Test
        fun `A Landlord can update their address to a manually entered address`(page: Page) {
            // Update details page
            var landlordDetailsUpdatePage = navigator.goToUpdateLandlordDetailsPage()
            assertThat(landlordDetailsUpdatePage.heading).containsText("Alexander Smith")

            val newFirstLine = "3 Example Road"
            val newTown = "Vilton"
            val newPostcode = "AB1 9YZ"
            landlordDetailsUpdatePage = updateLandlordAddressAndReturn(landlordDetailsUpdatePage, null, newFirstLine, newTown, newPostcode)

            // Submit changes TODO PRSD-355 add proper submit button and declaration page
            landlordDetailsUpdatePage.submitButton.clickAndWait()
            val landlordDetailsPage = assertPageIs(page, LandlordDetailsPage::class)

            // Check changes have occurred
            val newSingleLineAddress = AddressDataModel.manualAddressDataToSingleLineAddress(newFirstLine, newTown, newPostcode)
            assertThat(landlordDetailsPage.personalDetailsSummaryList.addressRow.value).containsText(newSingleLineAddress)
        }
    }

    private fun updateLandlordNameAndReturn(
        detailsPage: LandlordUpdateDetailsPage,
        newName: String,
    ): LandlordUpdateDetailsPage {
        val page = detailsPage.page
        detailsPage.personalDetailsSummaryList.nameRow.actions.actionLink
            .clickAndWait()
        val updateNamePage = assertPageIs(page, NameFormPageUpdateLandlordDetails::class)

        updateNamePage.submitName(newName)
        return assertPageIs(page, LandlordUpdateDetailsPage::class)
    }

    private fun updateLandlordEmailAndReturn(
        detailsPage: LandlordUpdateDetailsPage,
        newEmail: String,
    ): LandlordUpdateDetailsPage {
        val page = detailsPage.page
        detailsPage.personalDetailsSummaryList.emailRow.actions.actionLink
            .clickAndWait()
        val updateEmailPage = assertPageIs(page, EmailFormPageUpdateLandlordDetails::class)

        updateEmailPage.submitEmail(newEmail)
        return assertPageIs(page, LandlordUpdateDetailsPage::class)
    }

    private fun updateLandlordPhoneNumberAndReturn(
        detailsPage: LandlordUpdateDetailsPage,
        newPhoneNumber: String,
    ): LandlordUpdateDetailsPage {
        val page = detailsPage.page
        detailsPage.personalDetailsSummaryList.phoneNumberRow.actions.actionLink
            .clickAndWait()
        val updatePhoneNumberPage = assertPageIs(page, PhoneNumberFormPageUpdateLandlordDetails::class)

        updatePhoneNumberPage.submitPhoneNumber(newPhoneNumber)
        return assertPageIs(page, LandlordUpdateDetailsPage::class)
    }

    private fun updateLandlordAddressAndReturn(
        detailsPage: LandlordUpdateDetailsPage,
        addressSelected: String?,
        firstLine: String? = null,
        town: String? = null,
        postcode: String? = null,
    ): LandlordUpdateDetailsPage {
        val page = detailsPage.page
        detailsPage.personalDetailsSummaryList.addressRow.actions.actionLink
            .clickAndWait()
        val updateAddressPage = assertPageIs(page, LookupAddressFormPageUpdateLandlordDetails::class)

        updateAddressPage.submitPostcodeAndBuildingNameOrNumber("EG", "5")
        val selectAddressPage = assertPageIs(page, SelectAddressFormPageUpdateLandlordDetails::class)

        if (addressSelected != null) {
            selectAddressPage.selectAddressAndSubmit(addressSelected)
        } else {
            selectAddressPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)
            val manualAddressPage = assertPageIs(page, ManualAddressFormPageUpdateLandlordDetails::class)
            manualAddressPage.submitAddress(firstLine, townOrCity = town, postcode = postcode)
        }
        return assertPageIs(page, LandlordUpdateDetailsPage::class)
    }
}
