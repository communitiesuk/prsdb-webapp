package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.GeneratePasscodeController.Companion.GENERATE_PASSCODE_URL
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController.Companion.LOCAL_COUNCIL_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.exceptions.PasscodeLimitExceededException
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.PasscodeService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.createLocalCouncilUser
import kotlin.test.Test

@WebMvcTest(GeneratePasscodeController::class)
@ActiveProfiles("require-passcode")
class GeneratePasscodeControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var passcodeService: PasscodeService

    @MockitoBean
    private lateinit var localCouncilDataService: LocalCouncilDataService

    @Test
    fun `generatePasscodeGet returns a redirect for unauthenticated user`() {
        mvc
            .get(GENERATE_PASSCODE_URL)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `generatePasscodeGet returns 403 for unauthorized user`() {
        mvc
            .get(GENERATE_PASSCODE_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
    fun `generatePasscodeGet returns 200 and generates passcode for authorized LC admin`() {
        val localCouncilUser = createLocalCouncilUser()
        val testPasscode = "ABC123"

        whenever(localCouncilDataService.getLocalCouncilUser("user")).thenReturn(localCouncilUser)
        whenever(passcodeService.getOrGeneratePasscode(localCouncilUser.localCouncil.id.toLong()))
            .thenReturn(testPasscode)

        mvc
            .get(GENERATE_PASSCODE_URL)
            .andExpect {
                status { isOk() }
                view { name("generatePasscode") }
                model {
                    attribute("passcode", testPasscode)
                    attribute("dashboardUrl", LOCAL_COUNCIL_DASHBOARD_URL)
                }
            }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
    fun `generatePasscodeGet returns passcode limit error when limit exceeded`() {
        val localCouncilUser = createLocalCouncilUser()

        whenever(localCouncilDataService.getLocalCouncilUser("user")).thenReturn(localCouncilUser)
        whenever(passcodeService.getOrGeneratePasscode(localCouncilUser.localCouncil.id.toLong()))
            .thenThrow(PasscodeLimitExceededException("Passcode limit exceeded"))

        mvc
            .get(GENERATE_PASSCODE_URL)
            .andExpect {
                status { isOk() }
                view { name("error/passcodeLimit") }
            }
    }

    @Test
    fun `generatePasscodePost returns a redirect for unauthenticated user`() {
        mvc
            .post(GENERATE_PASSCODE_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `generatePasscodePost returns 403 for unauthorized user`() {
        mvc
            .post(GENERATE_PASSCODE_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
    fun `generatePasscodePost returns 200 and generates new passcode for authorized Local Council admin`() {
        val localCouncilUser = createLocalCouncilUser()
        val testPasscode = "DEF456"

        whenever(localCouncilDataService.getLocalCouncilUser("user")).thenReturn(localCouncilUser)
        whenever(passcodeService.generateAndStorePasscode(localCouncilUser.localCouncil.id.toLong()))
            .thenReturn(testPasscode)

        mvc
            .post(GENERATE_PASSCODE_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }.andExpect {
                status { isOk() }
                view { name("generatePasscode") }
                model {
                    attribute("passcode", testPasscode)
                    attribute("dashboardUrl", LOCAL_COUNCIL_DASHBOARD_URL)
                    attribute("backUrl", LOCAL_COUNCIL_DASHBOARD_URL)
                }
            }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
    fun `generatePasscodePost returns passcode limit error when limit exceeded`() {
        val localCouncilUser = createLocalCouncilUser()

        whenever(localCouncilDataService.getLocalCouncilUser("user")).thenReturn(localCouncilUser)
        whenever(passcodeService.generateAndStorePasscode(localCouncilUser.localCouncil.id.toLong()))
            .thenThrow(PasscodeLimitExceededException("Passcode limit exceeded"))

        mvc
            .post(GENERATE_PASSCODE_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }.andExpect {
                status { isOk() }
                view { name("error/passcodeLimit") }
            }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
    fun `generatePasscodeGet returns 403 for LOCAL_COUNCIL_USER role (should only allow LOCAL_COUNCIL_ADMIN)`() {
        mvc
            .get(GENERATE_PASSCODE_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
    fun `generatePasscodePost returns 403 for LOCAL_COUNCIL_USER role (should only allow LOCAL_COUNCIL_ADMIN)`() {
        mvc
            .post(GENERATE_PASSCODE_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }
}
