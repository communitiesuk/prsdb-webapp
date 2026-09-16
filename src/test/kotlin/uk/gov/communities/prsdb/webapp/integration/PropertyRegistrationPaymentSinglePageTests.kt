package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.PAYMENTS
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PaymentSummaryFormPagePropertyRegistration

class PropertyRegistrationPaymentSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun enableFlags() {
        featureFlagManager.enableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        featureFlagManager.enableFeature(PAYMENTS)
    }

    @Test
    fun `submitting check your answers routes to the payment summary step when payments is enabled`(page: Page) {
        val checkAnswersPage = navigator.skipToPropertyRegistrationCheckAnswersPage()

        checkAnswersPage.confirm()

        val paymentSummaryPage = assertPageIs(page, PaymentSummaryFormPagePropertyRegistration::class)
        assertThat(paymentSummaryPage.heading).containsText("Payment summary")
    }
}
