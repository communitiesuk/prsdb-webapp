package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateEpcController
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLettingAgentView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.CURRENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcSummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.CheckEpcAnswersFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.ConfirmEpcDetailsRetrievedByCertificateNumberPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.ConfirmEpcDetailsRetrievedByUprnFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.FindYourEpcFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.HasEpcFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.StartEpcStep
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat as assertThatComponent

class LettingAgentUpdateEpcJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val token = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222a")
    private val urlArguments = mapOf("token" to token.toString())
    private val updateRoute = LettingAgentUpdateEpcController.getUpdateEpcRoute(token)
    private val validExpiryDate = DateTimeHelper().getCurrentDateInUK().plus(DatePeriod(years = 5))

    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    @Value("\${epc.certificate-base-url}")
    private lateinit var epcCertificateBaseUrl: String

    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)
    }

    @Test
    fun `The EPC update journey shows Continue buttons rather than Save and continue`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        startUpdate()

        val hasEpcPage = assertPageIs(page, HasEpcFormPageLettingAgentUpdateEpc::class, urlArguments)
        assertThatComponent(hasEpcPage.form.submitButton).hasText("Continue")
    }

    @Test
    fun `A letting agent can update a property's EPC found by UPRN lookup`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any()))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse(energyRating = "C", expiryDate = validExpiryDate))

        startUpdate()

        val confirmPage = assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPageLettingAgentUpdateEpc::class, urlArguments)
        assertThatComponent(confirmPage.form.submitButton).hasText("Continue")
        confirmPage.submitYes()

        saveAnswers(page)

        assertCertificateSaved(page)
    }

    @Test
    fun `A letting agent can update a property's EPC by entering a certificate number`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
        whenever(epcRegisterClient.getByRrn(CURRENT_EPC_CERTIFICATE_NUMBER))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse(energyRating = "C", expiryDate = validExpiryDate))

        startUpdate()

        assertPageIs(page, HasEpcFormPageLettingAgentUpdateEpc::class, urlArguments).submitHasEpc()
        assertPageIs(page, FindYourEpcFormPageLettingAgentUpdateEpc::class, urlArguments).submitCurrentEpcNumber()

        val confirmPage = assertPageIs(page, ConfirmEpcDetailsRetrievedByCertificateNumberPageLettingAgentUpdateEpc::class, urlArguments)
        assertThatComponent(confirmPage.form.submitButton).hasText("Continue")
        confirmPage.submitYes()

        saveAnswers(page)

        assertCertificateSaved(page)
    }

    @Test
    fun `a not found page is returned when the delegate to letting agent flag is disabled`(page: Page) {
        featureFlagManager.disable(DELEGATE_TO_LETTING_AGENT)

        navigator.navigate("$updateRoute/${StartEpcStep.ROUTE_SEGMENT}")

        val errorPage = createValidPage(page, ErrorPage::class)
        assertThatComponent(errorPage.heading).containsText("Page not found")
    }

    private fun startUpdate() {
        navigator
            .goToPropertyDetailsLettingAgentView(token)
            .epcCard
            .getAction("Change")
            .link
            .clickAndWait()
    }

    private fun saveAnswers(page: Page) {
        val checkAnswersPage = assertPageIs(page, CheckEpcAnswersFormPageLettingAgentUpdateEpc::class, urlArguments)
        assertThatComponent(checkAnswersPage.form.submitButton).hasText("Continue")
        checkAnswersPage.form.submit()
    }

    private fun assertCertificateSaved(
        page: Page,
        certificateNumber: String = CURRENT_EPC_CERTIFICATE_NUMBER,
        energyRating: String = "C",
        expiryDate: LocalDate = validExpiryDate,
    ): EpcSummaryCard {
        assertPageIs(page, PropertyDetailsPageLettingAgentView::class, urlArguments)
        val epcCard = EpcSummaryCard(page, "Energy performance certificate (EPC)")
        assertThatComponent(epcCard.getAction("Change").link).hasAttribute("href", "$updateRoute/${StartEpcStep.ROUTE_SEGMENT}")
        assertThat(epcCard.summaryList.certificateNumberRow.value).hasText(certificateNumber)
        assertThat(epcCard.summaryList.energyRatingRow.value).hasText(energyRating)
        assertThat(epcCard.summaryList.expiryDateRow.value).hasText(
            expiryDate.toJavaLocalDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)),
        )
        assertThatComponent(epcCard.getAction("View full EPC").link).hasAttribute("href", "$epcCertificateBaseUrl/$certificateNumber")
        return epcCard
    }
}
