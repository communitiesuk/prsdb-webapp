package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.communities.prsdb.webapp.constants.PASSPHRASE_SMS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.GeneratePassphrasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeneratePassphraseSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun enablePassphraseNotifications() {
        featureFlagManager.enableFeature(PASSPHRASE_SMS)
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

        generatePassphrasePage.smsForm.phoneNumber.fill(input)
        generatePassphrasePage.smsForm.submit()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.smsForm.getErrorMessage("phoneNumber")).containsText(message)
        assertThat(generatePassphrasePage.errorSummary.errorLinks(message).locator("a")).hasAttribute("href", "#phoneNumber")
        assertThat(generatePassphrasePage.smsForm.phoneNumber).hasValue(input)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertTrue(page.title().startsWith("Error: "))
        assertThat(generatePassphrasePage.smsForm.submitButton).isVisible()
    }

    @ParameterizedTest
    @CsvSource(
        "+447700900001, Wait a short time",
        "+447700900002, not submitted because of a technical problem",
    )
    fun `SMS delivery failures require a new passphrase`(
        phoneNumber: String,
        message: String,
        page: Page,
    ) {
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()
        val passphrase = generatePassphrasePage.passphrase

        generatePassphrasePage.smsForm.phoneNumber.fill(phoneNumber)
        generatePassphrasePage.smsForm.submit()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.notificationStatus).containsText(message)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.successBanner).isHidden()
        assertThat(generatePassphrasePage.smsForm.submitButton).isHidden()
        assertThat(generatePassphrasePage.letterGuidance).isHidden()

        generatePassphrasePage.generateAnotherButton.clickAndWait()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.smsForm.submitButton).isVisible()
    }

    @Test
    fun `submitting an old passphrase displays an expiry message and generates a new one`(page: Page) {
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()
        val otherPage = page.context().newPage()
        otherPage.navigate(page.url())
        otherPage.close()

        generatePassphrasePage.smsForm.phoneNumber.fill("07400123456")
        generatePassphrasePage.smsForm.submit()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.notificationStatus)
            .hasText("That passphrase is no longer available to send. A new passphrase has been generated.")
        assertThat(generatePassphrasePage.smsForm.submitButton).isVisible()

        generatePassphrasePage.smsForm.phoneNumber.fill("07400123456")
        generatePassphrasePage.smsForm.submit()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.successBanner.content).hasText("The text message has been submitted to 07400123456.")
    }
}
