package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.communities.prsdb.webapp.constants.PASSPHRASE_EMAIL
import uk.gov.communities.prsdb.webapp.constants.PASSPHRASE_SMS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.GeneratePassphrasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GeneratePassphraseSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun enablePassphraseNotifications() {
        featureFlagManager.enableFeature(PASSPHRASE_EMAIL)
        featureFlagManager.enableFeature(PASSPHRASE_SMS)
    }

    @ParameterizedTest
    @CsvSource(
        "'', Enter an email address",
        "invalid, Enter a valid email address",
    )
    fun `email validation displays the correct error`(
        input: String,
        message: String,
        page: Page,
    ) {
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()
        val passphrase = generatePassphrasePage.passphrase

        generatePassphrasePage.emailAddress.fill(input)
        generatePassphrasePage.sendEmailButton.clickAndWait()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.emailAddressError).containsText(message)
        assertThat(generatePassphrasePage.errorSummary.errorLinks(message).locator("a")).hasAttribute("href", "#emailAddress")
        assertThat(generatePassphrasePage.phoneNumberError).hasCount(0)
        assertThat(generatePassphrasePage.emailAddress).hasValue(input)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertTrue(page.title().startsWith("Error: "))
        assertThat(generatePassphrasePage.sendEmailButton).isVisible()
        assertThat(generatePassphrasePage.sendSmsButton).isVisible()
    }

    @ParameterizedTest
    @CsvSource(
        "'', Enter a telephone number",
        "invalid, Enter a valid telephone number",
    )
    fun `telephone validation displays the correct error`(
        input: String,
        message: String,
        page: Page,
    ) {
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()
        val passphrase = generatePassphrasePage.passphrase

        generatePassphrasePage.phoneNumber.fill(input)
        generatePassphrasePage.sendSmsButton.clickAndWait()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.phoneNumberError).containsText(message)
        assertThat(generatePassphrasePage.errorSummary.errorLinks(message).locator("a")).hasAttribute("href", "#phoneNumber")
        assertThat(generatePassphrasePage.emailAddressError).hasCount(0)
        assertThat(generatePassphrasePage.phoneNumber).hasValue(input)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertTrue(page.title().startsWith("Error: "))
        assertThat(generatePassphrasePage.sendEmailButton).isVisible()
        assertThat(generatePassphrasePage.sendSmsButton).isVisible()
    }

    @ParameterizedTest
    @CsvSource(
        "+447700900001, The text message could not be sent",
        "+447700900002, We could not confirm whether the text message was sent",
    )
    fun `SMS delivery failures require a new passphrase`(
        phoneNumber: String,
        message: String,
        page: Page,
    ) {
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()
        val passphrase = generatePassphrasePage.passphrase

        generatePassphrasePage.phoneNumber.fill(phoneNumber)
        generatePassphrasePage.sendSmsButton.clickAndWait()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.notificationStatus).containsText(message)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.successBanner).isHidden()
        assertThat(generatePassphrasePage.sendEmailButton).isHidden()
        assertThat(generatePassphrasePage.sendSmsButton).isHidden()

        generatePassphrasePage.generateAnotherButton.clickAndWait()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.sendEmailButton).isVisible()
        assertThat(generatePassphrasePage.sendSmsButton).isVisible()
    }

    @Test
    fun `submitting an old passphrase displays an expiry message and generates a new one`(page: Page) {
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()
        val oldToken = page.locator("#smsPassphraseToken").inputValue()
        val otherPage = page.context().newPage()
        otherPage.navigate(page.url())
        otherPage.close()

        generatePassphrasePage.phoneNumber.fill("07400123456")
        generatePassphrasePage.sendSmsButton.clickAndWait()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.notificationStatus)
            .hasText("That passphrase is no longer available to send. A new passphrase has been generated.")
        assertNotEquals(oldToken, page.locator("#smsPassphraseToken").inputValue())
        assertThat(generatePassphrasePage.sendEmailButton).isVisible()
        assertThat(generatePassphrasePage.sendSmsButton).isVisible()

        generatePassphrasePage.phoneNumber.fill("07400123456")
        generatePassphrasePage.sendSmsButton.clickAndWait()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.successBanner.content).hasText("The text message has been submitted to 07400123456.")
    }
}
