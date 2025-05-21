package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.CheckMatchedEpcPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionConfirmationPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionMissingPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrIssueDatePagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrOutdatedPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrUploadConfirmationPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrUploadPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcExemptionConfirmationPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcExemptionReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcMissingPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.FireSafetyDeclarationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafeEngineerNumPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionConfirmationPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionMissingPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyIssueDatePagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyOutdatedPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyUploadConfirmationPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyUploadPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.TaskListPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.services.FileUploader

class PropertyComplianceJourneyTests : JourneyTestWithSeedData("data-local.sql") {
    @MockitoBean
    private lateinit var fileUploader: FileUploader

    @Test
    fun `User can navigate whole journey if pages are filled in correctly (in-date certs)`(page: Page) {
        // Start page
        val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
        assertThat(startPage.heading).containsText("Compliance certificates")
        startPage.startButton.clickAndWait()
        val taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

        // Task List page
        taskListPage.clickUploadTaskWithName("Upload the gas safety certificate")
        val gasSafetyPage = assertPageIs(page, GasSafetyPagePropertyCompliance::class, urlArguments)

        // Gas Safety page
        gasSafetyPage.submitHasCert()
        val gasSafetyIssueDatePage = assertPageIs(page, GasSafetyIssueDatePagePropertyCompliance::class, urlArguments)

        // Gas Safety Cert. Issue Date page
        gasSafetyIssueDatePage.submitDate(currentDate)
        val gasSafeEngineerNumPage = assertPageIs(page, GasSafeEngineerNumPagePropertyCompliance::class, urlArguments)

        // Gas Safe Engineer Num. page
        gasSafeEngineerNumPage.submitEngineerNum("1234567")
        val gasSafetyUploadPage = assertPageIs(page, GasSafetyUploadPagePropertyCompliance::class, urlArguments)

        // Gas Safety Cert. Upload page
        whenever(
            fileUploader.uploadFile(
                eq(
                    PropertyComplianceJourneyHelper.getCertFilename(
                        PROPERTY_OWNERSHIP_ID,
                        PropertyComplianceStepId.GasSafetyUpload.urlPathSegment,
                        "validFile.png",
                    ),
                ),
                any(),
            ),
        ).thenReturn(true)
        gasSafetyUploadPage.uploadCertificate("validFile.png")
        val gasSafetyUploadConfirmationPage = assertPageIs(page, GasSafetyUploadConfirmationPagePropertyCompliance::class, urlArguments)

        // Gas Safety Cert. Upload Confirmation page
        assertThat(gasSafetyUploadConfirmationPage.heading).containsText("Your file is being scanned")
        gasSafetyUploadConfirmationPage.saveAndContinueButton.clickAndWait()
        val eicrPage = assertPageIs(page, EicrPagePropertyCompliance::class, urlArguments)

        // EICR page
        eicrPage.submitHasCert()
        val eicrIssueDatePage = assertPageIs(page, EicrIssueDatePagePropertyCompliance::class, urlArguments)

        // EICR Issue Date page
        eicrIssueDatePage.submitDate(currentDate)
        val eicrUploadPage = assertPageIs(page, EicrUploadPagePropertyCompliance::class, urlArguments)

        // EICR Upload page
        whenever(
            fileUploader.uploadFile(
                eq(
                    PropertyComplianceJourneyHelper.getCertFilename(
                        PROPERTY_OWNERSHIP_ID,
                        PropertyComplianceStepId.EicrUpload.urlPathSegment,
                        "validFile.png",
                    ),
                ),
                any(),
            ),
        ).thenReturn(true)
        eicrUploadPage.uploadCertificate("validFile.png")
        val eicrUploadConfirmationPage = assertPageIs(page, EicrUploadConfirmationPagePropertyCompliance::class, urlArguments)

        // EICR Upload Confirmation page
        assertThat(eicrUploadConfirmationPage.heading).containsText("Your file is being scanned")
        eicrUploadConfirmationPage.saveAndContinueButton.clickAndWait()
        val epcPage = assertPageIs(page, EpcPagePropertyCompliance::class, urlArguments)

        // EPC page
        epcPage.submitHasCert()
        assertPageIs(page, CheckMatchedEpcPagePropertyCompliance::class, urlArguments)

        // TODO PRSD-1132: continue test
    }

    @Test
    fun `User can navigate whole journey if pages are filled in correctly (outdated certs)`(page: Page) {
        // Start page
        val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
        assertThat(startPage.heading).containsText("Compliance certificates")
        startPage.startButton.clickAndWait()
        val taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

        // Task List page
        taskListPage.clickUploadTaskWithName("Upload the gas safety certificate")
        val gasSafetyPage = assertPageIs(page, GasSafetyPagePropertyCompliance::class, urlArguments)

        // Gas Safety page
        gasSafetyPage.submitHasCert()
        val gasSafetyIssueDatePage = assertPageIs(page, GasSafetyIssueDatePagePropertyCompliance::class, urlArguments)

        // Gas Safety Cert Issue Date page
        val outdatedIssueDate = currentDate.minus(DatePeriod(years = 1))
        gasSafetyIssueDatePage.submitDate(outdatedIssueDate)
        val gasSafetyOutdatedPage = assertPageIs(page, GasSafetyOutdatedPagePropertyCompliance::class, urlArguments)

        // Gas Safety Outdated page
        assertThat(gasSafetyOutdatedPage.heading).containsText("Your gas safety certificate is out of date")
        gasSafetyOutdatedPage.saveAndContinueToEicrButton.clickAndWait()
        val eicrPage = assertPageIs(page, EicrPagePropertyCompliance::class, urlArguments)

        // EICR page
        eicrPage.submitHasCert()
        val eicrIssueDatePage = assertPageIs(page, EicrIssueDatePagePropertyCompliance::class, urlArguments)

        // EICR Issue Date page
        eicrIssueDatePage.submitDate(currentDate.minus(DatePeriod(years = 5)))
        val eicrOutdatedPage = assertPageIs(page, EicrOutdatedPagePropertyCompliance::class, urlArguments)

        // EICR Outdated page
        assertThat(eicrOutdatedPage.heading).containsText("This property’s EICR is out of date")
        eicrOutdatedPage.saveAndContinueToEpcButton.clickAndWait()
        val epcPage = assertPageIs(page, EpcPagePropertyCompliance::class, urlArguments)

        // EPC page
        epcPage.submitHasCert()
        assertPageIs(page, CheckMatchedEpcPagePropertyCompliance::class, urlArguments)

        // TODO PRSD-1132: continue test
    }

    @Test
    fun `User can navigate whole journey if pages are filled in correctly (no certs, exemptions)`(page: Page) {
        // Start page
        val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
        assertThat(startPage.heading).containsText("Compliance certificates")
        startPage.startButton.clickAndWait()
        val taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

        // Task List page
        taskListPage.clickUploadTaskWithName("Upload the gas safety certificate")
        val gasSafetyPage = assertPageIs(page, GasSafetyPagePropertyCompliance::class, urlArguments)

        // Gas Safety page
        gasSafetyPage.submitHasNoCert()
        val gasSafetyExemptionPage = assertPageIs(page, GasSafetyExemptionPagePropertyCompliance::class, urlArguments)

        // Gas Safety Exemption page
        gasSafetyExemptionPage.submitHasExemption()
        val gasSafetyExemptionReasonPage = assertPageIs(page, GasSafetyExemptionReasonPagePropertyCompliance::class, urlArguments)

        // Gas Safety Exemption Reason page
        gasSafetyExemptionReasonPage.submitExemptionReason(GasSafetyExemptionReason.NO_GAS_SUPPLY)
        val gasSafetyExemptionConfirmationPage =
            assertPageIs(page, GasSafetyExemptionConfirmationPagePropertyCompliance::class, urlArguments)

        // Gas Safety Exemption Confirmation page
        assertThat(gasSafetyExemptionConfirmationPage.heading)
            .containsText("You’ve marked this property as not needing a gas safety certificate")
        gasSafetyExemptionConfirmationPage.saveAndContinueToEicrButton.clickAndWait()
        val eicrPage = assertPageIs(page, EicrPagePropertyCompliance::class, urlArguments)

        // EICR page
        eicrPage.submitHasNoCert()
        val eicrExemptionPage = assertPageIs(page, EicrExemptionPagePropertyCompliance::class, urlArguments)

        // EICR Exemption page
        eicrExemptionPage.submitHasExemption()
        val eicrExemptionReasonPage = assertPageIs(page, EicrExemptionReasonPagePropertyCompliance::class, urlArguments)

        // EICR Exemption Reason page
        eicrExemptionReasonPage.submitExemptionReason(EicrExemptionReason.LIVE_IN_LANDLORD)
        val eicrExemptionConfirmationPage =
            assertPageIs(page, EicrExemptionConfirmationPagePropertyCompliance::class, urlArguments)

        // EICR Exemption Confirmation page
        assertThat(eicrExemptionConfirmationPage.heading).containsText("You’ve marked this property as exempt from needing an EICR")
        eicrExemptionConfirmationPage.saveAndContinueToEpcButton.clickAndWait()
        val epcPage = assertPageIs(page, EpcPagePropertyCompliance::class, urlArguments)

        // EPC page
        epcPage.submitCertNotRequired()
        val epcExemptionReasonPage = assertPageIs(page, EpcExemptionReasonPagePropertyCompliance::class, urlArguments)

        // EPC exemption reason page
        epcExemptionReasonPage.submitExemptionReason(EpcExemptionReason.LISTED_BUILDING)
        val epcExemptionConfirmationPage = assertPageIs(page, EpcExemptionConfirmationPagePropertyCompliance::class, urlArguments)

        // EPC Exemption Confirmation page
        assertThat(epcExemptionConfirmationPage.heading)
            .containsText("You’ve marked this property as not needing an EPC")
        epcExemptionConfirmationPage.saveAndContinueToLandlordResponsibilitiesButton.clickAndWait()
        assertPageIs(page, FireSafetyDeclarationPage::class, urlArguments)

        // TODO PRSD-1150 - continue test
    }

    @Test
    fun `User can navigate whole journey if pages are filled in correctly (no certs, no exemptions)`(page: Page) {
        // Start page
        val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
        assertThat(startPage.heading).containsText("Compliance certificates")
        startPage.startButton.clickAndWait()
        val taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

        // Task List page
        taskListPage.clickUploadTaskWithName("Upload the gas safety certificate")
        val gasSafetyPage = assertPageIs(page, GasSafetyPagePropertyCompliance::class, urlArguments)

        // Gas Safety page
        gasSafetyPage.submitHasNoCert()
        val gasSafetyExemptionPage = assertPageIs(page, GasSafetyExemptionPagePropertyCompliance::class, urlArguments)

        // Gas Safety Exemption page
        gasSafetyExemptionPage.submitHasNoExemption()
        val gasSafetyExemptionMissingPage = assertPageIs(page, GasSafetyExemptionMissingPagePropertyCompliance::class, urlArguments)

        // Gas Safety Exemption Missing page
        assertThat(gasSafetyExemptionMissingPage.heading).containsText("You must get a valid gas safety certificate for this property")
        gasSafetyExemptionMissingPage.saveAndContinueToEicrButton.clickAndWait()
        val eicrPage = assertPageIs(page, EicrPagePropertyCompliance::class, urlArguments)

        // EICR page
        eicrPage.submitHasNoCert()
        val eicrExemptionPage = assertPageIs(page, EicrExemptionPagePropertyCompliance::class, urlArguments)

        // EICR Exemption page
        eicrExemptionPage.submitHasNoExemption()
        val eicrExemptionMissingPage = assertPageIs(page, EicrExemptionMissingPagePropertyCompliance::class, urlArguments)

        // EICR Exemption Missing page
        assertThat(eicrExemptionMissingPage.heading).containsText("You must get a valid EICR for this property")
        eicrExemptionMissingPage.saveAndContinueToEicrButton.clickAndWait()
        val epcPage = assertPageIs(page, EpcPagePropertyCompliance::class, urlArguments)

        // EPC page
        epcPage.submitHasNoCert()
        val epcMissingPage = assertPageIs(page, EpcMissingPagePropertyCompliance::class, urlArguments)

        // EPC missing page
        epcMissingPage.saveAndContinueButton.clickAndWait()
        assertPageIs(page, FireSafetyDeclarationPage::class, urlArguments)

        // TODO: PRSD-1150 continue test
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 1L
        private val urlArguments = mapOf("propertyOwnershipId" to PROPERTY_OWNERSHIP_ID.toString())

        private val currentDate = DateTimeHelper().getCurrentDateInUK()
    }
}
