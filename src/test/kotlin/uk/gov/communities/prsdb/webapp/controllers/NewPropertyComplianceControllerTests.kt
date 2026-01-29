package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_UPLOAD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.NewPropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.TokenCookieService

@WebMvcTest(NewPropertyComplianceController::class)
class NewPropertyComplianceControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var mockPropertyComplianceJourneyFactory: NewPropertyComplianceJourneyFactory

    @MockitoBean
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var mockTokenCookieService: TokenCookieService

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val redirectUrl = "any-url"

    private val validPropertyOwnershipId = 1L
    private val validPropertyComplianceUrl = NewPropertyComplianceController.getPropertyCompliancePath(validPropertyOwnershipId)
    private val validPropertyComplianceTaskListStepUrl = "$validPropertyComplianceUrl/$TASK_LIST_PATH_SEGMENT"
    private val validPropertyComplianceFileUploadUrl = "$validPropertyComplianceUrl/$GAS_SAFETY_UPLOAD_PATH_SEGMENT"
    private val validFileUploadCookie = Cookie(FILE_UPLOAD_COOKIE_NAME, "valid-token")

    private val invalidPropertyOwnershipId = 2L
    private val invalidPropertyComplianceUrl = NewPropertyComplianceController.getPropertyCompliancePath(invalidPropertyOwnershipId)
    private val invalidPropertyComplianceTaskListStepUrl = "$invalidPropertyComplianceUrl/$TASK_LIST_PATH_SEGMENT"

    @BeforeEach
    fun setUp() {
        whenever(mockPropertyOwnershipService.getIsPrimaryLandlord(eq(validPropertyOwnershipId), any())).thenReturn(true)
        whenever(mockPropertyOwnershipService.getIsPrimaryLandlord(eq(invalidPropertyOwnershipId), any())).thenReturn(false)
    }

    @Nested
    inner class GetJourneyStep {
        @Test
        fun `getJourneyStep returns a redirect for unauthenticated user`() {
            mvc.get(validPropertyComplianceTaskListStepUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getJourneyStep returns 403 for an unauthorised user`() {
            mvc.get(validPropertyComplianceTaskListStepUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidPropertyComplianceTaskListStepUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 200 without a cookie for a valid non-file-upload request`() {
            whenever(mockStepLifecycleOrchestrator.getStepModelAndView())
                .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))
            whenever(mockPropertyComplianceJourneyFactory.createJourneySteps())
                .thenReturn(mapOf(TASK_LIST_PATH_SEGMENT to mockStepLifecycleOrchestrator))

            mvc.get(validPropertyComplianceTaskListStepUrl).andExpect {
                status { isOk() }
                cookie { doesNotExist(FILE_UPLOAD_COOKIE_NAME) }
            }
        }

        // TODO PDJB-457 - enable this test once a file upload endpoint is implemented
        @Disabled
        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 200 with a cookie for a valid file-upload request`() {
            whenever(mockStepLifecycleOrchestrator.getStepModelAndView())
                .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))
            whenever(mockPropertyComplianceJourneyFactory.createJourneySteps())
                .thenReturn(mapOf(GAS_SAFETY_UPLOAD_PATH_SEGMENT to mockStepLifecycleOrchestrator))

            mvc.get(validPropertyComplianceFileUploadUrl).andExpect {
                status { isOk() }
                cookie { value(FILE_UPLOAD_COOKIE_NAME, validFileUploadCookie.value) }
            }

            verify(mockTokenCookieService).createCookieForValue(FILE_UPLOAD_COOKIE_NAME, validPropertyComplianceFileUploadUrl)
        }
    }

    @Nested
    inner class PostJourneyData {
        @Test
        fun `postJourneyData returns a redirect for unauthenticated user`() {
            mvc
                .post(validPropertyComplianceTaskListStepUrl) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        @WithMockUser
        fun `postJourneyData returns 403 for an unauthorised user`() {
            mvc
                .post(validPropertyComplianceTaskListStepUrl) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postJourneyData returns 404 for a landlord user that doesn't own the property`() {
            mvc
                .post(validPropertyComplianceTaskListStepUrl) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { isNotFound() }
                }
        }

        // TODO PDJB-467 - add this in once we have a postable endpoint
        @Disabled
        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postJourneyData returns a redirect for a landlord user that does own the property`() {
            mvc
                .post(validPropertyComplianceFileUploadUrl) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(redirectUrl)
                }
        }
    }
}
