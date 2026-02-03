package uk.gov.communities.prsdb.webapp.controllers

import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
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
import org.springframework.util.ResourceUtils
import org.springframework.validation.SimpleErrors
import org.springframework.validation.Validator
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.controllers.NewPropertyComplianceController.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.NewPropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyEngineerNumberStep
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.TokenCookieService
import uk.gov.communities.prsdb.webapp.services.UploadService

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

    @MockitoBean
    private lateinit var mockUploadService: UploadService

    @MockitoBean
    private lateinit var mockValidator: Validator

    private val redirectUrl = "any-url"

    private val validPropertyOwnershipId = 1L
    private val validPropertyComplianceUrl = NewPropertyComplianceController.getPropertyCompliancePath(validPropertyOwnershipId)
    private val validPropertyComplianceStepUrl = "$validPropertyComplianceUrl/${GasSafetyEngineerNumberStep.ROUTE_SEGMENT}"
    private val validPropertyComplianceFileUploadUrl = "$validPropertyComplianceUrl/${GasSafetyCertificateUploadStep.ROUTE_SEGMENT}"
    private val validFileUploadCookie = Cookie(FILE_UPLOAD_COOKIE_NAME, "valid-token")

    private val invalidPropertyOwnershipId = 2L
    private val invalidPropertyComplianceUrl = NewPropertyComplianceController.getPropertyCompliancePath(invalidPropertyOwnershipId)
    private val invalidPropertyComplianceStepUrl = "$invalidPropertyComplianceUrl/${GasSafetyEngineerNumberStep.ROUTE_SEGMENT}"
    private val invalidPropertyComplianceFileUploadUrl = "$invalidPropertyComplianceUrl/${GasSafetyCertificateUploadStep.ROUTE_SEGMENT}"
    private val invalidFileUploadCookie = Cookie(FILE_UPLOAD_COOKIE_NAME, "invalid-token")

    @BeforeEach
    fun setUp() {
        whenever(mockPropertyOwnershipService.getIsPrimaryLandlord(eq(validPropertyOwnershipId), any())).thenReturn(true)
        whenever(mockPropertyOwnershipService.getIsPrimaryLandlord(eq(invalidPropertyOwnershipId), any())).thenReturn(false)
        whenever(mockStepLifecycleOrchestrator.postStepModelAndView(any()))
            .thenReturn(ModelAndView("redirect:$redirectUrl", null))
        whenever(mockStepLifecycleOrchestrator.getStepModelAndView())
            .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))
    }

    @Nested
    inner class Index {
        @Test
        fun `index returns a redirect for unauthenticated user`() {
            mvc.get(validPropertyComplianceUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `index returns 403 for an unauthorised user`() {
            mvc.get(validPropertyComplianceUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `index returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidPropertyComplianceUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `index returns 200 for a landlord user that does own the property`() {
            mvc.get(validPropertyComplianceUrl).andExpect {
                status { isOk() }
            }
        }
    }

    @Nested
    inner class GetJourneyStep {
        @Test
        fun `getJourneyStep returns a redirect for unauthenticated user`() {
            mvc.get(validPropertyComplianceStepUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getJourneyStep returns 403 for an unauthorised user`() {
            mvc.get(validPropertyComplianceStepUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidPropertyComplianceStepUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 200 without a cookie for a valid non-file-upload request`() {
            whenever(mockPropertyComplianceJourneyFactory.createJourneySteps())
                .thenReturn(mapOf(GasSafetyEngineerNumberStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))

            mvc.get(validPropertyComplianceStepUrl).andExpect {
                status { isOk() }
                cookie { doesNotExist(FILE_UPLOAD_COOKIE_NAME) }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 200 with a cookie for a valid file-upload request`() {
            whenever(mockPropertyComplianceJourneyFactory.createJourneySteps())
                .thenReturn(mapOf(GasSafetyCertificateUploadStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
            whenever(mockTokenCookieService.createCookieForValue(eq(FILE_UPLOAD_COOKIE_NAME), any(), any()))
                .thenReturn(validFileUploadCookie)

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
                .post(validPropertyComplianceStepUrl) {
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
                .post(validPropertyComplianceStepUrl) {
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
                .post(validPropertyComplianceStepUrl) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postJourneyData returns a redirect for a landlord user that does own the property`() {
            whenever(mockPropertyComplianceJourneyFactory.createJourneySteps())
                .thenReturn(mapOf(GasSafetyEngineerNumberStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))

            mvc
                .post(validPropertyComplianceStepUrl) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(redirectUrl)
                }
        }
    }

    @Nested
    inner class PostFileUploadJourneyData {
        private val httpEntity =
            MultipartEntityBuilder
                .create()
                .addTextBody("_csrf", "any-csrf-token")
                .addBinaryBody("certificate", ResourceUtils.getFile("classpath:data/certificates/validFile.png"))
                .build()

        private val validationErrors = SimpleErrors(object {}).apply { reject("any-error-code") }
        private val noValidationErrors = SimpleErrors(object {})

        @BeforeEach
        fun setUp() {
            whenever(
                mockTokenCookieService.isTokenForCookieValue(validFileUploadCookie.value, validPropertyComplianceFileUploadUrl),
            ).thenReturn(true)
            whenever(
                mockTokenCookieService.isTokenForCookieValue(invalidFileUploadCookie.value, validPropertyComplianceFileUploadUrl),
            ).thenReturn(false)
            whenever(mockPropertyComplianceJourneyFactory.createJourneySteps())
                .thenReturn(mapOf(GasSafetyCertificateUploadStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
            whenever(mockTokenCookieService.createCookieForValue(eq(FILE_UPLOAD_COOKIE_NAME), any(), any()))
                .thenReturn(validFileUploadCookie)
        }

        @Test
        fun `postFileUploadJourneyData returns a redirect for unauthenticated user`() {
            mvc
                .post(validPropertyComplianceFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(validFileUploadCookie)
                }.andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        @WithMockUser
        fun `postFileUploadJourneyData returns 403 for an unauthorised user`() {
            mvc
                .post(validPropertyComplianceFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(validFileUploadCookie)
                }.andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postFileUploadJourneyData returns 404 for a landlord user that doesn't own the property`() {
            mvc
                .post(invalidPropertyComplianceFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(validFileUploadCookie)
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postFileUploadJourneyData returns 400 for a valid user without a cookie`() {
            mvc
                .post(validPropertyComplianceFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postFileUploadJourneyData returns 400 for a valid user with an invalid cookie`() {
            mvc
                .post(validPropertyComplianceFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(invalidFileUploadCookie)
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postFileUploadJourneyData returns a redirect with a cookie for a valid user with an invalid file`() {
            whenever(mockValidator.validateObject(any())).thenReturn(validationErrors)
            whenever(mockUploadService.uploadFile(any(), any(), any())).thenReturn(FileUpload())

            mvc
                .post(validPropertyComplianceFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(validFileUploadCookie)
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(redirectUrl)
                    cookie { value(FILE_UPLOAD_COOKIE_NAME, validFileUploadCookie.value) }
                }

            verify(mockTokenCookieService).useToken(validFileUploadCookie.value)
            verify(mockUploadService, never()).uploadFile(any(), any(), any())
            verify(mockTokenCookieService).createCookieForValue(FILE_UPLOAD_COOKIE_NAME, validPropertyComplianceFileUploadUrl)
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postFileUploadJourneyData returns a redirect with a cookie for a valid user with an unsuccessful file upload`() {
            whenever(mockValidator.validateObject(any())).thenReturn(noValidationErrors)
            whenever(mockUploadService.uploadFile(any(), any(), any())).thenReturn(null)

            mvc
                .post(validPropertyComplianceFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(validFileUploadCookie)
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(redirectUrl)
                    cookie { value(FILE_UPLOAD_COOKIE_NAME, validFileUploadCookie.value) }
                }

            verify(mockTokenCookieService).useToken(validFileUploadCookie.value)
            verify(mockUploadService).uploadFile(any(), any(), any())
            verify(mockTokenCookieService).createCookieForValue(FILE_UPLOAD_COOKIE_NAME, validPropertyComplianceFileUploadUrl)
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postFileUploadJourneyData returns a redirect without a cookie for a valid user with an successful file upload`() {
            whenever(mockValidator.validateObject(any())).thenReturn(noValidationErrors)
            whenever(mockUploadService.uploadFile(any(), any(), any())).thenReturn(FileUpload())

            mvc
                .post(validPropertyComplianceFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(validFileUploadCookie)
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(redirectUrl)
                }

            verify(mockTokenCookieService).useToken(validFileUploadCookie.value)
            verify(mockUploadService).uploadFile(any(), any(), any())
            verify(mockTokenCookieService, never()).createCookieForValue(any(), any(), any())
        }
    }
}
