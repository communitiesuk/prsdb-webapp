package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.communities.prsdb.webapp.constants.EIC_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_MONTHS
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckElectricalCertUploadsFormBasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UploadCertificateFormPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckAnswersPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckEicUploadsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckEicrUploadsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckElectricalSafetyAnswersFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckGasCertUploadsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckGasSafetyAnswersFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.ConfirmationPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EicExpiredFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EicIssueDateFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EicrExpiredFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EicrExpiryDateFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.ElectricalCertMissingFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EpcFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.GasCertExpiredFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.GasCertIssueDateFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasElectricalCertFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasGasCertFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasGasSupplyFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.InfoFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.LicensingFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.TenancyDetailsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.UploadEicFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.UploadEicrFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.UploadGasCertFormPagePropertyReactivation
import java.nio.file.Path
import kotlin.test.assertEquals

class PropertyReactivationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    companion object {
        const val INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER = 52L
        const val INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER = 6L
        const val INACTIVE_PROPERTY_OWNED_BY_ANOTHER_LANDLORD = 2L
        const val ACTIVE_PROPERTY = 1L
    }

    @Test
    fun `a landlord can walk through every provide later step to the confirmation page`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        assertThat(infoPage.heading).containsText("Reactivate your property")
        infoPage.submit()

        val licensingPage = assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments)
        assertThat(licensingPage.heading).containsText("PDJB-2037")
        licensingPage.submit()

        val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments)
        hasGasSupplyPage.submitHasNoGasSupply()

        val checkGasSafetyAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments)
        assertThat(checkGasSafetyAnswersPage.gasSupplySummaryList.gasSupplyRow.value).containsText("No")
        checkGasSafetyAnswersPage.form.submit()

        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPagePropertyReactivation::class, urlArguments)
        hasElectricalCertPage.submitHasNoCert()

        val electricalCertMissingPage = assertPageIs(page, ElectricalCertMissingFormPagePropertyReactivation::class, urlArguments)
        assertThat(electricalCertMissingPage.form.submitButton).hasText("Continue without a valid electrical safety certificate")
        electricalCertMissingPage.form.submit()

        val checkElectricalSafetyAnswersPage =
            assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyReactivation::class, urlArguments)
        checkElectricalSafetyAnswersPage.form.submit()

        val epcPage = assertPageIs(page, EpcFormPagePropertyReactivation::class, urlArguments)
        assertThat(epcPage.heading).containsText("PDJB-2040")
        epcPage.submit()

        val tenancyDetailsPage = assertPageIs(page, TenancyDetailsFormPagePropertyReactivation::class, urlArguments)
        assertThat(tenancyDetailsPage.heading).containsText("PDJB-2041")
        tenancyDetailsPage.submit()

        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        assertThat(checkAnswersPage.heading).containsText("PDJB-2042")
        checkAnswersPage.submit()

        assertPageIs(page, ConfirmationPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord can provide a gas safety certificate and every gas safety button says continue`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        infoPage.submit()

        val licensingPage = assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments)
        licensingPage.submit()

        val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments)
        assertThat(hasGasSupplyPage.provideThisLaterButton).isHidden()
        assertThat(hasGasSupplyPage.form.submitButton).hasText("Continue")
        hasGasSupplyPage.submitHasGasSupply()

        val hasGasCertPage = assertPageIs(page, HasGasCertFormPagePropertyReactivation::class, urlArguments)
        assertThat(hasGasCertPage.form.submitButton).hasText("Continue")
        hasGasCertPage.submitHasCertificate()

        var issueDatePage = assertPageIs(page, GasCertIssueDateFormPagePropertyReactivation::class, urlArguments)
        assertThat(issueDatePage.form.submitButton).hasText("Continue")
        issueDatePage.submitDate(
            DateTimeHelper().getCurrentDateInUK().minus(DatePeriod(months = GAS_SAFETY_CERT_VALIDITY_MONTHS, days = 5)),
        )

        val expiredPage = assertPageIs(page, GasCertExpiredFormPagePropertyReactivation::class, urlArguments)
        assertThat(expiredPage.form.submitButton).hasText("Continue without a valid gas safety certificate")
        expiredPage.changeIssueDateLink.clickAndWait()

        issueDatePage = assertPageIs(page, GasCertIssueDateFormPagePropertyReactivation::class, urlArguments)
        issueDatePage.submitDate(DateTimeHelper().getCurrentDateInUK())

        val uploadPage = assertPageIs(page, UploadGasCertFormPagePropertyReactivation::class, urlArguments)
        uploadPage.uploadGasCertificate(Path.of("src/test/resources/test-files/blank.png"))

        val checkUploadsPage = assertPageIs(page, CheckGasCertUploadsFormPagePropertyReactivation::class, urlArguments)
        assertThat(checkUploadsPage.form.submitButton).hasText("Continue")
        checkUploadsPage.form.submit()

        val checkGasSafetyAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments)
        assertThat(checkGasSafetyAnswersPage.gasSupplySummaryList.gasSupplyRow.value).containsText("Yes")
        assertThat(checkGasSafetyAnswersPage.form.submitButton).hasText("Continue")
        checkGasSafetyAnswersPage.form.submit()

        assertPageIs(page, HasElectricalCertFormPagePropertyReactivation::class, urlArguments)
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = CertificateType::class, names = ["Eic", "Eicr"])
    fun `a landlord can provide an electrical safety certificate and every electrical safety button says continue`(
        certificateType: CertificateType,
        page: Page,
    ) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        val hasElectricalCertPage = navigateToHasElectricalCertPage(page, urlArguments)
        assertThat(hasElectricalCertPage.provideThisLaterButton).isHidden()
        assertThat(hasElectricalCertPage.form.submitButton).hasText("Continue")
        submitHasCertificate(hasElectricalCertPage, certificateType)

        var certDatePage = assertCertDatePage(page, certificateType, urlArguments)
        assertThat(certDatePage.form.submitButton).hasText("Continue")
        certDatePage.submitDate(expiredCertDate(certificateType))

        val (expiredPageForm, changeDateLink) = assertExpiredPageFormAndChangeDateLink(page, certificateType, urlArguments)
        assertThat(expiredPageForm.submitButton).hasText("Continue without a valid electrical safety certificate")
        changeDateLink.clickAndWait()

        certDatePage = assertCertDatePage(page, certificateType, urlArguments)
        certDatePage.submitDate(validCertDate(certificateType))

        assertUploadPage(page, certificateType, urlArguments).uploadCertificate("validFile.png")

        var checkUploadsPage = assertCheckUploadsPage(page, certificateType, urlArguments)
        assertThat(checkUploadsPage.form.submitButton).hasText("Continue")
        checkUploadsPage.backLink.clickAndWait()

        assertCertDatePage(page, certificateType, urlArguments).submitDate(validCertDate(certificateType))

        checkUploadsPage = assertCheckUploadsPage(page, certificateType, urlArguments)
        checkUploadsPage.form.submit()

        val checkElectricalSafetyAnswersPage =
            assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyReactivation::class, urlArguments)
        assertThat(checkElectricalSafetyAnswersPage.form.submitButton).hasText("Continue")
        checkElectricalSafetyAnswersPage.form.submit()

        assertPageIs(page, EpcFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord goes straight to check answers when there are no details to provide`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER)
        infoPage.submit()

        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        checkAnswersPage.backLink.clickAndWait()

        assertPageIs(page, InfoFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `the back link on a provide later step returns to the previous step`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        infoPage.submit()

        val licensingPage = assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments)
        licensingPage.backLink.clickAndWait()

        assertPageIs(page, InfoFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `the back links into and out of the gas safety task return to the adjacent steps`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        infoPage.submit()
        assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments).submit()

        val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments)
        hasGasSupplyPage.backLink.clickAndWait()
        assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments).submit()

        assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments).submitHasNoGasSupply()
        assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()

        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPagePropertyReactivation::class, urlArguments)
        hasElectricalCertPage.backLink.clickAndWait()

        assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `the back link out of the electrical safety task returns to check electrical safety answers`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        navigateToHasElectricalCertPage(page, urlArguments).submitHasNoCert()
        assertPageIs(page, ElectricalCertMissingFormPagePropertyReactivation::class, urlArguments).form.submit()
        assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()

        val epcPage = assertPageIs(page, EpcFormPagePropertyReactivation::class, urlArguments)
        epcPage.backLink.clickAndWait()

        assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord is sent to the info page if they visit a step the property does not need`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER.toString())

        navigator.navigateToPropertyReactivationLicensingPageAfterInfo(INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER)

        assertPageIs(page, InfoFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord receives a 404 for an active property`() {
        val response = navigator.navigate(ReactivatePropertyController.getPropertyReactivationPath(ACTIVE_PROPERTY))

        assertEquals(404, response?.status())
    }

    @Test
    fun `a landlord receives a 404 for an inactive property they do not own`() {
        val response =
            navigator.navigate(ReactivatePropertyController.getPropertyReactivationPath(INACTIVE_PROPERTY_OWNED_BY_ANOTHER_LANDLORD))

        assertEquals(404, response?.status())
    }

    private fun navigateToHasElectricalCertPage(
        page: Page,
        urlArguments: Map<String, String>,
    ): HasElectricalCertFormPagePropertyReactivation {
        navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER).submit()
        assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments).submit()
        assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments).submitHasNoGasSupply()
        assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()
        return assertPageIs(page, HasElectricalCertFormPagePropertyReactivation::class, urlArguments)
    }

    private fun submitHasCertificate(
        hasElectricalCertPage: HasElectricalCertFormPagePropertyReactivation,
        certificateType: CertificateType,
    ) = when (certificateType) {
        CertificateType.Eic -> hasElectricalCertPage.submitHasEic()
        CertificateType.Eicr -> hasElectricalCertPage.submitHasEicr()
        else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
    }

    private fun validCertDate(certificateType: CertificateType) =
        when (certificateType) {
            CertificateType.Eic -> DateTimeHelper().getCurrentDateInUK().minus(DatePeriod(years = 1))
            else -> DateTimeHelper().getCurrentDateInUK().plus(DatePeriod(years = 1))
        }

    private fun expiredCertDate(certificateType: CertificateType) =
        when (certificateType) {
            CertificateType.Eic -> DateTimeHelper().getCurrentDateInUK().minus(DatePeriod(years = EIC_VALIDITY_YEARS, days = 5))
            else -> DateTimeHelper().getCurrentDateInUK().minus(DatePeriod(days = 5))
        }

    private fun assertCertDatePage(
        page: Page,
        certificateType: CertificateType,
        urlArguments: Map<String, String>,
    ): DateFormPage =
        when (certificateType) {
            CertificateType.Eic -> assertPageIs(page, EicIssueDateFormPagePropertyReactivation::class, urlArguments)
            CertificateType.Eicr -> assertPageIs(page, EicrExpiryDateFormPagePropertyReactivation::class, urlArguments)
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun assertExpiredPageFormAndChangeDateLink(
        page: Page,
        certificateType: CertificateType,
        urlArguments: Map<String, String>,
    ): Pair<PostForm, Link> =
        when (certificateType) {
            CertificateType.Eic ->
                assertPageIs(page, EicExpiredFormPagePropertyReactivation::class, urlArguments)
                    .let { it.form to it.changeIssueDateLink }
            CertificateType.Eicr ->
                assertPageIs(page, EicrExpiredFormPagePropertyReactivation::class, urlArguments)
                    .let { it.form to it.changeExpiryDateLink }
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun assertUploadPage(
        page: Page,
        certificateType: CertificateType,
        urlArguments: Map<String, String>,
    ): UploadCertificateFormPage =
        when (certificateType) {
            CertificateType.Eic -> assertPageIs(page, UploadEicFormPagePropertyReactivation::class, urlArguments)
            CertificateType.Eicr -> assertPageIs(page, UploadEicrFormPagePropertyReactivation::class, urlArguments)
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun assertCheckUploadsPage(
        page: Page,
        certificateType: CertificateType,
        urlArguments: Map<String, String>,
    ): CheckElectricalCertUploadsFormBasePage =
        when (certificateType) {
            CertificateType.Eic -> assertPageIs(page, CheckEicUploadsFormPagePropertyReactivation::class, urlArguments)
            CertificateType.Eicr -> assertPageIs(page, CheckEicrUploadsFormPagePropertyReactivation::class, urlArguments)
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }
}
