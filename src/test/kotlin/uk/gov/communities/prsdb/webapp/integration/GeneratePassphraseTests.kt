package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.PASSPHRASE_SMS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.GeneratePassphrasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertEquals

class GeneratePassphraseTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `system operator can access generate passphrase page`() {
        featureFlagManager.disableFeature(PASSPHRASE_SMS)
        val generatePassphrasePage = navigator.goToGeneratePassphrasePage()

        assertThat(generatePassphrasePage.heading).containsText("New passphrase")
        assertEquals(3, generatePassphrasePage.passphrase.split(" ").size)
        assertThat(generatePassphrasePage.sendSmsButton).isHidden()
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
    fun `generate another is a secondary action immediately after the review guidance`(page: Page) {
        navigator.goToGeneratePassphrasePage()

        assertThat(page.locator("#passphrase-preview + p + a.govuk-button--secondary")).hasText("Generate another passphrase")
    }

    @Test
    fun `system operator can send the displayed passphrase by SMS`(page: Page) {
        featureFlagManager.enableFeature(PASSPHRASE_SMS)
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()
        val passphrase = generatePassphrasePage.passphrase

        generatePassphrasePage.phoneNumber.fill("02079460123")
        generatePassphrasePage.sendSmsButton.clickAndWait()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.smsStatus).containsText("The text message has been submitted to 02079460123.")
        assertThat(generatePassphrasePage.sendSmsButton).isHidden()

        page.reload()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.smsStatus).isHidden()
        assertThat(generatePassphrasePage.sendSmsButton).isVisible()
    }

    @Test
    fun `invalid SMS number keeps the displayed passphrase for correction`(page: Page) {
        featureFlagManager.enableFeature(PASSPHRASE_SMS)
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()
        val passphrase = generatePassphrasePage.passphrase

        generatePassphrasePage.phoneNumber.fill("not a number")
        generatePassphrasePage.sendSmsButton.clickAndWait()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.phoneNumber).hasValue("not a number")

        generatePassphrasePage.phoneNumber.fill("07400123456")
        generatePassphrasePage.sendSmsButton.clickAndWait()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.smsStatus).containsText("The text message has been submitted")
    }
}
