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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.util.ResourceUtils
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.CertificateUploadHelper
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.electricalSafety.UpdateElectricalSafetyJourneyFactory
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData
import java.util.UUID

@WebMvcTest(LettingAgentUpdateElectricalSafetyController::class)
class LettingAgentUpdateElectricalSafetyControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateElectricalSafetyJourneyFactory

    @MockitoBean
    private lateinit var lettingAgentAccessService: LettingAgentAccessService

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var certificateUploadHelper: CertificateUploadHelper

    @MockitoBean
    private lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    private val token: UUID = UUID.randomUUID()

    private val updateStepRoute =
        LettingAgentUpdateElectricalSafetyController.getUpdateElectricalSafetyRoute(token) +
            "/${HasElectricalCertStep.ROUTE_SEGMENT}"

    @BeforeEach
    fun enableFeatureFlag() {
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
    }

    @Test
    fun `getUpdateStep dispatches the journey step for a valid token`() {
        val propertyOwnership = createOccupiedPropertyOwnership()
        stubValidTokenJourney(propertyOwnership)
        whenever(stepLifecycleOrchestrator.getStepModelAndView())
            .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc.get(updateStepRoute).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `postUpdateStep dispatches the journey step for a valid token`() {
        val propertyOwnership = createOccupiedPropertyOwnership()
        stubValidTokenJourney(propertyOwnership)
        val redirectUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
        whenever(stepLifecycleOrchestrator.postStepModelAndView(any()))
            .thenReturn(ModelAndView("redirect:$redirectUrl"))

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = "electricalCertType=HAS_EIC"
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(redirectUrl)
            }
    }

    @Test
    fun `getUpdateStep returns not found when the token is not recognised`() {
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token))).thenReturn(null)

        mvc.get(updateStepRoute).andExpect {
            status { isNotFound() }
        }
    }

    @Nested
    inner class PostFileUploadStep {
        private val journeyId = "test-journey-id"
        private val redirectUrl = "any-url"

        private val validFileUploadUrl =
            LettingAgentUpdateElectricalSafetyController.getUpdateElectricalSafetyRoute(token) +
                "/${UploadElectricalCertStep.ROUTE_SEGMENT}?journeyId=$journeyId"

        private val validFileUploadCookie = Cookie(FILE_UPLOAD_COOKIE_NAME, "valid-token")

        private val httpEntity =
            MultipartEntityBuilder
                .create()
                .addTextBody("_csrf", "any-csrf-token")
                .addBinaryBody("certificate", ResourceUtils.getFile("classpath:data/certificates/validFile.png"))
                .build()

        @BeforeEach
        fun setUp() {
            val propertyOwnership = createOccupiedPropertyOwnership()
            stubValidTokenJourney(propertyOwnership, UploadElectricalCertStep.ROUTE_SEGMENT)
        }

        @Test
        fun `postFileUploadStep delegates to the certificate upload helper and redirects`() {
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

    private fun stubValidTokenJourney(
        propertyOwnership: PropertyOwnership,
        routeSegment: String = HasElectricalCertStep.ROUTE_SEGMENT,
    ) {
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token)))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(token = token, propertyOwnership = propertyOwnership))
        val returnUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
        whenever(journeyFactory.createJourneySteps(eq(propertyOwnership.id), eq(returnUrl)))
            .thenReturn(mapOf(routeSegment to stepLifecycleOrchestrator))
    }
}
