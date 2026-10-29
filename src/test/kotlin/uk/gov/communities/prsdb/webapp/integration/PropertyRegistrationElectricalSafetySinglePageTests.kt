package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckElectricalCertUploadsFormBasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckEicUploadsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckEicrUploadsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.EicExpiryDateFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.EicrExpiryDateFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasElectricalCertFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyStateSessionBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.parameterProviders.AnyDateValidationTestParameterProvider
import java.util.regex.Pattern

class PropertyRegistrationElectricalSafetySinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    // TODO PDJB-959: remove this override and update these tests for the associated parties journey
    @BeforeEach
    fun disableAssociatedParties() {
        featureFlagManager.disableFeature(ASSOCIATED_PARTIES)
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

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = CertificateType::class, names = ["Eic", "Eicr"])
        fun `Cert uploaded - expiry date change link navigates to expiry date page`(
            certificateType: CertificateType,
            page: Page,
        ) {
            val cyaPage = navigator.skipToPropertyRegistrationCheckElectricalSafetyAnswersPage(certUploadedSession(certificateType))
            cyaPage.summaryList.expiryDateRow.clickFirstActionLinkAndWait()
            assertPageIs(page, expiryDatePageClass(certificateType))
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
    inner class ElectricalCertExpiryDateStepTests {
        @ParameterizedTest(name = "{0}: {1}")
        @MethodSource(
            "uk.gov.communities.prsdb.webapp.integration.PropertyRegistrationElectricalSafetySinglePageTests#provideInvalidDatesPerType",
        )
        fun `Submitting returns a corresponding error when`(
            certificateType: CertificateType,
            dayMonthYear: Triple<String, String, String>,
            expectedErrorMessage: String,
        ) {
            val (day, month, year) = dayMonthYear
            val electricalCertExpiryDatePage = skipToExpiryDatePage(certificateType)
            electricalCertExpiryDatePage.submitDate(day, month, year)
            assertThat(electricalCertExpiryDatePage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }

        private fun skipToExpiryDatePage(certificateType: CertificateType): DateFormPage =
            when (certificateType) {
                CertificateType.Eic -> navigator.skipToPropertyRegistrationEicExpiryDatePage()
                CertificateType.Eicr -> navigator.skipToPropertyRegistrationEicrExpiryDatePage()
                else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
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

    private fun expiryDatePageClass(certificateType: CertificateType) =
        when (certificateType) {
            CertificateType.Eic -> EicExpiryDateFormPagePropertyRegistration::class
            CertificateType.Eicr -> EicrExpiryDateFormPagePropertyRegistration::class
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    companion object {
        @JvmStatic
        fun provideInvalidDatesPerType() =
            listOf(CertificateType.Eic, CertificateType.Eicr).flatMap { certificateType ->
                AnyDateValidationTestParameterProvider.provideInvalidDateStrings().map { arguments ->
                    val (dayMonthYear, expectedErrorMessage) = arguments.get()
                    Arguments.of(Named.of(certificateType.name, certificateType), dayMonthYear, expectedErrorMessage)
                }
            }
    }
}
