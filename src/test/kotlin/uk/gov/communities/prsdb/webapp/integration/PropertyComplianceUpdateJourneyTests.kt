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
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
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

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 8L

        private val urlArguments = mapOf("propertyOwnershipId" to PROPERTY_OWNERSHIP_ID.toString())

        private val currentDate = DateTimeHelper().getCurrentDateInUK()
    }
}
