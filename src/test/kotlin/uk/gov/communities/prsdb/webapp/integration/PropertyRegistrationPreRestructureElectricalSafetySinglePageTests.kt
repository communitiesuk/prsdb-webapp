package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckElectricalCertUploadsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ElectricalCertExpiryDateFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasElectricalCertFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyStateSessionBuilder

// Covers the property registration journey with the restructure-and-skipping feature flag OFF.
// TODO PDJB-1340: delete every PropertyRegistrationPreRestructure*SinglePageTests file when
// PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING is removed. Every class here has an equivalent
// in the flag-on files, so they can all be removed wholesale.
class PropertyRegistrationPreRestructureElectricalSafetySinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun disableRestructureAndSkippingFlag() {
        featureFlagManager.disableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
    }

    @Nested
    inner class HasElectricalCertStep {
        @Test
        fun `Submitting with the Continue button with no option selected returns an error`(page: Page) {
            val hasElectricalCertPage = navigator.skipToPropertyRegistrationHasElectricalCertPage()
            hasElectricalCertPage.form.submitPrimaryButton()
            assertThat(
                hasElectricalCertPage.form.getErrorMessage(),
            ).containsText("Select which electrical safety certificate you have")
        }
    }

    @Nested
    inner class CheckElectricalSafetyAnswersStep {
        @Test
        fun `Cert uploaded EIC - cert type change link navigates to has electrical cert page`(page: Page) {
            val cyaPage =
                navigator.skipToPropertyRegistrationCheckElectricalSafetyAnswersPage(
                    PropertyStateSessionBuilder.beforePropertyRegistrationCheckElectricalSafetyAnswersUploadedEic(),
                )
            cyaPage.summaryList.electricalCertRow.clickFirstActionLinkAndWait()
            assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)
        }

        @Test
        fun `Cert uploaded EIC - expiry date change link navigates to expiry date page`(page: Page) {
            val cyaPage =
                navigator.skipToPropertyRegistrationCheckElectricalSafetyAnswersPage(
                    PropertyStateSessionBuilder.beforePropertyRegistrationCheckElectricalSafetyAnswersUploadedEic(),
                )
            cyaPage.summaryList.expiryDateRow.clickFirstActionLinkAndWait()
            assertPageIs(page, ElectricalCertExpiryDateFormPagePropertyRegistration::class)
        }

        @Test
        fun `Cert uploaded EIC - certificate change link navigates to check uploads page`(page: Page) {
            val cyaPage =
                navigator.skipToPropertyRegistrationCheckElectricalSafetyAnswersPage(
                    PropertyStateSessionBuilder.beforePropertyRegistrationCheckElectricalSafetyAnswersUploadedEic(),
                )
            cyaPage.summaryList.yourCertificateRow.clickFirstActionLinkAndWait()
            assertPageIs(page, CheckElectricalCertUploadsFormPagePropertyRegistration::class)
        }

        @Test
        fun `Cert uploaded EICR - cert type change link navigates to has electrical cert page`(page: Page) {
            val cyaPage =
                navigator.skipToPropertyRegistrationCheckElectricalSafetyAnswersPage(
                    PropertyStateSessionBuilder.beforePropertyRegistrationCheckElectricalSafetyAnswersUploadedEicr(),
                )
            cyaPage.summaryList.electricalCertRow.clickFirstActionLinkAndWait()
            assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)
        }

        @Test
        fun `Provide later - cert type change link navigates to has electrical cert page`(page: Page) {
            val cyaPage =
                navigator.skipToPropertyRegistrationCheckElectricalSafetyAnswersPage(
                    PropertyStateSessionBuilder.beforePropertyRegistrationCheckElectricalSafetyAnswersProvideLater(),
                )
            cyaPage.summaryList.electricalCertRow.clickFirstActionLinkAndWait()
            assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)
        }

        @Test
        fun `No cert - cert type change link navigates to has electrical cert page`(page: Page) {
            val cyaPage =
                navigator.skipToPropertyRegistrationCheckElectricalSafetyAnswersPage(
                    PropertyStateSessionBuilder.beforePropertyRegistrationCheckElectricalSafetyAnswersNoCert(),
                )
            cyaPage.summaryList.electricalCertRow.clickFirstActionLinkAndWait()
            assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)
        }

        @Test
        fun `Cert expired - cert type change link navigates to has electrical cert page`(page: Page) {
            val cyaPage =
                navigator.skipToPropertyRegistrationCheckElectricalSafetyAnswersPage(
                    PropertyStateSessionBuilder.beforePropertyRegistrationCheckElectricalSafetyAnswersCertExpired(),
                )
            cyaPage.summaryList.electricalCertRow.clickFirstActionLinkAndWait()
            assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class ElectricalCertExpiryDateStepTests {
        @ParameterizedTest(name = "{0}")
        @Suppress("ktlint:standard:max-line-length")
        @MethodSource(
            "uk.gov.communities.prsdb.webapp.testHelpers.parameterProviders.AnyDateValidationTestParameterProvider#provideInvalidDateStrings",
        )
        fun `Submitting returns a corresponding error when`(
            dayMonthYear: Triple<String, String, String>,
            expectedErrorMessage: String,
        ) {
            val (day, month, year) = dayMonthYear
            val electricalCertExpiryDatePage = navigator.skipToPropertyRegistrationElectricalCertExpiryDatePage()
            electricalCertExpiryDatePage.submitDate(day, month, year)
            assertThat(electricalCertExpiryDatePage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }
    }
}
