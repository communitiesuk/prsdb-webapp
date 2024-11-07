package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.PhoneNumberFormPage

class LandlordRegistrationJourneyTests : IntegrationTest() {
    val journeyType = JourneyType.LANDLORD_REGISTRATION

    @Nested
    inner class LandlordRegistrationStepEmail {
        @Test
        fun `Submitting a valid email address redirects to the next step`() {
            val formPage = navigator.goToEmailFormPage(journeyType)
            formPage.fillEmail("test@example.com")
            formPage.submit<PhoneNumberFormPage>()
        }

        @Test
        fun `Submitting an empty e-mail address returns an error`() {
            val formPage = navigator.goToEmailFormPage(journeyType)
            formPage.fillEmail("")
            formPage.submitUnsuccessfully()
            formPage.assertEmailFormErrorContains("Enter a valid email address to continue. An email is required for contact purposes.")
        }

        @Test
        fun `Submitting an invalid e-mail address returns an error`() {
            val formPage = navigator.goToEmailFormPage(journeyType)
            formPage.fillEmail("notAnEmail")
            formPage.submitUnsuccessfully()
            formPage.assertEmailFormErrorContains("Enter an email address in the right format")
        }
    }
}
