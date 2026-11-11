package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.GeneratePassphrasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertEquals

class GeneratePassphraseTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `system operator can access generate passphrase page`() {
        val generatePassphrasePage = navigator.goToGeneratePassphrasePage()

        assertThat(generatePassphrasePage.banner.title).containsText("New passphrase")
        assertEquals(3, generatePassphrasePage.passphrase.split(" ").size)
    }

    @Test
    fun `clicking generate another passphrase displays a passphrase`(page: Page) {
        var generatePassphrasePage = navigator.goToGeneratePassphrasePage()

        generatePassphrasePage.generateAnotherButton.clickAndWait()

        generatePassphrasePage = assertPageIs(page, GeneratePassphrasePage::class)
        assertThat(generatePassphrasePage.banner.title).containsText("New passphrase")
        assertEquals(3, generatePassphrasePage.passphrase.split(" ").size)
    }
}
