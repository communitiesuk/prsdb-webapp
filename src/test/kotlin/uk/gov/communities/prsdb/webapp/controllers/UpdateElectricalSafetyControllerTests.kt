package uk.gov.communities.prsdb.webapp.controllers

import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
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
import org.springframework.test.web.servlet.post
import org.springframework.util.ResourceUtils
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.helpers.CertificateUploadHelper
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.electricalSafety.UpdateElectricalSafetyJourneyFactory
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(UpdateElectricalSafetyController::class)
class UpdateElectricalSafetyControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BasePropertyDetailsUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateElectricalSafetyJourneyFactory

    @MockitoBean
    override lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var certificateUploadHelper: CertificateUploadHelper

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val propertyOwnershipId = 1L

    override val updateStepRoute =
        UpdateElectricalSafetyController.getUpdateElectricalSafetyFirstStepRoute(propertyOwnershipId)

    override val formContent = "electricalCertType=HAS_EIC"

    override fun stubCreateJourneySteps() {
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(HasElectricalCertStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    private val journeyId = "test-journey-id"
    private val redirectUrl = "any-url"

    private val validFileUploadUrl =
        UpdateElectricalSafetyController.UPDATE_ELECTRICAL_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", propertyOwnershipId.toString()) +
            "/${UploadElectricalCertStep.ROUTE_SEGMENT}?journeyId=$journeyId"

    private val validFileUploadCookie = Cookie(FILE_UPLOAD_COOKIE_NAME, "valid-token")

    @Nested
    inner class PostFileUploadStep {
        private val httpEntity =
            MultipartEntityBuilder
                .create()
                .addTextBody("_csrf", "any-csrf-token")
                .addBinaryBody("certificate", ResourceUtils.getFile("classpath:data/certificates/validFile.png"))
                .build()

        @BeforeEach
        fun setUp() {
            whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
                .thenReturn(mapOf(UploadElectricalCertStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
        }

        @Test
        fun `postFileUploadStep returns a redirect for unauthenticated user`() {
            mvc
                .post(validFileUploadUrl) {
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
        fun `postFileUploadStep returns 403 for an unauthorised user`() {
            mvc
                .post(validFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(validFileUploadCookie)
                }.andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"], value = LANDLORD_USER)
        fun `postFileUploadStep returns 400 for a valid user without a cookie`() {
            whenever(propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, LANDLORD_USER))
                .thenReturn(true)

            mvc
                .post(validFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"], value = LANDLORD_USER)
        fun `postFileUploadStep delegates to the certificate upload helper and redirects`() {
            whenever(propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, LANDLORD_USER))
                .thenReturn(true)
            whenever(certificateUploadHelper.uploadFileAndReturnFormModel(any(), any(), any(), any()))
                .thenReturn(mapOf<String, Any>())
            whenever(stepLifecycleOrchestrator.postStepModelAndView(any()))
                .thenReturn(ModelAndView("redirect:$redirectUrl"))

            mvc
                .post(validFileUploadUrl) {
                    contentType = MediaType.parseMediaType(httpEntity.contentType)
                    content = httpEntity.content.readAllBytes()
                    with(csrf().asHeader())
                    cookie(validFileUploadCookie)
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(redirectUrl)
                }

            verify(certificateUploadHelper).uploadFileAndReturnFormModel(any(), any(), eq(validFileUploadCookie.value), any())
        }
    }
}
