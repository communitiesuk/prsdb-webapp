package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.integration.PropertyRegistrationJourneyTests.Companion.expiredExpiryDate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.CheckEpcAnswersFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.ConfirmEpcDetailsRetrievedByCertificateNumberPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.ConfirmEpcDetailsRetrievedByUprnFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.EpcExemptionFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.EpcExpiredFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.EpcInDateAtStartOfTenancyCheckPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.EpcMissingFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.FindYourEpcFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.HasEpcFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.HasMeesExemptionFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.IsEpcRequiredFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.MeesExemptionFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

class UpdateEpcJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val propertyOwnershipId = 8L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())
    private val uprn = 1013L

    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    private fun enterUpdateJourney(): PropertyDetailsPageLandlordView {
        val propertyDetails = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetails.tabs.goToComplianceInformation()
        propertyDetails.epcCard.getAction("Change").link.clickAndWait()
        return propertyDetails
    }

    private fun assertPropertyDetailsUpdated(
        page: Page,
        epcStatus: String,
    ) {
        val propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.epcRow.value).containsText(epcStatus)
    }

    @Test
    fun `A property can have its EPC updated with a valid certificate with MEES found by UPRN`(page: Page) {
        whenever(epcRegisterClient.getByUprn(uprn))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    energyRating = "F",
                ),
            )

        enterUpdateJourney()

        val confirmEpcPage = assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPageUpdateEpc::class, urlArguments)
        confirmEpcPage.submitYes()

        // MEES flow
        val hasMeesExemptionPage = assertPageIs(page, HasMeesExemptionFormPageUpdateEpc::class, urlArguments)
        hasMeesExemptionPage.submitHasMeesExemption()
        val meesExemptionPage = assertPageIs(page, MeesExemptionFormPageUpdateEpc::class, urlArguments)
        meesExemptionPage.submitExemptionReason(MeesExemptionReason.HIGH_COST)

        val checkAnswersPage = assertPageIs(page, CheckEpcAnswersFormPageUpdateEpc::class, urlArguments)
        checkAnswersPage.form.submit()

        assertPropertyDetailsUpdated(page, "View EPC (opens in new tab)")
    }

    @Test
    fun `A property can have its EPC updated with an expired certificate not found by UPRN`(page: Page) {
        whenever(epcRegisterClient.getByUprn(uprn))
            .thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        enterUpdateJourney()

        // Has EPC
        val hasEpcPage = assertPageIs(page, HasEpcFormPageUpdateEpc::class, urlArguments)
        // The "Provide this later" route should not be available on the update journey
        assertThat(hasEpcPage.provideThisLaterButton).isHidden()
        hasEpcPage.submitHasEpc()

        // EPC Search
        val findYourEpcPage = assertPageIs(page, FindYourEpcFormPageUpdateEpc::class, urlArguments)
        whenever(epcRegisterClient.getByRrn(CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    certificateNumber = CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER,
                    expiryDate = expiredExpiryDate,
                    latestCertificateNumberForThisProperty = CURRENT_EXPIRED_EPC_CERTIFICATE_NUMBER,
                ),
            )
        findYourEpcPage.submitCurrentEpcNumberWhichIsExpired()

        // Confirm EPC
        val confirmYourEpcPage = assertPageIs(page, ConfirmEpcDetailsRetrievedByCertificateNumberPageUpdateEpc::class, urlArguments)
        confirmYourEpcPage.submitYes()

        // Expiry check
        val epcExpiryCheckPage = assertPageIs(page, EpcInDateAtStartOfTenancyCheckPageUpdateEpc::class, urlArguments)
        epcExpiryCheckPage.submitEpcExpired()

        // EPC is expired
        val epcExpiredPage = assertPageIs(page, EpcExpiredFormPageUpdateEpc::class, urlArguments)
        epcExpiredPage.form.submit()

        // CYA
        val checkEpcAnswersPage = assertPageIs(page, CheckEpcAnswersFormPageUpdateEpc::class, urlArguments)
        checkEpcAnswersPage.form.submit()

        // Return to property details
        assertPropertyDetailsUpdated(page, "View expired EPC (opens in new tab)")
    }

    @Test
    fun `A property can have its EPC updated an exemption reason`(page: Page) {
        whenever(epcRegisterClient.getByUprn(uprn))
            .thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        enterUpdateJourney()

        val hasEpcPage = assertPageIs(page, HasEpcFormPageUpdateEpc::class, urlArguments)
        // The "Provide this later" route should not be available on the update journey
        assertThat(hasEpcPage.provideThisLaterButton).isHidden()
        hasEpcPage.submitHasNoEpc()

        val isEpcRequiredPage = assertPageIs(page, IsEpcRequiredFormPageUpdateEpc::class, urlArguments)
        isEpcRequiredPage.submitNo()

        val epcExemptionPage = assertPageIs(page, EpcExemptionFormPageUpdateEpc::class, urlArguments)
        epcExemptionPage.submitExemptionReason(EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT)

        val checkAnswersPage = assertPageIs(page, CheckEpcAnswersFormPageUpdateEpc::class, urlArguments)
        checkAnswersPage.form.submit()

        assertPropertyDetailsUpdated(page, "Not required")
    }

    @Test
    fun `A property can have its EPC updated to missing`(page: Page) {
        whenever(epcRegisterClient.getByUprn(uprn))
            .thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        enterUpdateJourney()

        val hasEpcPage = assertPageIs(page, HasEpcFormPageUpdateEpc::class, urlArguments)
        // The "Provide this later" route should not be available on the update journey
        assertThat(hasEpcPage.provideThisLaterButton).isHidden()
        hasEpcPage.submitHasNoEpc()

        val isEpcRequiredPage = assertPageIs(page, IsEpcRequiredFormPageUpdateEpc::class, urlArguments)
        isEpcRequiredPage.submitYes()
        val epcMissingPage = assertPageIs(page, EpcMissingFormPageUpdateEpc::class, urlArguments)

        epcMissingPage.form.submit()
        val checkAnswersPage = assertPageIs(page, CheckEpcAnswersFormPageUpdateEpc::class, urlArguments)
        checkAnswersPage.form.submit()

        assertPropertyDetailsUpdated(page, "Not added")
    }
}
