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
        assertThat(generatePassphrasePage.smsForm.submitButton).isHidden()
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
        assertEquals(passphrase, generatePassphrasePage.passphrase)
        assertThat(generatePassphrasePage.successBanner.content).hasText("The text message has been submitted to 07400123456.")
        assertThat(generatePassphrasePage.readBackGuidance).isVisible()
        assertThat(generatePassphrasePage.smsForm.submitButton).isHidden()
        assertThat(generatePassphrasePage.letterGuidance).isHidden()

        page.reload()
        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
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
        assertThat(generatePassphrasePage.successBanner.content).containsText("The text message has been submitted")
    }
}
