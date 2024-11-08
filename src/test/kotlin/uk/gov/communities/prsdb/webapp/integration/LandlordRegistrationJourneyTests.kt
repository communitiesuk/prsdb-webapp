package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LandlordRegistrationJourneyTests : IntegrationTest() {
    @Nested
    inner class LandlordRegistrationStepEmail {
        @Test
        fun `Submitting a valid email address redirects to the next step`() {
            val formPage = navigator.goToLandlordRegistrationEmailFormPage()
            formPage.fillEmail("test@example.com")
            formPage.submit()
        }

        @Test
        fun `Submitting an empty e-mail address returns an error`() {
            val formPage = navigator.goToLandlordRegistrationEmailFormPage()
            formPage.fillEmail("")
            formPage.submitUnsuccessfully()
            formPage.assertEmailFormErrorContains("Enter a valid email address to continue. An email is required for contact purposes.")
        }

        @Test
        fun `Submitting an invalid e-mail address returns an error`() {
            val formPage = navigator.goToLandlordRegistrationEmailFormPage()
            formPage.fillEmail("notAnEmail")
            formPage.submitUnsuccessfully()
            formPage.assertEmailFormErrorContains("Enter an email address in the right format")
        }
    }

    @Nested
    inner class LandlordRegistrationStepPhoneNumber {
        @Test
        fun `Attempting to access page without previous steps being valid returns an error`() {
            val formPage = navigator.goToPhoneNumberFormPage(journeyType)
        }
//        @Test
//        fun `Submitting a valid email address redirects to the next step`() {
//            val formPage = navigator.goToEmailFormPage(journeyType)
//            formPage.fillEmail("test@example.com")
//            formPage.submit<PhoneNumberFormPage>()
//        }
//
//        @Test
//        fun `Submitting an empty e-mail address returns an error`() {
//            val formPage = navigator.goToEmailFormPage(journeyType)
//            formPage.fillEmail("")
//            formPage.submitUnsuccessfully()
//            formPage.assertEmailFormErrorContains("Enter a valid email address to continue. An email is required for contact purposes.")
//        }
//
//        @Test
//        fun `Submitting an invalid e-mail address returns an error`() {
//            val formPage = navigator.goToEmailFormPage(journeyType)
//            formPage.fillEmail("notAnEmail")
//            formPage.submitUnsuccessfully()
//            formPage.assertEmailFormErrorContains("Enter an email address in the right format")
//        }
    }
}
