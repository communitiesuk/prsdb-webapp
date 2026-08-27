package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import kotlin.test.assertEquals

class GeneratePassphraseTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `system operator can access generate passphrase page`() {
        val generatePassphrasePage = navigator.goToGeneratePassphrasePage()

        assertThat(generatePassphrasePage.banner.title).containsText("New passphrase")
        assertEquals(3, generatePassphrasePage.passphrase.split(" ").size)
    }
}
