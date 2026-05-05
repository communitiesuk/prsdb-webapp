package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages.CheckGasCertUploadsFormPageUpdateGasSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages.CheckGasSafetyAnswersFormPageUpdateGasSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages.GasCertExpiredFormPageUpdateGasSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages.GasCertIssueDateFormPageUpdateGasSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages.HasGasCertFormPageUpdateGasSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages.HasGasSupplyFormPageUpdateGasSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages.RemoveGasCertUploadFormPageUpdateGasSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages.UploadGasCertFormPageUpdateGasSafety
import java.net.URI
import java.nio.file.Path

class UpdateGasSafetyJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
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
    fun `A property can have its gas safety updated to no gas supply`(page: Page) {
        // Navigate to property details and go to compliance tab
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToComplianceInformation()
        propertyDetailsPage.gasSafetyCard.getAction("Change").link.clickAndWait()

        // Has gas supply page
        val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPageUpdateGasSafety::class, urlArguments)
        hasGasSupplyPage.submitHasNoGasSupply()

        // Check gas safety answers page
        val checkAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPageUpdateGasSafety::class, urlArguments)
        assertThat(checkAnswersPage.gasSupplySummaryList.gasSupplyRow.value).containsText("No")
        checkAnswersPage.form.submit()

        // Verify we're back on property details
        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.gasSafetyRow.value).containsText("Exempt")
    }

    @Test
    fun `A property can have its gas safety updated with a valid certificate`(page: Page) {
        // Navigate to property details and go to compliance tab
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToComplianceInformation()
        propertyDetailsPage.gasSafetyCard.getAction("Change").link.clickAndWait()

        // Has gas supply page
        val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPageUpdateGasSafety::class, urlArguments)
        hasGasSupplyPage.submitHasGasSupply()

        // Has gas cert page
        val hasGasCertPage = assertPageIs(page, HasGasCertFormPageUpdateGasSafety::class, urlArguments)
        // The "Provide this later" route should not be available on the update journey
        assertThat(hasGasCertPage.provideThisLaterButton).isHidden()
        hasGasCertPage.submitHasCertificate()

        // Gas cert issue date page
        val issueDatePage = assertPageIs(page, GasCertIssueDateFormPageUpdateGasSafety::class, urlArguments)
        issueDatePage.submitDate(currentDate)

        // Upload gas cert page
        var uploadPage = assertPageIs(page, UploadGasCertFormPageUpdateGasSafety::class, urlArguments)
        uploadPage.uploadGasCertificate(Path.of("src/test/resources/test-files/blank.png"))

        // Check gas cert uploads, add another and remove files
        var checkUploadsPage = assertPageIs(page, CheckGasCertUploadsFormPageUpdateGasSafety::class, urlArguments)
        checkUploadsPage.form.addAnotherButton.clickAndWait()
        uploadPage = assertPageIs(page, UploadGasCertFormPageUpdateGasSafety::class, urlArguments)
        uploadPage.uploadGasCertificate(Path.of("src/test/resources/test-files/blank.png"))
        checkUploadsPage = assertPageIs(page, CheckGasCertUploadsFormPageUpdateGasSafety::class, urlArguments)
        checkUploadsPage.table
            .getClickableCell(0, 2)
            .link
            .clickAndWait()
        val removeGasCertUploadPage = assertPageIs(page, RemoveGasCertUploadFormPageUpdateGasSafety::class, urlArguments)
        removeGasCertUploadPage.form.radios.selectValue("true")
        removeGasCertUploadPage.form.submit()
        checkUploadsPage = assertPageIs(page, CheckGasCertUploadsFormPageUpdateGasSafety::class, urlArguments)
        checkUploadsPage.form.submit()

        // Check gas safety answers page
        val checkAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPageUpdateGasSafety::class, urlArguments)
        assertThat(checkAnswersPage.gasSupplySummaryList.gasSupplyRow.value).containsText("Yes")
        checkAnswersPage.form.submit()

        // Verify we're back on property details
        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.gasSafetyRow.value).containsText("Pending virus scan")
    }

    @Test
    fun `A property can have its gas safety updated with an expired certificate`(page: Page) {
        // Navigate to property details and go to compliance tab
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToComplianceInformation()
        propertyDetailsPage.gasSafetyCard.getAction("Change").link.clickAndWait()

        // Has gas supply page
        val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPageUpdateGasSafety::class, urlArguments)
        hasGasSupplyPage.submitHasGasSupply()

        // Has gas cert page
        val hasGasCertPage = assertPageIs(page, HasGasCertFormPageUpdateGasSafety::class, urlArguments)
        hasGasCertPage.submitHasCertificate()

        // Gas cert issue date page - enter an expired date
        val expiredIssueDate =
            currentDate
                .minus(DatePeriod(years = GAS_SAFETY_CERT_VALIDITY_YEARS))
                .minus(DatePeriod(days = 5))
        val issueDatePage = assertPageIs(page, GasCertIssueDateFormPageUpdateGasSafety::class, urlArguments)
        issueDatePage.submitDate(expiredIssueDate)

        // Gas cert expired page
        val expiredPage = assertPageIs(page, GasCertExpiredFormPageUpdateGasSafety::class, urlArguments)
        expiredPage.form.submit()

        // Check gas safety answers page
        val checkAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPageUpdateGasSafety::class, urlArguments)
        assertThat(checkAnswersPage.gasSupplySummaryList.gasSupplyRow.value).containsText("Yes")
        checkAnswersPage.form.submit()

        // Verify we're back on property details
        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.gasSafetyRow.value).containsText("Expired")
    }
}
