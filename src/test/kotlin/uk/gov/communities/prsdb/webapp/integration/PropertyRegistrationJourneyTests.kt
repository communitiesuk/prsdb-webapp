package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HouseholdsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OccupancyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PeopleFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyTypeFormPagePropertyRegistration
import java.net.URI

@Sql("/data-local.sql")
class PropertyRegistrationJourneyTests : IntegrationTest() {
    @Test
    fun `User can navigate the whole journey if pages are correctly filled in`(page: Page) {
        // TODO: this can be added to as more steps are added to the journey

        // Start page (not a journey step, but it is how the user accesses the journey)
        val registerPropertyStartPage = navigator.goToPropertyRegistrationStartPage()
        assertThat(registerPropertyStartPage.heading).containsText("Enter your property details")
        registerPropertyStartPage.startButton.click()
        val propertyTypePage = assertPageIs(page, PropertyTypeFormPagePropertyRegistration::class)

        // Property type selection step - render page
        assertThat(propertyTypePage.form.getFieldsetHeading()).containsText("What type of property are you registering?")
        // fill in and submit
        propertyTypePage.form.getRadios().selectValue(PropertyType.DETACHED_HOUSE)
        propertyTypePage.form.submit()
        // goes to the next page
        val ownershipTypePage = assertPageIs(page, OwnershipTypeFormPagePropertyRegistration::class)

        // Ownership type selection step - render page
        assertThat(ownershipTypePage.form.getFieldsetHeading()).containsText("Select the ownership type for your property")
        // fill in and submit
        ownershipTypePage.form.getRadios().selectValue(OwnershipType.FREEHOLD)
        propertyTypePage.form.submit()
        val occupancyPage = assertPageIs(page, OccupancyFormPagePropertyRegistration::class)

        // Occupancy - render page
        assertThat(occupancyPage.form.getFieldsetHeading()).containsText("Is your property occupied by tenants?")
        // fill in "yes" and submit
        occupancyPage.form.getRadios().selectValue("true")
        occupancyPage.form.submit()
        val householdsPage = assertPageIs(page, HouseholdsFormPagePropertyRegistration::class)

        // Number of Households - render page
        assertThat(householdsPage.form.getFieldsetHeading()).containsText("How many households live in your property?")
        // fill in and submit
        householdsPage.householdsInput.fill("2")
        householdsPage.form.submit()
        val peoplePage = assertPageIs(page, PeopleFormPagePropertyRegistration::class)

        // Number of people - render page
        assertThat(peoplePage.form.getFieldsetHeading()).containsText("How many people live in your property?")
        // fill in and submit
        peoplePage.peopleInput.fill("2")
        peoplePage.form.submit()
        assertEquals("/register-property/placeholder", URI(page.url()).path)
    }

    @Nested
    inner class PropertyTypeStep {
        @Test
        fun `Submitting with other selected and the input filled in redirects to the next step`(page: Page) {
            val propertyTypePage = navigator.goToPropertyRegistrationPropertyTypePage()
            propertyTypePage.form.getRadios().selectValue(PropertyType.OTHER)
            propertyTypePage.customPropertyTypeInput.fill("End terrace house")
            propertyTypePage.form.submit()
            assertPageIs(page, OwnershipTypeFormPagePropertyRegistration::class)
        }

        @Test
        fun `Submitting with no propertyType selected returns an error`(page: Page) {
            val propertyTypePage = navigator.goToPropertyRegistrationPropertyTypePage()
            propertyTypePage.form.submit()
            assertThat(propertyTypePage.form.getErrorMessage()).containsText("Select the type of property")
        }

        @Test
        fun `Submitting with the Other propertyType selected but an empty customPropertyType field returns an error`(page: Page) {
            val propertyTypePage = navigator.goToPropertyRegistrationPropertyTypePage()
            propertyTypePage.form.getRadios().selectValue(PropertyType.OTHER)
            propertyTypePage.form.submit()
            assertThat(propertyTypePage.form.getErrorMessage()).containsText("Enter the property type")
        }
    }

    @Nested
    inner class OwnershipTypeStep {
        @Test
        fun `Submitting with no ownershipType selected returns an error`(page: Page) {
            val ownershipTypePage = navigator.goToPropertyRegistrationOwnershipTypePage()
            ownershipTypePage.form.submit()
            assertThat(ownershipTypePage.form.getErrorMessage()).containsText("Select the ownership type")
        }
    }

    @Nested
    inner class OccupancyStep {
        @Test
        fun `Submitting with the not occupied option selected skips to the next step`(page: Page) {
            val occupancyPage = navigator.goToPropertyRegistrationOccupancyPage()
            occupancyPage.form.getRadios().selectValue("false")
            occupancyPage.form.submit()
            assertEquals("/register-property/placeholder", URI(page.url()).path)
        }

        @Test
        fun `Submitting with no occupancy option selected returns an error`(page: Page) {
            val occupancyPage = navigator.goToPropertyRegistrationOccupancyPage()
            occupancyPage.form.submit()
            assertThat(occupancyPage.form.getErrorMessage()).containsText("Select whether the property is occupied")
        }
    }

    @Nested
    inner class NumberOfHouseholdsStep {
        @Test
        fun `Submitting with a blank numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.goToPropertyRegistrationHouseholdsPage()
            householdsPage.form.submit()
            assertThat(householdsPage.form.getErrorMessage()).containsText("Enter the number of households living in your property")
        }

        @Test
        fun `Submitting with a non-numerical value in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.goToPropertyRegistrationHouseholdsPage()
            householdsPage.householdsInput.fill("not-a-number")
            householdsPage.form.submit()
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Number of households in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with a non-integer number in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.goToPropertyRegistrationHouseholdsPage()
            householdsPage.householdsInput.fill("2.3")
            householdsPage.form.submit()
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Number of households in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with a negative integer in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.goToPropertyRegistrationHouseholdsPage()
            householdsPage.householdsInput.fill("-2")
            householdsPage.form.submit()
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Number of households in your property must be a positive, whole number, like 3")
        }
    }

    @Nested
    inner class NumberOfPeopleStep {
        @Test
        fun `Submitting with a blank numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.goToPropertyRegistrationPeoplePage()
            peoplePage.form.submit()
            assertThat(peoplePage.form.getErrorMessage()).containsText("Enter the number of people living in your property")
        }

        @Test
        fun `Submitting with a non-numerical value in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.goToPropertyRegistrationPeoplePage()
            peoplePage.peopleInput.fill("not-a-number")
            peoplePage.form.submit()
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Number of people in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with a non-integer number in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.goToPropertyRegistrationPeoplePage()
            peoplePage.peopleInput.fill("2.3")
            peoplePage.form.submit()
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Number of people in your property must be a positive, whole number, like 3")
        }

        @Test
        fun `Submitting with a negative integer in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.goToPropertyRegistrationPeoplePage()
            peoplePage.peopleInput.fill("-2")
            peoplePage.form.submit()
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Number of people in your property must be a positive, whole number, like 3")
        }
    }
}
