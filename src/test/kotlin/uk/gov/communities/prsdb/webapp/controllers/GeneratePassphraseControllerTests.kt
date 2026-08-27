package uk.gov.communities.prsdb.webapp.controllers

import org.hamcrest.Matchers.containsString
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.MessageSourceConfig
import uk.gov.communities.prsdb.webapp.controllers.GeneratePassphraseController.Companion.GENERATE_PASSPHRASE_URL
import uk.gov.communities.prsdb.webapp.services.PassphraseService
import kotlin.test.Test

@WebMvcTest(GeneratePassphraseController::class)
@Import(MessageSourceConfig::class)
class GeneratePassphraseControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var passphraseService: PassphraseService

    @Test
    fun `generatePassphrase redirects unauthenticated users`() {
        mvc
            .get(GENERATE_PASSPHRASE_URL)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `generatePassphrase returns 403 for users who are not system operators`() {
        mvc
            .get(GENERATE_PASSPHRASE_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `generatePassphrase returns the generated passphrase for a system operator`() {
        whenever(passphraseService.generatePassphrase()).thenReturn("time anything responsibility costume")

        mvc
            .get(GENERATE_PASSPHRASE_URL)
            .andExpect {
                status { isOk() }
                view { name("generatePassphrase") }
                model {
                    attribute("passphrase", "time anything responsibility costume")
                }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `generatePassphrase renders the generated passphrase`() {
        whenever(passphraseService.generatePassphrase()).thenReturn("time anything responsibility costume")

        mvc
            .get(GENERATE_PASSPHRASE_URL)
            .andExpect {
                status { isOk() }
                content {
                    string(containsString("New passphrase"))
                    string(containsString("The passphrase is"))
                    string(containsString("time anything responsibility costume"))
                }
            }
    }
}
