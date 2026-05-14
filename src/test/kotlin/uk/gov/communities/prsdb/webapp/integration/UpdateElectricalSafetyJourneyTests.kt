package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.CheckElectricalCertUploadsFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.ElectricalCertExpiredFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.ElectricalCertExpiryDateFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.ElectricalCertMissingFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.HasElectricalCertFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.RemoveElectricalCertUploadFormPageUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages.UploadElectricalCertFormPageUpdateElectricalSafety
import java.net.URI

class UpdateElectricalSafetyJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val propertyOwnershipId = 8L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())
    private val currentDate = DateTimeHelper().getCurrentDateInUK()

    @BeforeEach
    fun setUp() {
        whenever(absoluteUrlProvider.buildLandlordDashboardUri())
            .thenReturn(URI("example.com"))
        whenever(absoluteUrlProvider.buildComplianceInformationUri(any()))
            .thenReturn(URI("example.com"))
    }

    @Test
    fun `A property can have its electrical safety updated with missing, valid or expired certificates`(page: Page) {
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
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.electricalSafetyRow.value).containsText("Not added")

        // =====================================================================================================
        // A property can have its electrical safety updated with a valid certificate
        // =====================================================================================================
        propertyDetailsPage.electricalSafetyCard.getAction("Change").link.clickAndWait()
        hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPageUpdateElectricalSafety::class, urlArguments)
        hasElectricalCertPage.submitHasEicr()

        val expiryDate = currentDate.plus(DatePeriod(years = 1))
        var expiryDatePage = assertPageIs(page, ElectricalCertExpiryDateFormPageUpdateElectricalSafety::class, urlArguments)
        expiryDatePage.submitDate(expiryDate)

        var uploadPage = assertPageIs(page, UploadElectricalCertFormPageUpdateElectricalSafety::class, urlArguments)
        uploadPage.uploadCertificate("validFile.png")

        var checkUploadsPage = assertPageIs(page, CheckElectricalCertUploadsFormPageUpdateElectricalSafety::class, urlArguments)
        checkUploadsPage.form.addAnotherButton.clickAndWait()
        uploadPage = assertPageIs(page, UploadElectricalCertFormPageUpdateElectricalSafety::class, urlArguments)
        uploadPage.uploadCertificate("validFile.png")
        checkUploadsPage = assertPageIs(page, CheckElectricalCertUploadsFormPageUpdateElectricalSafety::class, urlArguments)
        checkUploadsPage.table
            .getClickableCell(0, 2)
            .link
            .clickAndWait()
        val removeUploadPage = assertPageIs(page, RemoveElectricalCertUploadFormPageUpdateElectricalSafety::class, urlArguments)
        removeUploadPage.form.radios.selectValue("true")
        removeUploadPage.form.submit()
        checkUploadsPage = assertPageIs(page, CheckElectricalCertUploadsFormPageUpdateElectricalSafety::class, urlArguments)
        checkUploadsPage.form.submit()

        checkAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety::class, urlArguments)
        checkAnswersPage.form.submit()

        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(
            propertyDetailsPage.propertyComplianceSummaryList.eicrRow.value,
        ).containsText("validFile.png (Pending virus scan)")

        // =====================================================================================================
        // A property can have its electrical safety updated with a valid certificate
        // =====================================================================================================
        propertyDetailsPage.electricalSafetyCard.getAction("Change").link.clickAndWait()

        hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPageUpdateElectricalSafety::class, urlArguments)
        hasElectricalCertPage.submitHasEic()

        val expiredExpiryDate = currentDate.minus(DatePeriod(days = 5))
        expiryDatePage = assertPageIs(page, ElectricalCertExpiryDateFormPageUpdateElectricalSafety::class, urlArguments)
        expiryDatePage.submitDate(expiredExpiryDate)

        val expiredPage = assertPageIs(page, ElectricalCertExpiredFormPageUpdateElectricalSafety::class, urlArguments)
        expiredPage.form.submit()

        checkAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety::class, urlArguments)
        checkAnswersPage.form.submit()

        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.eicRow.value).containsText("Expired")
    }
}
