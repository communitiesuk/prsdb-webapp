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
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_RESPONSIBILITIES_URL
import uk.gov.communities.prsdb.webapp.constants.LOGGED_IN_LANDLORD_SHOULD_SEE_FEEDBACK_PAGES
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.NewPropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyEngineerNumberStep
import uk.gov.communities.prsdb.webapp.models.viewModels.PropertyComplianceConfirmationMessageKeys
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.GiveFeedbackLaterEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.TokenCookieService
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData

@WebMvcTest(PropertyComplianceController::class)
class PropertyComplianceControllerTests(
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

    @MockitoBean
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @MockitoBean
    private lateinit var mockEmailSender: EmailNotificationService<GiveFeedbackLaterEmail>

    @MockitoBean
    private lateinit var mockLandlordService: LandlordService

    private val redirectUrl = "any-url"

    private val validPropertyOwnershipId = 1L
    private val validPropertyComplianceUrl = PropertyComplianceController.getPropertyCompliancePath(validPropertyOwnershipId)
    private val validPropertyComplianceStepUrl = "$validPropertyComplianceUrl/${GasSafetyEngineerNumberStep.ROUTE_SEGMENT}"
    private val validPropertyComplianceFileUploadUrl = "$validPropertyComplianceUrl/${GasSafetyCertificateUploadStep.ROUTE_SEGMENT}"
    private val validFileUploadCookie = Cookie(FILE_UPLOAD_COOKIE_NAME, "valid-token")

    private val invalidPropertyOwnershipId = 2L
    private val invalidPropertyComplianceUrl = PropertyComplianceController.getPropertyCompliancePath(invalidPropertyOwnershipId)
    private val invalidPropertyComplianceStepUrl = "$invalidPropertyComplianceUrl/${GasSafetyEngineerNumberStep.ROUTE_SEGMENT}"
    private val invalidPropertyComplianceFileUploadUrl = "$invalidPropertyComplianceUrl/${GasSafetyCertificateUploadStep.ROUTE_SEGMENT}"
    private val invalidFileUploadCookie = Cookie(FILE_UPLOAD_COOKIE_NAME, "invalid-token")

    private val userShouldSeeFeedback = false

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
            whenever(mockPropertyComplianceJourneyFactory.createJourneySteps(validPropertyOwnershipId, userShouldSeeFeedback))
                .thenReturn(mapOf(GasSafetyEngineerNumberStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))

            mvc.get(validPropertyComplianceStepUrl).andExpect {
                status { isOk() }
                cookie { doesNotExist(FILE_UPLOAD_COOKIE_NAME) }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 200 with a cookie for a valid file-upload request`() {
            whenever(mockPropertyComplianceJourneyFactory.createJourneySteps(validPropertyOwnershipId, userShouldSeeFeedback))
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
            whenever(mockPropertyComplianceJourneyFactory.createJourneySteps(validPropertyOwnershipId, userShouldSeeFeedback))
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
            whenever(mockPropertyComplianceJourneyFactory.createJourneySteps(validPropertyOwnershipId, userShouldSeeFeedback))
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
            whenever(mockPropertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(false)

            mvc.get(validPropertyComplianceConfirmationUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getConfirmation returns 500 if the landlord added compliance details this session but no compliance record is found`() {
            whenever(mockPropertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(true)
            whenever(mockPropertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(null)

            mvc.get(validPropertyComplianceConfirmationUrl).andExpect {
                status { is5xxServerError() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getConfirmation returns 200 for if the landlord added compliance details for the property this session`() {
            val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
            val expectedConfirmationMessageKeys = PropertyComplianceConfirmationMessageKeys(propertyCompliance)

            whenever(mockPropertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(true)
            whenever(mockPropertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(propertyCompliance)

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
            whenever(mockPropertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(false)

            mvc.get(validFeedbackUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getFeedback returns 200 for if the landlord added compliance details for the property this session`() {
            val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance()

            whenever(mockPropertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(true)
            whenever(mockPropertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(propertyCompliance)

            mvc.get(validFeedbackUrl).andExpect {
                status { isOk() }
                view { name("postComplianceFeedback") }
            }
        }
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
            whenever(mockPropertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(false)

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

            whenever(mockPropertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(true)
            whenever(mockPropertyOwnershipService.getPropertyOwnership(validPropertyOwnershipId))
                .thenReturn(propertyCompliance.propertyOwnership)

            mvc.get(validPropertyComplianceSendFeedbackUrl(route)).andExpect {
                status { is3xxRedirection() }
                redirectedUrl(destination)
            }

            verify(mockLandlordService).setHasRespondedToFeedback(
                eq(propertyCompliance.propertyOwnership.primaryLandlord),
            )
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `sendFeedbackLater sends an email to the landlord`() {
            val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance()

            whenever(mockPropertyComplianceService.wasPropertyComplianceAddedThisSession(validPropertyOwnershipId)).thenReturn(true)
            whenever(mockPropertyOwnershipService.getPropertyOwnership(validPropertyOwnershipId))
                .thenReturn(propertyCompliance.propertyOwnership)

            mvc.get(validPropertyComplianceSendFeedbackUrl(FEEDBACK_LATER_PATH_SEGMENT))

            verify(mockEmailSender).sendEmail(
                eq(propertyCompliance.propertyOwnership.primaryLandlord.email),
                any(),
            )
        }
    }

    @Nested
    inner class GetFireSafetyReview {
        private val validPropertyComplianceFireSafetyReviewUrl =
            PropertyComplianceController.getReviewPropertyComplianceStepPath(
                validPropertyOwnershipId,
                PropertyComplianceStepId.FireSafetyDeclaration.urlPathSegment,
            )
        private val invalidPropertyComplianceFireSafetyReviewUrl =
            PropertyComplianceController.getReviewPropertyComplianceStepPath(
                invalidPropertyOwnershipId,
                PropertyComplianceStepId.FireSafetyDeclaration.urlPathSegment,
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
            whenever(mockPropertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(null)

            mvc.get(validPropertyComplianceFireSafetyReviewUrl).andExpect {
                status { is3xxRedirection() }
                redirectedUrl(PropertyDetailsController.getPropertyCompliancePath(validPropertyOwnershipId))
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getFireSafetyReview returns 200 for a landlord user that owns the property with compliance`() {
            whenever(
                mockPropertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId),
            ).thenReturn(PropertyCompliance())

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
                PropertyComplianceStepId.KeepPropertySafe.urlPathSegment,
            )
        private val invalidPropertyComplianceKeepPropertySafeReviewUrl =
            PropertyComplianceController.getReviewPropertyComplianceStepPath(
                invalidPropertyOwnershipId,
                PropertyComplianceStepId.KeepPropertySafe.urlPathSegment,
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
            whenever(mockPropertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(null)

            mvc.get(validPropertyComplianceKeepPropertySafeReviewUrl).andExpect {
                status { is3xxRedirection() }
                redirectedUrl(PropertyDetailsController.getPropertyCompliancePath(validPropertyOwnershipId))
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getKeepPropertySafeReview returns 200 for a landlord user that owns the property with compliance`() {
            whenever(mockPropertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(
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
                PropertyComplianceStepId.ResponsibilityToTenants.urlPathSegment,
            )
        private val invalidPropertyComplianceResponsibilityToTenantsReviewUrl =
            PropertyComplianceController.getReviewPropertyComplianceStepPath(
                invalidPropertyOwnershipId,
                PropertyComplianceStepId.ResponsibilityToTenants.urlPathSegment,
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
            whenever(mockPropertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(null)

            mvc.get(validPropertyComplianceResponsibilityToTenantsReviewUrl).andExpect {
                status { is3xxRedirection() }
                redirectedUrl(PropertyDetailsController.getPropertyCompliancePath(validPropertyOwnershipId))
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getResponsibilityToTenantsReview returns 200 for a landlord user that owns the property with compliance`() {
            whenever(mockPropertyComplianceService.getComplianceForPropertyOrNull(validPropertyOwnershipId)).thenReturn(
                PropertyCompliance(),
            )

            val expectedPropertyComplianceUrl = PropertyDetailsController.getPropertyCompliancePath(validPropertyOwnershipId)

            mvc.get(validPropertyComplianceResponsibilityToTenantsReviewUrl).andExpect {
                status { isOk() }
                view { name("forms/responsibilityToTenantsReview") }
                model {
                    attribute("backUrl", expectedPropertyComplianceUrl)
                    attribute("landlordResponsibilitiesUrl", LANDLORD_RESPONSIBILITIES_URL)
                    attribute("governmentApprovedDepositProtectionSchemeUrl", GOVERNMENT_APPROVED_DEPOSIT_PROTECTION_SCHEME_URL)
                    attribute("howToRentGuideUrl", HOW_TO_RENT_GUIDE_URL)
                    attribute("propertyComplianceUrl", expectedPropertyComplianceUrl)
                }
            }
        }
    }

    @Nested
    inner class GetCurrentUserShouldSeeFeedbackPages {
        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getCurrentUserShouldSeeFeedbackPages uses database value when session value is not present`() {
            val expectedUsername = "user"
            whenever(mockLandlordService.getLandlordUserShouldSeeFeedbackPages(expectedUsername)).thenReturn(true)
            whenever(mockPropertyComplianceJourneyFactory.createJourneySteps(validPropertyOwnershipId, true))
                .thenReturn(mapOf(GasSafetyEngineerNumberStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))

            mvc.get(validPropertyComplianceStepUrl).andExpect {
                status { isOk() }
            }

            verify(mockLandlordService).getLandlordUserShouldSeeFeedbackPages(expectedUsername)
            verify(mockPropertyComplianceJourneyFactory).createJourneySteps(validPropertyOwnershipId, true)
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getCurrentUserShouldSeeFeedbackPages uses session value if present and does not call database`() {
            val expectedUsername = "user"
            whenever(mockPropertyComplianceJourneyFactory.createJourneySteps(validPropertyOwnershipId, true))
                .thenReturn(mapOf(GasSafetyEngineerNumberStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))

            mvc
                .get(validPropertyComplianceStepUrl) {
                    sessionAttr(LOGGED_IN_LANDLORD_SHOULD_SEE_FEEDBACK_PAGES, true)
                }.andExpect {
                    status { isOk() }
                }

            verify(mockLandlordService, never()).getLandlordUserShouldSeeFeedbackPages(expectedUsername)
            verify(mockPropertyComplianceJourneyFactory).createJourneySteps(validPropertyOwnershipId, true)
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getCurrentUserShouldSeeFeedbackPages stores database value in session`() {
            val expectedUsername = "user"
            whenever(mockLandlordService.getLandlordUserShouldSeeFeedbackPages(expectedUsername)).thenReturn(false)
            whenever(mockPropertyComplianceJourneyFactory.createJourneySteps(validPropertyOwnershipId, false))
                .thenReturn(mapOf(GasSafetyEngineerNumberStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))

            mvc.get(validPropertyComplianceStepUrl).andExpect {
                status { isOk() }
                request {
                    sessionAttribute(LOGGED_IN_LANDLORD_SHOULD_SEE_FEEDBACK_PAGES, false)
                }
            }

            verify(mockLandlordService).getLandlordUserShouldSeeFeedbackPages(expectedUsername)
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
}
