package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.freeholders.UpdateFreeholdersJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(UpdateFreeholdersController::class)
class UpdateFreeholdersControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BasePropertyDetailsUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateFreeholdersJourneyFactory

    @MockitoBean
    override lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    override val propertyOwnershipId = 1L

    override val updateStepRoute =
        UpdateFreeholdersController.getUpdateFreeholdersRoute(propertyOwnershipId) +
            "/${FreeholdersListStep.ROUTE_SEGMENT}"

    override val formContent = "freeholderId=1"

    // TODO PDJB-2016: Remove this setup and the featureFlagManager mock when the ASSOCIATED_PARTIES flag is removed.
    @BeforeEach
    fun enableAssociatedPartiesFeature() {
        whenever(featureFlagManager.checkFeature(ASSOCIATED_PARTIES)).thenReturn(true)
    }

    override fun stubCreateJourneySteps() {
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(FreeholdersListStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    // TODO PDJB-2016: Remove this nested class when the ASSOCIATED_PARTIES flag is removed.
    @Nested
    inner class BeforePdjb955AssociatedParties {
        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getUpdateStep returns 404 when associated parties feature is disabled`() {
            // Arrange
            whenever(featureFlagManager.checkFeature(ASSOCIATED_PARTIES)).thenReturn(false)

            // Act
            val result = mvc.get(updateStepRoute)

            // Assert
            result.andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `postUpdateStep returns 404 when associated parties feature is disabled`() {
            // Arrange
            whenever(featureFlagManager.checkFeature(ASSOCIATED_PARTIES)).thenReturn(false)

            // Act
            val result =
                mvc.post(updateStepRoute) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = formContent
                    with(csrf())
                }

            // Assert
            result.andExpect {
                status { isNotFound() }
            }
        }
    }
}
