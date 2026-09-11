package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.PrsdbWebMvcRegistration
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.journeys.JourneyIdProvider
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.epc.UpdateEpcJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData
import java.util.UUID

@WebMvcTest(LettingAgentUpdateEpcController::class)
@Import(LettingAgentUpdateEpcControllerTests.FeatureFlagMappingConfiguration::class)
@ActiveProfiles(LettingAgentUpdateEpcControllerTests.FEATURE_FLAG_ROUTING_PROFILE)
class LettingAgentUpdateEpcControllerTests(
    @Autowired webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateEpcJourneyFactory

    @MockitoBean
    private lateinit var lettingAgentAccessService: LettingAgentAccessService

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    private val token = UUID.randomUUID()
    private val propertyOwnership = createOccupiedPropertyOwnership()
    private val returnUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
    private val updateStepRoute =
        LettingAgentUpdateEpcController.getUpdateEpcRoute(token) + "/${HasEpcStep.ROUTE_SEGMENT}"
    private val formContent = "hasCert=true"

    @BeforeEach
    fun enableFeatureFlag() {
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
    }

    private fun stubValidToken() {
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(token))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(token = token, propertyOwnership = propertyOwnership))
    }

    private fun stubJourneySteps() {
        stubValidToken()
        whenever(journeyFactory.createJourneySteps(propertyOwnership.id, returnUrl))
            .thenReturn(mapOf(HasEpcStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    @Test
    fun `getUpdateStep returns 200 for an editable delegated property`() {
        stubJourneySteps()
        whenever(stepLifecycleOrchestrator.getStepModelAndView())
            .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc.get(updateStepRoute).andExpect {
            status { isOk() }
        }

        verify(propertyOwnershipService).throwIfCurrentUserNotAuthorizedToEdit(propertyOwnership.id)
        verify(journeyFactory).createJourneySteps(propertyOwnership.id, returnUrl)
    }

    @Test
    fun `getUpdateStep initializes a missing journey with the delegation token`() {
        stubValidToken()
        whenever(journeyFactory.createJourneySteps(propertyOwnership.id, returnUrl))
            .thenThrow(NoSuchJourneyException())
        whenever(journeyFactory.initializeJourneyState(token)).thenReturn("journey-id")

        mvc.get(updateStepRoute).andExpect {
            status { is3xxRedirection() }
            redirectedUrl("$updateStepRoute?${JourneyIdProvider.PARAMETER_NAME}=journey-id")
        }

        verify(journeyFactory).initializeJourneyState(token)
    }

    @Test
    fun `getUpdateStep returns 404 when the token is not recognised`() {
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(token)).thenReturn(null)

        mvc.get(updateStepRoute).andExpect {
            status { isNotFound() }
        }

        verifyNoInteractions(propertyOwnershipService, journeyFactory, stepLifecycleOrchestrator)
    }

    @Test
    fun `getUpdateStep returns 404 when edit access is denied`() {
        stubValidToken()
        whenever(propertyOwnershipService.throwIfCurrentUserNotAuthorizedToEdit(propertyOwnership.id))
            .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))

        mvc.get(updateStepRoute).andExpect {
            status { isNotFound() }
        }

        verifyNoInteractions(journeyFactory, stepLifecycleOrchestrator)
    }

    @Test
    fun `getUpdateStep returns 404 when the feature flag is disabled`() {
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)

        mvc.get(updateStepRoute).andExpect {
            status { isNotFound() }
        }

        verifyNoInteractions(lettingAgentAccessService, propertyOwnershipService, journeyFactory, stepLifecycleOrchestrator)
    }

    @Test
    fun `postUpdateStep submits answers and returns to the agent property record`() {
        stubJourneySteps()
        whenever(stepLifecycleOrchestrator.postStepModelAndView(any()))
            .thenReturn(ModelAndView("redirect:$returnUrl"))

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(returnUrl)
            }

        verify(propertyOwnershipService).throwIfCurrentUserNotAuthorizedToEdit(propertyOwnership.id)
        verify(journeyFactory).createJourneySteps(propertyOwnership.id, returnUrl)
        verify(stepLifecycleOrchestrator).postStepModelAndView(argThat { this["hasCert"] == "true" })
    }

    @Test
    fun `postUpdateStep initializes a missing journey with the delegation token`() {
        stubValidToken()
        whenever(journeyFactory.createJourneySteps(propertyOwnership.id, returnUrl))
            .thenThrow(NoSuchJourneyException())
        whenever(journeyFactory.initializeJourneyState(token)).thenReturn("journey-id")

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl("$updateStepRoute?${JourneyIdProvider.PARAMETER_NAME}=journey-id")
            }

        verify(journeyFactory).initializeJourneyState(token)
    }

    @Test
    fun `postUpdateStep returns 404 when the token is not recognised`() {
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(token)).thenReturn(null)

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }

        verifyNoInteractions(propertyOwnershipService, journeyFactory, stepLifecycleOrchestrator)
    }

    @Test
    fun `postUpdateStep returns 404 when edit access is denied`() {
        stubValidToken()
        whenever(propertyOwnershipService.throwIfCurrentUserNotAuthorizedToEdit(propertyOwnership.id))
            .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }

        verifyNoInteractions(journeyFactory, stepLifecycleOrchestrator)
    }

    @Test
    fun `postUpdateStep returns 404 when the feature flag is disabled`() {
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }

        verifyNoInteractions(lettingAgentAccessService, propertyOwnershipService, journeyFactory, stepLifecycleOrchestrator)
    }

    @Test
    fun `postUpdateStep rejects submissions without a CSRF token`() {
        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
            }.andExpect {
                status { isForbidden() }
            }

        verifyNoInteractions(lettingAgentAccessService, propertyOwnershipService, journeyFactory, stepLifecycleOrchestrator)
    }

    // FeatureFlagConfig declares an unfiltered @ComponentScan over uk.gov.communities.prsdb.webapp, which bypasses
    // Spring Boot's TypeExcludeFilter and so also scans @TestConfiguration classes from the test classpath. The
    // profile guard keeps this configuration out of every other test context (see PrsdbTaskApplicationTests).
    @Profile(FEATURE_FLAG_ROUTING_PROFILE)
    @TestConfiguration(proxyBeanMethods = false)
    class FeatureFlagMappingConfiguration {
        @Bean
        @Primary
        fun featureFlagWebMvcRegistrations(featureFlagManager: FeatureFlagManager): WebMvcRegistrations =
            // ControllerTest mocks PrsdbWebMvcRegistration; keep real flag-aware routing in this slice.
            object : WebMvcRegistrations {
                override fun getRequestMappingHandlerMapping() =
                    PrsdbWebMvcRegistration(featureFlagManager).getRequestMappingHandlerMapping()
            }
    }

    companion object {
        const val FEATURE_FLAG_ROUTING_PROFILE = "feature-flag-routing"
    }
}
