package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.propertyManager.UpdatePropertyManagerJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(UpdatePropertyManagerController::class)
class UpdatePropertyManagerControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BasePropertyDetailsUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdatePropertyManagerJourneyFactory

    @MockitoBean
    override lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    // TODO PDJB-2016: Remove this mock and enableFeatureFlag when the ASSOCIATED_PARTIES flag is removed
    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    override val propertyOwnershipId = 1L

    override val updateStepRoute =
        UpdatePropertyManagerController.getUpdatePropertyManagerRoute(propertyOwnershipId) +
            "/${PropertyManagerStep.ROUTE_SEGMENT}"

    override val formContent = "usingPropertyManager=true"

    override fun stubCreateJourneySteps() {
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(PropertyManagerStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    @BeforeEach
    fun enableFeatureFlag() {
        whenever(featureFlagManager.checkFeature(ASSOCIATED_PARTIES)).thenReturn(true)
    }
}
