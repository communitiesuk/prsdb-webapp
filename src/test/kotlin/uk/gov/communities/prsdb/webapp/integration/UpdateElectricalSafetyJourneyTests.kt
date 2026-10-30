package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithRadios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckElectricalCertUploadsFormBasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UploadCertificateFormPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.CheckEicUploadsFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.CheckEicrUploadsFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.EicExpiredFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.EicExpiryDateFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.EicrExpiredFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.EicrExpiryDateFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.ElectricalCertMissingFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.HasElectricalCertFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.RemoveEicUploadFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.RemoveEicrUploadFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.UploadEicFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.UploadEicrFormPageUpdateElectricalSafety

class UpdateElectricalSafetyJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val propertyOwnershipId = 8L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())
    private val currentDate = DateTimeHelper().getCurrentDateInUK()

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = CertificateType::class, names = ["Eic", "Eicr"])
    fun `A property can have its electrical safety updated with missing, valid or expired certificates`(
        certificateType: CertificateType,
        page: Page,
    ) {
        // =====================================================================================================
        // A property can have its electrical safety updated with no certificate
        // =====================================================================================================
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToComplianceInformation()
        propertyDetailsPage.electricalSafetyCard.getAction("Change").link.clickAndWait()

        var hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPageUpdateElectricalSafety::class, urlArguments)
        hasElectricalCertPage.submitHasNoCert()

        val missingPage = assertPageIs(page, ElectricalCertMissingFormPageUpdateElectricalSafety::class, urlArguments)
        missingPage.form.submit()

        var checkAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety::class, urlArguments)
        checkAnswersPage.form.submit()

        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.electricalSafetyCard.summaryList.whichCertificateRow.value).containsText("None")

        // =====================================================================================================
        // A property can have its electrical safety updated with a valid certificate
        // =====================================================================================================
        propertyDetailsPage.electricalSafetyCard.getAction("Change").link.clickAndWait()
        hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPageUpdateElectricalSafety::class, urlArguments)
        submitHasCertificate(hasElectricalCertPage, certificateType)

        assertExpiryDatePage(page, certificateType).submitDate(currentDate.plus(DatePeriod(years = 1)))

        assertUploadPage(page, certificateType).uploadCertificate("validFile.png")

        var checkUploadsPage = assertCheckUploadsPage(page, certificateType)
        checkUploadsPage.form.addAnotherButton.clickAndWait()
        assertUploadPage(page, certificateType).uploadCertificate("validFile.png")
        checkUploadsPage = assertCheckUploadsPage(page, certificateType)
        checkUploadsPage.table
            .getClickableCell(0, 2)
            .link
            .clickAndWait()
        val removeUploadForm = assertRemoveUploadPageForm(page, certificateType)
        removeUploadForm.radios.selectValue("true")
        removeUploadForm.submit()
        checkUploadsPage = assertCheckUploadsPage(page, certificateType)
        checkUploadsPage.form.submit()

        checkAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety::class, urlArguments)
        checkAnswersPage.form.submit()

        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(
            propertyDetailsPage.electricalSafetyCard.summaryList.yourCertificateRow.value,
        ).containsText("Pending virus scan")

        // =====================================================================================================
        // A property can have its electrical safety updated with an expired certificate
        // =====================================================================================================
        propertyDetailsPage.electricalSafetyCard.getAction("Change").link.clickAndWait()

        hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPageUpdateElectricalSafety::class, urlArguments)
        submitHasCertificate(hasElectricalCertPage, certificateType)

        assertExpiryDatePage(page, certificateType).submitDate(currentDate.minus(DatePeriod(days = 5)))

        assertExpiredPageForm(page, certificateType).submit()

        checkAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety::class, urlArguments)
        checkAnswersPage.form.submit()

        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.electricalSafetyCard.summaryList.certificateStatusRow.value).containsText("Expired")
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = CertificateType::class, names = ["Eic", "Eicr"])
    fun `The back link on the electrical cert expiry date and certificate pages returns to the check answers page when reached from there`(
        certificateType: CertificateType,
        page: Page,
    ) {
        // Reach the check answers page with a valid electrical certificate uploaded
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToComplianceInformation()
        propertyDetailsPage.electricalSafetyCard.getAction("Change").link.clickAndWait()

        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPageUpdateElectricalSafety::class, urlArguments)
        submitHasCertificate(hasElectricalCertPage, certificateType)

        assertExpiryDatePage(page, certificateType).submitDate(currentDate.plus(DatePeriod(years = 1)))

        assertUploadPage(page, certificateType).uploadCertificate("validFile.png")

        var checkUploadsPage = assertCheckUploadsPage(page, certificateType)
        checkUploadsPage.form.submit()

        var checkAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety::class, urlArguments)

        // Change the certificate date from the check answers page, then check the back link returns there
        checkAnswersPage.summaryList.certificateDateRow(certificateType).clickFirstActionLinkAndWait()
        assertExpiryDatePage(page, certificateType).backLink.clickAndWait()
        checkAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety::class, urlArguments)

        // Change the certificate from the check answers page, then check the back link returns there
        checkAnswersPage.summaryList.yourCertificateRow.clickFirstActionLinkAndWait()
        checkUploadsPage = assertCheckUploadsPage(page, certificateType)
        checkUploadsPage.backLink.clickAndWait()
        assertPageIs(page, CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety::class, urlArguments)
    }

    private fun submitHasCertificate(
        hasElectricalCertPage: HasElectricalCertFormPageUpdateElectricalSafety,
        certificateType: CertificateType,
    ) = when (certificateType) {
        CertificateType.Eic -> hasElectricalCertPage.submitHasEic()
        CertificateType.Eicr -> hasElectricalCertPage.submitHasEicr()
        else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
    }

    private fun assertExpiryDatePage(
        page: Page,
        certificateType: CertificateType,
    ): DateFormPage =
        when (certificateType) {
            CertificateType.Eic -> assertPageIs(page, EicExpiryDateFormPageUpdateElectricalSafety::class, urlArguments)
            CertificateType.Eicr -> assertPageIs(page, EicrExpiryDateFormPageUpdateElectricalSafety::class, urlArguments)
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun assertUploadPage(
        page: Page,
        certificateType: CertificateType,
    ): UploadCertificateFormPage =
        when (certificateType) {
            CertificateType.Eic -> assertPageIs(page, UploadEicFormPageUpdateElectricalSafety::class, urlArguments)
            CertificateType.Eicr -> assertPageIs(page, UploadEicrFormPageUpdateElectricalSafety::class, urlArguments)
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun assertCheckUploadsPage(
        page: Page,
        certificateType: CertificateType,
    ): CheckElectricalCertUploadsFormBasePage =
        when (certificateType) {
            CertificateType.Eic -> assertPageIs(page, CheckEicUploadsFormPageUpdateElectricalSafety::class, urlArguments)
            CertificateType.Eicr -> assertPageIs(page, CheckEicrUploadsFormPageUpdateElectricalSafety::class, urlArguments)
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun assertRemoveUploadPageForm(
        page: Page,
        certificateType: CertificateType,
    ): FormWithRadios =
        when (certificateType) {
            CertificateType.Eic -> assertPageIs(page, RemoveEicUploadFormPageUpdateElectricalSafety::class, urlArguments).form
            CertificateType.Eicr -> assertPageIs(page, RemoveEicrUploadFormPageUpdateElectricalSafety::class, urlArguments).form
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun assertExpiredPageForm(
        page: Page,
        certificateType: CertificateType,
    ): PostForm =
        when (certificateType) {
            CertificateType.Eic -> assertPageIs(page, EicExpiredFormPageUpdateElectricalSafety::class, urlArguments).form
            CertificateType.Eicr -> assertPageIs(page, EicrExpiredFormPageUpdateElectricalSafety::class, urlArguments).form
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }
}
