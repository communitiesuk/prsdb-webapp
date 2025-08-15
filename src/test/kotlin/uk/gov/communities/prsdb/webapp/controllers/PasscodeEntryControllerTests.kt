package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.PASSCODE_REDIRECT_URL
import uk.gov.communities.prsdb.webapp.constants.SUBMITTED_PASSCODE
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.PASSCODE_ALREADY_USED_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.PasscodeEntryController.Companion.PASSCODE_ENTRY_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.services.PasscodeService
import kotlin.test.Test

@WebMvcTest(PasscodeEntryController::class)
@ActiveProfiles("require-passcode")
class PasscodeEntryControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var passcodeService: PasscodeService

    @Test
    fun `passcodeEntry GET returns 200 and displays passcode entry form`() {
        mvc
            .get(PASSCODE_ENTRY_ROUTE)
            .andExpect {
                status { isOk() }
                view { name("passcodeEntry") }
                model {
                    attributeExists("passcodeRequestModel")
                }
            }
    }

    @Test
    fun `submitPasscode POST with valid passcode redirects to landlord registration`() {
        val validPasscode = "ABC123"
        whenever(passcodeService.isValidPasscode(validPasscode)).thenReturn(true)

        mvc
            .post(PASSCODE_ENTRY_ROUTE) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("passcode", validPasscode)
                with(csrf())
            }
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(LANDLORD_REGISTRATION_ROUTE)
            }
            .andExpect {
                request {
                    sessionAttribute(SUBMITTED_PASSCODE, validPasscode)
                }
            }
    }

    @Test
    fun `submitPasscode POST with valid passcode and redirect URL redirects to stored URL`() {
        val validPasscode = "ABC123"
        val redirectUrl = "/a-redirect-url"
        whenever(passcodeService.isValidPasscode(validPasscode)).thenReturn(true)

        mvc
            .post(PASSCODE_ENTRY_ROUTE) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("passcode", validPasscode)
                sessionAttr(PASSCODE_REDIRECT_URL, redirectUrl)
                with(csrf())
            }
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(redirectUrl)
            }
            .andExpect {
                request {
                    sessionAttribute(SUBMITTED_PASSCODE, validPasscode)
                    sessionAttributeDoesNotExist(PASSCODE_REDIRECT_URL)
                }
            }
    }

    @Test
    fun `submitPasscode POST with invalid passcode returns form with error`() {
        val invalidPasscode = "INVALID"
        whenever(passcodeService.isValidPasscode(invalidPasscode)).thenReturn(false)

        mvc
            .post(PASSCODE_ENTRY_ROUTE) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("passcode", invalidPasscode)
                with(csrf())
            }
            .andExpect {
                status { isOk() }
                view { name("passcodeEntry") }
                model {
                    attributeHasFieldErrors("passcodeRequestModel", "passcode")
                }
            }
    }

    @Test
    fun `submitPasscode POST with blank passcode returns form with validation error`() {
        mvc
            .post(PASSCODE_ENTRY_ROUTE) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("passcode", "")
                with(csrf())
            }
            .andExpect {
                status { isOk() }
                view { name("passcodeEntry") }
                model {
                    attributeHasFieldErrors("passcodeRequestModel", "passcode")
                }
            }
    }

    @Test
    fun `submitPasscode POST with missing passcode parameter returns form with validation error`() {
        mvc
            .post(PASSCODE_ENTRY_ROUTE) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }
            .andExpect {
                status { isOk() }
                view { name("passcodeEntry") }
                model {
                    attributeHasFieldErrors("passcodeRequestModel", "passcode")
                }
            }
    }

    @Test
    fun `submitPasscode POST without CSRF token returns 403`() {
        mvc
            .post(PASSCODE_ENTRY_ROUTE) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("passcode", "ABC123")
            }
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `submitPasscode POST with wrong content type returns 415`() {
        mvc
            .post(PASSCODE_ENTRY_ROUTE) {
                contentType = MediaType.APPLICATION_JSON
                content = """{"passcode": "ABC123"}"""
                with(csrf())
            }
            .andExpect {
                status { isUnsupportedMediaType() }
            }
    }

    @Test
    fun `passcodeAlreadyUsed GET returns 200 and displays passcode-already-used page for unauthenticated users`() {
        mvc
            .get(PASSCODE_ALREADY_USED_ROUTE)
            .andExpect {
                status { isOk() }
                view { name("passcodeAlreadyUsed") }
                model {
                    attribute("passcodeEntryUrl", PASSCODE_ENTRY_ROUTE)
                }
            }
    }
}
