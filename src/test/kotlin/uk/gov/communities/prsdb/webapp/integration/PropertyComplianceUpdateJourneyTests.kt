package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.CheckAutoMatchedEpcPagePropertyComplianceUpdate
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.UpdateEicrPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate
import uk.gov.communities.prsdb.webapp.services.FileUploader
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

class PropertyComplianceUpdateJourneyTests : JourneyTestWithSeedData("data-local.sql") {
    @MockitoBean
    private lateinit var fileUploader: FileUploader

    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    // Date format like "1 January 2023"
    private val dateFormat =
        LocalDate
            .Format {
                dayOfMonth()
                char(' ')
                monthName(MonthNames.ENGLISH_FULL)
                char(' ')
                year()
            }

    @Test
    fun `User can navigate the gas safety update task if pages are filled in correctly (add new in-date certificate)`(page: Page) {
        // Update certificate or add exemption page
        val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
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
        val cyaPage = assertPageIs(page, GasSafetyCheckYourAnswersPropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Check Your Answers page
        assertThat(cyaPage.form.summaryList.gasSafetyRow.value).containsText("TODO PRSD-976")
        assertThat(cyaPage.form.summaryList.issueDateRow.value).containsText(dateFormat.format(currentDate))
        assertThat(cyaPage.form.summaryList.engineerRow.value).containsText("1234567")
        cyaPage.form.submit()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can navigate the gas safety update task if pages are filled in correctly (add new outdated certificate)`(page: Page) {
        // Update certificate or add exemption page
        val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
        updateGasSafetyPage.submitHasNewCertificate()
        val gasSafetyIssueDatePage =
            assertPageIs(page, GasSafetyIssueDatePagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Cert. Issue Date page
        val outdatedIssueDate = currentDate.minus(DatePeriod(years = 1))
        gasSafetyIssueDatePage.submitDate(outdatedIssueDate)
        val gasSafetyOutdatedPage =
            assertPageIs(page, GasSafetyOutdatedPagePropertyComplianceUpdate::class, urlArguments)

        // Gas Safety Outdated page
        assertThat(gasSafetyOutdatedPage.heading).containsText("Your gas safety certificate is out of date")
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
        val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
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
        val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
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
        // Property details before update
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.eicrRow.value).containsText("Exempt")
        propertyDetailsPage.propertyComplianceSummaryList.eicrRow.actions.actionLink
            .clickAndWait()
        val updateEicrPage = assertPageIs(page, UpdateEicrPagePropertyComplianceUpdate::class, urlArguments)

        // Update certificate or add exemption page
        updateEicrPage.submitHasNewCertificate()
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
        val eicrUploadConfirmationPage =
            assertPageIs(page, EicrUploadConfirmationPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Upload Confirmation page
        assertThat(eicrUploadConfirmationPage.heading).containsText("Your file is being scanned")
        eicrUploadConfirmationPage.saveAndContinueButton.clickAndWait()

        assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Check Your Answers page
        val cyaPage = assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)
        assertThat(cyaPage.form.summaryList.eicrRow.value).containsText("TODO PRSD-976")
        assertThat(cyaPage.form.summaryList.issueDateRow.value).containsText(dateFormat.format(currentDate))
        cyaPage.form.submit()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can navigate the EICR update task if pages are filled in correctly (add new expired certificate)`(page: Page) {
        // Property details before update
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.eicrRow.value).containsText("Exempt")
        propertyDetailsPage.propertyComplianceSummaryList.eicrRow.actions.actionLink
            .clickAndWait()
        val updateEicrPage = assertPageIs(page, UpdateEicrPagePropertyComplianceUpdate::class, urlArguments)

        // Update certificate or add exemption page
        updateEicrPage.submitHasNewCertificate()
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
        val cyaPage = assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)
        assertThat(cyaPage.form.summaryList.eicrRow.value).containsText("Expired")
        assertThat(cyaPage.form.summaryList.issueDateRow.value).containsText(dateFormat.format(outdatedIssueDate))
        cyaPage.form.submit()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can add a new EICR exemption if the pages are filled in correctly`(page: Page) {
        // Property details before update
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.eicrRow.value).containsText("Exempt")
        propertyDetailsPage.propertyComplianceSummaryList.eicrRow.actions.actionLink
            .clickAndWait()
        val updateEicrPage = assertPageIs(page, UpdateEicrPagePropertyComplianceUpdate::class, urlArguments)

        // Update certificate or add exemption page
        updateEicrPage.submitHasNewExemption()
        val eicrExemptionReasonPage =
            assertPageIs(page, EicrExemptionReasonPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Exemption Reason page
        eicrExemptionReasonPage.submitExemptionReason(EicrExemptionReason.LIVE_IN_LANDLORD)
        val eicrExemptionConfirmationPage =
            assertPageIs(page, EicrExemptionConfirmationPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Exemption Confirmation page
        assertThat(eicrExemptionConfirmationPage.heading).containsText("You’ve marked this property as exempt from needing an EICR")
        eicrExemptionConfirmationPage.saveAndContinueButton.clickAndWait()

        assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)

        // EICR Check Your Answers page
        val cyaPage = assertPageIs(page, EicrCheckYourAnswersPagePropertyComplianceUpdate::class, urlArguments)
        assertThat(cyaPage.form.summaryList.eicrRow.value).containsText("Not required")
        assertThat(cyaPage.form.summaryList.exemptionRow.value).containsText("You live in the property with the tenant")
        cyaPage.form.submit()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `User can add a new EICR exemption if the pages are filled in correctly (with 'other' exemption reason)`(page: Page) {
        // Property details before update
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.eicrRow.value).containsText("Exempt")
        propertyDetailsPage.propertyComplianceSummaryList.eicrRow.actions.actionLink
            .clickAndWait()
        val updateEicrPage = assertPageIs(page, UpdateEicrPagePropertyComplianceUpdate::class, urlArguments)

        // Update certificate or add exemption page
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
        assertThat(eicrExemptionConfirmationPage.heading).containsText("You’ve marked this property as exempt from needing an EICR")
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

    // TODO: PRSD-1312 - remove @Disabled when Gas Safety completion links to the rest of the journey
    @Disabled
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
        updateEpcPage.submitHasNewCertificate()
        assertPageIs(
            page,
            CheckAutoMatchedEpcPagePropertyComplianceUpdate::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )

        // Check Auto Matched EPC page
        // TODO PRSD-1312 - continue journey test
    }

    // TODO: PRSD-1312 - remove @Disabled when Gas Safety completion links to the rest of the journey
    @Disabled
    @Test
    fun `User can add a new looked up EPC if the pages are filled in correctly`(page: Page) {
        val propertyOwnershipId = 33L // EPC should be auto-matched to this property ownership ID
        whenever(epcRegisterClient.getByUprn(100090154792L))
            .thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        // Update EPC page
        val updateEpcPage = navigator.goToPropertyComplianceUpdateUpdateEpcPage(propertyOwnershipId)
        updateEpcPage.submitHasNewCertificate()
        assertPageIs(
            page,
            EpcNotAutoMatchedPagePropertyComplianceUpdate::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )

        // Epc Not Auto Matched page
        // TODO PRSD-1312 - continue journey test
    }

    // TODO PRSD-1312 - remove @Disabled when Gas Safety completion links to the rest of the journey
    @Disabled
    @Test
    fun `User can add a new EPC exemption if the pages are filled in correctly`(page: Page) {
        // Update EPC page
        val updateEpcPage = navigator.goToPropertyComplianceUpdateUpdateEpcPage(PROPERTY_OWNERSHIP_ID)
        updateEpcPage.submitHasNewExemption()
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
