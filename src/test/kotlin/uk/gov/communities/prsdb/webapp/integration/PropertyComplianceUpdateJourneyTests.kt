package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrCheckYourAnswersPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrExemptionConfirmationPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrExemptionOtherReasonPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrExemptionPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrExemptionReasonPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrIssueDatePagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrOutdatedPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrUploadConfirmationPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrUploadPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafeEngineerNumPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyCheckYourAnswersPropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyExemptionConfirmationPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyExemptionOtherReasonPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyExemptionPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyExemptionReasonPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyIssueDatePagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyOutdatedPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyUploadConfirmationPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyUploadPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.services.FileUploader

class PropertyComplianceUpdateJourneyTests : JourneyTestWithSeedData("data-local.sql") {
    @MockitoBean
    private lateinit var fileUploader: FileUploader

    @Test
    fun `User can navigate the gas safety update task if pages are filled in correctly (add new in-date certificate)`(page: Page) {
        // Update certificate or add exemption page
        val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
        // TODO: PRSD-1244 - check page content, go to Issue Date only if user has submitted "Add a new gas safety certificate"
        updateGasSafetyPage.continueButton.clickAndWait()
        val gasSafetyIssueDatePage = assertPageIs(page, GasSafetyIssueDatePagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Cert. Issue Date page
        gasSafetyIssueDatePage.submitDate(currentDate)
        val gasSafeEngineerNumPage = assertPageIs(page, GasSafeEngineerNumPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safe Engineer Num. page
        gasSafeEngineerNumPage.submitEngineerNum("1234567")
        val gasSafetyUploadPage = assertPageIs(page, GasSafetyUploadPagePropertyComplianceUpdate::class, urlArguments)

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
        val gasSafetyUploadConfirmationPage =
            assertPageIs(
                page,
                GasSafetyUploadConfirmationPagePropertyComplianceUpdate::class,
                urlArguments,
            )

        // Gas Safety Cert. Upload Confirmation page
        assertThat(gasSafetyUploadConfirmationPage.heading).containsText("Your file is being scanned")
        gasSafetyUploadConfirmationPage.saveAndContinueButton.clickAndWait()
        assertPageIs(page, GasSafetyCheckYourAnswersPropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Check Your Answers page
        // TODO PRSD-1245 - check this page, should return to the Property Record page
    }

    @Test
    fun `User can navigate the gas safety update task if pages are filled in correctly (add new outdated certificate)`(page: Page) {
        // Update certificate or add exemption page
        val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
        // TODO: PRSD-1244 - go to Issue Date only if user has submitted "Add a new gas safety certificate"
        updateGasSafetyPage.continueButton.clickAndWait()
        val gasSafetyIssueDatePage = assertPageIs(page, GasSafetyIssueDatePagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Cert. Issue Date page
        val outdatedIssueDate = currentDate.minus(DatePeriod(years = 1))
        gasSafetyIssueDatePage.submitDate(outdatedIssueDate)
        val gasSafetyOutdatedPage = assertPageIs(page, GasSafetyOutdatedPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Outdated page
        assertThat(gasSafetyOutdatedPage.heading).containsText("Your gas safety certificate is out of date")
        gasSafetyOutdatedPage.saveAndContinueButton.clickAndWait()
        assertPageIs(page, GasSafetyCheckYourAnswersPropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Check Your Answers page
        // TODO PRSD-1245 - check this page, should return to the Property Record page
    }

    @Disabled
    @Test
    fun `User can add a new gas safety exemption if the pages are filled in correctly`(page: Page) {
        // Update certificate or add exemption page
        val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
        // TODO: PRSD-1244 - go to Exemption only if user has submitted "Add an exemption"
        updateGasSafetyPage.continueButton.clickAndWait()
        val gasSafetyExemptionPage = assertPageIs(page, GasSafetyExemptionPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Exemption page
        gasSafetyExemptionPage.submitHasExemption()
        val gasSafetyExemptionReasonPage = assertPageIs(page, GasSafetyExemptionReasonPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Exemption Reason page
        gasSafetyExemptionReasonPage.submitExemptionReason(GasSafetyExemptionReason.NO_GAS_SUPPLY)
        val gasSafetyExemptionConfirmationPage =
            assertPageIs(page, GasSafetyExemptionConfirmationPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Exemption Confirmation page
        assertThat(gasSafetyExemptionConfirmationPage.heading)
            .containsText("You’ve marked this property as not needing a gas safety certificate")
        gasSafetyExemptionConfirmationPage.saveAndContinueButton.clickAndWait()
        assertPageIs(page, GasSafetyCheckYourAnswersPropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Check Your Answers page
        // TODO PRSD-1245 - check this page, should return to the Property Record page
    }

    @Disabled
    @Test
    fun `User can add a new gas safety exemption if the pages are filled in correctly (with 'other' exemption reason)`(page: Page) {
        // Update certificate or add exemption page
        val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
        // TODO: PRSD-1244 - go to Exemption only if user has submitted "Add an exemption"
        updateGasSafetyPage.continueButton.clickAndWait()
        val gasSafetyExemptionPage = assertPageIs(page, GasSafetyExemptionPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Exemption page
        gasSafetyExemptionPage.submitHasExemption()
        val gasSafetyExemptionReasonPage = assertPageIs(page, GasSafetyExemptionReasonPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Exemption Reason page
        gasSafetyExemptionReasonPage.submitExemptionReason(GasSafetyExemptionReason.OTHER)
        val gasSafetyExemptionOtherReasonPage =
            assertPageIs(page, GasSafetyExemptionOtherReasonPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Exemption Other Reason page
        gasSafetyExemptionOtherReasonPage.submitReason("valid reason")
        val gasSafetyExemptionConfirmationPage =
            assertPageIs(page, GasSafetyExemptionConfirmationPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Exemption Confirmation page
        assertThat(gasSafetyExemptionConfirmationPage.heading)
            .containsText("You’ve marked this property as not needing a gas safety certificate")
        gasSafetyExemptionConfirmationPage.saveAndContinueButton.clickAndWait()
        assertPageIs(page, GasSafetyCheckYourAnswersPropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Check Your Answers page
        // TODO PRSD-1245 - check this page, should return to the Property Record page
    }

    @Test
    fun `User can navigate the EICR update task if pages are filled in correctly (add new in-date certificate)`(page: Page) {
        // Update certificate or add exemption page
        val updateEicrPage = navigator.goToPropertyComplianceUpdateUpdateEicrPage(PROPERTY_OWNERSHIP_ID)
        updateEicrPage.continueButton.clickAndWait()
        // TODO: PRSD-1246 - go to Issue Date only if user has submitted "Add a new eicr certificate"
        val eicrIssueDatePage = assertPageIs(page, EicrIssueDatePagePropertyComplianceUpdate::class, urlArguments)

        // EICR Issue Date page
        eicrIssueDatePage.submitDate(currentDate)
        val eicrUploadPage = assertPageIs(page, EicrUploadPagePropertyComplianceUpdate::class, urlArguments)

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
        val eicrUploadConfirmationPage = assertPageIs(page, EicrUploadConfirmationPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Upload Confirmation page
        assertThat(eicrUploadConfirmationPage.heading).containsText("Your file is being scanned")
        eicrUploadConfirmationPage.saveAndContinueButton.clickAndWait()

        assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Check Your Answers page
        // TODO PRSD-1247 - submit page, should return to the Property Record page
    }

    @Test
    fun `User can navigate the EICR update task if pages are filled in correctly (add new expired certificate)`(page: Page) {
        // Update certificate or add exemption page
        val updateEicrPage = navigator.goToPropertyComplianceUpdateUpdateEicrPage(PROPERTY_OWNERSHIP_ID)
        updateEicrPage.continueButton.clickAndWait()
        // TODO: PRSD-1246 - go to Issue Date only if user has submitted "Add a new eicr certificate"
        val eicrIssueDatePage = assertPageIs(page, EicrIssueDatePagePropertyComplianceUpdate::class, urlArguments)

        // EICR Issue Date page
        val outdatedIssueDate = currentDate.minus(DatePeriod(years = 5))
        eicrIssueDatePage.submitDate(outdatedIssueDate)
        val eicrOutdatedPage = assertPageIs(page, EicrOutdatedPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Outdated page
        assertThat(eicrOutdatedPage.heading).containsText("This property’s EICR is out of date")
        eicrOutdatedPage.saveAndContinueButton.clickAndWait()
        assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Check Your Answers page
        // TODO PRSD-1247 - submit page, should return to the Property Record page
    }

    @Disabled
    @Test
    fun `User can add a new EICR exemption if the pages are filled in correctly`(page: Page) {
        // Update certificate or add exemption page
        val updateEicrPage = navigator.goToPropertyComplianceUpdateUpdateEicrPage(PROPERTY_OWNERSHIP_ID)
        updateEicrPage.continueButton.clickAndWait()
        // TODO: PRSD-1246 - go to exemption page if user has submitted "Add a new exemption"
        val eicrExemptionPage = assertPageIs(page, EicrExemptionPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Exemption page
        eicrExemptionPage.submitHasExemption()
        val eicrExemptionReasonPage = assertPageIs(page, EicrExemptionReasonPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Exemption Reason page
        eicrExemptionReasonPage.submitExemptionReason(EicrExemptionReason.LIVE_IN_LANDLORD)
        val eicrExemptionConfirmationPage = assertPageIs(page, EicrExemptionConfirmationPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Exemption Confirmation page
        assertThat(eicrExemptionConfirmationPage.heading).containsText("You’ve marked this property as exempt from needing an EICR")
        eicrExemptionConfirmationPage.saveAndContinueButton.clickAndWait()

        assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Check Your Answers page
        // TODO: PRSD-1247 - submit page, should return to the Property Record page
    }

    @Disabled
    @Test
    fun `User can add a new EICR exemption if the pages are filled in correctly (with 'other' exemption reason)`(page: Page) {
        // Update certificate or add exemption page
        val updateEicrPage = navigator.goToPropertyComplianceUpdateUpdateEicrPage(PROPERTY_OWNERSHIP_ID)
        updateEicrPage.continueButton.clickAndWait()
        // TODO: PRSD-1246 - go to exemption page if user has submitted "Add a new exemption"
        val eicrExemptionPage = assertPageIs(page, EicrExemptionPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Exemption page
        eicrExemptionPage.submitHasExemption()
        val eicrExemptionReasonPage = assertPageIs(page, EicrExemptionReasonPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Exemption Reason page
        eicrExemptionReasonPage.submitExemptionReason(EicrExemptionReason.OTHER)
        val eicrExemptionOtherReasonPage = assertPageIs(page, EicrExemptionOtherReasonPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Exemption Other Reason page
        eicrExemptionOtherReasonPage.submitReason("valid reason")
        val eicrExemptionConfirmationPage = assertPageIs(page, EicrExemptionConfirmationPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Exemption Confirmation page
        assertThat(eicrExemptionConfirmationPage.heading).containsText("You’ve marked this property as exempt from needing an EICR")
        eicrExemptionConfirmationPage.saveAndContinueButton.clickAndWait()

        assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Check Your Answers page
        // TODO: PRSD-1247 - submit page, should return to the Property Record page
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 9L

        private val urlArguments = mapOf("propertyOwnershipId" to PROPERTY_OWNERSHIP_ID.toString())

        private val currentDate = DateTimeHelper().getCurrentDateInUK()
    }
}
