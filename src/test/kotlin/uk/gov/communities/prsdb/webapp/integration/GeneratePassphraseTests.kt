package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.communities.prsdb.webapp.constants.ASYNCHRONOUS_EMAIL_SENDING
import uk.gov.communities.prsdb.webapp.constants.PASSPHRASE_EMAIL
import uk.gov.communities.prsdb.webapp.constants.PASSPHRASE_SMS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.GeneratePassphrasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertEquals

class GeneratePassphraseTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `system operator can access generate passphrase page`() {
        featureFlagManager.disableFeature(PASSPHRASE_SMS)
        featureFlagManager.disableFeature(PASSPHRASE_EMAIL)
        val generatePassphrasePage = navigator.goToGeneratePassphrasePage()

        assertThat(generatePassphrasePage.heading).containsText("New passphrase")
        assertEquals(3, generatePassphrasePage.passphrase.split(" ").size)
        assertThat(generatePassphrasePage.smsForm.submitButton).isHidden()
        assertThat(generatePassphrasePage.emailForm.submitButton).isHidden()
        assertThat(generatePassphrasePage.letterGuidance).isVisible()
    }

    @Test
    fun `clicking generate another passphrase displays a passphrase`(page: Page) {
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()

        generatePassphrasePage.generateAnotherButton.clickAndWait()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.heading).containsText("New passphrase")
        assertEquals(3, generatePassphrasePage.passphrase.split(" ").size)
    }

    @Test
    fun `system operator can send the displayed passphrase by SMS`(page: Page) {
        featureFlagManager.enableFeature(PASSPHRASE_SMS)
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()
        val passphrase = generatePassphrasePage.passphrase
        assertThat(generatePassphrasePage.letterGuidance).isVisible()

        generatePassphrasePage.smsForm.phoneNumber.fill("07400123456")
        generatePassphrasePage.smsForm.submit()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.heading).containsText("Passphrase sent")
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.successBanner.title).hasText("Success")
        assertThat(generatePassphrasePage.successBanner.content).hasText("The text message has been submitted to 07400123456.")
        assertThat(page.locator(".govuk-inset-text")).hasCount(1)
        assertThat(generatePassphrasePage.readBackGuidance).hasText(
            "Ask the landlord to read the passphrase back when it arrives.",
        )
        assertThat(generatePassphrasePage.notificationStatus).isHidden()
        assertThat(generatePassphrasePage.smsForm.submitButton).isHidden()
        assertThat(generatePassphrasePage.letterGuidance).isHidden()

        page.reload()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.notificationStatus).isHidden()
        assertThat(generatePassphrasePage.successBanner).isHidden()
        assertThat(generatePassphrasePage.smsForm.submitButton).isVisible()
        assertThat(generatePassphrasePage.letterGuidance).isVisible()
    }

    @Test
    fun `invalid SMS number keeps the displayed passphrase for correction`(page: Page) {
        featureFlagManager.enableFeature(PASSPHRASE_SMS)
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()
        val passphrase = generatePassphrasePage.passphrase

        generatePassphrasePage.smsForm.phoneNumber.fill("02079460123")
        generatePassphrasePage.smsForm.submit()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.smsForm.phoneNumber).hasValue("02079460123")

        generatePassphrasePage.smsForm.phoneNumber.fill("07400123456")
        generatePassphrasePage.smsForm.submit()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.successBanner.content).hasText("The text message has been submitted to 07400123456.")
    }

    @Test
    fun `email-only sending works when ordinary emails are queued`(page: Page) {
        featureFlagManager.enableFeature(PASSPHRASE_EMAIL)
        featureFlagManager.disableFeature(PASSPHRASE_SMS)
        featureFlagManager.enableFeature(ASYNCHRONOUS_EMAIL_SENDING)
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()
        val passphrase = generatePassphrasePage.passphrase
        assertThat(generatePassphrasePage.smsForm.submitButton).isHidden()
        assertThat(generatePassphrasePage.letterGuidance).isVisible()

        generatePassphrasePage.emailForm.emailAddress.fill("recipient@example.com")
        generatePassphrasePage.emailForm.submit()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.successBanner.content).hasText("The email has been submitted to recipient@example.com.")
        assertThat(generatePassphrasePage.emailForm.submitButton).isHidden()
        assertThat(generatePassphrasePage.letterGuidance).isHidden()

        generatePassphrasePage.generateAnotherButton.clickAndWait()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.successBanner).isHidden()
        assertThat(generatePassphrasePage.emailForm.submitButton).isVisible()
        assertThat(generatePassphrasePage.letterGuidance).isVisible()
    }

    @Test
    fun `invalid submissions keep both channels available until an email attempt`(page: Page) {
        featureFlagManager.enableFeature(PASSPHRASE_EMAIL)
        featureFlagManager.enableFeature(PASSPHRASE_SMS)
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()
        val passphrase = generatePassphrasePage.passphrase

        val smsToken = generatePassphrasePage.smsForm.passphraseToken.locator
        val emailToken = generatePassphrasePage.emailForm.passphraseToken.locator
        assertThat(smsToken).hasAttribute("name", "passphraseToken")
        assertThat(emailToken).hasAttribute("name", "passphraseToken")
        assertThat(emailToken).hasValue(smsToken.inputValue())
        assertEquals(generatePassphrasePage.smsForm.csrfToken, generatePassphrasePage.emailForm.csrfToken)

        generatePassphrasePage.smsForm.phoneNumber.fill("invalid")
        generatePassphrasePage.smsForm.submit()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.emailForm.submitButton).isVisible()
        assertThat(generatePassphrasePage.letterGuidance).isVisible()

        generatePassphrasePage.emailForm.emailAddress.fill("invalid")
        generatePassphrasePage.emailForm.submit()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.emailForm.emailAddress).hasValue("invalid")
        assertThat(generatePassphrasePage.smsForm.submitButton).isVisible()
        assertThat(generatePassphrasePage.letterGuidance).isVisible()

        generatePassphrasePage.emailForm.emailAddress.fill("recipient@example.com")
        generatePassphrasePage.emailForm.submit()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.successBanner.content).hasText("The email has been submitted to recipient@example.com.")
        assertThat(generatePassphrasePage.emailForm.submitButton).isHidden()
        assertThat(generatePassphrasePage.smsForm.submitButton).isHidden()
        assertThat(generatePassphrasePage.letterGuidance).isHidden()

        page.reload()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.notificationStatus).isHidden()
        assertThat(generatePassphrasePage.successBanner).isHidden()
        assertThat(generatePassphrasePage.emailForm.submitButton).isVisible()
        assertThat(generatePassphrasePage.smsForm.submitButton).isVisible()
        assertThat(generatePassphrasePage.letterGuidance).isVisible()
    }

    @ParameterizedTest
    @ValueSource(strings = ["transient@example.com", "persistent@example.com"])
    fun `email failure requires generating a new passphrase for either channel`(
        recipient: String,
        page: Page,
    ) {
        featureFlagManager.enableFeature(PASSPHRASE_EMAIL)
        featureFlagManager.enableFeature(PASSPHRASE_SMS)
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()
        val passphrase = generatePassphrasePage.passphrase

        generatePassphrasePage.emailForm.emailAddress.fill(recipient)
        generatePassphrasePage.emailForm.submit()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.notificationStatus).containsText(
            if (recipient.startsWith("transient")) "Wait a short time" else "Contact support",
        )
        assertThat(generatePassphrasePage.successBanner).isHidden()
        assertThat(generatePassphrasePage.readBackGuidance).isHidden()
        assertThat(generatePassphrasePage.emailForm.submitButton).isHidden()
        assertThat(generatePassphrasePage.smsForm.submitButton).isHidden()
        assertThat(generatePassphrasePage.letterGuidance).isHidden()

        generatePassphrasePage.generateAnotherButton.clickAndWait()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.emailForm.submitButton).isVisible()
        assertThat(generatePassphrasePage.smsForm.submitButton).isVisible()
        assertThat(generatePassphrasePage.letterGuidance).isVisible()
    }

    @ParameterizedTest
    @CsvSource(
        "+447700900001, Wait a short time",
        "+447700900002, Contact support",
    )
    fun `failed SMS submission shows guidance and requires a new passphrase`(
        phoneNumber: String,
        guidance: String,
        page: Page,
    ) {
        featureFlagManager.enableFeature(PASSPHRASE_SMS)
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()
        val passphrase = generatePassphrasePage.passphrase

        generatePassphrasePage.smsForm.phoneNumber.fill(phoneNumber)
        generatePassphrasePage.smsForm.submit()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.notificationStatus).containsText(guidance)
        assertThat(generatePassphrasePage.smsForm.submitButton).isHidden()
        assertThat(generatePassphrasePage.letterGuidance).isHidden()

        generatePassphrasePage.generateAnotherButton.clickAndWait()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.smsForm.submitButton).isVisible()
    }
}
