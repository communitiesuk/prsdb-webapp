package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_MONTHS
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckAnswersPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckEpcAnswersFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckGasCertUploadsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckGasSafetyAnswersFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.ConfirmationPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.ElectricalSafetyFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EpcExemptionFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.GasCertExpiredFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.GasCertIssueDateFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasEpcFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasGasCertFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasGasSupplyFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.InfoFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.IsEpcRequiredFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.LicensingFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.TenancyDetailsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.UploadGasCertFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.nio.file.Path
import kotlin.test.assertEquals

class PropertyReactivationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    companion object {
        const val INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER = 52L
        const val INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER = 6L
        const val INACTIVE_PROPERTY_OWNED_BY_ANOTHER_LANDLORD = 2L
        const val ACTIVE_PROPERTY = 1L
    }

    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    @BeforeEach
    fun stubEpcLookup() {
        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse())
    }

    @Test
    fun `a landlord can walk through every provide later step to the confirmation page`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        assertThat(infoPage.heading).containsText("Reactivate your property")
        infoPage.submit()

        val licensingPage = assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments)
        assertThat(licensingPage.heading).containsText("PDJB-2037")
        licensingPage.submit()

        val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments)
        hasGasSupplyPage.submitHasNoGasSupply()

        val checkGasSafetyAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments)
        assertThat(checkGasSafetyAnswersPage.gasSupplySummaryList.gasSupplyRow.value).containsText("No")
        checkGasSafetyAnswersPage.form.submit()

        val electricalSafetyPage = assertPageIs(page, ElectricalSafetyFormPagePropertyReactivation::class, urlArguments)
        assertThat(electricalSafetyPage.heading).containsText("PDJB-2039")
        electricalSafetyPage.submit()

        assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation::class, urlArguments).submitYes()
        assertPageIs(page, CheckEpcAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()

        val tenancyDetailsPage = assertPageIs(page, TenancyDetailsFormPagePropertyReactivation::class, urlArguments)
        assertThat(tenancyDetailsPage.heading).containsText("PDJB-2041")
        tenancyDetailsPage.submit()

        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        assertThat(checkAnswersPage.heading).containsText("PDJB-2042")
        checkAnswersPage.submit()

        assertPageIs(page, ConfirmationPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord can provide a gas safety certificate and every gas safety button says continue`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        infoPage.submit()

        val licensingPage = assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments)
        licensingPage.submit()

        val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments)
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
        assertThat(checkGasSafetyAnswersPage.gasSupplySummaryList.gasSupplyRow.value).containsText("Yes")
        assertThat(checkGasSafetyAnswersPage.form.submitButton).hasText("Continue")
        checkGasSafetyAnswersPage.form.submit()

        assertPageIs(page, ElectricalSafetyFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord can provide an EPC exemption and every EPC button on the way says continue`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        infoPage.submit()
        assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments).submit()
        assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments).submitHasNoGasSupply()
        assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()
        assertPageIs(page, ElectricalSafetyFormPagePropertyReactivation::class, urlArguments).submit()

        val confirmEpcPage = assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation::class, urlArguments)
        assertThat(confirmEpcPage.form.submitButton).hasText("Continue")
        confirmEpcPage.submitNo()

        val hasEpcPage = assertPageIs(page, HasEpcFormPagePropertyReactivation::class, urlArguments)
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
        assertThat(checkEpcAnswersPage.rows.hasEpcRow.value).containsText("No")
        assertThat(checkEpcAnswersPage.form.submitButton).hasText("Continue")
        checkEpcAnswersPage.form.submit()

        assertPageIs(page, TenancyDetailsFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord goes straight to check answers when there are no details to provide`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER)
        infoPage.submit()

        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        checkAnswersPage.backLink.clickAndWait()

        assertPageIs(page, InfoFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `the back link on a provide later step returns to the previous step`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        infoPage.submit()

        val licensingPage = assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments)
        licensingPage.backLink.clickAndWait()

        assertPageIs(page, InfoFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `the back links into and out of the gas safety task return to the adjacent steps`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        infoPage.submit()
        assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments).submit()

        val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments)
        hasGasSupplyPage.backLink.clickAndWait()
        assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments).submit()

        assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments).submitHasNoGasSupply()
        assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()

        val electricalSafetyPage = assertPageIs(page, ElectricalSafetyFormPagePropertyReactivation::class, urlArguments)
        electricalSafetyPage.backLink.clickAndWait()

        assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `the back links into and out of the EPC task return to the adjacent steps`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

        val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
        infoPage.submit()
        assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments).submit()
        assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments).submitHasNoGasSupply()
        assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()
        assertPageIs(page, ElectricalSafetyFormPagePropertyReactivation::class, urlArguments).submit()

        val confirmEpcPage = assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation::class, urlArguments)
        confirmEpcPage.backLink.clickAndWait()
        assertPageIs(page, ElectricalSafetyFormPagePropertyReactivation::class, urlArguments).submit()

        assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation::class, urlArguments).submitYes()
        assertPageIs(page, CheckEpcAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()

        val tenancyDetailsPage = assertPageIs(page, TenancyDetailsFormPagePropertyReactivation::class, urlArguments)
        tenancyDetailsPage.backLink.clickAndWait()

        assertPageIs(page, CheckEpcAnswersFormPagePropertyReactivation::class, urlArguments)
    }

    @Test
    fun `a landlord is sent to the info page if they visit a step the property does not need`(page: Page) {
        val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER.toString())

        navigator.navigateToPropertyReactivationLicensingPageAfterInfo(INACTIVE_PROPERTY_WITH_NO_DETAILS_TO_PROVIDE_LATER)

        assertPageIs(page, InfoFormPagePropertyReactivation::class, urlArguments)
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
            val urlArguments = mapOf("propertyOwnershipId" to INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER.toString())

            val infoPage = navigator.goToPropertyReactivationInfoPage(INACTIVE_PROPERTY_WITH_ALL_DETAILS_TO_PROVIDE_LATER)
            infoPage.submit()
            assertPageIs(page, LicensingFormPagePropertyReactivation::class, urlArguments).submit()
            assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments).submitHasNoGasSupply()
            assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyReactivation::class, urlArguments).form.submit()
            assertPageIs(page, ElectricalSafetyFormPagePropertyReactivation::class, urlArguments).submit()

            assertPageIs(page, HasEpcFormPagePropertyReactivation::class, urlArguments)
            verify(epcRegisterClient, never()).getByUprn(any())
        }
    }
}
