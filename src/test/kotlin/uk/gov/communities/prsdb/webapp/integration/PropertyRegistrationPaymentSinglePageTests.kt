package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.PAYMENTS
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAnswersPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ConfirmMissingComplianceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ConfirmationPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PaymentSummaryFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyStateSessionBuilder

class PropertyRegistrationPaymentSinglePageTests : IntegrationTestWithMutableData("data-local.sql") {
    @BeforeEach
    fun enableFlags() {
        featureFlagManager.enableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        featureFlagManager.enableFeature(PAYMENTS)
    }

    @Test
    fun `submitting check your answers routes to the payment summary step when payments is enabled`(page: Page) {
        val checkAnswersPage = navigator.goToRestructuredPropertyRegistrationCheckAnswersPageWithPayments()
        assertThat(checkAnswersPage.submitButton).containsText("Submit and pay")

        checkAnswersPage.confirm()

        val paymentSummaryPage = assertPageIs(page, PaymentSummaryFormPagePropertyRegistration::class)
        assertThat(paymentSummaryPage.heading).containsText("Payment summary")
    }

    @Test
    fun `submitting check your answers routes straight to confirmation when payments is disabled`(page: Page) {
        featureFlagManager.disableFeature(PAYMENTS)

        val checkAnswersPage = navigator.goToRestructuredPropertyRegistrationCheckAnswersPage()
        assertThat(checkAnswersPage.sectionHeader).containsText("Submit your registration")
        assertThat(checkAnswersPage.submitButton).containsText("Complete registration")

        checkAnswersPage.confirm()

        assertPageIs(page, ConfirmationPagePropertyRegistration::class)
    }

    @Test
    fun `confirming missing compliance routes straight to confirmation when payments is disabled`(page: Page) {
        featureFlagManager.disableFeature(PAYMENTS)

        val taskListPage =
            navigator.goToRestructuredPropertyRegistrationTaskList(
                PropertyStateSessionBuilder
                    .beforePropertyRegistrationCheckAnswersOccupied()
                    .withBedrooms(),
            )
        taskListPage.clickSubmitYourRegistrationTaskWithName("Check and submit your answers")
        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

        checkAnswersPage.confirm()

        val confirmMissingCompliancePage = assertPageIs(page, ConfirmMissingComplianceFormPagePropertyRegistration::class)
        confirmMissingCompliancePage.form.radios.selectValue("true")
        confirmMissingCompliancePage.form.submit()

        assertPageIs(page, ConfirmationPagePropertyRegistration::class)
    }
}
