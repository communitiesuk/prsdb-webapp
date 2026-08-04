package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.AlreadyRegisteredFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LookupAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ManualAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NoAddressFoundFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

// Covers the property registration journey with the restructure-and-skipping feature flag OFF.
// TODO PDJB-1340: delete every PropertyRegistrationPreRestructure*SinglePageTests file when
// PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING is removed. Every class here has an equivalent
// in the flag-on files, so they can all be removed wholesale.
class PropertyRegistrationPreRestructureAddressSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun disableRestructureAndSkippingFlag() {
        featureFlagManager.disableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
    }

    @Nested
    inner class LookupAddressAndNoAddressFoundSteps {
        @Test
        fun `Submitting with empty data fields returns an error`(page: Page) {
            val lookupAddressPage = navigator.goToPropertyRegistrationLookupAddressPage()
            lookupAddressPage.clearForm() // There may be form answers in the journey state
            lookupAddressPage.form.submit()
            assertThat(lookupAddressPage.form.getErrorMessage("postcode")).containsText("Enter a postcode")
            assertThat(lookupAddressPage.form.getErrorMessage("houseNameOrNumber")).containsText("Enter a house name or number")
        }

        @Test
        fun `If no English addresses are found, user can search again or enter address manually via the No Address Found step`(
            page: Page,
        ) {
            // Lookup address finds no English results
            val houseNumber = "NOT A HOUSE NUMBER"
            val postcode = "NOT A POSTCODE"
            var lookupAddressPage = navigator.goToPropertyRegistrationLookupAddressPage()
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber(postcode, houseNumber)

            // redirect to noAddressFoundPage
            var noAddressFoundPage = assertPageIs(page, NoAddressFoundFormPagePropertyRegistration::class)
            BaseComponent
                .assertThat(noAddressFoundPage.heading)
                .containsText("No matching address in England found for $postcode and $houseNumber")

            // Search Again
            noAddressFoundPage.searchAgain.clickAndWait()
            lookupAddressPage = assertPageIs(page, LookupAddressFormPagePropertyRegistration::class)
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber(postcode, houseNumber)

            // Submit no address found page
            noAddressFoundPage = assertPageIs(page, NoAddressFoundFormPagePropertyRegistration::class)
            noAddressFoundPage.form.submit()
            assertPageIs(page, ManualAddressFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class SelectAddressStep {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val selectAddressPage = navigator.skipToPropertyRegistrationSelectAddressPage()
            selectAddressPage.form.submit()
            assertThat(selectAddressPage.form.getErrorMessage()).containsText("Select an address")
        }

        @Test
        fun `Clicking Search Again navigates to the previous step`(page: Page) {
            val selectAddressPage = navigator.skipToPropertyRegistrationSelectAddressPage()
            selectAddressPage.searchAgain.clickAndWait()
            assertPageIs(page, LookupAddressFormPagePropertyRegistration::class)
        }

        @Test
        fun `Selecting an already-registered address navigates to the AlreadyRegistered step`(page: Page) {
            val alreadyRegisteredAddress = AddressDataModel("1 Example Road", uprn = 1123456)
            val selectAddressPage = navigator.skipToPropertyRegistrationSelectAddressPage(listOf(alreadyRegisteredAddress))
            selectAddressPage.selectAddressAndSubmit(alreadyRegisteredAddress.singleLineAddress)
            assertPageIs(page, AlreadyRegisteredFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class ManualAddressEntryStep {
        @Test
        fun `Submitting empty data fields returns errors`(page: Page) {
            val manualAddressPage = navigator.skipToPropertyRegistrationManualAddressPage()
            manualAddressPage.submitAddress()
            assertThat(manualAddressPage.form.getErrorMessage("addressLineOne"))
                .containsText("Enter the first line of an address, typically the building and street")
            assertThat(manualAddressPage.form.getErrorMessage("townOrCity")).containsText("Enter town or city")
            assertThat(manualAddressPage.form.getErrorMessage("postcode")).containsText("Enter postcode")
        }
    }

    @Nested
    inner class SelectLocalCouncilStep {
        @Test
        fun `Submitting without selecting an LA return an error`(page: Page) {
            val selectLocalCouncilPage = navigator.skipToPropertyRegistrationSelectLocalCouncilPage()
            selectLocalCouncilPage.form.submit()
            assertThat(selectLocalCouncilPage.form.getErrorMessage("localCouncilId"))
                .containsText("Select a local council to continue")
        }
    }
}
