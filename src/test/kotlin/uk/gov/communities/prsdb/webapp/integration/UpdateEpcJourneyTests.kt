package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.CheckEpcAnswersFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.ConfirmEpcDetailsRetrievedByUprnFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.EpcExemptionFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.HasEpcFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages.IsEpcRequiredFormPageUpdateEpc
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

class UpdateEpcJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val propertyOwnershipId = 8L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())
    private val uprn = 1013L

    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    @BeforeEach
    fun setUp() {
        whenever(epcRegisterClient.getByUprn(uprn))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse())
    }

    @Test
    fun `A property can have its EPC updated with a valid certificate found by UPRN`(page: Page) {
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToComplianceInformation()
        propertyDetailsPage.epcCard
            .getAction("Change")
            .link
            .clickAndWait()

        val confirmEpcPage = assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPageUpdateEpc::class, urlArguments)
        confirmEpcPage.submitMatchedEpcDetailsCorrect()

        val checkAnswersPage = assertPageIs(page, CheckEpcAnswersFormPageUpdateEpc::class, urlArguments)
        checkAnswersPage.form.submit()

        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.epcRow.value).not().containsText("Not added")
    }

    @Test
    fun `A property can have its EPC updated with no certificate and an exemption reason`(page: Page) {
        whenever(epcRegisterClient.getByUprn(uprn))
            .thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToComplianceInformation()
        propertyDetailsPage.epcCard
            .getAction("Change")
            .link
            .clickAndWait()

        val hasEpcPage = assertPageIs(page, HasEpcFormPageUpdateEpc::class, urlArguments)
        hasEpcPage.submitHasNoEpc()

        val isEpcRequiredPage = assertPageIs(page, IsEpcRequiredFormPageUpdateEpc::class, urlArguments)
        isEpcRequiredPage.submitNo()

        val epcExemptionPage = assertPageIs(page, EpcExemptionFormPageUpdateEpc::class, urlArguments)
        epcExemptionPage.submitExemptionReason(EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT.name)

        val checkAnswersPage = assertPageIs(page, CheckEpcAnswersFormPageUpdateEpc::class, urlArguments)
        checkAnswersPage.form.submit()

        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToComplianceInformation()
        assertThat(propertyDetailsPage.propertyComplianceSummaryList.epcRow.value).containsText("Not required")
    }

    @Test
    fun `The provide this later button is not shown on the has EPC page`(page: Page) {
        whenever(epcRegisterClient.getByUprn(uprn))
            .thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToComplianceInformation()
        propertyDetailsPage.epcCard
            .getAction("Change")
            .link
            .clickAndWait()

        assertPageIs(page, HasEpcFormPageUpdateEpc::class, urlArguments)
        assertThat(page.locator("button:has-text('Provide this later')")).hasCount(0)
    }
}
