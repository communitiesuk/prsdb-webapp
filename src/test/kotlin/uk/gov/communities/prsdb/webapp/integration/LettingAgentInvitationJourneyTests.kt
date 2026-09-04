package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages.EnterPasswordPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages.PasswordCreationConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages.SetPasswordPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages.StoreAccessPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages.ValidateTokenPage
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import java.net.URI

class LettingAgentInvitationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var lettingAgentAccessRepository: LettingAgentAccessRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private val tokenWithoutPassword = "3334abcd-5678-abcd-1234-567abcd1111a"
    private val tokenWithPassword = "3334abcd-5678-abcd-1234-567abcd2222b"
    private val invitationLink = "http://localhost/letting-agent/invitation?token=$tokenWithoutPassword"

    @MockitoBean
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @BeforeEach
    fun setup() {
        whenever(absoluteUrlProvider.buildLettingAgentInvitationUri(any())).thenReturn(URI(invitationLink))
    }

    @Test
    fun `user who does not have a password can walk the set password journey`(page: Page) {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)

        val validateTokenPage = navigator.goToLettingAgentInvitationJourney(tokenWithoutPassword)
        // TODO PDJB-1659: Update when validate token step is replaced by an interceptor
        assertPageIs(page, ValidateTokenPage::class)
        validateTokenPage.form.submit()

        val rawPassword = "password1"
        val setPasswordPage = assertPageIs(page, SetPasswordPage::class)
        setPasswordPage.submitPasswords(rawPassword, rawPassword)

        // TODO PDJB-1659: Remove this step from the journey test once store-access becomes a silent step
        val storeAccessPage = assertPageIs(page, StoreAccessPage::class)
        storeAccessPage.form.submit()

        val confirmationPage = assertPageIs(page, PasswordCreationConfirmationPage::class)
        BaseComponent
            .assertThat(confirmationPage.confirmationBanner)
            .containsText("Property password created")
        assertThat(confirmationPage.backLink.locator).hasCount(0)
        assertThat(confirmationPage.updateLink.locator).hasAttribute("href", invitationLink)
        assertThat(confirmationPage.updateLink.locator).hasText(invitationLink)
        confirmationPage.form.submit()

        // TODO PDJB-1570: Assert redirect to letting agent property record page
    }

    @Test
    fun `user who has a password can walk the enter password journey`(page: Page) {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)

        val validateTokenPage = navigator.goToLettingAgentInvitationJourney(tokenWithPassword)
        // TODO PDJB-1659: Update when validate token step is replaced by an interceptor
        assertPageIs(page, ValidateTokenPage::class)
        validateTokenPage.form.submit()

        // TODO PDJB-1568: Update when enter password page is implemented
        val enterPasswordPage = assertPageIs(page, EnterPasswordPage::class)
        enterPasswordPage.form.submit()

        // TODO PDJB-1659: Remove this step from the journey test
        val storeAccessPage = assertPageIs(page, StoreAccessPage::class)
        storeAccessPage.form.submit()

        // TODO PDJB-1570: Assert redirect to letting agent property record page
    }
}
