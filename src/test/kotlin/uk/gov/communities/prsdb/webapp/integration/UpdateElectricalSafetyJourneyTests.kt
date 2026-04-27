package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.jupiter.api.Test
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

class UpdateElectricalSafetyJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val propertyOwnershipId = 8L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())
    private val currentDate = DateTimeHelper().getCurrentDateInUK()

    @Test
    fun `A property can have its electrical safety updated with no certificate`(page: Page) {
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToComplianceInformation()
        propertyDetailsPage.propertyComplianceSummaryList.eicrRow.clickFirstActionLinkAndWait()

        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPageUpdateElectricalSafety::class, urlArguments)
        hasElectricalCertPage.submitHasNoCert()

        val missingPage = assertPageIs(page, ElectricalCertMissingFormPageUpdateElectricalSafety::class, urlArguments)
        missingPage.form.submit()

        val checkAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety::class, urlArguments)
        checkAnswersPage.form.submit()

        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.eicrRow.value).containsText("Not added")
    }

    @Test
    fun `A property can have its electrical safety updated with a valid certificate`(page: Page) {
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToComplianceInformation()
        propertyDetailsPage.propertyComplianceSummaryList.eicrRow.clickFirstActionLinkAndWait()

        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPageUpdateElectricalSafety::class, urlArguments)
        hasElectricalCertPage.submitHasEicr()

        val expiryDate = currentDate.plus(DatePeriod(years = 1))
        val expiryDatePage = assertPageIs(page, ElectricalCertExpiryDateFormPageUpdateElectricalSafety::class, urlArguments)
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

        val checkAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety::class, urlArguments)
        checkAnswersPage.form.submit()

        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.eicrRow.value).containsText("Not added")
    }

    @Test
    fun `A property can have its electrical safety updated with an expired certificate`(page: Page) {
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToComplianceInformation()
        propertyDetailsPage.propertyComplianceSummaryList.eicrRow.clickFirstActionLinkAndWait()

        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPageUpdateElectricalSafety::class, urlArguments)
        hasElectricalCertPage.submitHasEic()

        val expiredExpiryDate = currentDate.minus(DatePeriod(days = 5))
        val expiryDatePage = assertPageIs(page, ElectricalCertExpiryDateFormPageUpdateElectricalSafety::class, urlArguments)
        expiryDatePage.submitDate(expiredExpiryDate)

        val expiredPage = assertPageIs(page, ElectricalCertExpiredFormPageUpdateElectricalSafety::class, urlArguments)
        expiredPage.form.submit()

        val checkAnswersPage = assertPageIs(page, CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety::class, urlArguments)
        checkAnswersPage.form.submit()

        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.eicrRow.value).containsText("Expired")
    }
}
