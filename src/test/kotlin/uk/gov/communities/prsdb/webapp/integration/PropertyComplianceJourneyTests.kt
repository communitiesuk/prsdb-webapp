package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordIncompleteCompiancesPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.CheckAndSubmitPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.CheckAutoMatchedEpcPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.CheckMatchedEpcPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.ConfirmationPagePropertyCompliance
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcExpiredPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcExpiryCheckPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.CURRENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.NONEXISTENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.SUPERSEDED_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcMissingPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcNotAutoMatchedPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcNotFoundPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcSupersededPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.FireSafetyDeclarationPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.FireSafetyRiskPagePropertyCompliance
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.KeepPropertySafePagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.MeesExemptionCheckPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.MeesExemptionConfirmationPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.MeesExemptionReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.ResponsibilityToTenantsPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.TaskListPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailBulletPointList
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.FullPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PartialPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.FileUploader
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.net.URI
import java.time.format.DateTimeFormatter
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PropertyComplianceJourneyTests : JourneyTestWithSeedData("data-local.sql") {
    @MockitoBean
    private lateinit var fileUploader: FileUploader

    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    @MockitoBean
    private lateinit var fullComplianceConfirmationEmailService: EmailNotificationService<FullPropertyComplianceConfirmationEmail>

    @MockitoBean
    private lateinit var partialComplianceConfirmationEmailService: EmailNotificationService<PartialPropertyComplianceConfirmationEmail>

    @BeforeEach
    fun setUp() {
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI(ABSOLUTE_DASHBOARD_URL))
        whenever(absoluteUrlProvider.buildComplianceInformationUri(PROPERTY_OWNERSHIP_ID)).thenReturn(URI(ABSOLUTE_COMPLIANCE_INFO_URL))
    }

    @Test
    fun `User can navigate whole journey if pages are filled in correctly (in-date certs, declaration)`(page: Page) {
        // Start page
        val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
        assertThat(startPage.heading).containsText("Add compliance information")
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

        // EPC page, epcRegisterClient finds an EPC when submitting that we have a certificate
        whenever(epcRegisterClient.getByUprn(1123456L))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    expiryDate = LocalDate(currentDate.year + 5, 1, 5),
                ),
            )

        epcPage.submitHasCert()
        val checkAutoMatchedEpcPage = assertPageIs(page, CheckAutoMatchedEpcPagePropertyCompliance::class, urlArguments)

        // Check Auto Matched EPC page - details correct, certificate not expired and high enough rating
        val singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT"
        BaseComponent.assertThat(checkAutoMatchedEpcPage.form.fieldsetHeading).containsText(singleLineAddress)
        assertThat(checkAutoMatchedEpcPage.form.summaryList.addressRow.value).containsText(singleLineAddress)
        assertThat(checkAutoMatchedEpcPage.form.summaryList.energyRatingRow.value).containsText("C")
        assertThat(checkAutoMatchedEpcPage.form.summaryList.expiryDateRow.value).containsText("5 January")
        checkAutoMatchedEpcPage.submitMatchedEpcDetailsCorrect()
        val fireSafetyDeclarationPage = assertPageIs(page, FireSafetyDeclarationPagePropertyCompliance::class, urlArguments)

        // Fire Safety Declaration page
        BaseComponent.assertThat(fireSafetyDeclarationPage.heading).containsText("Fire safety in your property")
        assertThat(
            fireSafetyDeclarationPage.form.fieldHeading,
        ).containsText("Have you followed all fire safety responsibilities relevant for this property?")
        fireSafetyDeclarationPage.submitHasDeclaredFireSafety()
        val keepPropertySafePage = assertPageIs(page, KeepPropertySafePagePropertyCompliance::class, urlArguments)

        // Keep Property Safe page
        BaseComponent.assertThat(keepPropertySafePage.heading).containsText("Keeping this property safe")
        keepPropertySafePage.agreeAndSubmit()
        val responsibilityToTenantsPage = assertPageIs(page, ResponsibilityToTenantsPagePropertyCompliance::class, urlArguments)

        // Responsibility To Tenants page
        BaseComponent
            .assertThat(
                responsibilityToTenantsPage.heading,
            ).containsText("Make sure you follow your legal responsibilities to your tenants")
        responsibilityToTenantsPage.agreeAndSubmit()
        val checkAndSubmitPage = assertPageIs(page, CheckAndSubmitPagePropertyCompliance::class, urlArguments)

        // Check Answers page
        BaseComponent
            .assertThat(checkAndSubmitPage.form.fieldsetHeading)
            .containsText("Check the compliance information for: $PROPERTY_ADDRESS")
        checkAndSubmitPage.form.submit()

        // Confirmation page
        val confirmationPage = assertPageIs(page, ConfirmationPagePropertyCompliance::class, urlArguments)
        BaseComponent
            .assertThat(confirmationPage.confirmationBanner)
            .containsText("You have added compliance information for this property")
        assertNotNull(confirmationPage.compliantMessages.getElementByTextOrNull("gas safety"))
        assertNotNull(confirmationPage.compliantMessages.getElementByTextOrNull("electrical safety"))
        assertNotNull(confirmationPage.compliantMessages.getElementByTextOrNull("energy performance"))
        assertNotNull(confirmationPage.compliantMessages.getElementByTextOrNull("your landlord responsibilities"))

        // Check confirmation email
        verify(fullComplianceConfirmationEmailService).sendEmail(
            LANDLORD_EMAIL,
            FullPropertyComplianceConfirmationEmail(
                PROPERTY_ADDRESS,
                EmailBulletPointList(listOf("gas safety", "electrical safety", "energy performance", "your landlord responsibilities")),
                ABSOLUTE_DASHBOARD_URL,
            ),
        )

        // Go to Incomplete Compliances page
        confirmationPage.addForAnotherPropertyButton.clickAndWait()
        assertPageIs(page, LandlordIncompleteCompiancesPage::class)
    }

    @Test
    fun `User can navigate whole journey if pages are filled in correctly (outdated certs, declaration)`(page: Page) {
        // Start page
        val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
        assertThat(startPage.heading).containsText("Add compliance information")
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

        // EPC page, epcRegisterClient finds an expired EPC when submitting that we have a certificate
        whenever(epcRegisterClient.getByUprn(1123456L))
            .thenReturn(
                MockEpcData
                    .createEpcRegisterClientEpcFoundResponse(expiryDate = LocalDate(2022, 1, 5)),
            )

        epcPage.submitHasCert()
        val checkAutoMatchedEpcPage = assertPageIs(page, CheckAutoMatchedEpcPagePropertyCompliance::class, urlArguments)

        // Check Auto Matched EPC page
        checkAutoMatchedEpcPage.submitMatchedEpcDetailsIncorrect()
        var epcLookupPage = assertPageIs(page, EpcLookupPagePropertyCompliance::class, urlArguments)

        // EPC Lookup page - submit superseded certificate
        whenever(epcRegisterClient.getByRrn(SUPERSEDED_EPC_CERTIFICATE_NUMBER)).thenReturn(
            MockEpcData.createEpcRegisterClientEpcFoundResponse(
                certificateNumber = SUPERSEDED_EPC_CERTIFICATE_NUMBER,
                expiryDate = MockEpcData.expiryDateInThePast,
                latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
            ),
        )
        epcLookupPage.submitSupersededEpcNumber()
        val epcSupersededPage = assertPageIs(page, EpcSupersededPagePropertyCompliance::class, urlArguments)

        // EPC Superseded page
        assertTrue(epcSupersededPage.page.content().contains(CURRENT_EPC_CERTIFICATE_NUMBER))
        whenever(epcRegisterClient.getByRrn(CURRENT_EPC_CERTIFICATE_NUMBER))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse())
        epcSupersededPage.continueButton.clickAndWait()
        var checkMatchedEpcPage = assertPageIs(page, CheckMatchedEpcPagePropertyCompliance::class, urlArguments)

        // Check Matched EPC page
        val singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT"
        val expectedExpiryDate =
            DateTimeHelper()
                .getCurrentDateInUK()
                .plus(DatePeriod(years = 5))
                .toJavaLocalDate()
                .format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
        BaseComponent.assertThat(checkMatchedEpcPage.form.fieldsetHeading).containsText(singleLineAddress)
        assertThat(checkMatchedEpcPage.form.summaryList.addressRow.value).containsText(singleLineAddress)
        assertThat(checkMatchedEpcPage.form.summaryList.energyRatingRow.value).containsText("C")
        assertThat(checkMatchedEpcPage.form.summaryList.expiryDateRow.value).containsText(expectedExpiryDate)
        checkMatchedEpcPage.submitMatchedEpcDetailsIncorrect()
        epcLookupPage = assertPageIs(page, EpcLookupPagePropertyCompliance::class, urlArguments)

        // EPC Lookup page - submit latest certificate but it is expired
        whenever(epcRegisterClient.getByRrn(CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    expiryDate = MockEpcData.expiryDateInThePast,
                    energyRating = "C",
                ),
            )
        epcLookupPage.submitCurrentEpcNumberWhichIsExpired()
        checkMatchedEpcPage = assertPageIs(page, CheckMatchedEpcPagePropertyCompliance::class, urlArguments)

        // Check Matched EPC page
        checkMatchedEpcPage.submitMatchedEpcDetailsCorrect()
        val expiryCheckPage = assertPageIs(page, EpcExpiryCheckPagePropertyCompliance::class, urlArguments)

        // Expiry Check page
        assertTrue(expiryCheckPage.page.content().contains("5 January 2022"))
        expiryCheckPage.submitTenancyStartedAfterExpiry()
        val epcExpiredPage = assertPageIs(page, EpcExpiredPagePropertyCompliance::class, urlArguments)

        // EPC Expired page (good energy rating)
        assertTrue(epcExpiredPage.page.content().contains("5 January 2022"))
        assertFalse(epcExpiredPage.page.content().contains("The expired certificate shows an energy rating below E"))
        epcExpiredPage.continueButton.clickAndWait()
        val fireSafetyDeclarationPage = assertPageIs(page, FireSafetyDeclarationPagePropertyCompliance::class, urlArguments)

        // Fire Safety Declaration page
        BaseComponent.assertThat(fireSafetyDeclarationPage.heading).containsText("Fire safety in your property")
        assertThat(
            fireSafetyDeclarationPage.form.fieldHeading,
        ).containsText("Have you followed all fire safety responsibilities relevant for this property?")
        fireSafetyDeclarationPage.submitHasDeclaredFireSafety()
        val keepPropertySafePage = assertPageIs(page, KeepPropertySafePagePropertyCompliance::class, urlArguments)

        // Keep Property Safe page
        BaseComponent.assertThat(keepPropertySafePage.heading).containsText("Keeping this property safe")
        keepPropertySafePage.agreeAndSubmit()
        val responsibilityToTenantsPage = assertPageIs(page, ResponsibilityToTenantsPagePropertyCompliance::class, urlArguments)

        // Responsibility To Tenants page
        BaseComponent
            .assertThat(
                responsibilityToTenantsPage.heading,
            ).containsText("Make sure you follow your legal responsibilities to your tenants")
        responsibilityToTenantsPage.agreeAndSubmit()
        val checkAndSubmitPage = assertPageIs(page, CheckAndSubmitPagePropertyCompliance::class, urlArguments)

        // Check Answers page
        BaseComponent
            .assertThat(checkAndSubmitPage.form.fieldsetHeading)
            .containsText("Check the compliance information for: $PROPERTY_ADDRESS")
        checkAndSubmitPage.form.submit()

        // Confirmation page
        val confirmationPage = assertPageIs(page, ConfirmationPagePropertyCompliance::class, urlArguments)
        assertContains(confirmationPage.heading.getText(), "You need to take action")
        assertNotNull(confirmationPage.nonCompliantMessages.getElementByTextOrNull("you have an expired gas safety certificate"))
        assertNotNull(
            confirmationPage.nonCompliantMessages
                .getElementByTextOrNull("you have an expired Electrical Installation Condition Report (EICR)"),
        )
        assertNotNull(
            confirmationPage.nonCompliantMessages.getElementByTextOrNull("you have an expired energy performance certificate (EPC)"),
        )
        assertNotNull(confirmationPage.compliantMessages.getElementByTextOrNull("your landlord responsibilities"))

        // Check confirmation email
        verify(partialComplianceConfirmationEmailService).sendEmail(
            LANDLORD_EMAIL,
            PartialPropertyComplianceConfirmationEmail(
                PROPERTY_ADDRESS,
                EmailBulletPointList(listOf("your landlord responsibilities")),
                EmailBulletPointList(
                    listOf(
                        "you have an expired gas safety certificate",
                        "you have an expired Electrical Installation Condition Report (EICR)",
                        "you have an expired energy performance certificate (EPC)",
                    ),
                ),
                ABSOLUTE_COMPLIANCE_INFO_URL,
            ),
        )

        // Go to Incomplete Compliances page
        confirmationPage.addForAnotherPropertyButton.clickAndWait()
        assertPageIs(page, LandlordIncompleteCompiancesPage::class)
    }

    @Test
    fun `User can navigate whole journey if pages are filled in correctly (no certs, exemptions, declaration)`(page: Page) {
        // Start page
        val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
        assertThat(startPage.heading).containsText("Add compliance information")
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
        val fireSafetyDeclarationPage = assertPageIs(page, FireSafetyDeclarationPagePropertyCompliance::class, urlArguments)

        // Fire Safety Declaration page
        BaseComponent.assertThat(fireSafetyDeclarationPage.heading).containsText("Fire safety in your property")
        assertThat(
            fireSafetyDeclarationPage.form.fieldHeading,
        ).containsText("Have you followed all fire safety responsibilities relevant for this property?")
        fireSafetyDeclarationPage.submitHasDeclaredFireSafety()
        val keepPropertySafePage = assertPageIs(page, KeepPropertySafePagePropertyCompliance::class, urlArguments)

        // Keep Property Safe page
        BaseComponent.assertThat(keepPropertySafePage.heading).containsText("Keeping this property safe")
        keepPropertySafePage.agreeAndSubmit()
        val responsibilityToTenantsPage = assertPageIs(page, ResponsibilityToTenantsPagePropertyCompliance::class, urlArguments)

        // Responsibility To Tenants page
        BaseComponent
            .assertThat(
                responsibilityToTenantsPage.heading,
            ).containsText("Make sure you follow your legal responsibilities to your tenants")
        responsibilityToTenantsPage.agreeAndSubmit()
        val checkAndSubmitPage = assertPageIs(page, CheckAndSubmitPagePropertyCompliance::class, urlArguments)

        // Check Answers page
        BaseComponent
            .assertThat(checkAndSubmitPage.form.fieldsetHeading)
            .containsText("Check the compliance information for: $PROPERTY_ADDRESS")
        checkAndSubmitPage.form.submit()

        // Confirmation page
        val confirmationPage = assertPageIs(page, ConfirmationPagePropertyCompliance::class, urlArguments)
        BaseComponent
            .assertThat(confirmationPage.confirmationBanner)
            .containsText("You have added compliance information for this property")
        assertNotNull(confirmationPage.compliantMessages.getElementByTextOrNull("gas safety"))
        assertNotNull(confirmationPage.compliantMessages.getElementByTextOrNull("electrical safety"))
        assertNotNull(confirmationPage.compliantMessages.getElementByTextOrNull("energy performance"))
        assertNotNull(confirmationPage.compliantMessages.getElementByTextOrNull("your landlord responsibilities"))

        // Check confirmation email
        verify(fullComplianceConfirmationEmailService).sendEmail(
            LANDLORD_EMAIL,
            FullPropertyComplianceConfirmationEmail(
                PROPERTY_ADDRESS,
                EmailBulletPointList(listOf("gas safety", "electrical safety", "energy performance", "your landlord responsibilities")),
                ABSOLUTE_DASHBOARD_URL,
            ),
        )

        // Go to Dashboard
        confirmationPage.goToDashboardButton.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `User can navigate whole journey if pages are filled in correctly (no certs, no exemptions, no declaration)`(page: Page) {
        // Start page
        val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
        assertThat(startPage.heading).containsText("Add compliance information")
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
        val fireSafetyDeclarationPage = assertPageIs(page, FireSafetyDeclarationPagePropertyCompliance::class, urlArguments)

        // Fire Safety Declaration page
        fireSafetyDeclarationPage.submitHasNotDeclaredFireSafety()
        val fireSafetyRiskPage = assertPageIs(page, FireSafetyRiskPagePropertyCompliance::class, urlArguments)

        // Fire Safety Risk page
        BaseComponent.assertThat(fireSafetyRiskPage.heading).containsText("Your property is at risk of fire")
        fireSafetyRiskPage.form.submit()
        val keepPropertySafePage = assertPageIs(page, KeepPropertySafePagePropertyCompliance::class, urlArguments)

        // Keep Property Safe page
        keepPropertySafePage.agreeAndSubmit()
        val responsibilityToTenantsPage = assertPageIs(page, ResponsibilityToTenantsPagePropertyCompliance::class, urlArguments)

        // Responsibility To Tenants page
        responsibilityToTenantsPage.agreeAndSubmit()
        val checkAndSubmitPage = assertPageIs(page, CheckAndSubmitPagePropertyCompliance::class, urlArguments)

        // Check Answers page
        BaseComponent
            .assertThat(checkAndSubmitPage.form.fieldsetHeading)
            .containsText("Check the compliance information for: $PROPERTY_ADDRESS")
        checkAndSubmitPage.form.submit()

        // Confirmation page
        val confirmationPage = assertPageIs(page, ConfirmationPagePropertyCompliance::class, urlArguments)
        assertContains(confirmationPage.heading.getText(), "You need to take action")
        assertNotNull(confirmationPage.nonCompliantMessages.getElementByTextOrNull("you have not added information about gas safety"))
        assertNotNull(
            confirmationPage.nonCompliantMessages.getElementByTextOrNull("you have not added information about electrical safety"),
        )
        assertNotNull(
            confirmationPage.nonCompliantMessages.getElementByTextOrNull("you have not added information about energy performance"),
        )
        assertNotNull(confirmationPage.compliantMessages.getElementByTextOrNull("your landlord responsibilities"))

        // Check confirmation email
        verify(partialComplianceConfirmationEmailService).sendEmail(
            LANDLORD_EMAIL,
            PartialPropertyComplianceConfirmationEmail(
                PROPERTY_ADDRESS,
                EmailBulletPointList(listOf("your landlord responsibilities")),
                EmailBulletPointList(
                    listOf(
                        "you have not added information about gas safety",
                        "you have not added information about electrical safety",
                        "you have not added information about energy performance",
                    ),
                ),
                ABSOLUTE_COMPLIANCE_INFO_URL,
            ),
        )

        // Go to Dashboard
        confirmationPage.goToDashboardButton.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `User can navigate EPC task if pages are filled in correctly (EPC not found)`(page: Page) {
        // EPC page
        val epcPage = navigator.skipToPropertyComplianceEpcPage(PROPERTY_OWNERSHIP_ID)
        whenever(epcRegisterClient.getByUprn(1123456L)).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
        epcPage.submitHasCert()
        val epcNotAutomatched = assertPageIs(page, EpcNotAutoMatchedPagePropertyCompliance::class, urlArguments)

        // EPC Not Auto Matched page
        epcNotAutomatched.continueButton.clickAndWait()
        var epcLookupPage = assertPageIs(page, EpcLookupPagePropertyCompliance::class, urlArguments)

        // EPC Lookup page
        whenever(
            epcRegisterClient.getByRrn(NONEXISTENT_EPC_CERTIFICATE_NUMBER),
        ).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
        epcLookupPage.submitNonexistentEpcNumber()
        var epcNotFoundPage = assertPageIs(page, EpcNotFoundPagePropertyCompliance::class, urlArguments)

        // EPC Not Found page - search again
        epcNotFoundPage.searchAgainButton.clickAndWait()
        epcLookupPage = assertPageIs(page, EpcLookupPagePropertyCompliance::class, urlArguments)

        // EPC lookup page
        epcLookupPage.submitNonexistentEpcNumber()
        epcNotFoundPage = assertPageIs(page, EpcNotFoundPagePropertyCompliance::class, urlArguments)

        // Epc Not Found page - continue
        epcNotFoundPage.continueButton.clickAndWait()
        assertPageIs(page, FireSafetyDeclarationPagePropertyCompliance::class, urlArguments)
    }

    @Test
    fun `User can navigate EPC task if pages are filled in correctly (MEES exemption)`(page: Page) {
        // EPC page
        val epcPage = navigator.skipToPropertyComplianceEpcPage(PROPERTY_OWNERSHIP_ID)
        whenever(epcRegisterClient.getByUprn(1123456L)).thenReturn(
            MockEpcData.createEpcRegisterClientEpcFoundResponse(
                energyRating = "G",
                expiryDate = MockEpcData.expiryDateInThePast,
            ),
        )
        epcPage.submitHasCert()
        val checkAutoMatchedEpcPage = assertPageIs(page, CheckAutoMatchedEpcPagePropertyCompliance::class, urlArguments)

        // Check Auto Matched EPC page (past expiry date and low rating)
        checkAutoMatchedEpcPage.submitMatchedEpcDetailsCorrect()
        val expiryCheckPage = assertPageIs(page, EpcExpiryCheckPagePropertyCompliance::class, urlArguments)

        // Epc Expiry Check page - tenancy started before expiry
        expiryCheckPage.submitTenancyStartedBeforeExpiry()
        val meesExemptionCheckPage = assertPageIs(page, MeesExemptionCheckPagePropertyCompliance::class, urlArguments)

        // MEES exemption check page (has exemption)
        assertTrue(meesExemptionCheckPage.page.content().contains(MockEpcData.defaultSingleLineAddress))
        meesExemptionCheckPage.submitHasExemption()
        val meesExemptionReasonPage = assertPageIs(page, MeesExemptionReasonPagePropertyCompliance::class, urlArguments)

        // MEES exemption reason page
        meesExemptionReasonPage.submitExemptionReason(MeesExemptionReason.LISTED_BUILDING)
        val meesExemptionConfirmationPage = assertPageIs(page, MeesExemptionConfirmationPagePropertyCompliance::class, urlArguments)

        // MEES exemption confirmation page
        meesExemptionConfirmationPage.saveAndContinueToLandlordResponsibilitiesButton.clickAndWait()
        assertPageIs(page, FireSafetyDeclarationPagePropertyCompliance::class, urlArguments)
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 1L
        private const val LANDLORD_EMAIL = "alex.surname@example.com"
        private const val PROPERTY_ADDRESS = "1, Example Road, EG"

        private val urlArguments = mapOf("propertyOwnershipId" to PROPERTY_OWNERSHIP_ID.toString())

        private val currentDate = DateTimeHelper().getCurrentDateInUK()

        private const val ABSOLUTE_DASHBOARD_URL = "www.prsd.gov.uk/dashboard"
        private const val ABSOLUTE_COMPLIANCE_INFO_URL = "www.prsd.gov.uk/compliance-info"
    }
}
