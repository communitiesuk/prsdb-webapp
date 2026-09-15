package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLettingAgentView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages.EnterPasswordPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages.PasswordCreationConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages.SetPasswordPage
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import java.net.URI

class LettingAgentInvitationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val tokenWithoutPassword = "3334abcd-5678-abcd-1234-567abcd1111a"

    private val tokenWithPassword = "3334abcd-5678-abcd-1234-567abcd2222b"
    private val invitationLink = "http://localhost/letting-agent/invitation?token=$tokenWithoutPassword"

    @MockitoBean
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @BeforeEach
    fun setup() {
        whenever(absoluteUrlProvider.buildLettingAgentInvitationUri(any())).thenReturn(URI(invitationLink))
    }

    private val seededPassword = "Password123!" // pragma: allowlist secret

    @Test
    fun `user who does not have a password can walk the set password journey`(page: Page) {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)

        val setPasswordPage = navigator.goToLettingAgentInvitationSetPasswordJourney(tokenWithoutPassword)

        val rawPassword = "password1" // pragma: allowlist secret
        assertPageIs(page, SetPasswordPage::class)
        setPasswordPage.submitPasswords(rawPassword, rawPassword)

        val confirmationPage = assertPageIs(page, PasswordCreationConfirmationPage::class)
        BaseComponent
            .assertThat(confirmationPage.confirmationBanner)
            .containsText("Property password created")
        assertThat(confirmationPage.backLink.locator).hasCount(0)
        assertThat(confirmationPage.updateLink.locator).hasAttribute("href", invitationLink)
        assertThat(confirmationPage.updateLink.locator).hasText(invitationLink)
        confirmationPage.form.submit()

        assertPageIs(page, PropertyDetailsPageLettingAgentView::class, mapOf("token" to tokenWithoutPassword))
    }

    @Test
    fun `user who has a password can walk the enter password journey`(page: Page) {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)

        val enterPasswordPage = navigator.goToLettingAgentInvitationEnterPasswordJourney(tokenWithPassword)
        assertPageIs(page, EnterPasswordPage::class)
        enterPasswordPage.submitPassword(seededPassword)

        assertPageIs(page, PropertyDetailsPageLettingAgentView::class, mapOf("token" to tokenWithPassword))
    }
}
