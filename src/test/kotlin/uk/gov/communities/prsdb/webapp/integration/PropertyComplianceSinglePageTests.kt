package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.junit.jupiter.api.Assertions
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
import uk.gov.communities.prsdb.webapp.constants.EXEMPTION_OTHER_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionConfirmationPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EicrExemptionOtherReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionConfirmationPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionOtherReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.services.FileUploader

class PropertyComplianceSinglePageTests : SinglePageTestWithSeedData("data-local.sql") {
    @MockitoBean
    private lateinit var fileUploader: FileUploader

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
        @MethodSource("uk.gov.communities.prsdb.webapp.integration.PropertyComplianceSinglePageTests#provideInvalidDateStrings")
        fun `Submitting returns a corresponding error when`(
            dayMonthYear: Triple<String, String, String>,
            expectedErrorMessage: String,
        ) {
            val (day, month, year) = dayMonthYear

            val gasSafetyIssueDatePage = navigator.skipToPropertyComplianceGasSafetyIssueDatePage(PROPERTY_OWNERSHIP_ID)
            gasSafetyIssueDatePage.submitDate(day, month, year)
            assertThat(gasSafetyIssueDatePage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }
    }

    @Nested
    inner class GasSafetyEngineerNumStepTests {
        @Test
        fun `Submitting with no value entered returns an error`() {
            val gasSafeEngineerNumPage = navigator.skipToPropertyComplianceGasSafetyEngineerNumPage(PROPERTY_OWNERSHIP_ID)
            gasSafeEngineerNumPage.form.submit()
            assertThat(gasSafeEngineerNumPage.form.getErrorMessage())
                .containsText("You need to enter a Gas Safe engineer's registered number.")
        }

        @Test
        fun `Submitting with an invalid value entered returns an error`() {
            val gasSafeEngineerNumPage = navigator.skipToPropertyComplianceGasSafetyEngineerNumPage(PROPERTY_OWNERSHIP_ID)
            gasSafeEngineerNumPage.submitEngineerNum("ABCDEFG")
            assertThat(gasSafeEngineerNumPage.form.getErrorMessage()).containsText("Enter a 7-digit number.")
        }
    }

    @Nested
    inner class GasSafetyUploadStepTests {
        @Test
        fun `Submitting with no file staged returns an error`() {
            val gasSafetyUploadPage = navigator.skipToPropertyComplianceGasSafetyUploadPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyUploadPage.form.submit()
            assertThat(gasSafetyUploadPage.form.getErrorMessage()).containsText("Select a gas safety certificate")
        }

        @Test
        fun `Submitting with an invalid file (wrong type) staged returns an error`() {
            val gasSafetyUploadPage = navigator.skipToPropertyComplianceGasSafetyUploadPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyUploadPage.uploadCertificate("invalidFileType.bmp")
            assertThat(gasSafetyUploadPage.form.getErrorMessage()).containsText("The selected file must be a PDF, PNG or JPG")
        }

        @Test
        fun `Submitting with an invalid file (too big) staged returns an error`() {
            val gasSafetyUploadPage = navigator.skipToPropertyComplianceGasSafetyUploadPage(PROPERTY_OWNERSHIP_ID)
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

            val gasSafetyUploadPage = navigator.skipToPropertyComplianceGasSafetyUploadPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyUploadPage.uploadCertificate("validFile.png")
            assertThat(gasSafetyUploadPage.form.getErrorMessage())
                .containsText("The selected file could not be uploaded - try again")
        }

        @Test
        fun `Submitting valid file metadata to complete file upload does not succeed`() {
            val gasSafetyUploadPage = navigator.skipToPropertyComplianceGasSafetyUploadPage(PROPERTY_OWNERSHIP_ID)
            val response = gasSafetyUploadPage.metadataOnlySubmission("metadata.pdf", 1000, "application/pdf")
            Assertions.assertEquals(response.status(), 500)
        }
    }

    @Nested
    inner class GasSafetyExemptionStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val gasSafetyExemptionPage = navigator.skipToPropertyComplianceGasSafetyExemptionPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionPage.form.submit()
            assertThat(gasSafetyExemptionPage.form.getErrorMessage())
                .containsText("Select whether you have a gas safety certificate exemption")
        }
    }

    @Nested
    inner class GasSafetyExemptionReasonStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val gasSafetyExemptionReasonPage = navigator.skipToPropertyComplianceGasSafetyExemptionReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionReasonPage.form.submit()
            assertThat(gasSafetyExemptionReasonPage.form.getErrorMessage())
                .containsText("Select why this property is exempt from gas safety")
        }

        @Test
        fun `Submitting with 'other' selected redirects to the gas safety exemption other reason page`(page: Page) {
            val gasSafetyExemptionReasonPage = navigator.skipToPropertyComplianceGasSafetyExemptionReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionReasonPage.submitExemptionReason(GasSafetyExemptionReason.OTHER)
            BasePage.assertPageIs(page, GasSafetyExemptionOtherReasonPagePropertyCompliance::class, urlArguments)
        }
    }

    @Nested
    inner class GasSafetyExemptionOtherReasonStepTests {
        @Test
        fun `Submitting with no reason returns an error`() {
            val gasSafetyExemptionOtherReasonPage =
                navigator.skipToPropertyComplianceGasSafetyExemptionOtherReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionOtherReasonPage.form.submit()
            assertThat(gasSafetyExemptionOtherReasonPage.form.getErrorMessage())
                .containsText("Explain why your property is exempt from having a gas safety certificate")
        }

        @Test
        fun `Submitting with a too long reason returns an error`() {
            val gasSafetyExemptionOtherReasonPage =
                navigator.skipToPropertyComplianceGasSafetyExemptionOtherReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionOtherReasonPage.submitReason("too long reason".repeat(EXEMPTION_OTHER_REASON_MAX_LENGTH))
            assertThat(gasSafetyExemptionOtherReasonPage.form.getErrorMessage("otherReason"))
                .containsText("Explanation must be 200 characters or fewer")
        }

        @Test
        fun `Submitting with a valid reason redirects to the gas safety exemption confirmation page`(page: Page) {
            val gasSafetyExemptionOtherReasonPage =
                navigator.skipToPropertyComplianceGasSafetyExemptionOtherReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionOtherReasonPage.submitReason("valid reason")
            BasePage.assertPageIs(page, GasSafetyExemptionConfirmationPagePropertyCompliance::class, urlArguments)
        }
    }

    @Nested
    inner class EicrStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val eicrPage = navigator.skipToPropertyComplianceEicrPage(PROPERTY_OWNERSHIP_ID)
            eicrPage.form.submit()
            assertThat(eicrPage.form.getErrorMessage()).containsText("Select whether you have an EICR for this property")
        }
    }

    @Nested
    inner class EicrIssueDateStepTests {
        @ParameterizedTest(name = "{0}")
        @MethodSource("uk.gov.communities.prsdb.webapp.integration.PropertyComplianceSinglePageTests#provideInvalidDateStrings")
        fun `Submitting returns a corresponding error when`(
            dayMonthYear: Triple<String, String, String>,
            expectedErrorMessage: String,
        ) {
            val (day, month, year) = dayMonthYear

            val eicrIssueDatePage = navigator.skipToPropertyComplianceEicrIssueDatePage(PROPERTY_OWNERSHIP_ID)
            eicrIssueDatePage.submitDate(day, month, year)
            assertThat(eicrIssueDatePage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }
    }

    @Nested
    inner class EicrUploadStepTests {
        @Test
        fun `Submitting with no file staged returns an error`() {
            val eicrUploadPage = navigator.skipToPropertyComplianceEicrUploadPage(PROPERTY_OWNERSHIP_ID)
            eicrUploadPage.form.submit()
            assertThat(eicrUploadPage.form.getErrorMessage()).containsText("Select an EICR")
        }

        @Test
        fun `Submitting with an invalid file (wrong type) staged returns an error`() {
            val eicrUploadPage = navigator.skipToPropertyComplianceEicrUploadPage(PROPERTY_OWNERSHIP_ID)
            eicrUploadPage.uploadCertificate("invalidFileType.bmp")
            assertThat(eicrUploadPage.form.getErrorMessage()).containsText("The selected file must be a PDF, PNG or JPG")
        }

        @Test
        fun `Submitting with an invalid file (too big) staged returns an error`() {
            val eicrUploadPage = navigator.skipToPropertyComplianceEicrUploadPage(PROPERTY_OWNERSHIP_ID)
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

            val eicrUploadPage = navigator.skipToPropertyComplianceEicrUploadPage(PROPERTY_OWNERSHIP_ID)
            eicrUploadPage.uploadCertificate("validFile.png")
            assertThat(eicrUploadPage.form.getErrorMessage()).containsText("The selected file could not be uploaded - try again")
        }

        @Test
        fun `Submitting valid file metadata to complete file upload does not succeed`() {
            val gasSafetyUploadPage = navigator.skipToPropertyComplianceEicrUploadPage(PROPERTY_OWNERSHIP_ID)
            val response = gasSafetyUploadPage.metadataOnlySubmission("metadata.pdf", 1000, "application/pdf")
            Assertions.assertEquals(response.status(), 500)
        }
    }

    @Nested
    inner class EicrExemptionStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val eicrExemptionPage = navigator.skipToPropertyComplianceEicrExemptionPage(PROPERTY_OWNERSHIP_ID)
            eicrExemptionPage.form.submit()
            assertThat(eicrExemptionPage.form.getErrorMessage()).containsText("Select whether this property has an EICR exemption")
        }
    }

    @Nested
    inner class EicrExemptionReasonStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val eicrExemptionReasonPage = navigator.skipToPropertyComplianceEicrExemptionReasonPage(PROPERTY_OWNERSHIP_ID)
            eicrExemptionReasonPage.form.submit()
            assertThat(eicrExemptionReasonPage.form.getErrorMessage()).containsText("Select why this property has an EICR exemption")
        }

        @Test
        fun `Submitting with 'other' selected redirects to the EICR exemption other reason page`(page: Page) {
            val eicrExemptionReasonPage = navigator.skipToPropertyComplianceEicrExemptionReasonPage(PROPERTY_OWNERSHIP_ID)
            eicrExemptionReasonPage.submitExemptionReason(EicrExemptionReason.OTHER)
            BasePage.assertPageIs(page, EicrExemptionOtherReasonPagePropertyCompliance::class, urlArguments)
        }
    }

    @Nested
    inner class EicrExemptionOtherReasonStepTests {
        @Test
        fun `Submitting with no reason returns an error`() {
            val eicrExemptionOtherReasonPage = navigator.skipToPropertyComplianceEicrExemptionOtherReasonPage(PROPERTY_OWNERSHIP_ID)
            eicrExemptionOtherReasonPage.form.submit()
            assertThat(eicrExemptionOtherReasonPage.form.getErrorMessage())
                .containsText("Explain why your property is exempt from needing an EICR")
        }

        @Test
        fun `Submitting with a too long reason returns an error`() {
            val eicrExemptionOtherReasonPage = navigator.skipToPropertyComplianceEicrExemptionOtherReasonPage(PROPERTY_OWNERSHIP_ID)
            eicrExemptionOtherReasonPage.submitReason("too long reason".repeat(EXEMPTION_OTHER_REASON_MAX_LENGTH))
            assertThat(eicrExemptionOtherReasonPage.form.getErrorMessage("otherReason"))
                .containsText("Explanation must be 200 characters or fewer")
        }

        @Test
        fun `Submitting with a valid reason redirects to the gas safety exemption confirmation page`(page: Page) {
            val eicrExemptionOtherReasonPage = navigator.skipToPropertyComplianceEicrExemptionOtherReasonPage(PROPERTY_OWNERSHIP_ID)
            eicrExemptionOtherReasonPage.submitReason("valid reason")
            BasePage.assertPageIs(page, EicrExemptionConfirmationPagePropertyCompliance::class, urlArguments)
        }
    }

    @Nested
    inner class EpcStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val epcPage = navigator.skipToPropertyComplianceEpcPage(PROPERTY_OWNERSHIP_ID)
            epcPage.form.submit()
            assertThat(epcPage.form.getErrorMessage()).containsText("Select whether you have an EPC for this property")
        }
    }

    @Nested
    inner class EpcExemptionReasonTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val epcExemptionReasonPage = navigator.skipToPropertyComplianceEpcExemptionReasonPage(PROPERTY_OWNERSHIP_ID)
            epcExemptionReasonPage.form.submit()
            assertThat(epcExemptionReasonPage.form.getErrorMessage()).containsText("Select why your property does not need an EPC")
        }
    }

    @Nested
    inner class FireSafetyDeclarationStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val fireSafetyDeclarationPage = navigator.skipToPropertyComplianceFireSafetyDeclarationPage(PROPERTY_OWNERSHIP_ID)
            fireSafetyDeclarationPage.form.submit()
            assertThat(fireSafetyDeclarationPage.form.getErrorMessage())
                .containsText("Select whether you have followed fire safety responsibilities")
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
                Arguments.of(
                    Named.of("day and month missing", Triple("", "", "1990")),
                    "You must include a day and a month",
                ),
                Arguments.of(
                    Named.of("month and year missing", Triple("12", "", "")),
                    "You must include a month and a year",
                ),
                Arguments.of(
                    Named.of("day and year missing", Triple("", "11", "")),
                    "You must include a day and a year",
                ),
                // Blank and invalid fields
                Arguments.of(
                    Named.of("day missing (other fields invalid)", Triple("", "0", "0")),
                    "You must include a day",
                ),
                Arguments.of(
                    Named.of("month missing (other fields invalid)", Triple("0", "", "0")),
                    "You must include a month",
                ),
                Arguments.of(
                    Named.of("year missing (other fields invalid)", Triple("0", "0", "")),
                    "You must include a year",
                ),
                Arguments.of(
                    Named.of("day and month missing (year invalid)", Triple("", "", "0")),
                    "You must include a day and a month",
                ),
                Arguments.of(
                    Named.of("month and year missing (day invalid)", Triple("0", "", "")),
                    "You must include a month and a year",
                ),
                Arguments.of(
                    Named.of("day and year missing (month invalid)", Triple("", "0", "")),
                    "You must include a day and a year",
                ),
                // Invalid fields
                Arguments.of(Named.of("invalid day", Triple("0", "11", "1990")), INVALID_DAY_ERR),
                Arguments.of(Named.of("invalid month", Triple("12", "0", "1990")), INVALID_MONTH_ERR),
                Arguments.of(Named.of("invalid year", Triple("12", "11", "0")), INVALID_YEAR_ERR),
                Arguments.of(
                    Named.of("invalid day and month", Triple("32", "0", "1990")),
                    "$INVALID_DAY_ERR. $INVALID_MONTH_ERR",
                ),
                Arguments.of(
                    Named.of("invalid month and year", Triple("12", "13", "0")),
                    "$INVALID_MONTH_ERR. $INVALID_YEAR_ERR",
                ),
                Arguments.of(
                    Named.of("invalid day and year", Triple("0", "11", "1899")),
                    "$INVALID_DAY_ERR. $INVALID_YEAR_ERR",
                ),
                Arguments.of(
                    Named.of("invalid fields", Triple("0", "0", "0")),
                    "$INVALID_DAY_ERR. $INVALID_MONTH_ERR. $INVALID_YEAR_ERR",
                ),
                // Invalid date
                Arguments.of(Named.of("invalid date", Triple("31", "11", "1990")), "You must enter a real date"),
                Arguments.of(Named.of("invalid leap date", Triple("29", "02", "2005")), "You must enter a real date"),
                // Not today or past date
                Arguments.of(Named.of("not today or past date", futureDate), "The date must be today or in the past"),
            )
    }
}
