package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NumberOfPeopleFormPagePropertyRegistration

// Covers the property registration journey with the restructure-and-skipping feature flag OFF.
// TODO PDJB-1340: delete every PropertyRegistrationPreRestructure*SinglePageTests file when
// PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING is removed. Every class here has an equivalent
// in the flag-on files, so they can all be removed wholesale.
class PropertyRegistrationPreRestructureOccupancySinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun disableRestructureAndSkippingFlag() {
        featureFlagManager.disableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
    }

    @Nested
    inner class OccupancyStep {
        @Test
        fun `Submitting with no occupancy option selected returns an error`(page: Page) {
            val occupancyPage = navigator.skipToPropertyRegistrationOccupancyPage()
            occupancyPage.form.submit()
            assertThat(occupancyPage.form.getErrorMessage()).containsText("Select whether the property is occupied")
        }
    }

    @Nested
    inner class NumberOfHouseholdsStep {
        @Test
        fun `Submitting with a blank numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.skipToPropertyRegistrationHouseholdsPage()
            householdsPage.form.submit()
            assertThat(householdsPage.form.getErrorMessage()).containsText("Enter how many separate households, like 1 or 2")
        }

        @Test
        fun `Submitting with a non-numerical value in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.skipToPropertyRegistrationHouseholdsPage()
            householdsPage.submitNumberOfHouseholds("not-a-number")
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Enter how many separate households, like 1 or 2")
        }

        @Test
        fun `Submitting with a non-integer number in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.skipToPropertyRegistrationHouseholdsPage()
            householdsPage.submitNumberOfHouseholds("2.3")
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Enter how many separate households, like 1 or 2")
        }

        @Test
        fun `Submitting with a negative integer in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.skipToPropertyRegistrationHouseholdsPage()
            householdsPage.submitNumberOfHouseholds(-2)
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Enter how many separate households, like 1 or 2")
        }

        @Test
        fun `Submitting with a zero integer in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.skipToPropertyRegistrationHouseholdsPage()
            householdsPage.submitNumberOfHouseholds(0)
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Enter how many separate households, like 1 or 2")
        }
    }

    @Nested
    inner class NumberOfPeopleStep {
        @Test
        fun `Submitting with a blank numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.skipToPropertyRegistrationPeoplePage()
            peoplePage.form.submit()
            assertThat(peoplePage.form.getErrorMessage()).containsText("Enter how many people, like 2 or 5")
        }

        @Test
        fun `Submitting with a non-numerical value in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.skipToPropertyRegistrationPeoplePage()
            peoplePage.submitNumOfPeople("not-a-number")
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Enter how many people, like 2 or 5")
        }

        @Test
        fun `Submitting with a non-integer number in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.skipToPropertyRegistrationPeoplePage()
            peoplePage.submitNumOfPeople("2.3")
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Enter how many people, like 2 or 5")
        }

        @Test
        fun `Submitting with a negative integer in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.skipToPropertyRegistrationPeoplePage()
            peoplePage.submitNumOfPeople("-2")
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Enter how many people, like 2 or 5")
        }

        @Test
        fun `Submitting with a zero integer in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.skipToPropertyRegistrationPeoplePage()
            peoplePage.submitNumOfPeople(0)
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Enter how many people, like 2 or 5")
        }

        @Test
        fun `Submitting with an integer in the numberOfPeople field that is less than the numberOfHouseholds returns an error`(
            page: Page,
        ) {
            val householdsPage = navigator.skipToPropertyRegistrationHouseholdsPage()
            householdsPage.submitNumberOfHouseholds(3)
            val peoplePage = assertPageIs(page, NumberOfPeopleFormPagePropertyRegistration::class)
            peoplePage.submitNumOfPeople(2)
            assertThat(peoplePage.form.getErrorMessage())
                .containsText(
                    "The number of people in the property must be the same as or higher than the number of households in the property",
                )
        }
    }

    @Nested
    inner class NumberOfBedroomsStep {
        val numberOfBedroomsErrorMessage = "Enter the number of bedrooms, like 3 or 8"

        @Test
        fun `Submitting with a blank numberOfBedrooms field returns an error`(page: Page) {
            val bedroomsPage = navigator.skipToPropertyRegistrationBedroomsPage()
            bedroomsPage.form.submit()
            assertThat(bedroomsPage.form.getErrorMessage()).containsText(numberOfBedroomsErrorMessage)
        }

        @Test
        fun `Submitting with a non-numerical value in the numberOfBedrooms field returns an error`(page: Page) {
            val bedroomsPage = navigator.skipToPropertyRegistrationBedroomsPage()
            bedroomsPage.submitNumOfBedrooms("not-a-number")
            assertThat(bedroomsPage.form.getErrorMessage()).containsText(numberOfBedroomsErrorMessage)
        }

        @Test
        fun `Submitting with a non-integer number in the numberOfBedrooms field returns an error`(page: Page) {
            val bedroomsPage = navigator.skipToPropertyRegistrationBedroomsPage()
            bedroomsPage.submitNumOfBedrooms("2.3")
            assertThat(bedroomsPage.form.getErrorMessage()).containsText(numberOfBedroomsErrorMessage)
        }

        @Test
        fun `Submitting with a negative integer in the numberOfBedrooms field returns an error`(page: Page) {
            val bedroomsPage = navigator.skipToPropertyRegistrationBedroomsPage()
            bedroomsPage.submitNumOfBedrooms("-2")
            assertThat(bedroomsPage.form.getErrorMessage()).containsText(numberOfBedroomsErrorMessage)
        }

        @Test
        fun `Submitting with a zero integer in the numberOfBedrooms field returns an error`(page: Page) {
            val bedroomsPage = navigator.skipToPropertyRegistrationBedroomsPage()
            bedroomsPage.submitNumOfBedrooms(0)
            assertThat(bedroomsPage.form.getErrorMessage()).containsText(numberOfBedroomsErrorMessage)
        }
    }
}
