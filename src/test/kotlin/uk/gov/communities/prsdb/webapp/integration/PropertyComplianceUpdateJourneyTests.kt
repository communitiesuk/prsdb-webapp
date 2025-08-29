package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.CURRENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.NONEXISTENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.SUPERSEDED_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.CheckAutoMatchedEpcPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.CheckMatchedEpcPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrCheckYourAnswersPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrExemptionConfirmationPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrExemptionOtherReasonPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrExemptionReasonPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrIssueDatePagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrOutdatedPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrUploadConfirmationPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EicrUploadPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EpcExemptionConfirmationPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EpcExemptionReasonPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EpcExpiredPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EpcExpiryCheckPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EpcLookupPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EpcNotAutoMatchedPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EpcNotFoundPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.EpcSupersededPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafeEngineerNumPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyCheckYourAnswersPropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyExemptionConfirmationPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyExemptionOtherReasonPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyExemptionReasonPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyIssueDatePagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyOutdatedPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyUploadConfirmationPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.GasSafetyUploadPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.LowEnergyRatingPageMeesUpdatePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.LowEnergyRatingPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.MeesExemptionCheckPageMeesUpdatePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.MeesExemptionCheckPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.MeesExemptionConfirmationPageMeesUpdatePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.MeesExemptionConfirmationPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.MeesExemptionReasonPageMeesUpdatePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.MeesExemptionReasonPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.UpdateEicrPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.UpdateEpcPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.UpdateGasSafetyPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.UpdateMeesCheckYourAnswersPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.models.dataModels.UploadedFileLocator
import uk.gov.communities.prsdb.webapp.services.FileUploader
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PropertyComplianceUpdateJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    // Date format like "1 January 2023"
    private val dateFormat =
        LocalDate
            .Format {
                dayOfMonth(Padding.NONE)
                char(' ')
                monthName(MonthNames.ENGLISH_FULL)
                char(' ')
                year()
            }

    @MockitoBean
    private lateinit var fileUploader: FileUploader

    @Test
    fun `User can navigate the gas safety update task if pages are filled in correctly (add new in-date certificate)`(page: Page) {
        // Update certificate or add exemption page
        val updateGasSafetyPage = startUpdateGasSafetyTask(page)
        updateGasSafetyPage.submitHasNewCertificate()
        val gasSafetyIssueDatePage =
            assertPageIs(page, GasSafetyIssueDatePagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Cert. Issue Date page
        gasSafetyIssueDatePage.submitDate(currentDate)
        val gasSafeEngineerNumPage =
            assertPageIs(page, GasSafeEngineerNumPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safe Engineer Num. page
        gasSafeEngineerNumPage.submitEngineerNum("1234567")
        val gasSafetyUploadPage = assertPageIs(page, GasSafetyUploadPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Cert. Upload page
        whenever(
            fileUploader.uploadFile(
                any(),
                any(),
            ),
        ).thenReturn(UploadedFileLocator("validGasSafety", "mockETag", "mockVersionId"))

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
        val cyaPage = assertPageIs(page, GasSafetyCheckYourAnswersPropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Check Your Answers page
        assertThat(cyaPage.form.summaryList.gasSafetyRow.value).containsText("Pending virus scan")
        assertThat(cyaPage.form.summaryList.issueDateRow.value).containsText(dateFormat.format(currentDate))
        assertThat(cyaPage.form.summaryList.engineerRow.value).containsText("1234567")
        cyaPage.form.submit()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can navigate the gas safety update task if pages are filled in correctly (add new outdated certificate)`(page: Page) {
        // Update certificate or add exemption page
        val updateGasSafetyPage = startUpdateGasSafetyTask(page)
        updateGasSafetyPage.submitHasNewCertificate()
        val gasSafetyIssueDatePage =
            assertPageIs(page, GasSafetyIssueDatePagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Cert. Issue Date page
        val outdatedIssueDate = currentDate.minus(DatePeriod(years = 1))
        gasSafetyIssueDatePage.submitDate(outdatedIssueDate)
        val gasSafetyOutdatedPage =
            assertPageIs(page, GasSafetyOutdatedPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Outdated page
        assertThat(gasSafetyOutdatedPage.heading).containsText("This property’s gas safety certificate has expired")
        gasSafetyOutdatedPage.saveAndContinueButton.clickAndWait()
        val cyaPage = assertPageIs(page, GasSafetyCheckYourAnswersPropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Check Your Answers page
        assertThat(cyaPage.form.summaryList.gasSafetyRow.value).containsText("Expired")
        assertThat(cyaPage.form.summaryList.issueDateRow.value).containsText(dateFormat.format(outdatedIssueDate))
        cyaPage.form.submit()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can add a new gas safety exemption if the pages are filled in correctly`(page: Page) {
        // Update certificate or add exemption page
        val updateGasSafetyPage = startUpdateGasSafetyTask(page)
        updateGasSafetyPage.submitHasNewExemption()
        val gasSafetyExemptionReasonPage =
            assertPageIs(page, GasSafetyExemptionReasonPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Exemption Reason page
        gasSafetyExemptionReasonPage.submitExemptionReason(GasSafetyExemptionReason.NO_GAS_SUPPLY)
        val gasSafetyExemptionConfirmationPage =
            assertPageIs(page, GasSafetyExemptionConfirmationPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Exemption Confirmation page
        assertThat(gasSafetyExemptionConfirmationPage.heading)
            .containsText("You’ve marked this property as not needing a gas safety certificate")
        gasSafetyExemptionConfirmationPage.saveAndContinueButton.clickAndWait()
        assertPageIs(page, GasSafetyCheckYourAnswersPropertyComplianceUpdate::class, urlArguments)

        val cyaPage = assertPageIs(page, GasSafetyCheckYourAnswersPropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Check Your Answers page
        assertThat(cyaPage.form.summaryList.gasSafetyRow.value).containsText("Not required")
        assertThat(cyaPage.form.summaryList.exemptionRow.value).containsText("It does not have a gas supply")
        cyaPage.form.submit()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can add a new gas safety exemption if the pages are filled in correctly (with 'other' exemption reason)`(page: Page) {
        // Update certificate or add exemption page
        val updateGasSafetyPage = startUpdateGasSafetyTask(page)
        updateGasSafetyPage.submitHasNewExemption()
        val gasSafetyExemptionReasonPage =
            assertPageIs(page, GasSafetyExemptionReasonPagePropertyComplianceUpdate::class, urlArguments)

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

        val cyaPage = assertPageIs(page, GasSafetyCheckYourAnswersPropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Check Your Answers page
        assertThat(cyaPage.form.summaryList.gasSafetyRow.value).containsText("Not required")
        assertThat(cyaPage.form.summaryList.exemptionRow.value).containsText("Other")
        assertThat(cyaPage.form.summaryList.exemptionRow.value).containsText("valid reason")
        cyaPage.form.submit()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can navigate the EICR update task if pages are filled in correctly (add new in-date certificate)`(page: Page) {
        // Update certificate or add exemption page
        val updateEicrPage = startEicrUpdateTask(page)
        updateEicrPage.submitHasNewCertificate()
        val eicrIssueDatePage = assertPageIs(page, EicrIssueDatePagePropertyComplianceUpdate::class, urlArguments)

        // EICR Issue Date page
        eicrIssueDatePage.submitDate(currentDate)
        val eicrUploadPage = assertPageIs(page, EicrUploadPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Upload page
        whenever(
            fileUploader.uploadFile(
                any(),
                any(),
            ),
        ).thenReturn(UploadedFileLocator("validEicr", "mockETag", "mockVersionId"))

        eicrUploadPage.uploadCertificate("validFile.png")
        val eicrUploadConfirmationPage =
            assertPageIs(page, EicrUploadConfirmationPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Upload Confirmation page
        assertThat(eicrUploadConfirmationPage.heading).containsText("Your file is being scanned")
        eicrUploadConfirmationPage.saveAndContinueButton.clickAndWait()

        assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Check Your Answers page
        val cyaPage = assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)
        assertThat(cyaPage.form.summaryList.eicrRow.value).containsText("Pending virus scan")
        assertThat(cyaPage.form.summaryList.issueDateRow.value).containsText(dateFormat.format(currentDate))
        cyaPage.form.submit()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can navigate the EICR update task if pages are filled in correctly (add new expired certificate)`(page: Page) {
        // Update certificate or add exemption page
        val updateEicrPage = startEicrUpdateTask(page)
        updateEicrPage.submitHasNewCertificate()
        val eicrIssueDatePage = assertPageIs(page, EicrIssueDatePagePropertyComplianceUpdate::class, urlArguments)

        // EICR Issue Date page
        val outdatedIssueDate = currentDate.minus(DatePeriod(years = 5))
        eicrIssueDatePage.submitDate(outdatedIssueDate)
        val eicrOutdatedPage = assertPageIs(page, EicrOutdatedPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Outdated page
        assertThat(eicrOutdatedPage.heading).containsText("This property’s Electrical Installation Condition Report (EICR) has expired")
        eicrOutdatedPage.saveAndContinueButton.clickAndWait()
        assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Check Your Answers page
        val cyaPage = assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)
        assertThat(cyaPage.form.summaryList.eicrRow.value).containsText("Expired")
        assertThat(cyaPage.form.summaryList.issueDateRow.value).containsText(dateFormat.format(outdatedIssueDate))
        cyaPage.form.submit()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can add a new EICR exemption if the pages are filled in correctly`(page: Page) {
        // Update certificate or add exemption page
        val updateEicrPage = startEicrUpdateTask(page)
        updateEicrPage.submitHasNewExemption()
        val eicrExemptionReasonPage =
            assertPageIs(page, EicrExemptionReasonPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Exemption Reason page
        eicrExemptionReasonPage.submitExemptionReason(EicrExemptionReason.LONG_LEASE)
        val eicrExemptionConfirmationPage =
            assertPageIs(page, EicrExemptionConfirmationPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Exemption Confirmation page
        assertThat(eicrExemptionConfirmationPage.heading)
            .containsText("You’ve marked this property as exempt from needing an Electrical Installation Condition Report (EICR)")
        eicrExemptionConfirmationPage.saveAndContinueButton.clickAndWait()

        assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Check Your Answers page
        val cyaPage = assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)
        assertThat(cyaPage.form.summaryList.eicrRow.value).containsText("Not required")
        assertThat(cyaPage.form.summaryList.exemptionRow.value).containsText("The current tenancy lease has lasted 7 years or more")
        cyaPage.form.submit()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can add a new EICR exemption if the pages are filled in correctly (with 'other' exemption reason)`(page: Page) {
        // Update certificate or add exemption page
        val updateEicrPage = startEicrUpdateTask(page)
        updateEicrPage.submitHasNewExemption()
        val eicrExemptionReasonPage =
            assertPageIs(page, EicrExemptionReasonPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Exemption Reason page
        eicrExemptionReasonPage.submitExemptionReason(EicrExemptionReason.OTHER)
        val eicrExemptionOtherReasonPage =
            assertPageIs(page, EicrExemptionOtherReasonPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Exemption Other Reason page
        eicrExemptionOtherReasonPage.submitReason("valid reason")
        val eicrExemptionConfirmationPage =
            assertPageIs(page, EicrExemptionConfirmationPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Exemption Confirmation page
        assertThat(eicrExemptionConfirmationPage.heading)
            .containsText("You’ve marked this property as exempt from needing an Electrical Installation Condition Report (EICR)")
        eicrExemptionConfirmationPage.saveAndContinueButton.clickAndWait()

        assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Check Your Answers page
        val cyaPage = assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)
        assertThat(cyaPage.form.summaryList.eicrRow.value).containsText("Not required")
        assertThat(cyaPage.form.summaryList.exemptionRow.value).containsText("Other")
        assertThat(cyaPage.form.summaryList.exemptionRow.value).containsText("valid reason")
        cyaPage.form.submit()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can add an automatched EPC and MEES exemption if the pages are filled in correctly`(page: Page) {
        // Update EPC page
        val updateEpcPage = startUpdateEpcTask(page)
        val expiryDate = LocalDate(currentDate.year + 5, 1, 5)
        whenever(epcRegisterClient.getByUprn(PROPERTY_33_UPRN))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    expiryDate = expiryDate,
                    energyRating = "F",
                ),
            )
        updateEpcPage.submitHasNewCertificate()
        val checkAutoMatchedEpcPage = assertPageIs(page, CheckAutoMatchedEpcPagePropertyComplianceUpdate::class, urlArguments)

        // Check Auto Matched EPC page
        val singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT"
        BaseComponent.assertThat(checkAutoMatchedEpcPage.form.fieldsetHeading).containsText(singleLineAddress)
        assertThat(checkAutoMatchedEpcPage.form.summaryList.addressRow.value).containsText(singleLineAddress)
        assertThat(checkAutoMatchedEpcPage.form.summaryList.energyRatingRow.value).containsText("F")
        assertThat(checkAutoMatchedEpcPage.form.summaryList.expiryDateRow.value).containsText("5 January")
        checkAutoMatchedEpcPage.submitMatchedEpcDetailsCorrect()
        val meesExemptionCheckPage = assertPageIs(page, MeesExemptionCheckPagePropertyComplianceUpdate::class, urlArguments)

        // MEES exemption check page
        meesExemptionCheckPage.submitHasExemption()
        val meesExemptionReasonPage =
            assertPageIs(
                page,
                MeesExemptionReasonPagePropertyComplianceUpdate::class,
                urlArguments,
            )

        // MEES exemption reason page
        meesExemptionReasonPage.submitExemptionReason(MeesExemptionReason.HIGH_COST)
        val meesExemptionConfirmationPage = assertPageIs(page, MeesExemptionConfirmationPagePropertyComplianceUpdate::class, urlArguments)

        // MEES exemption confirmation page
        meesExemptionConfirmationPage.saveAndContinueButton.clickAndWait()
        val checkYourEpcAnswersPage = assertPageIs(page, UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        assertThat(checkYourEpcAnswersPage.form.summaryList.epcRow.value).containsText("View EPC")
        assertThat(checkYourEpcAnswersPage.form.summaryList.expiryDateRow.value).containsText(dateFormat.format(expiryDate))
        assertThat(checkYourEpcAnswersPage.form.summaryList.energyRatingRow.value).containsText("F")
        assertThat(checkYourEpcAnswersPage.form.summaryList.meesExemptionRow.value).containsText("High cost exemption")

        checkYourEpcAnswersPage.form.submit()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can add a new looked up EPC if the pages are filled in correctly`(page: Page) {
        // Update EPC page
        val updateEpcPage = startUpdateEpcTask(page)
        whenever(epcRegisterClient.getByUprn(PROPERTY_33_UPRN))
            .thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
        updateEpcPage.submitHasNewCertificate()
        val epcNotAutomatchedPage = assertPageIs(page, EpcNotAutoMatchedPagePropertyComplianceUpdate::class, urlArguments)

        // Epc Not Auto Matched page
        epcNotAutomatchedPage.continueButton.clickAndWait()
        val epcLookupPage = assertPageIs(page, EpcLookupPagePropertyComplianceUpdate::class, urlArguments)

        // Epc Lookup page
        val expiryDate = LocalDate(currentDate.year + 5, 1, 5)
        whenever(epcRegisterClient.getByRrn(CURRENT_EPC_CERTIFICATE_NUMBER))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    expiryDate = expiryDate,
                    energyRating = "C",
                ),
            )
        epcLookupPage.submitCurrentEpcNumber()
        val checkMatchedEpcPage = assertPageIs(page, CheckMatchedEpcPagePropertyComplianceUpdate::class, urlArguments)

        // Check Matched EPC page
        checkMatchedEpcPage.submitMatchedEpcDetailsCorrect()

        // Epc Check Your Answers page
        val checkYourEpcAnswersPage = assertPageIs(page, UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        assertThat(checkYourEpcAnswersPage.form.summaryList.epcRow.value).containsText("View EPC")
        assertThat(checkYourEpcAnswersPage.form.summaryList.expiryDateRow.value).containsText(dateFormat.format(expiryDate))
        assertThat(checkYourEpcAnswersPage.form.summaryList.energyRatingRow.value).containsText("C")

        checkYourEpcAnswersPage.form.submit()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can add a new EPC exemption if the pages are filled in correctly`(page: Page) {
        // Update EPC page
        val updateEpcPage = startUpdateEpcTask(page)
        updateEpcPage.submitHasNewExemption()
        val epcExemptionReasonPage = assertPageIs(page, EpcExemptionReasonPagePropertyComplianceUpdate::class, urlArguments)

        // EPC Exemption Reason page
        epcExemptionReasonPage.submitExemptionReason(EpcExemptionReason.DUE_FOR_DEMOLITION)
        val epcExemptionConfirmationPage = assertPageIs(page, EpcExemptionConfirmationPagePropertyComplianceUpdate::class, urlArguments)

        // EPC Exemption Confirmation page
        assertThat(
            epcExemptionConfirmationPage.heading,
        ).containsText("You’ve marked this property as not needing an energy performance certificate (EPC)")
        epcExemptionConfirmationPage.saveAndContinueButton.clickAndWait()

        // Epc Check Your Answers page
        val checkYourEpcAnswersPage = assertPageIs(page, UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        assertThat(checkYourEpcAnswersPage.form.summaryList.epcRow.value).containsText("Not required")
        assertThat(checkYourEpcAnswersPage.form.summaryList.exemptionReasonRow.value)
            .containsText("You can demonstrate that the building is due to be demolished")

        checkYourEpcAnswersPage.form.submit()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can navigate the journey when EPC lookup does not find an EPC`(page: Page) {
        // Update EPC page
        val updateEpcPage = startUpdateEpcTask(page)
        whenever(epcRegisterClient.getByUprn(PROPERTY_33_UPRN))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    expiryDate = LocalDate(currentDate.year + 5, 1, 5),
                    energyRating = "F",
                ),
            )
        updateEpcPage.submitHasNewCertificate()
        val checkAutoMatchedEpcPage = assertPageIs(page, CheckAutoMatchedEpcPagePropertyComplianceUpdate::class, urlArguments)

        // Check Auto Matched EPC page
        checkAutoMatchedEpcPage.submitMatchedEpcDetailsIncorrect()
        var epcLookupPage = assertPageIs(page, EpcLookupPagePropertyComplianceUpdate::class, urlArguments)

        // Epc Lookup page
        whenever(
            epcRegisterClient.getByRrn(NONEXISTENT_EPC_CERTIFICATE_NUMBER),
        ).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
        epcLookupPage.submitNonexistentEpcNumber()
        var epcNotFoundPage = assertPageIs(page, EpcNotFoundPagePropertyComplianceUpdate::class, urlArguments)

        // Epc Not Found page - search again
        epcNotFoundPage.searchAgainButton.clickAndWait()
        epcLookupPage = assertPageIs(page, EpcLookupPagePropertyComplianceUpdate::class, urlArguments)

        // Epc Lookup page
        epcLookupPage.submitNonexistentEpcNumber()
        epcNotFoundPage = assertPageIs(page, EpcNotFoundPagePropertyComplianceUpdate::class, urlArguments)

        // Epc Not Found page - continue
        epcNotFoundPage.continueButton.clickAndWait()
        assertPageIs(page, UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // Epc Check Your Answers page
        val checkYourEpcAnswersPage = assertPageIs(page, UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        assertThat(checkYourEpcAnswersPage.form.summaryList.epcRow.value).containsText("Not added")
        assertThat(checkYourEpcAnswersPage.form.summaryList.exemptionReasonRow.value).containsText("None")

        checkYourEpcAnswersPage.form.submit()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can add an expired EPC after EPC lookup finds a superseded EPC`(page: Page) {
        // Update EPC page
        val updateEpcPage = startUpdateEpcTask(page)
        whenever(epcRegisterClient.getByUprn(PROPERTY_33_UPRN))
            .thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
        updateEpcPage.submitHasNewCertificate()
        val epcNotAutomatchedPage = assertPageIs(page, EpcNotAutoMatchedPagePropertyComplianceUpdate::class, urlArguments)

        // Epc Not Auto Matched page
        epcNotAutomatchedPage.continueButton.clickAndWait()
        val epcLookupPage = assertPageIs(page, EpcLookupPagePropertyComplianceUpdate::class, urlArguments)

        // Epc Lookup page
        whenever(epcRegisterClient.getByRrn(SUPERSEDED_EPC_CERTIFICATE_NUMBER)).thenReturn(
            MockEpcData.createEpcRegisterClientEpcFoundResponse(
                certificateNumber = SUPERSEDED_EPC_CERTIFICATE_NUMBER,
                expiryDate = MockEpcData.expiryDateInThePast,
                latestCertificateNumberForThisProperty = CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER,
            ),
        )
        epcLookupPage.submitSupersededEpcNumber()
        val epcSupersededPage = assertPageIs(page, EpcSupersededPagePropertyComplianceUpdate::class, urlArguments)
        assertTrue(epcSupersededPage.page.content().contains(CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER))
        whenever(epcRegisterClient.getByRrn(CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    expiryDate = MockEpcData.expiryDateInThePast,
                    energyRating = "C",
                ),
            )
        epcSupersededPage.continueButton.clickAndWait()
        val checkMatchedEpcPage = assertPageIs(page, CheckMatchedEpcPagePropertyComplianceUpdate::class, urlArguments)

        // Check Matched EPC page
        val singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT"
        val expectedExpiryDate = dateFormat.format(MockEpcData.expiryDateInThePast)
        BaseComponent.assertThat(checkMatchedEpcPage.form.fieldsetHeading).containsText(singleLineAddress)
        assertThat(checkMatchedEpcPage.form.summaryList.addressRow.value).containsText(singleLineAddress)
        assertThat(checkMatchedEpcPage.form.summaryList.energyRatingRow.value).containsText("C")
        assertThat(checkMatchedEpcPage.form.summaryList.expiryDateRow.value).containsText(expectedExpiryDate)
        checkMatchedEpcPage.submitMatchedEpcDetailsCorrect()
        val expiryCheckPage = assertPageIs(page, EpcExpiryCheckPagePropertyComplianceUpdate::class, urlArguments)

        // EPC Expiry Check page
        assertTrue(expiryCheckPage.page.content().contains("5 January 2022"))
        expiryCheckPage.submitTenancyStartedAfterExpiry()
        val epcExpiredPage = assertPageIs(page, EpcExpiredPagePropertyComplianceUpdate::class, urlArguments)

        // EPC Expired page (good energy rating)
        assertTrue(epcExpiredPage.page.content().contains("5 January 2022"))
        assertFalse(epcExpiredPage.page.content().contains("The expired certificate shows an energy rating below E"))
        epcExpiredPage.continueButton.clickAndWait()

        assertPageIs(page, UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // Epc Check Your Answers page
        val checkYourEpcAnswersPage = assertPageIs(page, UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        assertThat(checkYourEpcAnswersPage.form.summaryList.epcRow.value).containsText("View expired EPC")
        assertThat(checkYourEpcAnswersPage.form.summaryList.expiryDateRow.value).containsText(expectedExpiryDate)
        assertThat(checkYourEpcAnswersPage.form.summaryList.energyRatingRow.value).containsText("C")

        checkYourEpcAnswersPage.form.submit()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can add an expired EPC with a low energy rating and no MEES exemption`(page: Page) {
        // Update EPC page
        val updateEpcPage = startUpdateEpcTask(page)
        val expiryDate = LocalDate(currentDate.year - 5, 1, 5)
        whenever(epcRegisterClient.getByUprn(PROPERTY_33_UPRN))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    expiryDate = expiryDate,
                    energyRating = "F",
                ),
            )
        updateEpcPage.submitHasNewCertificate()
        val checkAutoMatchedEpcPage = assertPageIs(page, CheckAutoMatchedEpcPagePropertyComplianceUpdate::class, urlArguments)

        // Check Auto Matched EPC page
        checkAutoMatchedEpcPage.submitMatchedEpcDetailsCorrect()
        val expiryCheckPage = assertPageIs(page, EpcExpiryCheckPagePropertyComplianceUpdate::class, urlArguments)

        // EPC Expiry check page
        expiryCheckPage.submitTenancyStartedBeforeExpiry()
        val meesExemptionCheckPage = assertPageIs(page, MeesExemptionCheckPagePropertyComplianceUpdate::class, urlArguments)

        // MEES exemption check page
        meesExemptionCheckPage.submitDoesNotHaveExemption()
        val lowEnergyRatingPage = assertPageIs(page, LowEnergyRatingPagePropertyComplianceUpdate::class, urlArguments)

        // Low energy rating page
        lowEnergyRatingPage.saveAndContinueButton.clickAndWait()
        assertPageIs(page, UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // Epc Check Your Answers page
        val checkYourEpcAnswersPage = assertPageIs(page, UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        assertThat(checkYourEpcAnswersPage.form.summaryList.epcRow.value).containsText("View EPC")
        assertThat(checkYourEpcAnswersPage.form.summaryList.expiryDateRow.value).containsText(dateFormat.format(expiryDate))
        assertThat(checkYourEpcAnswersPage.form.summaryList.energyRatingRow.value).containsText("F")
        assertThat(checkYourEpcAnswersPage.form.summaryList.meesExemptionRow.value).containsText("None")

        checkYourEpcAnswersPage.form.submit()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can update their MEES exemption without updating their EPC`(page: Page) {
        val propertyOwnershipIdRequiringMeesExemption = 10L
        val urlArguments = getUrlArguments(propertyOwnershipIdRequiringMeesExemption)

        // MEES exemption check page
        val meesExemptionCheckPage = startUpdateMeesTask(page, propertyOwnershipIdRequiringMeesExemption)
        meesExemptionCheckPage.submitHasExemption()
        val meesExemptionReasonPage =
            assertPageIs(page, MeesExemptionReasonPageMeesUpdatePropertyComplianceUpdate::class, urlArguments)

        // MEES exemption reason page
        meesExemptionReasonPage.submitExemptionReason(MeesExemptionReason.HIGH_COST)
        val meesExemptionConfirmationPage =
            assertPageIs(page, MeesExemptionConfirmationPageMeesUpdatePropertyComplianceUpdate::class, urlArguments)

        // MEES exemption confirmation page
        meesExemptionConfirmationPage.saveAndContinueButton.clickAndWait()
        val cyaPage = assertPageIs(page, UpdateMeesCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)
        assertThat(cyaPage.form.summaryList.energyRatingRow.value).containsText("G")
        assertThat(cyaPage.form.summaryList.meesExemptionRow.value).containsText("High cost exemption")

        cyaPage.form.submit()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can remove their MEES exemption without updating their EPC`(page: Page) {
        val propertyOwnershipIdWithMeesExemption = 11L
        val urlArguments = getUrlArguments(propertyOwnershipIdWithMeesExemption)

        // MEES exemption check page
        val meesExemptionCheckPage =
            startUpdateMeesTask(
                page,
                propertyOwnershipIdWithMeesExemption,
                "New landlord exemption",
            )
        meesExemptionCheckPage.submitDoesNotHaveExemption()
        val lowEnergyRatingPage = assertPageIs(page, LowEnergyRatingPageMeesUpdatePropertyComplianceUpdate::class, urlArguments)

        // Low energy rating page
        lowEnergyRatingPage.saveAndContinueButton.clickAndWait()
        val cyaPage = assertPageIs(page, UpdateMeesCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // MEES Check Your Answers page
        assertThat(cyaPage.form.summaryList.energyRatingRow.value).containsText("G")
        assertThat(cyaPage.form.summaryList.meesExemptionRow.value).containsText("None")

        cyaPage.form.submit()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `Mees exemption check back link url returns to CheckMatchedEpc if part of a full EPC update`(page: Page) {
        // Update EPC page
        val updateEpcPage = startUpdateEpcTask(page)
        val expiryDate = LocalDate(currentDate.year + 5, 1, 5)
        whenever(epcRegisterClient.getByUprn(PROPERTY_33_UPRN))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    expiryDate = expiryDate,
                    energyRating = "F",
                ),
            )
        updateEpcPage.submitHasNewCertificate()
        val checkAutoMatchedEpcPage = assertPageIs(page, CheckAutoMatchedEpcPagePropertyComplianceUpdate::class, urlArguments)

        // Check Auto Matched EPC page
        checkAutoMatchedEpcPage.submitMatchedEpcDetailsCorrect()
        val meesExemptionCheckPage = assertPageIs(page, MeesExemptionCheckPagePropertyComplianceUpdate::class, urlArguments)

        // Back link returns to Check Auto Matched EPC page
        meesExemptionCheckPage.backLink.clickAndWait()
        assertPageIs(page, CheckAutoMatchedEpcPagePropertyComplianceUpdate::class, urlArguments)
    }

    @Test
    fun `Mees exemption check back link url returns to PropertyDetails if part of a MEES-only update`(page: Page) {
        val propertyOwnershipIdWithMeesExemption = 11L
        val urlArguments = getUrlArguments(propertyOwnershipIdWithMeesExemption)

        // MEES exemption check page
        val meesExemptionCheckPage =
            startUpdateMeesTask(
                page,
                propertyOwnershipIdWithMeesExemption,
                "New landlord exemption",
            )

        // Back link returns to propertyDetailsPage
        meesExemptionCheckPage.backLink.clickAndWait()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    private fun startUpdateGasSafetyTask(page: Page): UpdateGasSafetyPagePropertyComplianceUpdate {
        // Property details before update
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.gasSafetyRow.value).containsText("Exempt")
        propertyDetailsPage.propertyComplianceSummaryList.gasSafetyRow.actions.actionLink
            .clickAndWait()
        return assertPageIs(page, UpdateGasSafetyPagePropertyComplianceUpdate::class, urlArguments)
    }

    private fun startEicrUpdateTask(page: Page): UpdateEicrPagePropertyComplianceUpdate {
        // Property details before update
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.eicrRow.value).containsText("Exempt")
        propertyDetailsPage.propertyComplianceSummaryList.eicrRow.actions.actionLink
            .clickAndWait()
        return assertPageIs(page, UpdateEicrPagePropertyComplianceUpdate::class, urlArguments)
    }

    private fun startUpdateEpcTask(page: Page): UpdateEpcPagePropertyComplianceUpdate {
        // Property details before update
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.epcRow.value).containsText("Not required")
        propertyDetailsPage.propertyComplianceSummaryList.epcRow.actions.actionLink
            .clickAndWait()
        return assertPageIs(page, UpdateEpcPagePropertyComplianceUpdate::class, urlArguments)
    }

    private fun startUpdateMeesTask(
        page: Page,
        propertyOwnershipId: Long,
        originalMeesExemption: String = "None",
    ): MeesExemptionCheckPageMeesUpdatePropertyComplianceUpdate {
        // Property details before update
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.meesExemptionRow.value).containsText(originalMeesExemption)
        propertyDetailsPage.propertyComplianceSummaryList.meesExemptionRow.actions.actionLink
            .clickAndWait()
        return assertPageIs(
            page,
            MeesExemptionCheckPageMeesUpdatePropertyComplianceUpdate::class,
            getUrlArguments(propertyOwnershipId),
        )
    }

    companion object {
        // This property starts with Gas Safety, EICR and EPC exemptions and has a known uprn
        private const val PROPERTY_OWNERSHIP_ID = 33L

        private const val PROPERTY_33_UPRN = 100090154792L

        private val urlArguments = getUrlArguments(PROPERTY_OWNERSHIP_ID)

        private fun getUrlArguments(propertyOwnershipId: Long): Map<String, String> =
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

        private val currentDate = DateTimeHelper().getCurrentDateInUK()
    }
}
