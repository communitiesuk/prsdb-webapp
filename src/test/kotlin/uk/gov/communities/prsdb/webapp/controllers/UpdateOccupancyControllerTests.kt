package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.time.Instant

@WebMvcTest(UpdateOccupancyController::class)
class UpdateOccupancyControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BasePropertyDetailsUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateOccupancyJourneyFactory

    @MockitoBean
    override lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val propertyOwnershipId = 1L

    override val updateStepRoute =
        UpdateOccupancyController.getUpdateOccupancyRoute(propertyOwnershipId) +
            "/${OccupiedStep.ROUTE_SEGMENT}"

    override val formContent = "occupied=true"

    override fun stubCreateJourneySteps() {
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(OccupiedStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = LANDLORD_USER)
    fun `getUpdateStep initializes the journey and redirects when no journey state exists`() {
        val currentLastModifiedDate = Instant.parse("2026-09-22T10:00:00Z")
        val journeyId = "occupancy-journey-id"

        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenThrow(NoSuchJourneyException())
        whenever(propertyOwnershipService.getLastModifiedDate(propertyOwnershipId))
            .thenReturn(currentLastModifiedDate)
        whenever(journeyFactory.initializeJourneyState(any(), eq(currentLastModifiedDate)))
            .thenReturn(journeyId)

        mvc.get(updateStepRoute).andExpect {
            status { is3xxRedirection() }
            redirectedUrl("$updateStepRoute?journeyId=$journeyId")
        }

        verify(journeyFactory).initializeJourneyState(any(), eq(currentLastModifiedDate))
    }
}
