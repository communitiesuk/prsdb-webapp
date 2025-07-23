package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.CURRENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.CheckAutoMatchedEpcPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.CheckMatchedEpcPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EpcExemptionConfirmationPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EpcExemptionReasonPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EpcLookupPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EpcNotAutoMatchedPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafeEngineerNumPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyCheckYourAnswersPropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyExemptionConfirmationPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyExemptionOtherReasonPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyExemptionReasonPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyIssueDatePagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyOutdatedPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyUploadConfirmationPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyUploadPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.services.FileUploader
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

class PropertyComplianceUpdateJourneyTests : JourneyTestWithSeedData("data-local.sql") {
    @MockitoBean
    private lateinit var fileUploader: FileUploader

    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    @Test
    fun `User can navigate the gas safety update task if pages are filled in correctly (add new in-date certificate)`(page: Page) {
        // Update certificate or add exemption page
        val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
        updateGasSafetyPage.form.hasNewCertificateRadios.selectValue("true")
        updateGasSafetyPage.form.submit()
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
        updateGasSafetyPage.form.hasNewCertificateRadios.selectValue("true")
        updateGasSafetyPage.form.submit()
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

    @Test
    fun `User can add a new gas safety exemption if the pages are filled in correctly`(page: Page) {
        // Update certificate or add exemption page
        val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
        updateGasSafetyPage.form.hasNewCertificateRadios.selectValue("false")
        updateGasSafetyPage.form.submit()
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

    @Test
    fun `User can add a new gas safety exemption if the pages are filled in correctly (with 'other' exemption reason)`(page: Page) {
        // Update certificate or add exemption page
        val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
        updateGasSafetyPage.form.hasNewCertificateRadios.selectValue("false")
        updateGasSafetyPage.form.submit()
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

    @Test
    fun `User can add a new automatched EPC if the pages are filled in correctly`(page: Page) {
        val propertyOwnershipId = 33L // EPC should be auto-matched to this property ownership ID
        whenever(epcRegisterClient.getByUprn(100090154792L))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    expiryDate = LocalDate(currentDate.year + 5, 1, 5),
                ),
            )

        // Update EPC page
        val updateEpcPage = navigator.goToPropertyComplianceUpdateUpdateEpcPage(propertyOwnershipId)
        updateEpcPage.form.hasNewCertificateRadios.selectValue("true")
        updateEpcPage.form.submit()
        val checkAutoMatchedEpcPage =
            assertPageIs(
                page,
                CheckAutoMatchedEpcPagePropertyComplianceUpdate::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )

        // Check Auto Matched EPC page
        val singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT"
        BaseComponent.assertThat(checkAutoMatchedEpcPage.form.fieldsetHeading).containsText(singleLineAddress)
        assertThat(checkAutoMatchedEpcPage.form.summaryList.addressRow.value).containsText(singleLineAddress)
        assertThat(checkAutoMatchedEpcPage.form.summaryList.energyRatingRow.value).containsText("C")
        assertThat(checkAutoMatchedEpcPage.form.summaryList.expiryDateRow.value).containsText("5 January")
        checkAutoMatchedEpcPage.submitMatchedEpcDetailsCorrect()

        // Epc Check Your Answers page
        assertPageIs(
            page,
            UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )

        // TODO PRSD-1313 - CYA page checks, should return to the Property Record page
    }

    @Test
    fun `User can add a new looked up EPC if the pages are filled in correctly`(page: Page) {
        val propertyOwnershipId = 33L
        val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())
        whenever(epcRegisterClient.getByUprn(100090154792L))
            .thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        // Update EPC page
        val updateEpcPage = navigator.goToPropertyComplianceUpdateUpdateEpcPage(propertyOwnershipId)
        updateEpcPage.form.hasNewCertificateRadios.selectValue("true")
        updateEpcPage.form.submit()
        val epcNotAutomatchedPage = assertPageIs(page, EpcNotAutoMatchedPagePropertyComplianceUpdate::class, urlArguments)

        // Epc Not Auto Matched page
        epcNotAutomatchedPage.continueButton.clickAndWait()
        val epcLookupPage = assertPageIs(page, EpcLookupPagePropertyComplianceUpdate::class, urlArguments)

        // Epc Lookup page
        whenever(epcRegisterClient.getByRrn(CURRENT_EPC_CERTIFICATE_NUMBER))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    expiryDate = LocalDate(currentDate.year + 5, 1, 5),
                ),
            )
        epcLookupPage.submitCurrentEpcNumber()
        val checkMatchedEpcPage = assertPageIs(page, CheckMatchedEpcPagePropertyComplianceUpdate::class, urlArguments)

        // Check Matched EPC page
        checkMatchedEpcPage.submitMatchedEpcDetailsCorrect()

        // Epc Check Your Answers page
        assertPageIs(
            page,
            UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )

        // TODO PRSD-1313 - CYA page checks, should return to the Property Record page
    }

    @Test
    fun `User can add a new EPC exemption if the pages are filled in correctly`(page: Page) {
        // Update EPC page
        val updateEpcPage = navigator.goToPropertyComplianceUpdateUpdateEpcPage(PROPERTY_OWNERSHIP_ID)
        updateEpcPage.form.hasNewCertificateRadios.selectValue("false")
        updateEpcPage.form.submit()
        val epcExemptionReasonPage = assertPageIs(page, EpcExemptionReasonPagePropertyComplianceUpdate::class, urlArguments)

        // EPC Exemption Reason page
        epcExemptionReasonPage.submitExemptionReason(EpcExemptionReason.DUE_FOR_DEMOLITION)
        val epcExemptionConfirmationPage = assertPageIs(page, EpcExemptionConfirmationPagePropertyComplianceUpdate::class, urlArguments)

        // EPC Exemption Confirmation page
        assertThat(
            epcExemptionConfirmationPage.heading,
        ).containsText("You’ve marked this property as not needing an EPC")
        epcExemptionConfirmationPage.saveAndContinueButton.clickAndWait()
        assertPageIs(page, UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // Check Your Answers page
        // TODO PRSD-1313 - check this page, should return to the Property Record page
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 12L

        private val urlArguments = mapOf("propertyOwnershipId" to PROPERTY_OWNERSHIP_ID.toString())

        private val currentDate = DateTimeHelper().getCurrentDateInUK()
    }
}
