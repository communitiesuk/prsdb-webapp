package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_EXEMPTION_OTHER_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrIssueDatePagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrUploadPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafeEngineerNumPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionConfirmationPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionMissingPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionOtherReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyIssueDatePagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyOutdatedPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyUploadConfirmationPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyUploadPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.TaskListPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.services.FileUploader
import kotlin.test.assertContains

@Sql("/data-local.sql")
class PropertyComplianceJourneyTests : IntegrationTest() {
    @MockitoBean
    private lateinit var fileUploader: FileUploader

    @Nested
    inner class JourneyTests {
        @Test
        fun `User can navigate whole journey if pages are filled in correctly (in-date certs)`(page: Page) {
            // Start page
            val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
            assertThat(startPage.heading).containsText("Compliance certificates")
            startPage.startButton.clickAndWait()
            var taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

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
            taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

            // Task List page
            taskListPage.clickUploadTaskWithName("Upload the Electrical Installation Condition Report (EICR)")
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

            // TODO PRSD-1128: Continue test
            assertContains(
                page.url(),
                PropertyComplianceController.getPropertyCompliancePath(PROPERTY_OWNERSHIP_ID) +
                    "/${PropertyComplianceStepId.EicrUploadConfirmation.urlPathSegment}",
            )
        }

        @Test
        fun `User can navigate whole journey if pages are filled in correctly (outdated certs)`(page: Page) {
            // Start page
            val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
            assertThat(startPage.heading).containsText("Compliance certificates")
            startPage.startButton.clickAndWait()
            var taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

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
            gasSafetyOutdatedPage.saveAndReturnToTaskListButton.clickAndWait()
            taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

            // Task List page
            taskListPage.clickUploadTaskWithName("Upload the Electrical Installation Condition Report (EICR)")
            val eicrPage = assertPageIs(page, EicrPagePropertyCompliance::class, urlArguments)

            // EICR page
            eicrPage.submitHasCert()
            val eicrIssueDatePage = assertPageIs(page, EicrIssueDatePagePropertyCompliance::class, urlArguments)

            // EICR Issue Date page
            eicrIssueDatePage.submitDate(currentDate.minus(DatePeriod(years = 5)))

            // TODO PRSD-961: Continue test
            assertContains(
                page.url(),
                PropertyComplianceController.getPropertyCompliancePath(PROPERTY_OWNERSHIP_ID) +
                    "/${PropertyComplianceStepId.EicrOutdated.urlPathSegment}",
            )
        }

        @Test
        fun `User can navigate whole journey if pages are filled in correctly (no certs, exemptions)`(page: Page) {
            // Start page
            val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
            assertThat(startPage.heading).containsText("Compliance certificates")
            startPage.startButton.clickAndWait()
            var taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

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
            gasSafetyExemptionConfirmationPage.saveAndReturnToTaskListButton.clickAndWait()
            taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

            // Task List page
            taskListPage.clickUploadTaskWithName("Upload the Electrical Installation Condition Report (EICR)")
            val eicrPage = assertPageIs(page, EicrPagePropertyCompliance::class, urlArguments)

            // EICR page
            eicrPage.submitHasNoCert()
            val eicrExemptionPage = assertPageIs(page, EicrExemptionPagePropertyCompliance::class, urlArguments)

            // EICR Exemption page
            eicrExemptionPage.submitHasExemption()
            val eicrExemptionReasonPage = assertPageIs(page, EicrExemptionReasonPagePropertyCompliance::class, urlArguments)

            // EICR Exemption Reason page
            eicrExemptionReasonPage.submitExemptionReason(EicrExemptionReason.LIVE_IN_LANDLORD)

            // TODO PRSD-959: Continue test
            assertContains(
                page.url(),
                PropertyComplianceController.getPropertyCompliancePath(PROPERTY_OWNERSHIP_ID) +
                    "/${PropertyComplianceStepId.EicrExemptionConfirmation.urlPathSegment}",
            )
        }

        @Test
        fun `User can navigate whole journey if pages are filled in correctly (no certs, no exemptions)`(page: Page) {
            // Start page
            val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
            assertThat(startPage.heading).containsText("Compliance certificates")
            startPage.startButton.clickAndWait()
            var taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

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
            gasSafetyExemptionMissingPage.saveAndReturnToTaskListButton.clickAndWait()
            taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

            // Task List page
            taskListPage.clickUploadTaskWithName("Upload the Electrical Installation Condition Report (EICR)")
            val eicrPage = assertPageIs(page, EicrPagePropertyCompliance::class, urlArguments)

            // EICR page
            eicrPage.submitHasNoCert()
            val eicrExemptionPage = assertPageIs(page, EicrExemptionPagePropertyCompliance::class, urlArguments)

            // EICR Exemption page
            eicrExemptionPage.submitHasNoExemption()

            // TODO PRSD-960: Continue test
            assertContains(
                page.url(),
                PropertyComplianceController.getPropertyCompliancePath(PROPERTY_OWNERSHIP_ID) +
                    "/${PropertyComplianceStepId.EicrExemptionMissing.urlPathSegment}",
            )
        }
    }

    @Nested
    inner class GasSafetyStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val gasSafetyPage = navigator.goToPropertyComplianceGasSafetyPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyPage.form.submit()
            assertThat(gasSafetyPage.form.getErrorMessage()).containsText("Select whether you have a gas safety certificate")
        }
    }

    @Nested
    inner class GasSafetyIssueDateStepTests {
        @ParameterizedTest(name = "{0}")
        @MethodSource("uk.gov.communities.prsdb.webapp.integration.PropertyComplianceJourneyTests#provideInvalidDateStrings")
        fun `Submitting returns a corresponding error when`(
            dayMonthYear: Triple<String, String, String>,
            expectedErrorMessage: String,
        ) {
            val (day, month, year) = dayMonthYear

            val gasSafetyIssueDatePage = navigator.goToPropertyComplianceGasSafetyIssueDatePage(PROPERTY_OWNERSHIP_ID)
            gasSafetyIssueDatePage.submitDate(day, month, year)
            assertThat(gasSafetyIssueDatePage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }
    }

    @Nested
    inner class GasSafetyEngineerNumStepTests {
        @Test
        fun `Submitting with no value entered returns an error`() {
            val gasSafeEngineerNumPage = navigator.goToPropertyComplianceGasSafetyEngineerNumPage(PROPERTY_OWNERSHIP_ID)
            gasSafeEngineerNumPage.form.submit()
            assertThat(gasSafeEngineerNumPage.form.getErrorMessage())
                .containsText("You need to enter a Gas Safe engineer's registered number.")
        }

        @Test
        fun `Submitting with an invalid value entered returns an error`() {
            val gasSafeEngineerNumPage = navigator.goToPropertyComplianceGasSafetyEngineerNumPage(PROPERTY_OWNERSHIP_ID)
            gasSafeEngineerNumPage.submitEngineerNum("ABCDEFG")
            assertThat(gasSafeEngineerNumPage.form.getErrorMessage()).containsText("Enter a 7-digit number.")
        }
    }

    @Nested
    inner class GasSafetyUploadStepTests {
        @Test
        fun `Submitting with no file staged returns an error`() {
            val gasSafetyUploadPage = navigator.goToPropertyComplianceGasSafetyUploadPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyUploadPage.form.submit()
            assertThat(gasSafetyUploadPage.form.getErrorMessage()).containsText("Select a file")
        }

        @Test
        fun `Submitting with an invalid file (wrong type) staged returns an error`() {
            val gasSafetyUploadPage = navigator.goToPropertyComplianceGasSafetyUploadPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyUploadPage.uploadCertificate("invalidFileType.bmp")
            assertThat(gasSafetyUploadPage.form.getErrorMessage()).containsText("The selected file must be a PDF, PNG or JPG")
        }

        @Test
        fun `Submitting with an invalid file (too big) staged returns an error`() {
            val gasSafetyUploadPage = navigator.goToPropertyComplianceGasSafetyUploadPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyUploadPage.uploadCertificate("invalidFileSize.jpg")
            assertThat(gasSafetyUploadPage.form.getErrorMessage()).containsText("The selected file must be smaller than 15MB")
        }

        @Test
        fun `Submitting a valid file returns an error if the upload attempt is unsuccessful`() {
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
            ).thenReturn(false)

            val gasSafetyUploadPage = navigator.goToPropertyComplianceGasSafetyUploadPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyUploadPage.uploadCertificate("validFile.png")
            assertThat(gasSafetyUploadPage.form.getErrorMessage()).containsText("The selected file could not be uploaded - try again")
        }
    }

    @Nested
    inner class GasSafetyExemptionStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val gasSafetyExemptionPage = navigator.goToPropertyComplianceGasSafetyExemptionPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionPage.form.submit()
            assertThat(gasSafetyExemptionPage.form.getErrorMessage())
                .containsText("Select whether you have a gas safety certificate exemption")
        }
    }

    @Nested
    inner class GasSafetyExemptionReasonStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val gasSafetyExemptionReasonPage = navigator.goToPropertyComplianceGasSafetyExemptionReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionReasonPage.form.submit()
            assertThat(gasSafetyExemptionReasonPage.form.getErrorMessage())
                .containsText("Select why this property is exempt from gas safety")
        }

        @Test
        fun `Submitting with 'other' selected redirects to the gas safety exemption other reason page`(page: Page) {
            val gasSafetyExemptionReasonPage = navigator.goToPropertyComplianceGasSafetyExemptionReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionReasonPage.submitExemptionReason(GasSafetyExemptionReason.OTHER)
            assertPageIs(page, GasSafetyExemptionOtherReasonPagePropertyCompliance::class, urlArguments)
        }
    }

    @Nested
    inner class GasSafetyExemptionOtherReasonStepTests {
        @Test
        fun `Submitting with no reason returns an error`(page: Page) {
            val gasSafetyExemptionOtherReasonPage = navigator.goToPropertyComplianceGasSafetyExemptionOtherReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionOtherReasonPage.form.submit()
            assertThat(gasSafetyExemptionOtherReasonPage.form.getErrorMessage())
                .containsText("Explain why your property is exempt from having a gas safety certificate")
        }

        @Test
        fun `Submitting with a too long reason returns an error`(page: Page) {
            val gasSafetyExemptionOtherReasonPage = navigator.goToPropertyComplianceGasSafetyExemptionOtherReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionOtherReasonPage.submitReason("too long reason".repeat(GAS_SAFETY_EXEMPTION_OTHER_REASON_MAX_LENGTH))
            assertThat(gasSafetyExemptionOtherReasonPage.form.getErrorMessage("otherReason"))
                .containsText("Explanation must be 200 characters or fewer")
        }

        @Test
        fun `Submitting with a valid reason redirects to the gas safety exemption confirmation page`(page: Page) {
            val gasSafetyExemptionOtherReasonPage = navigator.goToPropertyComplianceGasSafetyExemptionOtherReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionOtherReasonPage.submitReason("valid reason")

            // TODO PRSD-951: Replace with gas exemption confirmation page
            assertContains(
                page.url(),
                PropertyComplianceController.getPropertyCompliancePath(PROPERTY_OWNERSHIP_ID) +
                    "/${PropertyComplianceStepId.GasSafetyExemptionConfirmation.urlPathSegment}",
            )
        }
    }

    @Nested
    inner class EicrStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val eicrPage = navigator.goToPropertyComplianceEicrPage(PROPERTY_OWNERSHIP_ID)
            eicrPage.form.submit()
            assertThat(eicrPage.form.getErrorMessage()).containsText("Select whether you have an EICR for this property")
        }
    }

    @Nested
    inner class EicrIssueDateStepTests {
        @ParameterizedTest(name = "{0}")
        @MethodSource("uk.gov.communities.prsdb.webapp.integration.PropertyComplianceJourneyTests#provideInvalidDateStrings")
        fun `Submitting returns a corresponding error when`(
            dayMonthYear: Triple<String, String, String>,
            expectedErrorMessage: String,
        ) {
            val (day, month, year) = dayMonthYear

            val eicrIssueDatePage = navigator.goToPropertyComplianceEicrIssueDatePage(PROPERTY_OWNERSHIP_ID)
            eicrIssueDatePage.submitDate(day, month, year)
            assertThat(eicrIssueDatePage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }
    }

    @Nested
    inner class EicrUploadStepTests {
        @Test
        fun `Submitting with no file staged returns an error`() {
            val eicrUploadPage = navigator.goToPropertyComplianceEicrUploadPage(PROPERTY_OWNERSHIP_ID)
            eicrUploadPage.form.submit()
            assertThat(eicrUploadPage.form.getErrorMessage()).containsText("Select a file")
        }

        @Test
        fun `Submitting with an invalid file (wrong type) staged returns an error`() {
            val eicrUploadPage = navigator.goToPropertyComplianceEicrUploadPage(PROPERTY_OWNERSHIP_ID)
            eicrUploadPage.uploadCertificate("invalidFileType.bmp")
            assertThat(eicrUploadPage.form.getErrorMessage()).containsText("The selected file must be a PDF, PNG or JPG")
        }

        @Test
        fun `Submitting with an invalid file (too big) staged returns an error`() {
            val eicrUploadPage = navigator.goToPropertyComplianceEicrUploadPage(PROPERTY_OWNERSHIP_ID)
            eicrUploadPage.uploadCertificate("invalidFileSize.jpg")
            assertThat(eicrUploadPage.form.getErrorMessage()).containsText("The selected file must be smaller than 15MB")
        }

        @Test
        fun `Submitting a valid file returns an error if the upload attempt is unsuccessful`() {
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
            ).thenReturn(false)

            val eicrUploadPage = navigator.goToPropertyComplianceEicrUploadPage(PROPERTY_OWNERSHIP_ID)
            eicrUploadPage.uploadCertificate("validFile.png")
            assertThat(eicrUploadPage.form.getErrorMessage()).containsText("The selected file could not be uploaded - try again")
        }
    }

    @Nested
    inner class EicrExemptionStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val eicrExemptionPage = navigator.goToPropertyComplianceEicrExemptionPage(PROPERTY_OWNERSHIP_ID)
            eicrExemptionPage.form.submit()
            assertThat(eicrExemptionPage.form.getErrorMessage())
                .containsText("Select whether this property has an EICR exemption")
        }
    }

    @Nested
    inner class EicrExemptionReasonStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val eicrExemptionReasonPage = navigator.goToPropertyComplianceEicrExemptionReasonPage(PROPERTY_OWNERSHIP_ID)
            eicrExemptionReasonPage.form.submit()
            assertThat(eicrExemptionReasonPage.form.getErrorMessage())
                .containsText("Select why this property has an EICR exemption")
        }

        @Test
        fun `Submitting with 'other' selected redirects to the EICR exemption other reason page`(page: Page) {
            val eicrExemptionReasonPage = navigator.goToPropertyComplianceEicrExemptionReasonPage(PROPERTY_OWNERSHIP_ID)
            eicrExemptionReasonPage.submitExemptionReason(EicrExemptionReason.OTHER)

            // TODO PRSD-995: Replace with EICR other exemption page
            assertContains(
                page.url(),
                PropertyComplianceController.getPropertyCompliancePath(PROPERTY_OWNERSHIP_ID) +
                    "/${PropertyComplianceStepId.EicrExemptionOtherReason.urlPathSegment}",
            )
        }
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 1L
        private val urlArguments = mapOf("propertyOwnershipId" to PROPERTY_OWNERSHIP_ID.toString())

        private val currentDate = DateTimeHelper().getCurrentDateInUK()
        private val futureDate =
            currentDate.plus(DatePeriod(days = 1)).let {
                Triple(it.dayOfMonth.toString(), it.monthNumber.toString(), it.year.toString())
            }

        private const val INVALID_DAY_ERR = "Day must be a whole number between 1 and 31"
        private const val INVALID_MONTH_ERR = "Month must be a whole number between 1 and 12"
        private const val INVALID_YEAR_ERR = "Year must be a whole number greater than 1899"

        @JvmStatic
        private fun provideInvalidDateStrings() =
            arrayOf(
                // Blank fields
                Arguments.of(Named.of("all fields missing", Triple("", "", "")), "Enter a date"),
                Arguments.of(Named.of("day missing", Triple("", "11", "1990")), "You must include a day"),
                Arguments.of(Named.of("month missing", Triple("12", "", "1990")), "You must include a month"),
                Arguments.of(Named.of("year missing", Triple("12", "11", "")), "You must include a year"),
                Arguments.of(Named.of("day and month missing", Triple("", "", "1990")), "You must include a day and a month"),
                Arguments.of(Named.of("month and year missing", Triple("12", "", "")), "You must include a month and a year"),
                Arguments.of(Named.of("day and year missing", Triple("", "11", "")), "You must include a day and a year"),
                // Blank and invalid fields
                Arguments.of(Named.of("day missing (other fields invalid)", Triple("", "0", "0")), "You must include a day"),
                Arguments.of(Named.of("month missing (other fields invalid)", Triple("0", "", "0")), "You must include a month"),
                Arguments.of(Named.of("year missing (other fields invalid)", Triple("0", "0", "")), "You must include a year"),
                Arguments.of(Named.of("day and month missing (year invalid)", Triple("", "", "0")), "You must include a day and a month"),
                Arguments.of(Named.of("month and year missing (day invalid)", Triple("0", "", "")), "You must include a month and a year"),
                Arguments.of(Named.of("day and year missing (month invalid)", Triple("", "0", "")), "You must include a day and a year"),
                // Invalid fields
                Arguments.of(Named.of("invalid day", Triple("0", "11", "1990")), INVALID_DAY_ERR),
                Arguments.of(Named.of("invalid month", Triple("12", "0", "1990")), INVALID_MONTH_ERR),
                Arguments.of(Named.of("invalid year", Triple("12", "11", "0")), INVALID_YEAR_ERR),
                Arguments.of(Named.of("invalid day and month", Triple("32", "0", "1990")), "$INVALID_DAY_ERR. $INVALID_MONTH_ERR"),
                Arguments.of(Named.of("invalid month and year", Triple("12", "13", "0")), "$INVALID_MONTH_ERR. $INVALID_YEAR_ERR"),
                Arguments.of(Named.of("invalid day and year", Triple("0", "11", "1899")), "$INVALID_DAY_ERR. $INVALID_YEAR_ERR"),
                Arguments.of(Named.of("invalid fields", Triple("0", "0", "0")), "$INVALID_DAY_ERR. $INVALID_MONTH_ERR. $INVALID_YEAR_ERR"),
                // Invalid date
                Arguments.of(Named.of("invalid date", Triple("31", "11", "1990")), "You must enter a real date"),
                Arguments.of(Named.of("invalid leap date", Triple("29", "02", "2005")), "You must enter a real date"),
                // Not today or past date
                Arguments.of(Named.of("not today or past date", futureDate), "The date must be today or in the past"),
            )
    }
}
