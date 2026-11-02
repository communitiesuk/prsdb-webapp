package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.EIC_ISSUE_DATE
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckElectricalCertUploadsFormBasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckEicUploadsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckEicrUploadsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.EicExpiredFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.EicExpiryDateFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.EicIssueDateFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.EicrExpiryDateFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasElectricalCertFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.UploadEicFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyStateSessionBuilder
import java.util.regex.Pattern

class PropertyRegistrationElectricalSafetySinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
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
    inner class CheckElectricalCertUploadsStepTests {
        @ParameterizedTest(name = "{0}")
        @EnumSource(value = CertificateType::class, names = ["Eic", "Eicr"])
        fun `The actions column header text is visually hidden but the header cell is not`(
            certificateType: CertificateType,
            page: Page,
        ) {
            val checkUploadsPage = navigateToCheckUploadsPage(page, certificateType)

            assertThat(checkUploadsPage.table.actionsHeaderVisuallyHiddenText).hasText("Actions")
            assertThat(checkUploadsPage.table.actionsHeader).not().hasClass(Pattern.compile("govuk-visually-hidden"))
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = CertificateType::class, names = ["Eic", "Eicr"])
        fun `The remove link includes the file name as visually hidden text`(
            certificateType: CertificateType,
            page: Page,
        ) {
            val checkUploadsPage = navigateToCheckUploadsPage(page, certificateType)

            assertThat(checkUploadsPage.table.getClickableCell(0, 2).link).hasText("Remove electrical-safety-cert.pdf")
            assertThat(checkUploadsPage.table.getVisuallyHiddenText(0, 2)).hasText("electrical-safety-cert.pdf")
        }

        private fun navigateToCheckUploadsPage(
            page: Page,
            certificateType: CertificateType,
        ): CheckElectricalCertUploadsFormBasePage {
            val cyaPage = navigator.skipToPropertyRegistrationCheckElectricalSafetyAnswersPage(certUploadedSession(certificateType))
            cyaPage.summaryList.yourCertificateRow.clickFirstActionLinkAndWait()
            return assertPageIs(page, checkUploadsPageClass(certificateType))
        }
    }

    @Nested
    inner class CheckElectricalSafetyAnswersStep {
        @ParameterizedTest(name = "{0}")
        @EnumSource(value = CertificateType::class, names = ["Eic", "Eicr"])
        fun `Cert uploaded - cert type change link navigates to has electrical cert page`(
            certificateType: CertificateType,
            page: Page,
        ) {
            val cyaPage = navigator.skipToPropertyRegistrationCheckElectricalSafetyAnswersPage(certUploadedSession(certificateType))
            cyaPage.summaryList.electricalCertRow.clickFirstActionLinkAndWait()
            assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)
        }

        @Test
        fun `Cert uploaded - EICR expiry date change link navigates to expiry date page`(page: Page) {
            val cyaPage = navigator.skipToPropertyRegistrationCheckElectricalSafetyAnswersPage(certUploadedSession(CertificateType.Eicr))
            cyaPage.summaryList.expiryDateRow.clickFirstActionLinkAndWait()
            assertPageIs(page, EicrExpiryDateFormPagePropertyRegistration::class)
        }

        @Test
        fun `Cert uploaded - EIC date change link navigates to issue date page`(page: Page) {
            val cyaPage = navigator.skipToPropertyRegistrationCheckElectricalSafetyAnswersPage(certUploadedSession(CertificateType.Eic))
            cyaPage.summaryList.expiryDateRow.clickFirstActionLinkAndWait()
            assertPageIs(page, EicIssueDateFormPagePropertyRegistration::class)
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = CertificateType::class, names = ["Eic", "Eicr"])
        fun `Cert uploaded - certificate change link navigates to check uploads page`(
            certificateType: CertificateType,
            page: Page,
        ) {
            val cyaPage = navigator.skipToPropertyRegistrationCheckElectricalSafetyAnswersPage(certUploadedSession(certificateType))
            cyaPage.summaryList.yourCertificateRow.clickFirstActionLinkAndWait()
            assertPageIs(page, checkUploadsPageClass(certificateType))
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

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = CertificateType::class, names = ["Eic", "Eicr"])
        fun `Cert expired - cert type change link navigates to has electrical cert page`(
            certificateType: CertificateType,
            page: Page,
        ) {
            val cyaPage = navigator.skipToPropertyRegistrationCheckElectricalSafetyAnswersPage(certExpiredSession(certificateType))
            cyaPage.summaryList.electricalCertRow.clickFirstActionLinkAndWait()
            assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class EicrExpiryDateStepTests {
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
            val eicrExpiryDatePage = navigator.skipToPropertyRegistrationEicrExpiryDatePage()
            eicrExpiryDatePage.submitDate(day, month, year)
            assertThat(eicrExpiryDatePage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }
    }

    @Nested
    inner class EicIssueDateStepTests {
        @Test
        fun `Issue date page displays heading`() {
            val issueDatePage = navigator.skipToPropertyRegistrationEicIssueDatePage()

            assertThat(issueDatePage.heading).containsText("What’s the issue date on the Electrical Installation Certificate (EIC)?")
        }

        @ParameterizedTest(name = "{0}")
        @Suppress("ktlint:standard:max-line-length")
        @MethodSource(
            "uk.gov.communities.prsdb.webapp.testHelpers.parameterProviders.TodayOrPastDateValidationTestParameterProvider#provideInvalidDateStrings",
        )
        fun `Submitting returns a corresponding error when`(
            dayMonthYear: Triple<String, String, String>,
            expectedErrorMessage: String,
        ) {
            val (day, month, year) = dayMonthYear
            val issueDatePage = navigator.skipToPropertyRegistrationEicIssueDatePage()
            issueDatePage.submitDate(day, month, year)
            assertThat(issueDatePage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }

        @Test
        fun `Submitting an issue date more than 5 years ago redirects to the need different certificate page`(page: Page) {
            val issueDatePage = navigator.skipToPropertyRegistrationEicIssueDatePage()
            issueDatePage.submitDate(today.minus(DatePeriod(years = 5, days = 1)))

            val eicExpiredPage = assertPageIs(page, EicExpiredFormPagePropertyRegistration::class)
            assertThat(eicExpiredPage.heading).containsText("You need a different electrical safety certificate")
        }

        @Test
        fun `Submitting an issue date exactly 5 years ago redirects to the upload EIC page`(page: Page) {
            val issueDatePage = navigator.skipToPropertyRegistrationEicIssueDatePage()
            issueDatePage.submitDate(today.minus(DatePeriod(years = 5)))

            assertPageIs(page, UploadEicFormPagePropertyRegistration::class)
        }
    }

    // TODO PDJB-1679: Remove this test class when the EIC_ISSUE_DATE flag is removed
    @Nested
    inner class BeforePdjb1671EicExpiryDateStepTests {
        @Test
        fun `Submitting EIC navigates to the EIC expiry date page`(page: Page) {
            navigateToEicExpiryDatePage(page)
        }

        @Test
        fun `Submitting a future expiry date redirects to the upload EIC page`(page: Page) {
            val expiryDatePage = navigateToEicExpiryDatePage(page)
            expiryDatePage.submitDate(today.plus(DatePeriod(years = 1)))

            assertPageIs(page, UploadEicFormPagePropertyRegistration::class)
        }

        @Test
        fun `Submitting a past expiry date redirects to the need different certificate page`(page: Page) {
            val expiryDatePage = navigateToEicExpiryDatePage(page)
            expiryDatePage.submitDate(today.minus(DatePeriod(days = 5)))

            assertPageIs(page, EicExpiredFormPagePropertyRegistration::class)
        }

        private fun navigateToEicExpiryDatePage(page: Page): EicExpiryDateFormPagePropertyRegistration {
            featureFlagManager.disableFeature(EIC_ISSUE_DATE)

            val hasElectricalCertPage = navigator.skipToPropertyRegistrationHasElectricalCertPage()
            hasElectricalCertPage.submitHasEic()

            val expiryDatePage = assertPageIs(page, EicExpiryDateFormPagePropertyRegistration::class)
            assertThat(expiryDatePage.heading).containsText("What’s the expiry date on the Electrical Installation Certificate")
            return expiryDatePage
        }
    }

    private fun certUploadedSession(certificateType: CertificateType) =
        when (certificateType) {
            CertificateType.Eic -> PropertyStateSessionBuilder.beforePropertyRegistrationCheckElectricalSafetyAnswersUploadedEic()
            CertificateType.Eicr -> PropertyStateSessionBuilder.beforePropertyRegistrationCheckElectricalSafetyAnswersUploadedEicr()
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun certExpiredSession(certificateType: CertificateType) =
        when (certificateType) {
            CertificateType.Eic -> PropertyStateSessionBuilder.beforePropertyRegistrationCheckElectricalSafetyAnswersEicExpired()
            CertificateType.Eicr -> PropertyStateSessionBuilder.beforePropertyRegistrationCheckElectricalSafetyAnswersEicrExpired()
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun checkUploadsPageClass(certificateType: CertificateType) =
        when (certificateType) {
            CertificateType.Eic -> CheckEicUploadsFormPagePropertyRegistration::class
            CertificateType.Eicr -> CheckEicrUploadsFormPagePropertyRegistration::class
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    companion object {
        private val today = DateTimeHelper().getCurrentDateInUK()
    }
}
