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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipTypeFormPagePropertyRegistration
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
}