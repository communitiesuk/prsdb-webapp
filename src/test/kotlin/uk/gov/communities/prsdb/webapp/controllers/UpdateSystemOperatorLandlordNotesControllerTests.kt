package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
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
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.controllers.UpdateSystemOperatorLandlordNotesController.Companion.getUpdateNotesStepPath
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.landlordNotes.SystemOperatorLandlordNotesStep
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.landlordNotes.UpdateSystemOperatorLandlordNotesJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LandlordService

@WebMvcTest(UpdateSystemOperatorLandlordNotesController::class)
class UpdateSystemOperatorLandlordNotesControllerTests(
    @Autowired webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateSystemOperatorLandlordNotesJourneyFactory

    @MockitoBean
    private lateinit var landlordService: LandlordService

    @MockitoBean
    private lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val landlordId = 1L
    private val updateStepRoute = getUpdateNotesStepPath(landlordId)
    private val formContent = "notes=Some notes"

    @Test
    fun `getUpdateStep returns a redirect for unauthenticated user`() {
        mvc.get(updateStepRoute).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `getUpdateStep returns 403 for a landlord user`() {
        mvc.get(updateStepRoute).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
    fun `getUpdateStep returns 403 for a local council user`() {
        mvc.get(updateStepRoute).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `getUpdateStep returns 404 if the landlord does not exist when starting a new journey`() {
        stubNewJourneyForMissingLandlord()

        mvc.get(updateStepRoute).andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `getUpdateStep returns 200 for a system operator`() {
        stubJourneySteps()
        whenever(stepLifecycleOrchestrator.getStepModelAndView())
            .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc.get(updateStepRoute).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `postUpdateStep returns a redirect for unauthenticated user`() {
        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
    fun `postUpdateStep returns 403 for a local council user`() {
        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `postUpdateStep returns 404 if the landlord does not exist when starting a new journey`() {
        stubNewJourneyForMissingLandlord()

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `postUpdateStep redirects for a valid system operator request`() {
        val redirectUrl = LandlordDetailsController.getLandlordDetailsForSystemOperatorPath(landlordId)
        stubJourneySteps()
        whenever(stepLifecycleOrchestrator.postStepModelAndView(any()))
            .thenReturn(ModelAndView("redirect:$redirectUrl"))

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(redirectUrl)
            }
    }

    private fun stubJourneySteps() {
        whenever(journeyFactory.createJourneySteps(landlordId))
            .thenReturn(mapOf(SystemOperatorLandlordNotesStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    private fun stubNewJourneyForMissingLandlord() {
        whenever(journeyFactory.createJourneySteps(landlordId)).thenThrow(NoSuchJourneyException())
        whenever(landlordService.retrieveLandlordById(landlordId)).thenReturn(null)
    }
}
