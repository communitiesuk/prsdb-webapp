package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.communities.prsdb.webapp.controllers.GeneratePasscodeController.Companion.GENERATE_PASSCODE_URL
import uk.gov.communities.prsdb.webapp.database.repository.PasscodeRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.GeneratePasscodePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PasscodeLimitExceededPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@ActiveProfiles("require-passcode")
class GeneratePasscodeTests : IntegrationTestWithMutableData("data-local.sql") {
    @MockitoSpyBean
    lateinit var passcodeRepository: PasscodeRepository

    @Test
    fun `system operator can access generate passcode page`(page: Page) {
        val generatePasscodePage = navigator.goToGeneratePasscodePage()

        assertThat(generatePasscodePage.banner.title).containsText("New passcode")
        assert(generatePasscodePage.banner.passcode.isNotEmpty()) { "Passcode should be generated and displayed" }
    }

    @Test
    fun `refreshing the page gives the same passcode`(page: Page) {
        // Navigate to generate passcode page
        val generatePasscodePage = navigator.goToGeneratePasscodePage()

        // Get the initial passcode
        val initialPasscode = generatePasscodePage.banner.passcode
        assert(initialPasscode.isNotEmpty()) { "Initial passcode should be generated" }

        // Refresh the page
        page.reload()
        val refreshedPage = assertPageIs(page, GeneratePasscodePage::class)

        // Verify the same passcode is displayed
        val refreshedPasscode = refreshedPage.banner.passcode
        assertEquals(initialPasscode, refreshedPasscode)
    }

    @Test
    fun `clicking generate another passcode creates a new passcode`(page: Page) {
        // Navigate to generate passcode page
        val generatePasscodePage = navigator.goToGeneratePasscodePage()

        // Get the initial passcode
        val initialPasscode = generatePasscodePage.banner.passcode
        assert(initialPasscode.isNotEmpty()) { "Initial passcode should be generated" }

        // Click generate another passcode button
        generatePasscodePage.generateAnotherButton.clickAndWait()

        // Verify we're still on the generate passcode page
        val newGeneratePasscodePage = assertPageIs(page, GeneratePasscodePage::class)

        // Get the new passcode
        val newPasscode = newGeneratePasscodePage.banner.passcode
        assert(newPasscode.isNotEmpty()) { "New passcode should be generated" }
        assertNotEquals(initialPasscode, newPasscode)
    }

    @Test
    fun `exceeding maximum passcode limit redirects to error page`(page: Page) {
        whenever(passcodeRepository.count()).thenReturn(1000L)
        navigator.navigate(GENERATE_PASSCODE_URL)

        val errorPage = assertPageIs(page, PasscodeLimitExceededPage::class)
        assertThat(errorPage.heading).containsText("Maximum number of passcodes reached")
    }

    @Test
    fun `exceeding maximum passcode limit when generating new passcode redirects to error page`(page: Page) {
        val generatePasscodePage = navigator.goToGeneratePasscodePage()
        assert(generatePasscodePage.banner.passcode.isNotEmpty()) { "Initial passcode should be generated" }

        whenever(passcodeRepository.count()).thenReturn(1000L)
        generatePasscodePage.generateAnotherButton.clickAndWait()

        val errorPage = assertPageIs(page, PasscodeLimitExceededPage::class)
        assertThat(errorPage.heading).containsText("Maximum number of passcodes reached")
    }
}
