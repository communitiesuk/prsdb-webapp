package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent

// Covers the property registration journey with the restructure-and-skipping feature flag OFF.
// TODO PDJB-1340: delete every PropertyRegistrationPreRestructure*SinglePageTests file when
// PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING is removed. Every class here has an equivalent
// in the flag-on files, so they can all be removed wholesale.
class PropertyRegistrationPreRestructureRentAndBillsSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun disableRestructureAndSkippingFlag() {
        featureFlagManager.disableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
    }

    @Nested
    inner class RentIncludesBillsStep {
        @Test
        fun `Submitting with no rent included option selected returns an error`(page: Page) {
            val rentIncludesBillsPage = navigator.skipToPropertyRegistrationRentIncludesBillsPage()
            rentIncludesBillsPage.form.submit()
            assertThat(rentIncludesBillsPage.form.getErrorMessage()).containsText("Select whether the rent includes bills")
        }
    }

    @Nested
    inner class BillsIncludedStep {
        @Test
        fun `Submitting with no bills included selected returns an error`(page: Page) {
            val billsIncludedPage = navigator.skipToPropertyRegistrationBillsIncludedPage()
            billsIncludedPage.form.submit()
            assertThat(billsIncludedPage.form.getErrorMessage()).containsText("Select what you include in the rent")
        }

        @Test
        fun `Submitting with something else selected but no text entered returns an error`(page: Page) {
            val billsIncludedPage = navigator.skipToPropertyRegistrationBillsIncludedPage()
            billsIncludedPage.selectGasElectricityWater()
            billsIncludedPage.selectSomethingElseCheckbox()
            billsIncludedPage.form.submit()
            assertThat(billsIncludedPage.form.getErrorMessage()).containsText("Enter the bills and services you include in the rent")
        }

        @Test
        fun `Submitting with a very long something else text returns an error`(page: Page) {
            val billsIncludedPage = navigator.skipToPropertyRegistrationBillsIncludedPage()
            billsIncludedPage.selectGasElectricityWater()
            billsIncludedPage.selectSomethingElseCheckbox()
            val aVeryLongString =
                "This string is very long, so long that it is not feasible that it is a real description " +
                    "- therefore if it is submitted there will in fact be an error rather than a successful submission." +
                    " It is actually quite difficult for a string to be long enough to trigger this error, because the" +
                    " maximum length has been selected to be permissive of descriptions we do not expect while still having " +
                    "a cap reachable with a little effort."
            billsIncludedPage.fillCustomBills(aVeryLongString)
            billsIncludedPage.form.submit()
            assertThat(billsIncludedPage.form.getErrorMessage("customBillsIncluded"))
                .containsText("The description of other bills and services must be 200 characters or fewer")
        }
    }

    @Nested
    inner class FurnishedStatusStep {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val furnishedStatusPage = navigator.skipToPropertyRegistrationFurnishedStatusPage()
            furnishedStatusPage.form.submit()
            assertThat(
                furnishedStatusPage.form.getErrorMessage(),
            ).containsText("Select whether the property is furnished, partly furnished or unfurnished")
        }
    }

    @Nested
    inner class RentFrequencyStep {
        @Test
        fun `Submitting with no rentFrequency selected returns an error`(page: Page) {
            val rentFrequencyPage = navigator.skipToPropertyRegistrationRentFrequencyPage()
            rentFrequencyPage.form.submit()
            assertThat(rentFrequencyPage.form.getErrorMessage()).containsText("Select how often you charge rent")
        }

        @Test
        fun `Submitting with other rent frequency selected but no text entered returns an error`(page: Page) {
            val rentFrequencyPage = navigator.skipToPropertyRegistrationRentFrequencyPage()
            rentFrequencyPage.selectRentFrequency(RentFrequency.OTHER)
            rentFrequencyPage.form.submit()
            assertThat(rentFrequencyPage.form.getErrorMessage()).containsText("Enter how often you charge rent")
        }
    }

    @Nested
    inner class RentAmountStep {
        @Test
        fun `Submitting no rentAmount returns an error`(page: Page) {
            val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage()
            rentAmountPage.form.submit()
            assertThat(
                rentAmountPage.form.getErrorMessage(),
            ).containsText("Rent amount must only include numbers (and a decimal point), like 600 or 193.54")
        }

        @Test
        fun `Submitting a rentAmount greater than two decimals returns an error`(page: Page) {
            val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage()
            rentAmountPage.submitRentAmount("400.123")
            assertThat(
                rentAmountPage.form.getErrorMessage(),
            ).containsText("Rent amount must only include numbers (and a decimal point), like 600 or 193.54")
        }

        @Test
        fun `Submitting a negative rentAmount returns an error`(page: Page) {
            val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage()
            rentAmountPage.submitRentAmount("-400.12")
            assertThat(
                rentAmountPage.form.getErrorMessage(),
            ).containsText("Rent amount must only include numbers (and a decimal point), like 600 or 193.54")
        }

        @Test
        fun `Submitting a non-numerical rentAmount returns an error`(page: Page) {
            val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage()
            rentAmountPage.submitRentAmount("not-a-number")
            assertThat(
                rentAmountPage.form.getErrorMessage(),
            ).containsText("Rent amount must only include numbers (and a decimal point), like 600 or 193.54")
        }

        @Test
        fun `Submitting a rentAmount of 10000000 or above returns an error`(page: Page) {
            val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage()
            rentAmountPage.submitRentAmount("10000000")
            assertThat(
                rentAmountPage.form.getErrorMessage(),
            ).containsText("Rent amount must only include numbers (and a decimal point), like 600 or 193.54")
        }

        @Nested
        inner class ConditionalContentPerRentFrequency {
            @Test
            fun `Page renders correctly for weekly rent frequency`(page: Page) {
                val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage(RentFrequency.WEEKLY)
                BaseComponent
                    .assertThat(rentAmountPage.header)
                    .containsText("What is the weekly rent?")
                BaseComponent
                    .assertThat(rentAmountPage.subheading)
                    .containsText("Weekly rent")
                BaseComponent
                    .assertThat(rentAmountPage.billsExplanationForRentFrequency)
                    .containsText("The amount you enter must be the total weekly rent agreed with the tenant.")
                BaseComponent.assertThat(rentAmountPage.rentCalculationParagraph).isHidden()
            }

            @Test
            fun `Page renders correctly for four weekly rent frequency`(page: Page) {
                val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage(RentFrequency.FOUR_WEEKLY)
                BaseComponent
                    .assertThat(rentAmountPage.header)
                    .containsText("What is the 4-weekly rent?")
                BaseComponent
                    .assertThat(rentAmountPage.subheading)
                    .containsText("4-weekly rent")
                BaseComponent
                    .assertThat(rentAmountPage.billsExplanationForRentFrequency)
                    .containsText("The amount you enter must be the total 4-weekly rent agreed with the tenant.")
                BaseComponent.assertThat(rentAmountPage.rentCalculationParagraph).isHidden()
            }

            @Test
            fun `Page renders correctly for monthly rent frequency`(page: Page) {
                val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage(RentFrequency.MONTHLY)
                BaseComponent
                    .assertThat(rentAmountPage.header)
                    .containsText("What is the monthly rent?")
                BaseComponent
                    .assertThat(rentAmountPage.subheading)
                    .containsText("Monthly rent")
                BaseComponent
                    .assertThat(rentAmountPage.billsExplanationForRentFrequency)
                    .containsText("The amount you enter must be the total monthly rent agreed with the tenant.")
                BaseComponent.assertThat(rentAmountPage.rentCalculationParagraph).isHidden()
            }

            @Test
            fun `Page renders correctly for 'other' rent frequency`(page: Page) {
                val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage(RentFrequency.OTHER)
                BaseComponent
                    .assertThat(rentAmountPage.header)
                    .containsText("What is the monthly rent?")
                BaseComponent
                    .assertThat(rentAmountPage.subheading)
                    .containsText("Monthly rent")
                BaseComponent
                    .assertThat(rentAmountPage.billsExplanationForRentFrequency)
                    .containsText("The amount you enter must be the total monthly rent agreed with the tenant.")
                BaseComponent.assertThat(rentAmountPage.rentCalculationParagraph).isVisible()
            }
        }
    }
}
