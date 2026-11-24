package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.constants.EIC_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_MONTHS
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckElectricalCertUploadsFormBasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LicenceNumberFormPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UploadCertificateFormPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.AlreadyRegisteredPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.BillsIncludedFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckAnswersPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckEicUploadsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckEicrUploadsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckElectricalSafetyAnswersFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckEpcAnswersFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckGasCertUploadsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckGasSafetyAnswersFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.ConfirmMissingComplianceFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.ConfirmationPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EicExpiredFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EicIssueDateFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EicrExpiredFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EicrExpiryDateFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.ElectricalCertMissingFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EpcExemptionFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.FurnishedStatusFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.GasCertExpiredFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.GasCertIssueDateFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasElectricalCertFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasEpcFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasGasCertFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasGasSupplyFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HmoAdditionalLicenceFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HmoMandatoryLicenceFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.InfoFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.IsEpcRequiredFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.LicensingTypeFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.NumberOfHouseholdsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.NumberOfPeopleFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.RentAmountFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.RentFrequencyFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.RentIncludesBillsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.SelectiveLicenceFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.UploadEicFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.UploadEicrFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.UploadGasCertFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyReactivationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.math.BigDecimal
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PropertyReactivationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    companion object {
        const val INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER = 52L
        const val INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER = 6L
        const val INACTIVE_PROPERTY_OWNED_BY_ANOTHER_LANDLORD = 2L
        const val INACTIVE_PROPERTY_REGISTERED_AGAIN_BY_ANOTHER_LANDLORD = 53L
        const val ACTIVE_PROPERTY = 1L
    }

    private val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    @MockitoBean
    private lateinit var confirmationEmailSender: EmailNotificationService<PropertyReactivationConfirmationEmail>

    @BeforeEach
    fun stubEpcLookup() {
        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse())
    }

    @Test
    fun `a landlord can walk through every provide later step to the confirmation page`(
        page: Page,
        @Autowired jdbcTemplate: JdbcTemplate,
    ) {
        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        assertThat(infoPage.heading).containsText("Reactivate your property")
        infoPage.submit()

        val licensingPage = assertPageIs(page, LicensingTypeFormPagePropertyReactivation::class, urlArguments)
        licensingPage.submitLicensingType(LicensingType.NO_LICENSING)

        val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments)
        hasGasSupplyPage.submitHasNoGasSupply()

        val checkGasSafetyAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments)
        assertThat(checkGasSafetyAnswersPage.gasSupplySummaryList.gasSupplyRow.value).containsText("No")
        checkGasSafetyAnswersPage.form.submit()

        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPagePropertyReactivation::class, urlArguments)
        hasElectricalCertPage.submitHasNoCert()

        val electricalCertMissingPage = assertPageIs(page, ElectricalCertMissingFormPagePropertyReactivation::class, urlArguments)
        assertThat(electricalCertMissingPage.form.submitButton).hasText("Continue without a valid electrical safety certificate")
        electricalCertMissingPage.form.submit()

        val checkElectricalSafetyAnswersPage =
            assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyReactivation::class, urlArguments)
        checkElectricalSafetyAnswersPage.form.submit()

        assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation::class, urlArguments).submitYes()
        assertPageIs(page, CheckEpcAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()

        completeTenancyDetailsTask(page)

        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        assertThat(checkAnswersPage.heading).containsText("Check your answers for: Inactive House, PRSDB Road, IN1 1AA")
        assertThat(checkAnswersPage.licensingSummaryList.licensingTypeRow.value).containsText("This property does not need a licence")
        assertThat(checkAnswersPage.complianceSummaryList.gasSupplyRow.value).containsText("No")
        assertThat(checkAnswersPage.tenancySummaryList.numberOfHouseholdsRow.value).containsText("1")
        assertThat(checkAnswersPage.tenancySummaryList.rentAmountRow.value).containsText("400")
        checkAnswersPage.submit()

        val confirmMissingCompliancePage =
            assertPageIs(page, ConfirmMissingComplianceFormPagePropertyReactivation::class, urlArguments)
        assertThat(confirmMissingCompliancePage.electricalSectionHeading).isVisible()
        confirmMissingCompliancePage.submitYes()

        assertPageIs(page, ConfirmationPagePropertyReactivation::class, urlArguments)
        assertEquals(true, isPropertyActive(jdbcTemplate, INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER))

        val savedPropertyOwnership =
            jdbcTemplate.queryForMap(
                "SELECT license_provide_later, license_id, tenancy_provide_later, rent_amount FROM property_ownership WHERE id = ?",
                INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER,
            )
        assertEquals(false, savedPropertyOwnership["license_provide_later"])
        assertNull(savedPropertyOwnership["license_id"])
        assertEquals(false, savedPropertyOwnership["tenancy_provide_later"])
        assertEquals(0, BigDecimal("400").compareTo(savedPropertyOwnership["rent_amount"] as BigDecimal))

        val savedCompliance =
            jdbcTemplate.queryForMap(
                "SELECT has_gas_supply, gas_safety_cert_provide_later, electrical_safety_cert_provide_later, epc_provide_later, epc_url " +
                    "FROM property_compliance WHERE property_ownership_id = ?",
                INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER,
            )
        assertEquals(false, savedCompliance["has_gas_supply"])
        assertEquals(false, savedCompliance["gas_safety_cert_provide_later"])
        assertEquals(false, savedCompliance["electrical_safety_cert_provide_later"])
        assertEquals(false, savedCompliance["epc_provide_later"])
        assertNotNull(savedCompliance["epc_url"])
        verify(confirmationEmailSender).sendEmail(any(), any())
    }

    @Test
    fun `a landlord can provide a gas safety certificate and every gas safety button says continue`(page: Page) {
        val hasGasSupplyPage = navigateToHasGasSupplyPage(page)
        assertThat(hasGasSupplyPage.sectionHeader).containsText("Gas safety certificate")
        assertThat(hasGasSupplyPage.provideThisLaterButton).isHidden()
        assertThat(hasGasSupplyPage.form.submitButton).hasText("Continue")
        hasGasSupplyPage.submitHasGasSupply()

        val hasGasCertPage = assertPageIs(page, HasGasCertFormPagePropertyReactivation::class, urlArguments)
        assertThat(hasGasCertPage.form.submitButton).hasText("Continue")
        hasGasCertPage.submitHasCertificate()

        var issueDatePage = assertPageIs(page, GasCertIssueDateFormPagePropertyReactivation::class, urlArguments)
        assertThat(issueDatePage.form.submitButton).hasText("Continue")
        issueDatePage.submitDate(
            DateTimeHelper().getCurrentDateInUK().minus(DatePeriod(months = GAS_SAFETY_CERT_VALIDITY_MONTHS, days = 5)),
        )

        val expiredPage = assertPageIs(page, GasCertExpiredFormPagePropertyReactivation::class, urlArguments)
        assertThat(expiredPage.form.submitButton).hasText("Continue without a valid gas safety certificate")
        expiredPage.changeIssueDateLink.clickAndWait()

        issueDatePage = assertPageIs(page, GasCertIssueDateFormPagePropertyReactivation::class, urlArguments)
        issueDatePage.submitDate(DateTimeHelper().getCurrentDateInUK())

        val uploadPage = assertPageIs(page, UploadGasCertFormPagePropertyReactivation::class, urlArguments)
        uploadPage.uploadGasCertificate(Path.of("src/test/resources/test-files/blank.png"))

        val checkUploadsPage = assertPageIs(page, CheckGasCertUploadsFormPagePropertyReactivation::class, urlArguments)
        assertThat(checkUploadsPage.form.submitButton).hasText("Continue")
        checkUploadsPage.form.submit()

        val checkGasSafetyAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments)
        assertThat(checkGasSafetyAnswersPage.sectionHeader).isHidden()
        assertThat(checkGasSafetyAnswersPage.gasSupplySummaryList.gasSupplyRow.value).containsText("Yes")
        assertThat(checkGasSafetyAnswersPage.form.submitButton).hasText("Continue")
        checkGasSafetyAnswersPage.form.submit()

        assertPageIs(page, HasElectricalCertFormPagePropertyReactivation::class, urlArguments)
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = CertificateType::class, names = ["Eic", "Eicr"])
    fun `a landlord can provide an electrical safety certificate and every electrical safety button says continue`(
        certificateType: CertificateType,
        page: Page,
    ) {
        val hasElectricalCertPage = navigateToHasElectricalCertPage(page)
        assertThat(hasElectricalCertPage.form.sectionHeader).containsText("Electrical safety certificate")
        assertThat(hasElectricalCertPage.provideThisLaterButton).isHidden()
        assertThat(hasElectricalCertPage.form.submitButton).hasText("Continue")
        submitHasCertificate(hasElectricalCertPage, certificateType)

        var certDatePage = assertCertDatePage(page, certificateType)
        assertThat(certDatePage.form.submitButton).hasText("Continue")
        certDatePage.submitDate(expiredCertDate(certificateType))

        val (expiredPageForm, changeDateLink) = assertExpiredPageFormAndChangeDateLink(page, certificateType)
        assertThat(expiredPageForm.submitButton).hasText("Continue without a valid electrical safety certificate")
        changeDateLink.clickAndWait()

        certDatePage = assertCertDatePage(page, certificateType)
        certDatePage.submitDate(validCertDate(certificateType))

        assertUploadPage(page, certificateType).uploadCertificate("validFile.png")

        var checkUploadsPage = assertCheckUploadsPage(page, certificateType)
        assertThat(checkUploadsPage.form.submitButton).hasText("Continue")
        checkUploadsPage.backLink.clickAndWait()

        assertCertDatePage(page, certificateType).submitDate(validCertDate(certificateType))

        checkUploadsPage = assertCheckUploadsPage(page, certificateType)
        checkUploadsPage.form.submit()

        val checkElectricalSafetyAnswersPage =
            assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyReactivation::class, urlArguments)
        assertThat(checkElectricalSafetyAnswersPage.sectionHeader).isHidden()
        assertThat(checkElectricalSafetyAnswersPage.form.submitButton).hasText("Continue")
        checkElectricalSafetyAnswersPage.form.submit()

        assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord can provide an EPC exemption and every EPC button on the way says continue`(page: Page) {
        navigateToEpcTask(page)

        val confirmEpcPage = assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation::class, urlArguments)
        assertThat(confirmEpcPage.form.submitButton).hasText("Continue")
        confirmEpcPage.submitNo()

        val hasEpcPage = assertPageIs(page, HasEpcFormPagePropertyReactivation::class, urlArguments)
        assertThat(hasEpcPage.form.sectionHeader).containsText("Energy performance certificate (EPC)")
        assertThat(hasEpcPage.provideThisLaterButton).isHidden()
        assertThat(hasEpcPage.form.submitButton).hasText("Continue")
        hasEpcPage.submitHasNoEpc()

        val isEpcRequiredPage = assertPageIs(page, IsEpcRequiredFormPagePropertyReactivation::class, urlArguments)
        assertThat(isEpcRequiredPage.form.submitButton).hasText("Continue")
        isEpcRequiredPage.submitNo()

        val epcExemptionPage = assertPageIs(page, EpcExemptionFormPagePropertyReactivation::class, urlArguments)
        assertThat(epcExemptionPage.form.submitButton).hasText("Continue")
        epcExemptionPage.submitExemptionReason(EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT)

        val checkEpcAnswersPage = assertPageIs(page, CheckEpcAnswersFormPagePropertyReactivation::class, urlArguments)
        assertThat(checkEpcAnswersPage.sectionHeader).isHidden()
        assertThat(checkEpcAnswersPage.rows.hasEpcRow.value).containsText("No")
        assertThat(checkEpcAnswersPage.form.submitButton).hasText("Continue")
        checkEpcAnswersPage.form.submit()

        assertPageIs(page, NumberOfHouseholdsFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord can provide tenancy details and every tenancy details button says continue`(page: Page) {
        val numberOfHouseholdsPage = navigateToNumberOfHouseholdsPage(page)
        assertThat(numberOfHouseholdsPage.sectionHeader).containsText("Tenancy details")
        assertThat(numberOfHouseholdsPage.provideThisLaterButton).isHidden()
        assertThat(numberOfHouseholdsPage.form.submitButton).hasText("Continue")
        numberOfHouseholdsPage.submitNumberOfHouseholds(1)

        val numberOfPeoplePage = assertPageIs(page, NumberOfPeopleFormPagePropertyReactivation::class, urlArguments)
        assertThat(numberOfPeoplePage.form.submitButton).hasText("Continue")
        numberOfPeoplePage.submitNumOfPeople(2)

        val rentIncludesBillsPage = assertPageIs(page, RentIncludesBillsFormPagePropertyReactivation::class, urlArguments)
        assertThat(rentIncludesBillsPage.form.submitButton).hasText("Continue")
        rentIncludesBillsPage.submitIsIncluded()

        val billsIncludedPage = assertPageIs(page, BillsIncludedFormPagePropertyReactivation::class, urlArguments)
        assertThat(billsIncludedPage.form.submitButton).hasText("Continue")
        billsIncludedPage.selectGasElectricityWater()
        billsIncludedPage.form.submit()

        val furnishedStatusPage = assertPageIs(page, FurnishedStatusFormPagePropertyReactivation::class, urlArguments)
        assertThat(furnishedStatusPage.form.submitButton).hasText("Continue")
        furnishedStatusPage.submitFurnishedStatus(FurnishedStatus.FURNISHED)

        val rentFrequencyPage = assertPageIs(page, RentFrequencyFormPagePropertyReactivation::class, urlArguments)
        assertThat(rentFrequencyPage.form.submitButton).hasText("Continue")
        rentFrequencyPage.selectRentFrequency(RentFrequency.MONTHLY)
        rentFrequencyPage.form.submit()

        val rentAmountPage = assertPageIs(page, RentAmountFormPagePropertyReactivation::class, urlArguments)
        assertThat(rentAmountPage.form.submitButton).hasText("Continue")
        rentAmountPage.submitRentAmount("400")

        assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord goes straight to the confirmation page when there are no details to provide`(
        page: Page,
        @Autowired jdbcTemplate: JdbcTemplate,
    ) {
        val noDetailsUrlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER)
        infoPage.submit()

        val confirmationPage = assertPageIs(page, ConfirmationPagePropertyReactivation::class, noDetailsUrlArguments)
        assertThat(confirmationPage.jointLandlordsHeading).isHidden()
        assertThat(confirmationPage.landlordAccountHeading).isHidden()
        assertEquals(true, isPropertyActive(jdbcTemplate, INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER))
        verify(confirmationEmailSender).sendEmail(any(), argThat { !landlordReactivated && paymentDetails == null })
    }

    @Test
    fun `the back link on the info page returns to the deactivated property record`(page: Page) {
        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        infoPage.backLink.clickAndWait()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `the back link on a provide later step returns to the previous step`(page: Page) {
        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        infoPage.submit()

        val licensingPage = assertPageIs(page, LicensingTypeFormPagePropertyReactivation::class, urlArguments)
        licensingPage.backLink.clickAndWait()

        assertPageIs(page, InfoFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `the back links into and out of the gas safety task return to the adjacent steps`(page: Page) {
        val hasGasSupplyPage = navigateToHasGasSupplyPage(page)
        hasGasSupplyPage.backLink.clickAndWait()
        assertPageIs(page, LicensingTypeFormPagePropertyReactivation::class, urlArguments).submitLicensingType(LicensingType.NO_LICENSING)

        assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments).submitHasNoGasSupply()
        assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()

        val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPagePropertyReactivation::class, urlArguments)
        hasElectricalCertPage.backLink.clickAndWait()

        assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `the back links into and out of the EPC task return to the adjacent steps`(page: Page) {
        navigateToEpcTask(page)

        val confirmEpcPage = assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation::class, urlArguments)
        confirmEpcPage.backLink.clickAndWait()
        assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()

        assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation::class, urlArguments).submitYes()
        assertPageIs(page, CheckEpcAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()

        val numberOfHouseholdsPage = assertPageIs(page, NumberOfHouseholdsFormPagePropertyReactivation::class, urlArguments)
        numberOfHouseholdsPage.backLink.clickAndWait()

        assertPageIs(page, CheckEpcAnswersFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `the back links into and out of the tenancy details task return to the adjacent steps`(page: Page) {
        val numberOfHouseholdsPage = navigateToNumberOfHouseholdsPage(page)
        numberOfHouseholdsPage.backLink.clickAndWait()
        assertPageIs(page, CheckEpcAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()

        completeTenancyDetailsTask(page)

        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        checkAnswersPage.backLink.clickAndWait()

        assertPageIs(page, RentAmountFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `the back link on the rent includes bills page returns to the number of people page`(page: Page) {
        val numberOfHouseholdsPage = navigateToNumberOfHouseholdsPage(page)
        numberOfHouseholdsPage.submitNumberOfHouseholds(1)
        assertPageIs(page, NumberOfPeopleFormPagePropertyReactivation::class, urlArguments).submitNumOfPeople(2)

        val rentIncludesBillsPage = assertPageIs(page, RentIncludesBillsFormPagePropertyReactivation::class, urlArguments)
        rentIncludesBillsPage.backLink.clickAndWait()

        assertPageIs(page, NumberOfPeopleFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord is sent to the info page if they visit a step the property does not need`(page: Page) {
        val noDetailsUrlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER.toString())

        navigator.navigateToPropertyReactivationLicensingPageAfterInfo(INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER)

        assertPageIs(page, InfoFormPagePropertyReactivation::class, noDetailsUrlArguments)
    }

    @Test
    fun `a landlord is shown the already registered page when the property has already been registered again`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_REGISTERED_AGAIN_BY_ANOTHER_LANDLORD.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_REGISTERED_AGAIN_BY_ANOTHER_LANDLORD)
        infoPage.submit()

        val alreadyRegisteredPage = assertPageIs(page, AlreadyRegisteredPagePropertyReactivation::class, urlArguments)
        alreadyRegisteredPage.backLink.clickAndWait()

        assertPageIs(page, InfoFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord is shown the already registered page if the address is registered by someone else before they confirm their answers`(
        page: Page,
        @Autowired jdbcTemplate: JdbcTemplate,
    ) {
        val checkAnswersPage = navigator.skipToPropertyReactivationCheckAnswersPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        // Simulate someone else registering the address after the landlord started the journey
        jdbcTemplate.update(
            "UPDATE property_ownership SET address_id = (SELECT address_id FROM property_ownership WHERE id = ?) WHERE id = ?",
            INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER,
            ACTIVE_PROPERTY,
        )

        checkAnswersPage.submit()
        assertPageIs(page, ConfirmMissingComplianceFormPagePropertyReactivation::class, urlArguments).submitYes()

        assertPageIs(page, AlreadyRegisteredPagePropertyReactivation::class, urlArguments)
        assertEquals(false, isPropertyActive(jdbcTemplate, INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER))
        verify(confirmationEmailSender, never()).sendEmail(any(), any())
    }

    @Test
    fun `a landlord is sent to the info page if they visit the already registered page when the property hasn't already been registered`(
        page: Page,
    ) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        navigator.navigateToPropertyReactivationAlreadyRegisteredPageAfterInfo(
            INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER,
            isPropertyAlreadyRegistered = false,
        )

        assertPageIs(page, InfoFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord is sent to the info page if they visit a task step when the property has already been registered`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        navigator.navigateToPropertyReactivationLicensingPageAfterInfo(
            INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER,
            isPropertyAlreadyRegistered = true,
        )

        assertPageIs(page, InfoFormPagePropertyReactivation::class, urlArguments)
    }

    @ParameterizedTest
    @EnumSource(
        value = LicensingType::class,
        names = ["SELECTIVE_LICENCE", "HMO_MANDATORY_LICENCE", "HMO_ADDITIONAL_LICENCE"],
    )
    fun `licence number branches continue and retain their answers`(
        type: LicensingType,
        page: Page,
        @Autowired jdbcTemplate: JdbcTemplate,
    ) {
        val typePage = navigateToLicensingPage(page)
        assertThat(typePage.form.submitButton).hasCount(1)
        assertThat(typePage.form.submitButton).hasText("Continue")
        assertThat(typePage.provideThisLaterButton).isHidden()
        assertThat(typePage.form.sectionHeader).containsText("Tell us if this property needs a licence")
        typePage.submitLicensingType(type)

        var numberPage = assertLicenceNumberPage(page, type)
        assertThat(numberPage.form.submitButton).hasText("Continue")
        assertThat(numberPage.form.submitButton).hasCount(1)
        numberPage.submitLicenseNumber("LIC-123")

        val gasPage = assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments)
        assertEquals(
            true,
            jdbcTemplate.queryForObject(
                "SELECT license_provide_later AND license_id IS NULL FROM property_ownership WHERE id = ?",
                Boolean::class.java,
                INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER,
            ),
        )
        gasPage.backLink.clickAndWait()
        numberPage = assertLicenceNumberPage(page, type)
        assertThat(numberPage.form.licenceNumberInput).hasValue("LIC-123")
        numberPage.backLink.clickAndWait()
        assertPageIs(page, LicensingTypeFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `licensing type retains its validation`(page: Page) {
        val typePage = navigateToLicensingPage(page)
        typePage.form.submitPrimaryButton()
        assertThat(typePage.form.getErrorMessage())
            .containsText("Select this property’s licence, or select ‘This property does not need a licence")
    }

    @Test
    fun `choosing no licence does not persist the answer before final submission`(
        page: Page,
        @Autowired jdbcTemplate: JdbcTemplate,
    ) {
        val propertyId = INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER
        setLicensingAsTheOnlyDetailToProvideLater(jdbcTemplate, propertyId)
        navigator.goToPropertyReactivationInfoPage(propertyId).submit()
        val typePage = assertPageIs(page, LicensingTypeFormPagePropertyReactivation::class, urlArguments)
        typePage.submitLicensingType(LicensingType.NO_LICENSING)

        val checkAnswers = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        assertEquals(
            true,
            jdbcTemplate.queryForObject(
                "SELECT license_provide_later AND license_id IS NULL FROM property_ownership WHERE id = ?",
                Boolean::class.java,
                propertyId,
            ),
        )
        checkAnswers.backLink.clickAndWait()
        assertPageIs(page, LicensingTypeFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord skips the confirm missing compliance page when no compliance tasks are required`(
        page: Page,
        @Autowired jdbcTemplate: JdbcTemplate,
    ) {
        val propertyId = INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER
        setLicensingAsTheOnlyDetailToProvideLater(jdbcTemplate, propertyId)
        navigator.goToPropertyReactivationInfoPage(propertyId).submit()
        assertPageIs(page, LicensingTypeFormPagePropertyReactivation::class, urlArguments)
            .submitLicensingType(LicensingType.NO_LICENSING)

        assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments).submit()

        assertPageIs(page, ConfirmationPagePropertyReactivation::class, urlArguments)
        assertEquals(true, isPropertyActive(jdbcTemplate, propertyId))
    }

    @Test
    fun `a landlord receives a 404 for an active property`() {
        val response = navigator.navigate(ReactivatePropertyController.getPropertyReactivationPath(ACTIVE_PROPERTY))

        assertEquals(404, response?.status())
    }

    @Test
    fun `a landlord receives a 404 for an inactive property they do not own`() {
        val response =
            navigator.navigate(ReactivatePropertyController.getPropertyReactivationPath(INACTIVE_PROPERTY_OWNED_BY_ANOTHER_LANDLORD))

        assertEquals(404, response?.status())
    }

    @Nested
    inner class PropertyWithManualAddress :
        NestedIntegrationTestWithMutableData(
            listOf("data-local.sql", "data-overlay-inactive-property-has-manual-address.sql"),
        ) {
        @Test
        fun `a landlord is asked whether they have an EPC when the address has no UPRN to look one up with`(page: Page) {
            navigateToEpcTask(page)

            assertPageIs(page, HasEpcFormPagePropertyReactivation::class, urlArguments)
            verify(epcRegisterClient, never()).getByUprn(any())
        }
    }

    @Nested
    inner class WithInactiveLandlord :
        NestedIntegrationTestWithMutableData(
            listOf("data-mockuser-inactive-landlord.sql", "data-overlay-inactive-landlord-has-deactivated-property.sql"),
        ) {
        private val deactivatedPropertyId = 1L

        @Test
        fun `the confirmation page tells the landlord their account was reactivated`(page: Page) {
            val infoPage = navigator.goToPropertyReactivationInfoPage(deactivatedPropertyId)
            infoPage.submit()

            val confirmationPage =
                assertPageIs(
                    page,
                    ConfirmationPagePropertyReactivation::class,
                    mapOf("propertyOwnershipId" to deactivatedPropertyId.toString()),
                )
            assertThat(confirmationPage.reactivationDetailsHeading).isVisible()
            assertThat(confirmationPage.landlordAccountText).hasText("Your landlord account is now active.")
            assertThat(confirmationPage.jointLandlordsHeading).isHidden()
        }
    }

    @Nested
    inner class WithJointLandlord :
        NestedIntegrationTestWithMutableData(
            listOf("data-local.sql", "data-overlay-inactive-property-has-joint-landlord.sql"),
        ) {
        @Test
        fun `the confirmation page tells the landlord their joint landlords were notified`(page: Page) {
            val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER)
            infoPage.submit()

            val confirmationPage =
                assertPageIs(
                    page,
                    ConfirmationPagePropertyReactivation::class,
                    mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER.toString()),
                )
            assertThat(confirmationPage.jointLandlordsHeading).isVisible()
            assertThat(confirmationPage.landlordAccountHeading).isHidden()
        }
    }

    private fun navigateToLicensingPage(page: Page): LicensingTypeFormPagePropertyReactivation {
        navigator.navigateToPropertyReactivationLicensingPageAfterInfo(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        return assertPageIs(page, LicensingTypeFormPagePropertyReactivation::class, urlArguments)
    }

    private fun navigateToHasGasSupplyPage(page: Page): HasGasSupplyFormPagePropertyReactivation {
        navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER).submit()
        assertPageIs(page, LicensingTypeFormPagePropertyReactivation::class, urlArguments).submitLicensingType(LicensingType.NO_LICENSING)
        return assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments)
    }

    private fun navigateToHasElectricalCertPage(page: Page): HasElectricalCertFormPagePropertyReactivation {
        navigateToHasGasSupplyPage(page).submitHasNoGasSupply()
        assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()
        return assertPageIs(page, HasElectricalCertFormPagePropertyReactivation::class, urlArguments)
    }

    private fun navigateToEpcTask(page: Page) {
        navigateToHasElectricalCertPage(page).submitHasNoCert()
        assertPageIs(page, ElectricalCertMissingFormPagePropertyReactivation::class, urlArguments).form.submit()
        assertPageIs(page, CheckElectricalSafetyAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()
    }

    private fun navigateToNumberOfHouseholdsPage(page: Page): NumberOfHouseholdsFormPagePropertyReactivation {
        navigateToEpcTask(page)
        assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation::class, urlArguments).submitYes()
        assertPageIs(page, CheckEpcAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()
        return assertPageIs(page, NumberOfHouseholdsFormPagePropertyReactivation::class, urlArguments)
    }

    private fun completeTenancyDetailsTask(page: Page) {
        assertPageIs(page, NumberOfHouseholdsFormPagePropertyReactivation::class, urlArguments).submitNumberOfHouseholds(1)
        assertPageIs(page, NumberOfPeopleFormPagePropertyReactivation::class, urlArguments).submitNumOfPeople(2)
        assertPageIs(page, RentIncludesBillsFormPagePropertyReactivation::class, urlArguments).submitIsNotIncluded()
        assertPageIs(page, FurnishedStatusFormPagePropertyReactivation::class, urlArguments)
            .submitFurnishedStatus(FurnishedStatus.FURNISHED)
        val rentFrequencyPage = assertPageIs(page, RentFrequencyFormPagePropertyReactivation::class, urlArguments)
        rentFrequencyPage.selectRentFrequency(RentFrequency.MONTHLY)
        rentFrequencyPage.form.submit()
        assertPageIs(page, RentAmountFormPagePropertyReactivation::class, urlArguments).submitRentAmount("400")
    }

    private fun assertLicenceNumberPage(
        page: Page,
        type: LicensingType,
    ): LicenceNumberFormPage =
        when (type) {
            LicensingType.SELECTIVE_LICENCE ->
                assertPageIs(page, SelectiveLicenceFormPagePropertyReactivation::class, urlArguments)

            LicensingType.HMO_MANDATORY_LICENCE ->
                assertPageIs(page, HmoMandatoryLicenceFormPagePropertyReactivation::class, urlArguments)

            LicensingType.HMO_ADDITIONAL_LICENCE ->
                assertPageIs(page, HmoAdditionalLicenceFormPagePropertyReactivation::class, urlArguments)

            else -> error("Unexpected licensing type $type")
        }

    private fun submitHasCertificate(
        hasElectricalCertPage: HasElectricalCertFormPagePropertyReactivation,
        certificateType: CertificateType,
    ) = when (certificateType) {
        CertificateType.Eic -> hasElectricalCertPage.submitHasEic()
        CertificateType.Eicr -> hasElectricalCertPage.submitHasEicr()
        else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
    }

    private fun validCertDate(certificateType: CertificateType) =
        when (certificateType) {
            CertificateType.Eic -> DateTimeHelper().getCurrentDateInUK().minus(DatePeriod(years = 1))
            else -> DateTimeHelper().getCurrentDateInUK().plus(DatePeriod(years = 1))
        }

    private fun expiredCertDate(certificateType: CertificateType) =
        when (certificateType) {
            CertificateType.Eic -> DateTimeHelper().getCurrentDateInUK().minus(DatePeriod(years = EIC_VALIDITY_YEARS, days = 5))
            else -> DateTimeHelper().getCurrentDateInUK().minus(DatePeriod(days = 5))
        }

    private fun assertCertDatePage(
        page: Page,
        certificateType: CertificateType,
    ): DateFormPage =
        when (certificateType) {
            CertificateType.Eic -> assertPageIs(page, EicIssueDateFormPagePropertyReactivation::class, urlArguments)
            CertificateType.Eicr -> assertPageIs(page, EicrExpiryDateFormPagePropertyReactivation::class, urlArguments)
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun assertExpiredPageFormAndChangeDateLink(
        page: Page,
        certificateType: CertificateType,
    ): Pair<PostForm, Link> =
        when (certificateType) {
            CertificateType.Eic ->
                assertPageIs(page, EicExpiredFormPagePropertyReactivation::class, urlArguments)
                    .let { it.form to it.changeIssueDateLink }

            CertificateType.Eicr ->
                assertPageIs(page, EicrExpiredFormPagePropertyReactivation::class, urlArguments)
                    .let { it.form to it.changeExpiryDateLink }

            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun assertUploadPage(
        page: Page,
        certificateType: CertificateType,
    ): UploadCertificateFormPage =
        when (certificateType) {
            CertificateType.Eic -> assertPageIs(page, UploadEicFormPagePropertyReactivation::class, urlArguments)
            CertificateType.Eicr -> assertPageIs(page, UploadEicrFormPagePropertyReactivation::class, urlArguments)
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun assertCheckUploadsPage(
        page: Page,
        certificateType: CertificateType,
    ): CheckElectricalCertUploadsFormBasePage =
        when (certificateType) {
            CertificateType.Eic -> assertPageIs(page, CheckEicUploadsFormPagePropertyReactivation::class, urlArguments)
            CertificateType.Eicr -> assertPageIs(page, CheckEicrUploadsFormPagePropertyReactivation::class, urlArguments)
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun setLicensingAsTheOnlyDetailToProvideLater(
        jdbcTemplate: JdbcTemplate,
        propertyOwnershipId: Long,
    ) {
        jdbcTemplate.update("UPDATE property_ownership SET tenancy_provide_later = false WHERE id = ?", propertyOwnershipId)
        jdbcTemplate.update(
            "UPDATE property_compliance SET gas_safety_cert_provide_later = false, " +
                "electrical_safety_cert_provide_later = false, epc_provide_later = false WHERE property_ownership_id = ?",
            propertyOwnershipId,
        )
    }

    private fun isPropertyActive(
        jdbcTemplate: JdbcTemplate,
        propertyOwnershipId: Long,
    ) = jdbcTemplate.queryForObject(
        "SELECT is_active FROM property_ownership WHERE id = ?",
        Boolean::class.java,
        propertyOwnershipId,
    )
}
