package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.CORRESPONDENCE_ADDRESS
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.correspondenceEmail.UpdateCorrespondenceEmailJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(UpdateCorrespondenceEmailController::class)
class UpdateCorrespondenceEmailControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BasePropertyDetailsUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateCorrespondenceEmailJourneyFactory

    @MockitoBean
    override lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    override val propertyOwnershipId = 1L
    override val updateStepRoute =
        UpdateCorrespondenceEmailController.getUpdateCorrespondenceEmailRoute(propertyOwnershipId) +
            "/${CorrespondenceEmailStep.ROUTE_SEGMENT}"
    override val formContent = "correspondenceEmailOption=ACCOUNT_EMAIL" // pragma: allowlist secret

    @BeforeEach
    fun enableFeatureFlag() {
        whenever(featureFlagManager.checkFeature(CORRESPONDENCE_ADDRESS)).thenReturn(true)
    }

    override fun stubCreateJourneySteps() {
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(CorrespondenceEmailStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `GET is unavailable when the correspondence feature is disabled`() {
        whenever(featureFlagManager.checkFeature(CORRESPONDENCE_ADDRESS)).thenReturn(false)

        mvc.get(updateStepRoute).andExpect { status { isNotFound() } }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `POST is unavailable when the correspondence feature is disabled`() {
        whenever(featureFlagManager.checkFeature(CORRESPONDENCE_ADDRESS)).thenReturn(false)

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect { status { isNotFound() } }
    }
}
