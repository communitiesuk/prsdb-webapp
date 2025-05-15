package uk.gov.communities.prsdb.webapp.controllers

import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argWhere
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
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.services.FileUploader
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.TokenCookieService
import kotlin.reflect.full.memberProperties

@WebMvcTest(PropertyComplianceController::class)
class PropertyComplianceControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var tokenCookieService: TokenCookieService

    @MockitoBean
    private lateinit var fileUploader: FileUploader

    @MockitoBean
    private lateinit var propertyComplianceJourneyFactory: PropertyComplianceJourneyFactory

    @MockitoBean
    private lateinit var validator: Validator

    @Mock
    private lateinit var propertyComplianceJourney: PropertyComplianceJourney

    private val propertyComplianceJourneyRedirectUrl = "any-url"

    private val validPropertyOwnershipId = 1L
    private val validPropertyComplianceUrl = PropertyComplianceController.getPropertyCompliancePath(validPropertyOwnershipId)
    private val validPropertyComplianceInitialStepUrl =
        "$validPropertyComplianceUrl/${PropertyComplianceJourney.initialStepId.urlPathSegment}"
    private val validPropertyComplianceFileUploadUrl =
        "$validPropertyComplianceUrl/${PropertyComplianceStepId.GasSafetyUpload.urlPathSegment}"
    private val validFileUploadCookie = Cookie(FILE_UPLOAD_COOKIE_NAME, "valid-token")

    private val invalidPropertyOwnershipId = 2L
    private val invalidPropertyComplianceUrl = PropertyComplianceController.getPropertyCompliancePath(invalidPropertyOwnershipId)
    private val invalidPropertyComplianceInitialStepUrl =
        "$invalidPropertyComplianceUrl/${PropertyComplianceJourney.initialStepId.urlPathSegment}"
    private val invalidPropertyComplianceFileUploadUrl =
        "$invalidPropertyComplianceUrl/${PropertyComplianceStepId.GasSafetyUpload.urlPathSegment}"
    private val invalidFileUploadCookie = Cookie(FILE_UPLOAD_COOKIE_NAME, "invalid-token")

    @BeforeEach
    fun setUp() {
        whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(validPropertyOwnershipId), any())).thenReturn(true)
        whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(invalidPropertyOwnershipId), any())).thenReturn(false)

        whenever(
            tokenCookieService.createCookieForValue(validFileUploadCookie.name, validPropertyComplianceFileUploadUrl),
        ).thenReturn(validFileUploadCookie)

        whenever(propertyComplianceJourneyFactory.create(eq(validPropertyOwnershipId), any())).thenReturn(propertyComplianceJourney)
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
    inner class GetTaskList {
        private val validPropertyComplianceTaskListUrl = "$validPropertyComplianceUrl/$TASK_LIST_PATH_SEGMENT"
        private val invalidPropertyComplianceTaskListUrl = "$invalidPropertyComplianceUrl/$TASK_LIST_PATH_SEGMENT"

        @Test
        fun `getTaskList returns a redirect for unauthenticated user`() {
            mvc.get(validPropertyComplianceTaskListUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getTaskList returns 403 for an unauthorised user`() {
            mvc.get(validPropertyComplianceTaskListUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getTaskList returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidPropertyComplianceTaskListUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getTaskList returns 200 for a landlord user that does own the property`() {
            mvc.get(validPropertyComplianceTaskListUrl).andExpect {
                status { isOk() }
            }
        }
    }

    @Nested
    inner class GetJourneyStep {
        @Test
        fun `getJourneyStep returns a redirect for unauthenticated user`() {
            mvc.get(validPropertyComplianceInitialStepUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getJourneyStep returns 403 for an unauthorised user`() {
            mvc.get(validPropertyComplianceInitialStepUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidPropertyComplianceInitialStepUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 200 without a cookie for a valid non-file-upload request`() {
            mvc.get(validPropertyComplianceInitialStepUrl).andExpect {
                status { isOk() }
                cookie { doesNotExist(FILE_UPLOAD_COOKIE_NAME) }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 200 with a cookie for a valid file-upload request`() {
            mvc.get(validPropertyComplianceFileUploadUrl).andExpect {
                status { isOk() }
                cookie { value(FILE_UPLOAD_COOKIE_NAME, validFileUploadCookie.value) }
            }

            verify(tokenCookieService).createCookieForValue(FILE_UPLOAD_COOKIE_NAME, validPropertyComplianceFileUploadUrl)
        }
    }

    @Nested
    inner class PostJourneyData {
        @BeforeEach
        fun setUp() {
            whenever(
                propertyComplianceJourney.completeStep(
                    eq(PropertyComplianceJourney.initialStepId.urlPathSegment),
                    anyOrNull(),
                    eq(null),
                    anyOrNull(),
                    anyOrNull(),
                ),
            ).thenReturn(ModelAndView("redirect:$propertyComplianceJourneyRedirectUrl"))
        }

        @Test
        fun `postJourneyData returns a redirect for unauthenticated user`() {
            mvc
                .post(validPropertyComplianceInitialStepUrl) {
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
                .post(validPropertyComplianceInitialStepUrl) {
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
                .post(invalidPropertyComplianceInitialStepUrl) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postJourneyData returns a redirect for a landlord user that does own the property`() {
            mvc
                .post(validPropertyComplianceInitialStepUrl) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(propertyComplianceJourneyRedirectUrl)
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
                tokenCookieService.isTokenForCookieValue(validFileUploadCookie.value, validPropertyComplianceFileUploadUrl),
            ).thenReturn(true)
            whenever(
                tokenCookieService.isTokenForCookieValue(invalidFileUploadCookie.value, validPropertyComplianceFileUploadUrl),
            ).thenReturn(false)

            whenever(
                propertyComplianceJourney.completeStep(
                    eq(PropertyComplianceStepId.GasSafetyUpload.urlPathSegment),
                    argWhere { pageData -> UploadCertificateFormModel::class.memberProperties.all { it.name in pageData.keys } },
                    eq(null),
                    anyOrNull(),
                    anyOrNull(),
                ),
            ).thenReturn(ModelAndView("redirect:$propertyComplianceJourneyRedirectUrl"))
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
            whenever(validator.validateObject(any())).thenReturn(validationErrors)

            mvc
                .post(validPropertyComplianceFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(validFileUploadCookie)
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(propertyComplianceJourneyRedirectUrl)
                    cookie { value(FILE_UPLOAD_COOKIE_NAME, validFileUploadCookie.value) }
                }

            verify(tokenCookieService).useToken(validFileUploadCookie.value)
            verify(fileUploader, never()).uploadFile(any(), any())
            verify(tokenCookieService).createCookieForValue(FILE_UPLOAD_COOKIE_NAME, validPropertyComplianceFileUploadUrl)
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postFileUploadJourneyData returns a redirect with a cookie for a valid user with an unsuccessful file upload`() {
            whenever(validator.validateObject(any())).thenReturn(noValidationErrors)
            whenever(fileUploader.uploadFile(any(), any())).thenReturn(false)

            mvc
                .post(validPropertyComplianceFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(validFileUploadCookie)
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(propertyComplianceJourneyRedirectUrl)
                    cookie { value(FILE_UPLOAD_COOKIE_NAME, validFileUploadCookie.value) }
                }

            verify(tokenCookieService).useToken(validFileUploadCookie.value)
            verify(fileUploader).uploadFile(any(), any())
            verify(tokenCookieService).createCookieForValue(FILE_UPLOAD_COOKIE_NAME, validPropertyComplianceFileUploadUrl)
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postFileUploadJourneyData returns a redirect without a cookie for a valid user with an successful file upload`() {
            whenever(validator.validateObject(any())).thenReturn(noValidationErrors)
            whenever(fileUploader.uploadFile(any(), any())).thenReturn(true)

            mvc
                .post(validPropertyComplianceFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(validFileUploadCookie)
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(propertyComplianceJourneyRedirectUrl)
                }

            verify(tokenCookieService).useToken(validFileUploadCookie.value)
            verify(fileUploader).uploadFile(any(), any())
            verify(tokenCookieService, never()).createCookieForValue(any(), any(), any())
        }
    }
}
