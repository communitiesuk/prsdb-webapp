package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.CORRESPONDENCE_ADDRESS
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CorrespondenceSelectAddressFormPagePropertyRegistration
import java.util.regex.Pattern

class PropertyRegistrationCorrespondenceSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun enableFeatureFlags() {
        featureFlagManager.enableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        featureFlagManager.enableFeature(CORRESPONDENCE_ADDRESS)
    }

    @Nested
    inner class CorrespondenceLookupAddressStep {
        @Test
        fun `The page shows the guidance content from the design`() {
            val lookupAddressPage = navigator.skipToPropertyRegistrationCorrespondenceLookupAddressPage()

            BaseComponent.assertThat(lookupAddressPage.form.sectionHeader).containsText("Who the council should contact")
            BaseComponent
                .assertThat(lookupAddressPage.heading)
                .containsText("Where the council should send post about this property")
            BaseComponent
                .assertThat(lookupAddressPage.paragraph("The council will use this address if they need to send you anything by post."))
                .isVisible()
            BaseComponent.assertThat(lookupAddressPage.choosingAPostalAddressHeading).containsText("Choosing a postal address")
            BaseComponent
                .assertThat(lookupAddressPage.insetText)
                .containsText("This address must be in England or Wales. It must not be a PO Box.")
            BaseComponent
                .assertThat(lookupAddressPage.paragraph("You can use the residential address of any landlord."))
                .isVisible()
            BaseComponent
                .assertThat(lookupAddressPage.paragraph("You can also use the address of your:"))
                .isVisible()
            assertThat(lookupAddressPage.bulletPointList.items)
                .containsText(arrayOf("letting agent", "property manager", "solicitor"))
            BaseComponent
                .assertThat(lookupAddressPage.paragraph("Do not use the address of the rental property."))
                .isVisible()
        }

        @Test
        fun `The lookup fieldset uses a medium legend and the correspondence specific labels`() {
            val lookupAddressPage = navigator.skipToPropertyRegistrationCorrespondenceLookupAddressPage()

            BaseComponent.assertThat(lookupAddressPage.form.fieldsetLegend).containsText("Find an address")
            BaseComponent
                .assertThat(lookupAddressPage.form.fieldsetLegend)
                .hasClass(Pattern.compile(".*govuk-fieldset__legend--m.*"))
            assertThat(lookupAddressPage.postcodeLabel).containsText("Postcode")
            assertThat(lookupAddressPage.houseNameOrNumberLabel).containsText("House name or number")
        }

        @Test
        fun `Submitting with empty data fields returns an error`() {
            val lookupAddressPage = navigator.skipToPropertyRegistrationCorrespondenceLookupAddressPage()
            lookupAddressPage.clearForm() // There may be form answers in the journey state
            lookupAddressPage.form.submit()

            assertThat(lookupAddressPage.form.getErrorMessage("postcode")).containsText("Enter a postcode")
            assertThat(lookupAddressPage.form.getErrorMessage("houseNameOrNumber")).containsText("Enter a house name or number")
        }

        @Test
        fun `Submitting a valid address continues to the correspondence select address page`(page: Page) {
            val lookupAddressPage = navigator.skipToPropertyRegistrationCorrespondenceLookupAddressPage()
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("FA1 1AA", "1")

            assertPageIs(page, CorrespondenceSelectAddressFormPagePropertyRegistration::class)
        }
    }
}
