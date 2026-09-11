package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentPropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateEpcController
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList.SummaryListRow
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLettingAgentView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.CURRENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.NONEXISTENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.SUPERSEDED_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcSummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.CheckEpcAnswersFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.ConfirmEpcDetailsRetrievedByCertificateNumberPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.ConfirmEpcDetailsRetrievedByUprnFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.EpcExemptionFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.EpcExpiredFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.EpcInDateAtStartOfTenancyCheckPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.EpcMissingFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.EpcNotFoundFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.EpcSupersededFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.FindYourEpcFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.HasEpcFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.HasMeesExemptionFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.IsEpcRequiredFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.LowEnergyRatingFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages.MeesExemptionFormPageLettingAgentUpdateEpc
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.StartEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.epc.UpdateCheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.net.URI
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat as assertThatComponent

class LettingAgentUpdateEpcJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val token = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222a")
    private val urlArguments = mapOf("token" to token.toString())
    private val updateRoute = LettingAgentUpdateEpcController.getUpdateEpcRoute(token)
    private val validExpiryDate = DateTimeHelper().getCurrentDateInUK().plus(DatePeriod(years = 5))
    private val expiredExpiryDate = MockEpcData.expiryDateInThePast

    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    @Value("\${epc.certificate-base-url}")
    private lateinit var epcCertificateBaseUrl: String

    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)
    }

    @Test
    fun `accepting a UPRN matched EPC saves its certificate rating and expiry on the agent record`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any()))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse(energyRating = "C", expiryDate = validExpiryDate))

        startUpdate()
        assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPageLettingAgentUpdateEpc::class, urlArguments).submitYes()
        saveAnswers(page)

        assertCertificateSaved(page)
    }

    @Test
    fun `no UPRN match requires an answer and a valid certificate number without a provide later option`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        startUpdate()
        var hasEpcPage = assertPageIs(page, HasEpcFormPageLettingAgentUpdateEpc::class, urlArguments)
        val hasEpcUrl = page.url()
        assertThat(hasEpcPage.provideThisLaterButton).isHidden()
        hasEpcPage.form.submitPrimaryButton()
        hasEpcPage = assertPageIs(page, HasEpcFormPageLettingAgentUpdateEpc::class, urlArguments)
        assertThat(page).hasURL(hasEpcUrl)
        assertThat(hasEpcPage.form.getErrorMessage()).containsText("Select if you have an EPC for this property")
        hasEpcPage.submitHasEpc()

        var lookupPage = assertPageIs(page, FindYourEpcFormPageLettingAgentUpdateEpc::class, urlArguments)
        val lookupUrl = page.url()
        lookupPage.form.submit()
        lookupPage = assertPageIs(page, FindYourEpcFormPageLettingAgentUpdateEpc::class, urlArguments)
        assertThat(page).hasURL(lookupUrl)
        assertThat(lookupPage.form.getErrorMessage("certificateNumber")).containsText("Enter a certificate number")
        lookupPage.submitInvalidEpcNumber()
        lookupPage = assertPageIs(page, FindYourEpcFormPageLettingAgentUpdateEpc::class, urlArguments)
        assertThat(page).hasURL(lookupUrl)
        assertThat(lookupPage.form.getErrorMessage("certificateNumber")).containsText("Enter a 20 digit certificate number")
        verify(epcRegisterClient, never()).getByRrn(any())

        navigator.goToPropertyDetailsLettingAgentView(token)
        assertEpcStillOutstanding(page)
    }

    @Test
    fun `rejecting certificate details returns to search and saves only the subsequently accepted EPC`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
        whenever(epcRegisterClient.getByRrn(CURRENT_EPC_CERTIFICATE_NUMBER))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse(energyRating = "F", expiryDate = validExpiryDate))
        val acceptedCertificateNumber = MockEpcData.SECONDARY_EPC_CERTIFICATE_NUMBER
        whenever(epcRegisterClient.getByRrn(acceptedCertificateNumber))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    certificateNumber = acceptedCertificateNumber,
                    energyRating = "B",
                    expiryDate = validExpiryDate,
                    latestCertificateNumberForThisProperty = acceptedCertificateNumber,
                ),
            )

        startUpdate()
        assertPageIs(page, HasEpcFormPageLettingAgentUpdateEpc::class, urlArguments).submitHasEpc()
        var lookupPage = assertPageIs(page, FindYourEpcFormPageLettingAgentUpdateEpc::class, urlArguments)
        lookupPage.submitCurrentEpcNumber()
        var confirmPage =
            assertPageIs(page, ConfirmEpcDetailsRetrievedByCertificateNumberPageLettingAgentUpdateEpc::class, urlArguments)
        confirmPage.submitNo()
        lookupPage = assertPageIs(page, FindYourEpcFormPageLettingAgentUpdateEpc::class, urlArguments)
        lookupPage.form.epcCertificateNumberInput.fill(acceptedCertificateNumber)
        lookupPage.form.submit()
        confirmPage = assertPageIs(page, ConfirmEpcDetailsRetrievedByCertificateNumberPageLettingAgentUpdateEpc::class, urlArguments)
        confirmPage.submitYes()
        saveAnswers(page)

        val epcCard = assertCertificateSaved(page, acceptedCertificateNumber, "B")
        assertThatComponent(epcCard).not().containsText(CURRENT_EPC_CERTIFICATE_NUMBER)
    }

    @Test
    fun `rejecting a UPRN match opens the has EPC question without saving the rejected certificate`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any()))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse(expiryDate = validExpiryDate))

        startUpdate()
        assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPageLettingAgentUpdateEpc::class, urlArguments).submitNo()
        val hasEpcPage = assertPageIs(page, HasEpcFormPageLettingAgentUpdateEpc::class, urlArguments)
        assertThat(hasEpcPage.provideThisLaterButton).isHidden()
        navigator.goToPropertyDetailsLettingAgentView(token)
        assertEpcStillOutstanding(page)
    }

    @Test
    fun `an expired EPC valid at the start of tenancy is saved as valid with the tenancy answer`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any()))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse(expiryDate = expiredExpiryDate))

        startUpdate()
        assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPageLettingAgentUpdateEpc::class, urlArguments).submitYes()
        assertPageIs(page, EpcInDateAtStartOfTenancyCheckPageLettingAgentUpdateEpc::class, urlArguments).submitEpcInDate()
        saveAnswers(page)

        assertCertificateSaved(page, expiryDate = expiredExpiryDate)
        assertThat(summaryRow(page, TENANCY_START_QUESTION).value).hasText("Yes")
    }

    @Test
    fun `an EPC expired before tenancy follows the expired information page and is saved as expired`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any()))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse(expiryDate = expiredExpiryDate))

        startUpdate()
        assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPageLettingAgentUpdateEpc::class, urlArguments).submitYes()
        assertPageIs(page, EpcInDateAtStartOfTenancyCheckPageLettingAgentUpdateEpc::class, urlArguments).submitEpcExpired()
        assertPageIs(page, EpcExpiredFormPageLettingAgentUpdateEpc::class, urlArguments).form.submit()
        saveAnswers(page)

        assertCertificateSaved(page, expiryDate = expiredExpiryDate, status = "Expired")
        assertThat(summaryRow(page, TENANCY_START_QUESTION).value).hasText("No")
    }

    @Test
    fun `a low rated EPC saves the MEES exemption edited from check your answers`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any()))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse(energyRating = "F", expiryDate = validExpiryDate))

        startUpdate()
        assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPageLettingAgentUpdateEpc::class, urlArguments).submitYes()
        assertPageIs(page, HasMeesExemptionFormPageLettingAgentUpdateEpc::class, urlArguments).submitHasMeesExemption()
        var exemptionPage = assertPageIs(page, MeesExemptionFormPageLettingAgentUpdateEpc::class, urlArguments)
        exemptionPage.submitExemptionReason(MeesExemptionReason.HIGH_COST)
        var checkAnswersPage = assertPageIs(page, CheckEpcAnswersFormPageLettingAgentUpdateEpc::class, urlArguments)
        val checkAnswersUrl = checkAnswersPage.page.url()
        assertThat(summaryRow(page, REGISTERED_EXEMPTION).value).hasText("‘High cost’ exemption")
        summaryRow(page, REGISTERED_EXEMPTION).clickFirstActionLinkAndWait()
        exemptionPage = assertPageIs(page, MeesExemptionFormPageLettingAgentUpdateEpc::class, urlArguments)
        exemptionPage.submitExemptionReason(MeesExemptionReason.ALL_IMPROVEMENTS_MADE)
        checkAnswersPage = assertPageIs(page, CheckEpcAnswersFormPageLettingAgentUpdateEpc::class, urlArguments)
        assertThat(page).hasURL(checkAnswersUrl)
        assertThat(summaryRow(page, REGISTERED_EXEMPTION).value).hasText("‘All relevant improvements made’ exemption")
        checkAnswersPage.form.submit()

        val epcCard = assertCertificateSaved(page, energyRating = "F")
        assertThat(summaryRow(page, MEES_EXEMPTION_QUESTION).value).hasText("Yes")
        assertThat(summaryRow(page, REGISTERED_EXEMPTION).value).hasText("‘All relevant improvements made’ exemption")
        assertThatComponent(epcCard).not().containsText("‘High cost’ exemption")
    }

    @Test
    fun `a low rated EPC without a MEES exemption saves the non compliant outcome`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any()))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse(energyRating = "F", expiryDate = validExpiryDate))

        startUpdate()
        assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPageLettingAgentUpdateEpc::class, urlArguments).submitYes()
        assertPageIs(page, HasMeesExemptionFormPageLettingAgentUpdateEpc::class, urlArguments).submitHasNoMeesExemption()
        assertPageIs(page, LowEnergyRatingFormPageLettingAgentUpdateEpc::class, urlArguments).form.submit()
        saveAnswers(page)

        val epcCard = assertCertificateSaved(page, energyRating = "F", status = null)
        assertThat(summaryRow(page, MEES_EXEMPTION_QUESTION).value).hasText("No")
        assertThatComponent(summaryRow(page, REGISTERED_EXEMPTION)).isHidden()
        assertThatComponent(epcCard).containsText("The council will see that there’s no valid EPC")
    }

    @Test
    fun `an EPC exemption can be edited from check your answers before it is saved`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        startUpdate()
        assertPageIs(page, HasEpcFormPageLettingAgentUpdateEpc::class, urlArguments).submitHasNoEpc()
        assertPageIs(page, IsEpcRequiredFormPageLettingAgentUpdateEpc::class, urlArguments).submitNo()
        var exemptionPage = assertPageIs(page, EpcExemptionFormPageLettingAgentUpdateEpc::class, urlArguments)
        exemptionPage.submitExemptionReason(EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT)
        var checkAnswersPage = assertPageIs(page, CheckEpcAnswersFormPageLettingAgentUpdateEpc::class, urlArguments)
        val checkAnswersUrl = checkAnswersPage.page.url()
        assertThat(summaryRow(page, EPC_EXEMPTION_QUESTION).value).hasText("It has protected architectural or historical merit")
        summaryRow(page, EPC_EXEMPTION_QUESTION).clickFirstActionLinkAndWait()
        exemptionPage = assertPageIs(page, EpcExemptionFormPageLettingAgentUpdateEpc::class, urlArguments)
        exemptionPage.submitExemptionReason(EpcExemptionReason.TEMPORARY_BUILDING)
        checkAnswersPage = assertPageIs(page, CheckEpcAnswersFormPageLettingAgentUpdateEpc::class, urlArguments)
        assertThat(page).hasURL(checkAnswersUrl)
        assertThat(summaryRow(page, EPC_EXEMPTION_QUESTION).value).hasText("It’s a temporary building")
        checkAnswersPage.form.submit()

        val epcCard = assertNoCertificateSaved(page, isRequired = false)
        assertThat(epcCard.summaryList.epcExemptionRow.value).hasText("It’s a temporary building")
        assertThatComponent(epcCard).not().containsText("protected architectural or historical merit")
    }

    @Test
    fun `a required missing EPC is saved after the missing certificate information page`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        startUpdate()
        assertPageIs(page, HasEpcFormPageLettingAgentUpdateEpc::class, urlArguments).submitHasNoEpc()
        assertPageIs(page, IsEpcRequiredFormPageLettingAgentUpdateEpc::class, urlArguments).submitYes()
        assertPageIs(page, EpcMissingFormPageLettingAgentUpdateEpc::class, urlArguments).form.submit()
        saveAnswers(page)

        val epcCard = assertNoCertificateSaved(page, isRequired = true)
        assertThatComponent(epcCard.summaryList.epcExemptionRow).isHidden()
        assertThatComponent(epcCard).containsText("The council will see that there’s no valid EPC")
    }

    @Test
    fun `an unfound certificate leads to EPC required and exemption questions and saves the exemption`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
        whenever(epcRegisterClient.getByRrn(NONEXISTENT_EPC_CERTIFICATE_NUMBER))
            .thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        startUpdate()
        assertPageIs(page, HasEpcFormPageLettingAgentUpdateEpc::class, urlArguments).submitHasEpc()
        assertPageIs(page, FindYourEpcFormPageLettingAgentUpdateEpc::class, urlArguments).submitNonexistentEpcNumber()
        assertPageIs(page, EpcNotFoundFormPageLettingAgentUpdateEpc::class, urlArguments).form.submit()
        assertPageIs(page, IsEpcRequiredFormPageLettingAgentUpdateEpc::class, urlArguments).submitNo()
        assertPageIs(page, EpcExemptionFormPageLettingAgentUpdateEpc::class, urlArguments)
            .submitExemptionReason(EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT)
        saveAnswers(page)

        val epcCard = assertNoCertificateSaved(page, isRequired = false)
        assertThat(epcCard.summaryList.epcExemptionRow.value).hasText("It has protected architectural or historical merit")
        assertThatComponent(epcCard).not().containsText(NONEXISTENT_EPC_CERTIFICATE_NUMBER)
    }

    @Test
    fun `accepting the newer EPC saves its details instead of the superseded lookup result`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
        whenever(epcRegisterClient.getByRrn(SUPERSEDED_EPC_CERTIFICATE_NUMBER))
            .thenReturn(
                MockEpcData.createEpcRegisterClientEpcFoundResponse(
                    certificateNumber = SUPERSEDED_EPC_CERTIFICATE_NUMBER,
                    energyRating = "F",
                    expiryDate = expiredExpiryDate,
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                ),
            )
        whenever(epcRegisterClient.getByRrn(CURRENT_EPC_CERTIFICATE_NUMBER))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse(energyRating = "B", expiryDate = validExpiryDate))

        startUpdate()
        assertPageIs(page, HasEpcFormPageLettingAgentUpdateEpc::class, urlArguments).submitHasEpc()
        assertPageIs(page, FindYourEpcFormPageLettingAgentUpdateEpc::class, urlArguments).submitSupersededEpcNumber()
        assertPageIs(page, EpcSupersededFormPageLettingAgentUpdateEpc::class, urlArguments).form.submit()
        saveAnswers(page)

        val epcCard = assertCertificateSaved(page, energyRating = "B")
        assertThatComponent(epcCard).not().containsText(SUPERSEDED_EPC_CERTIFICATE_NUMBER)
    }

    @Test
    fun `Back from the first UPRN confirmation returns to the agent record without saving`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any()))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse(expiryDate = validExpiryDate))

        startUpdate()
        assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPageLettingAgentUpdateEpc::class, urlArguments)
        val backLink = BackLink.default(page)
        assertThatComponent(backLink).hasAttribute("href", LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token))
        backLink.clickAndWait()

        assertEpcStillOutstanding(page)
    }

    @Test
    fun `Back from the first has EPC page returns to the agent record without saving`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        startUpdate()
        assertPageIs(page, HasEpcFormPageLettingAgentUpdateEpc::class, urlArguments)
        BackLink.default(page).clickAndWait()

        assertEpcStillOutstanding(page)
    }

    @Test
    fun `omitting confirmation of a UPRN match stays on the agent step without saving`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any()))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse(expiryDate = validExpiryDate))

        startUpdate()
        var confirmPage = assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPageLettingAgentUpdateEpc::class, urlArguments)
        val confirmationUrl = page.url()
        confirmPage.form.submit()
        confirmPage = assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPageLettingAgentUpdateEpc::class, urlArguments)
        assertThat(page).hasURL(confirmationUrl)
        assertThat(confirmPage.form.getErrorMessage()).isVisible()

        navigator.goToPropertyDetailsLettingAgentView(token)
        assertEpcStillOutstanding(page)
    }

    @Test
    fun `the EPC update route is not found when delegation is disabled`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
        featureFlagManager.disable(DELEGATE_TO_LETTING_AGENT)

        val response = navigator.navigate("$updateRoute/${StartEpcStep.ROUTE_SEGMENT}")

        assertEquals(404, response?.status())
        assertThatComponent(createValidPage(page, ErrorPage::class).heading).containsText("Page not found")
        verify(epcRegisterClient, never()).getByUprn(any())
    }

    @Test
    fun `an unknown delegation token cannot enter the EPC update journey`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
        val unknownToken = UUID.fromString("00000000-0000-0000-0000-000000000000")

        val response =
            navigator.navigate(
                LettingAgentUpdateEpcController.getUpdateEpcRoute(unknownToken) + "/${StartEpcStep.ROUTE_SEGMENT}",
            )

        assertEquals(404, response?.status())
        assertThatComponent(createValidPage(page, ErrorPage::class).heading).containsText("Page not found")
        verify(epcRegisterClient, never()).getByUprn(any())
    }

    @Test
    fun `an unreachable check answers step returns to the agent record without changing its EPC`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

        startUpdate()
        assertPageIs(page, HasEpcFormPageLettingAgentUpdateEpc::class, urlArguments)
        val journeyQuery = URI(page.url()).rawQuery
        navigator.navigate("$updateRoute/${UpdateCheckEpcAnswersStep.ROUTE_SEGMENT}?$journeyQuery")

        assertEpcStillOutstanding(page)
    }

    @Test
    fun `repeated completed updates start fresh and remove stale certificate and exemption answers`(page: Page) {
        whenever(epcRegisterClient.getByUprn(any()))
            .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse(energyRating = "F", expiryDate = validExpiryDate))

        startUpdate()
        assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPageLettingAgentUpdateEpc::class, urlArguments).submitYes()
        assertPageIs(page, HasMeesExemptionFormPageLettingAgentUpdateEpc::class, urlArguments).submitHasMeesExemption()
        assertPageIs(page, MeesExemptionFormPageLettingAgentUpdateEpc::class, urlArguments)
            .submitExemptionReason(MeesExemptionReason.HIGH_COST)
        saveAnswers(page)
        assertCertificateSaved(page, energyRating = "F")
        assertThat(summaryRow(page, REGISTERED_EXEMPTION).value).hasText("‘High cost’ exemption")

        whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
        startUpdate()
        assertPageIs(page, HasEpcFormPageLettingAgentUpdateEpc::class, urlArguments).submitHasNoEpc()
        assertPageIs(page, IsEpcRequiredFormPageLettingAgentUpdateEpc::class, urlArguments).submitYes()
        assertPageIs(page, EpcMissingFormPageLettingAgentUpdateEpc::class, urlArguments).form.submit()
        saveAnswers(page)

        val epcCard = assertNoCertificateSaved(page, isRequired = true)
        assertThatComponent(epcCard).not().containsText(CURRENT_EPC_CERTIFICATE_NUMBER)
        assertThatComponent(summaryRow(page, REGISTERED_EXEMPTION)).isHidden()
        assertThatComponent(summaryRow(page, MEES_EXEMPTION_QUESTION)).isHidden()
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
        assertPageIs(page, CheckEpcAnswersFormPageLettingAgentUpdateEpc::class, urlArguments).form.submit()
    }

    private fun assertCertificateSaved(
        page: Page,
        certificateNumber: String = CURRENT_EPC_CERTIFICATE_NUMBER,
        energyRating: String = "C",
        expiryDate: LocalDate = validExpiryDate,
        status: String? = "Valid",
    ): EpcSummaryCard {
        val epcCard = assertAgentRecord(page)
        assertThat(epcCard.summaryList.certificateNumberRow.value).hasText(certificateNumber)
        assertThat(epcCard.summaryList.energyRatingRow.value).hasText(energyRating)
        assertThat(epcCard.summaryList.expiryDateRow.value).hasText(
            expiryDate.toJavaLocalDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)),
        )
        assertThatComponent(epcCard.getAction("View full EPC").link).hasAttribute("href", "$epcCertificateBaseUrl/$certificateNumber")
        if (status == null) {
            assertThatComponent(epcCard.summaryList.certificateStatusRow).isHidden()
        } else {
            assertThat(epcCard.summaryList.certificateStatusRow.value).hasText(status)
        }
        assertThatComponent(epcCard).not().containsText("Provide this later")
        assertThatComponent(epcCard.summaryList.epcExemptionRow).isHidden()
        return epcCard
    }

    private fun assertNoCertificateSaved(
        page: Page,
        isRequired: Boolean,
    ): EpcSummaryCard {
        val epcCard = assertAgentRecord(page)
        assertThat(epcCard.summaryList.hasEpcRow.value).hasText("No")
        assertThat(epcCard.summaryList.isEpcRequiredRow.value).hasText(if (isRequired) "Yes" else "No")
        assertThatComponent(epcCard.summaryList.certificateNumberRow).isHidden()
        assertThatComponent(epcCard.summaryList.energyRatingRow).isHidden()
        assertThatComponent(epcCard.summaryList.expiryDateRow).isHidden()
        assertThatComponent(epcCard.summaryList.certificateStatusRow).isHidden()
        assertThatComponent(epcCard.getAction("View full EPC").link).isHidden()
        assertThatComponent(epcCard).not().containsText("Provide this later")
        return epcCard
    }

    private fun assertEpcStillOutstanding(page: Page) {
        val epcCard = assertAgentRecord(page)
        assertThat(epcCard.summaryList.hasEpcRow.value).containsText("Provide this later")
        assertThatComponent(epcCard.summaryList.certificateNumberRow).isHidden()
        assertThatComponent(epcCard.summaryList.epcExemptionRow).isHidden()
        assertThatComponent(epcCard.getAction("View full EPC").link).isHidden()
    }

    private fun assertAgentRecord(page: Page): EpcSummaryCard {
        assertPageIs(page, PropertyDetailsPageLettingAgentView::class, urlArguments)
        val epcCard = EpcSummaryCard(page, "Energy performance certificate (EPC)")
        assertThatComponent(epcCard.getAction("Change").link).hasAttribute("href", "$updateRoute/${StartEpcStep.ROUTE_SEGMENT}")
        return epcCard
    }

    private fun summaryRow(
        page: Page,
        key: String,
    ) = SummaryListRow.byKey(page.getByRole(AriaRole.MAIN), key)

    companion object {
        private const val TENANCY_START_QUESTION = "Was the EPC still in date when the current tenancy began?"
        private const val MEES_EXEMPTION_QUESTION = "Do you have a registered energy efficiency exemption for this property?"
        private const val REGISTERED_EXEMPTION = "Registered exemption"
        private const val EPC_EXEMPTION_QUESTION = "Why does this property not need an EPC?"
    }
}
