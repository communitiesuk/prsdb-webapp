package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.journeys.JourneyIdProvider
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.epc.UpdateEpcJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@WebMvcTest(UpdateEpcController::class)
class UpdateEpcControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BasePropertyDetailsUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateEpcJourneyFactory

    @MockitoBean
    override lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val propertyOwnershipId = 1L

    override val updateStepRoute =
        UpdateEpcController.getUpdateEpcRoute(propertyOwnershipId) + "/${HasEpcStep.ROUTE_SEGMENT}"

    override val formContent = "hasCert=true"

    override fun stubCreateJourneySteps() {
        whenever(
            journeyFactory.createJourneySteps(
                propertyOwnershipId,
                PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId),
            ),
        )
            .thenReturn(
                mapOf(
                    HasEpcStep.ROUTE_SEGMENT to stepLifecycleOrchestrator,
                ),
            )
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = LANDLORD_USER)
    fun `getUpdateStep initializes a missing journey with the property and landlord`() {
        whenever(
            journeyFactory.createJourneySteps(
                propertyOwnershipId,
                PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId),
            ),
        ).thenThrow(NoSuchJourneyException())
        whenever(journeyFactory.initializeJourneyState(any())).thenReturn("journey-id")

        mvc.get(updateStepRoute).andExpect {
            status { is3xxRedirection() }
            redirectedUrl("$updateStepRoute?${JourneyIdProvider.PARAMETER_NAME}=journey-id")
        }

        verify(journeyFactory).initializeJourneyState(
            argThat {
                this is Pair<*, *> &&
                    first == propertyOwnershipId &&
                    (second as? Principal)?.name == LANDLORD_USER
            },
        )
    }
}
