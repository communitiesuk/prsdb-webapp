package uk.gov.communities.prsdb.webapp.controllers

import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers.samePropertyValuesAs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
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
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_TO_COMPLIANCE_CONFIRMATION_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_FORM_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_FORM_URL
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_LATER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.GOVERNMENT_APPROVED_DEPOSIT_PROTECTION_SCHEME_URL
import uk.gov.communities.prsdb.webapp.constants.HOMES_ACT_2018_URL
import uk.gov.communities.prsdb.webapp.constants.HOUSES_IN_MULTIPLE_OCCUPATION_URL
import uk.gov.communities.prsdb.webapp.constants.HOUSING_HEALTH_AND_SAFETY_RATING_SYSTEM_URL
import uk.gov.communities.prsdb.webapp.constants.HOW_TO_RENT_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.PRIVATE_RENTING_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.RIGHT_TO_RENT_CHECKS_URL
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceUpdateJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PropertyComplianceConfirmationMessageKeys
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.GiveFeedbackLaterEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.TokenCookieService
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData
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
    private lateinit var fileUploader: UploadService

    @MockitoBean
    private lateinit var propertyComplianceJourneyFactory: PropertyComplianceJourneyFactory

    @MockitoBean
    private lateinit var propertyComplianceUpdateJourneyFactory: PropertyComplianceUpdateJourneyFactory

    @MockitoBean
    private lateinit var validator: Validator

    @MockitoBean
    private lateinit var emailSender: EmailNotificationService<GiveFeedbackLaterEmail>

    @MockitoBean
    private lateinit var propertyComplianceService: PropertyComplianceService

    @MockitoBean
    private lateinit var landlordService: LandlordService

    @Mock
    private lateinit var propertyComplianceJourney: PropertyComplianceJourney

    @Mock
    private lateinit var propertyComplianceUpdateJourney: PropertyComplianceUpdateJourney

    private val propertyComplianceJourneyRedirectUrl = "any-url"
    private val propertyComplianceUpdateJourneyRedirectUrl = "any-url"

    private val validPropertyOwnershipId = 1L
    private val validPropertyComplianceUrl = PropertyComplianceController.getPropertyCompliancePath(validPropertyOwnershipId)
    private val validPropertyComplianceInitialStepUrl =
        "$validPropertyComplianceUrl/${PropertyComplianceJourney.initialStepId.urlPathSegment}"
    private val validPropertyComplianceFileUploadUrl =
        "$validPropertyComplianceUrl/${PropertyComplianceStepId.GasSafetyUpload.urlPathSegment}"
    private val validFileUploadCookie = Cookie(FILE_UPLOAD_COOKIE_NAME, "valid-token")
    private val validPropertyComplianceUpdateUrl =
        PropertyComplianceController.getUpdatePropertyComplianceBasePath(
            validPropertyOwnershipId,
        )
    private val validPropertyComplianceUpdateInitialStepUrl =
        "$validPropertyComplianceUpdateUrl/${PropertyComplianceUpdateJourney.initialStepId.urlPathSegment}"
    private val validPropertyComplianceUpdateFileUploadUrl =
        "$validPropertyComplianceUpdateUrl/${PropertyComplianceStepId.GasSafetyUpload.urlPathSegment}"

    private val invalidPropertyOwnershipId = 2L
    private val invalidPropertyComplianceUrl = PropertyComplianceController.getPropertyCompliancePath(invalidPropertyOwnershipId)
    private val invalidPropertyComplianceInitialStepUrl =
        "$invalidPropertyComplianceUrl/${PropertyComplianceJourney.initialStepId.urlPathSegment}"
    private val invalidPropertyComplianceFileUploadUrl =
        "$invalidPropertyComplianceUrl/${PropertyComplianceStepId.GasSafetyUpload.urlPathSegment}"
    private val invalidFileUploadCookie = Cookie(FILE_UPLOAD_COOKIE_NAME, "invalid-token")
    private val invalidPropertyComplianceUpdateUrl =
        PropertyComplianceController.getUpdatePropertyComplianceBasePath(
            invalidPropertyOwnershipId,
        )
    private val invalidPropertyComplianceUpdateInitialStepUrl =
        "$invalidPropertyComplianceUpdateUrl/${PropertyComplianceUpdateJourney.initialStepId.urlPathSegment}"
    private val invalidPropertyComplianceUpdateFileUploadUrl =
        "$invalidPropertyComplianceUpdateUrl/${PropertyComplianceStepId.GasSafetyUpload.urlPathSegment}"

    @BeforeEach
    fun setUp() {
        whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(validPropertyOwnershipId), any())).thenReturn(true)
        whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(invalidPropertyOwnershipId), any())).thenReturn(false)

        whenever(
            tokenCookieService.createCookieForValue(validFileUploadCookie.name, validPropertyComplianceFileUploadUrl),
        ).thenReturn(validFileUploadCookie)
        whenever(
            tokenCookieService.createCookieForValue(validFileUploadCookie.name, validPropertyComplianceUpdateFileUploadUrl),
        ).thenReturn(validFileUploadCookie)

        whenever(propertyComplianceJourneyFactory.create(any(), eq(validPropertyOwnershipId), anyOrNull()))
            .thenReturn(propertyComplianceJourney)

        whenever(propertyComplianceUpdateJourneyFactory.create(any(), eq(validPropertyOwnershipId), anyOrNull()))
            .thenReturn(propertyComplianceUpdateJourney)
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
            verify(fileUploader, never()).uploadFile(any(), any(), any())
            verify(tokenCookieService).createCookieForValue(FILE_UPLOAD_COOKIE_NAME, validPropertyComplianceFileUploadUrl)
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postFileUploadJourneyData returns a redirect with a cookie for a valid user with an unsuccessful file upload`() {
            whenever(validator.validateObject(any())).thenReturn(noValidationErrors)
            whenever(fileUploader.uploadFile(any(), any(), any())).thenReturn(null)

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
            verify(fileUploader).uploadFile(any(), any(), any())
            verify(tokenCookieService).createCookieForValue(FILE_UPLOAD_COOKIE_NAME, validPropertyComplianceFileUploadUrl)
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postFileUploadJourneyData returns a redirect without a cookie for a valid user with an successful file upload`() {
            whenever(validator.validateObject(any())).thenReturn(noValidationErrors)
            whenever(fileUploader.uploadFile(any(), any(), any())).thenReturn(FileUpload())

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
            verify(fileUploader).uploadFile(any(), any(), any())
            verify(tokenCookieService, never()).createCookieForValue(any(), any(), any())
        }
    }

    @Nested
    inner class GetConfirmation {
        private val validPropertyComplianceConfirmationUrl = "$validPropertyComplianceUrl/$CONFIRMATION_PATH_SEGMENT"
        private val invalidPropertyComplianceConfirmationUrl = "$invalidPropertyComplianceUrl/$CONFIRMATION_PATH_SEGMENT"

        @Test
        fun `getConfirmation returns a redirect for unauthenticated user`() {
            mvc.get(validPropertyComplianceConfirmationUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getConfirmation returns 403 for an unauthorised user`() {
            mvc.get(validPropertyComplianceConfirmationUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getConfirmation returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidPropertyComplianceConfirmationUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getConfirmation returns 404 if the landlord didn't add compliance details for the property this session`() {
            whenever(propertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(false)

            mvc.get(validPropertyComplianceConfirmationUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getConfirmation returns 500 if the landlord added compliance details this session but no compliance record is found`() {
            whenever(propertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(true)
            whenever(propertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(null)

            mvc.get(validPropertyComplianceConfirmationUrl).andExpect {
                status { is5xxServerError() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getConfirmation returns 200 for if the landlord added compliance details for the property this session`() {
            val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
            val expectedConfirmationMessageKeys = PropertyComplianceConfirmationMessageKeys(propertyCompliance)

            whenever(propertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(true)
            whenever(propertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(propertyCompliance)

            mvc.get(validPropertyComplianceConfirmationUrl).andExpect {
                status { isOk() }
                model { attribute("confirmationMessageKeys", samePropertyValuesAs(expectedConfirmationMessageKeys)) }
                view { name("fullyCompliantPropertyConfirmation") }
            }
        }
    }

    @Nested
    inner class GetFeedback {
        private val validFeedbackUrl = "$validPropertyComplianceUrl/$FEEDBACK_PATH_SEGMENT"
        private val invalidFeedbackUrl = "$invalidPropertyComplianceUrl/$FEEDBACK_PATH_SEGMENT"

        @Test
        fun `getFeedback returns a redirect for unauthenticated user`() {
            mvc.get(validFeedbackUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getFeedback returns 403 for an unauthorised user`() {
            mvc.get(validFeedbackUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getFeedback returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidFeedbackUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getFeedback returns 404 if the landlord didn't add compliance details for the property this session`() {
            whenever(propertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(false)

            mvc.get(validFeedbackUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getFeedback returns 200 for if the landlord added compliance details for the property this session`() {
            val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance()

            whenever(propertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(true)
            whenever(propertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(propertyCompliance)

            mvc.get(validFeedbackUrl).andExpect {
                status { isOk() }
                view { name("postComplianceFeedback") }
            }
        }
    }

    companion object {
        @JvmStatic
        fun feedbackResponseRoutes(): List<Arguments> =
            listOf(
                Arguments.of(Named.of("give feedback later", FEEDBACK_LATER_PATH_SEGMENT), CONFIRMATION_PATH_SEGMENT),
                Arguments.of(Named.of("feedback form", FEEDBACK_FORM_SEGMENT), FEEDBACK_FORM_URL),
                Arguments.of(Named.of("skip feedback", CONTINUE_TO_COMPLIANCE_CONFIRMATION_SEGMENT), CONFIRMATION_PATH_SEGMENT),
            )
    }

    @Nested
    inner class RespondToRequestForFeedback {
        private fun validPropertyComplianceSendFeedbackUrl(route: String) = "$validPropertyComplianceUrl/$route"

        private fun invalidPropertyComplianceSendFeedbackUrl(route: String) = "$invalidPropertyComplianceUrl/$route"

        @ParameterizedTest()
        @MethodSource("uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceControllerTests#feedbackResponseRoutes")
        fun `route returns a redirect for unauthenticated user`(route: String) {
            mvc.get(validPropertyComplianceSendFeedbackUrl(route)).andExpect {
                status { is3xxRedirection() }
            }
        }

        @ParameterizedTest()
        @MethodSource("uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceControllerTests#feedbackResponseRoutes")
        @WithMockUser
        fun `route returns 403 for an unauthorised user`(route: String) {
            mvc.get(validPropertyComplianceSendFeedbackUrl(route)).andExpect {
                status { isForbidden() }
            }
        }

        @ParameterizedTest()
        @MethodSource("uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceControllerTests#feedbackResponseRoutes")
        @WithMockUser(roles = ["LANDLORD"])
        fun `route returns 404 for a landlord user that doesn't own the property`(route: String) {
            mvc.get(invalidPropertyComplianceSendFeedbackUrl(route)).andExpect {
                status { isNotFound() }
            }
        }

        @ParameterizedTest()
        @MethodSource("uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceControllerTests#feedbackResponseRoutes")
        @WithMockUser(roles = ["LANDLORD"])
        fun `route returns 404 if the landlord didn't add compliance details for the property this session`(route: String) {
            whenever(propertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(false)

            mvc.get(validPropertyComplianceSendFeedbackUrl(route)).andExpect {
                status { isNotFound() }
            }
        }

        @ParameterizedTest()
        @MethodSource("uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceControllerTests#feedbackResponseRoutes")
        @WithMockUser(roles = ["LANDLORD"])
        fun `route redirects and marks landlord as having seen feedback if the compliance details were added for the property this session`(
            route: String,
            destination: String,
        ) {
            val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance()

            whenever(propertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(true)
            whenever(propertyOwnershipService.getPropertyOwnership(validPropertyOwnershipId))
                .thenReturn(propertyCompliance.propertyOwnership)

            mvc.get(validPropertyComplianceSendFeedbackUrl(route)).andExpect {
                status { is3xxRedirection() }
                redirectedUrl(destination)
            }

            verify(landlordService).setHasRespondedToFeedback(
                eq(propertyCompliance.propertyOwnership.primaryLandlord),
            )
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `sendFeedbackLater sends an email to the landlord`() {
            val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance()

            whenever(propertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(true)
            whenever(propertyOwnershipService.getPropertyOwnership(validPropertyOwnershipId))
                .thenReturn(propertyCompliance.propertyOwnership)

            mvc.get(validPropertyComplianceSendFeedbackUrl(FEEDBACK_LATER_PATH_SEGMENT))

            verify(emailSender).sendEmail(
                eq(propertyCompliance.propertyOwnership.primaryLandlord.email),
                any(),
            )
        }
    }

    @Nested
    inner class GetUpdateJourneyStep {
        @Test
        fun `getUpdateJourneyStep returns a redirect for unauthenticated user`() {
            mvc.get(validPropertyComplianceUpdateInitialStepUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getUpdateJourneyStep returns 403 for an unauthorised user`() {
            mvc.get(validPropertyComplianceUpdateInitialStepUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getUpdateJourneyStep returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidPropertyComplianceUpdateInitialStepUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getUpdateJourneyStep returns 200 without a cookie for a valid non-file-upload request`() {
            mvc.get(validPropertyComplianceUpdateInitialStepUrl).andExpect {
                status { isOk() }
                cookie { doesNotExist(FILE_UPLOAD_COOKIE_NAME) }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getUpdateJourneyStep returns 200 with a cookie for a valid file-upload request`() {
            mvc.get(validPropertyComplianceUpdateFileUploadUrl).andExpect {
                status { isOk() }
                cookie { value(FILE_UPLOAD_COOKIE_NAME, validFileUploadCookie.value) }
            }

            verify(tokenCookieService).createCookieForValue(FILE_UPLOAD_COOKIE_NAME, validPropertyComplianceUpdateFileUploadUrl)
        }
    }

    @Nested
    inner class PostUpdateJourneyData {
        @BeforeEach
        fun setUp() {
            whenever(
                propertyComplianceUpdateJourney.completeStep(
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                ),
            ).thenReturn(ModelAndView("redirect:$propertyComplianceUpdateJourneyRedirectUrl"))
        }

        @Test
        fun `postUpdateJourneyData returns a redirect for unauthenticated user`() {
            mvc
                .post(validPropertyComplianceUpdateInitialStepUrl) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        @WithMockUser
        fun `postUpdateJourneyData returns 403 for an unauthorised user`() {
            mvc
                .post(validPropertyComplianceUpdateInitialStepUrl) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postUpdateJourneyData returns 404 for a landlord user that doesn't own the property`() {
            mvc
                .post(invalidPropertyComplianceUpdateInitialStepUrl) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postUpdateJourneyData returns a redirect for a landlord user that does own the property`() {
            mvc
                .post(validPropertyComplianceUpdateInitialStepUrl) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(propertyComplianceJourneyRedirectUrl)
                }
        }
    }

    @Nested
    inner class PostFileUploadUpdateJourneyData {
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
                tokenCookieService.isTokenForCookieValue(validFileUploadCookie.value, validPropertyComplianceUpdateFileUploadUrl),
            ).thenReturn(true)
            whenever(
                tokenCookieService.isTokenForCookieValue(invalidFileUploadCookie.value, validPropertyComplianceUpdateFileUploadUrl),
            ).thenReturn(false)

            whenever(
                propertyComplianceUpdateJourney.completeStep(
                    argWhere { pageData -> UploadCertificateFormModel::class.memberProperties.all { it.name in pageData.keys } },
                    anyOrNull(),
                    anyOrNull(),
                ),
            ).thenReturn(ModelAndView("redirect:$propertyComplianceUpdateJourneyRedirectUrl"))
        }

        @Test
        fun `postFileUploadUpdateJourneyData returns a redirect for unauthenticated user`() {
            mvc
                .post(validPropertyComplianceUpdateFileUploadUrl) {
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
        fun `postFileUploadUpdateJourneyData returns 403 for an unauthorised user`() {
            mvc
                .post(validPropertyComplianceUpdateFileUploadUrl) {
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
        fun `postFileUploadUpdateJourneyData returns 404 for a landlord user that doesn't own the property`() {
            mvc
                .post(invalidPropertyComplianceUpdateFileUploadUrl) {
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
        fun `postFileUploadUpdateJourneyData returns 400 for a valid user without a cookie`() {
            mvc
                .post(validPropertyComplianceUpdateFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postFileUploadUpdateJourneyData returns 400 for a valid user with an invalid cookie`() {
            mvc
                .post(validPropertyComplianceUpdateFileUploadUrl) {
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
        fun `postFileUploadUpdateJourneyData returns a redirect with a cookie for a valid user with an invalid file`() {
            whenever(validator.validateObject(any())).thenReturn(validationErrors)

            mvc
                .post(validPropertyComplianceUpdateFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(validFileUploadCookie)
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(propertyComplianceUpdateJourneyRedirectUrl)
                    cookie { value(FILE_UPLOAD_COOKIE_NAME, validFileUploadCookie.value) }
                }

            verify(tokenCookieService).useToken(validFileUploadCookie.value)
            verify(fileUploader, never()).uploadFile(any(), any(), any())
            verify(tokenCookieService).createCookieForValue(FILE_UPLOAD_COOKIE_NAME, validPropertyComplianceUpdateFileUploadUrl)
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postFileUploadUpdateJourneyData returns a redirect with a cookie for a valid user with an unsuccessful file upload`() {
            whenever(validator.validateObject(any())).thenReturn(noValidationErrors)
            whenever(fileUploader.uploadFile(any(), any(), any())).thenReturn(null)

            mvc
                .post(validPropertyComplianceUpdateFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(validFileUploadCookie)
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(propertyComplianceUpdateJourneyRedirectUrl)
                    cookie { value(FILE_UPLOAD_COOKIE_NAME, validFileUploadCookie.value) }
                }

            verify(tokenCookieService).useToken(validFileUploadCookie.value)
            verify(fileUploader).uploadFile(any(), any(), any())
            verify(tokenCookieService).createCookieForValue(FILE_UPLOAD_COOKIE_NAME, validPropertyComplianceUpdateFileUploadUrl)
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postFileUploadUpdateJourneyData returns a redirect without a cookie for a valid user with an successful file upload`() {
            whenever(validator.validateObject(any())).thenReturn(noValidationErrors)
            whenever(fileUploader.uploadFile(any(), any(), any())).thenReturn(FileUpload())

            mvc
                .post(validPropertyComplianceUpdateFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(validFileUploadCookie)
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(propertyComplianceUpdateJourneyRedirectUrl)
                }

            verify(tokenCookieService).useToken(validFileUploadCookie.value)
            verify(fileUploader).uploadFile(any(), any(), any())
            verify(tokenCookieService, never()).createCookieForValue(any(), any(), any())
        }
    }

    @Nested
    inner class GetFireSafetyReview {
        private val validPropertyComplianceFireSafetyReviewUrl =
            PropertyComplianceController.getReviewPropertyComplianceStepPath(
                validPropertyOwnershipId,
                PropertyComplianceStepId.FireSafetyDeclaration,
            )
        private val invalidPropertyComplianceFireSafetyReviewUrl =
            PropertyComplianceController.getReviewPropertyComplianceStepPath(
                invalidPropertyOwnershipId,
                PropertyComplianceStepId.FireSafetyDeclaration,
            )

        @Test
        fun `getFireSafetyReview returns a redirect for unauthenticated user`() {
            mvc.get(validPropertyComplianceFireSafetyReviewUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getFireSafetyReview returns 403 for an unauthorised user`() {
            mvc.get(validPropertyComplianceFireSafetyReviewUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getFireSafetyReview returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidPropertyComplianceFireSafetyReviewUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getFireSafetyReview redirects to the compliance record for a landlord user that owns the property without compliance`() {
            whenever(propertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(null)

            mvc.get(validPropertyComplianceFireSafetyReviewUrl).andExpect {
                status { is3xxRedirection() }
                redirectedUrl(PropertyDetailsController.getPropertyCompliancePath(validPropertyOwnershipId))
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getFireSafetyReview returns 200 for a landlord user that owns the property with compliance`() {
            whenever(propertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(PropertyCompliance())

            val expectedPropertyComplianceUrl = PropertyDetailsController.getPropertyCompliancePath(validPropertyOwnershipId)

            mvc.get(validPropertyComplianceFireSafetyReviewUrl).andExpect {
                status { isOk() }
                view { name("forms/fireSafetyReview") }
                model {
                    attribute("backUrl", expectedPropertyComplianceUrl)
                    attribute("housesInMultipleOccupationUrl", HOUSES_IN_MULTIPLE_OCCUPATION_URL)
                    attribute("propertyComplianceUrl", expectedPropertyComplianceUrl)
                }
            }
        }
    }

    @Nested
    inner class GetKeepPropertySafeReview {
        private val validPropertyComplianceKeepPropertySafeReviewUrl =
            PropertyComplianceController.getReviewPropertyComplianceStepPath(
                validPropertyOwnershipId,
                PropertyComplianceStepId.KeepPropertySafe,
            )
        private val invalidPropertyComplianceKeepPropertySafeReviewUrl =
            PropertyComplianceController.getReviewPropertyComplianceStepPath(
                invalidPropertyOwnershipId,
                PropertyComplianceStepId.KeepPropertySafe,
            )

        @Test
        fun `getKeepPropertySafeReview returns a redirect for unauthenticated user`() {
            mvc.get(validPropertyComplianceKeepPropertySafeReviewUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getKeepPropertySafeReview returns 403 for an unauthorised user`() {
            mvc.get(validPropertyComplianceKeepPropertySafeReviewUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getKeepPropertySafeReview returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidPropertyComplianceKeepPropertySafeReviewUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getKeepPropertySafeReview redirects to the compliance record for a landlord user that owns the property without compliance`() {
            whenever(propertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(null)

            mvc.get(validPropertyComplianceKeepPropertySafeReviewUrl).andExpect {
                status { is3xxRedirection() }
                redirectedUrl(PropertyDetailsController.getPropertyCompliancePath(validPropertyOwnershipId))
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getKeepPropertySafeReview returns 200 for a landlord user that owns the property with compliance`() {
            whenever(propertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(
                PropertyCompliance(),
            )

            val expectedPropertyComplianceUrl = PropertyDetailsController.getPropertyCompliancePath(validPropertyOwnershipId)

            mvc.get(validPropertyComplianceKeepPropertySafeReviewUrl).andExpect {
                status { isOk() }
                view { name("forms/keepPropertySafeReview") }
                model {
                    attribute("backUrl", expectedPropertyComplianceUrl)
                    attribute("housingHealthAndSafetyRatingSystemUrl", HOUSING_HEALTH_AND_SAFETY_RATING_SYSTEM_URL)
                    attribute("homesAct2018Url", HOMES_ACT_2018_URL)
                    attribute("propertyComplianceUrl", expectedPropertyComplianceUrl)
                }
            }
        }
    }

    @Nested
    inner class GetResponsibilityToTenantsReview {
        private val validPropertyComplianceResponsibilityToTenantsReviewUrl =
            PropertyComplianceController.getReviewPropertyComplianceStepPath(
                validPropertyOwnershipId,
                PropertyComplianceStepId.ResponsibilityToTenants,
            )
        private val invalidPropertyComplianceResponsibilityToTenantsReviewUrl =
            PropertyComplianceController.getReviewPropertyComplianceStepPath(
                invalidPropertyOwnershipId,
                PropertyComplianceStepId.ResponsibilityToTenants,
            )

        @Test
        fun `getResponsibilityToTenantsReview returns a redirect for unauthenticated user`() {
            mvc.get(validPropertyComplianceResponsibilityToTenantsReviewUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getResponsibilityToTenantsReview returns 403 for an unauthorised user`() {
            mvc.get(validPropertyComplianceResponsibilityToTenantsReviewUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getResponsibilityToTenantsReview returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidPropertyComplianceResponsibilityToTenantsReviewUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getResponsibilityToTenantsReview redirects to compliance record for landlord that owns the property without compliance`() {
            whenever(propertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(null)

            mvc.get(validPropertyComplianceResponsibilityToTenantsReviewUrl).andExpect {
                status { is3xxRedirection() }
                redirectedUrl(PropertyDetailsController.getPropertyCompliancePath(validPropertyOwnershipId))
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getResponsibilityToTenantsReview returns 200 for a landlord user that owns the property with compliance`() {
            whenever(propertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(
                PropertyCompliance(),
            )

            val expectedPropertyComplianceUrl = PropertyDetailsController.getPropertyCompliancePath(validPropertyOwnershipId)

            mvc.get(validPropertyComplianceResponsibilityToTenantsReviewUrl).andExpect {
                status { isOk() }
                view { name("forms/responsibilityToTenantsReview") }
                model {
                    attribute("backUrl", expectedPropertyComplianceUrl)
                    attribute("privateRentingGuideUrl", PRIVATE_RENTING_GUIDE_URL)
                    attribute("rightToRentChecksUrl", RIGHT_TO_RENT_CHECKS_URL)
                    attribute("governmentApprovedDepositProtectionSchemeUrl", GOVERNMENT_APPROVED_DEPOSIT_PROTECTION_SCHEME_URL)
                    attribute("howToRentGuideUrl", HOW_TO_RENT_GUIDE_URL)
                    attribute("propertyComplianceUrl", expectedPropertyComplianceUrl)
                }
            }
        }
    }
}
