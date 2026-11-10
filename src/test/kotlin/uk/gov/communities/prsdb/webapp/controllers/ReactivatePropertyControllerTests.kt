package uk.gov.communities.prsdb.webapp.controllers

import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.util.ResourceUtils
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REACTIVATE_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController.Companion.getPropertyReactivationBasePath
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController.Companion.getPropertyReactivationPath
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.helpers.CertificateUploadHelper
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasCertStep
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@WebMvcTest(ReactivatePropertyController::class)
class ReactivatePropertyControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var propertyReactivationJourneyFactory: PropertyReactivationJourneyFactory

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    private lateinit var certificateUploadHelper: CertificateUploadHelper

    private val propertyOwnershipId = 1L
    private val placeholderModelAndView = ModelAndView("placeholder", mapOf("title" to "placeholder"))

    @BeforeEach
    fun setUp() {
        whenever(propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
            .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyOwnershipId, isActive = false))
    }

    private fun mockJourneySteps() {
        whenever(propertyReactivationJourneyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(ReactivationInfoStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
    }

    private fun mockPropertyCannotBeReactivated() {
        whenever(propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
            .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))
    }

    @Test
    fun `getPropertyReactivationPath returns a path to the reactivation info step`() {
        assertEquals(
            "/$LANDLORD_PATH_SEGMENT/$REACTIVATE_PROPERTY_JOURNEY_URL/$propertyOwnershipId/${ReactivationInfoStep.ROUTE_SEGMENT}",
            getPropertyReactivationPath(propertyOwnershipId),
        )
    }

    @Test
    fun `getJourneyStep returns a redirect for an unauthenticated user`() {
        mvc.get(getPropertyReactivationPath(propertyOwnershipId)).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns 403 for a user who is not a landlord`() {
        mvc.get(getPropertyReactivationPath(propertyOwnershipId)).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `getJourneyStep returns 404 when the property cannot be reactivated by the current user`() {
        mockPropertyCannotBeReactivated()

        mvc.get(getPropertyReactivationPath(propertyOwnershipId)).andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `getJourneyStep returns 200 for a landlord of the inactive property`() {
        mockJourneySteps()
        whenever(mockStepLifecycleOrchestrator.getStepModelAndView()).thenReturn(placeholderModelAndView)

        mvc.get(getPropertyReactivationPath(propertyOwnershipId)).andExpect {
            status { isOk() }
        }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `getJourneyStep redirects to initialise the journey when no journey state exists`() {
        val journeyId = "test-journey-id"
        whenever(propertyReactivationJourneyFactory.createJourneySteps(propertyOwnershipId)).thenThrow(NoSuchJourneyException())
        whenever(propertyReactivationJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc.get(getPropertyReactivationPath(propertyOwnershipId)).andExpect {
            status { is3xxRedirection() }
            redirectedUrl("${getPropertyReactivationPath(propertyOwnershipId)}?journeyId=$journeyId")
        }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `getJourneyStep redirects to initialise the journey when the property ownership does not match`() {
        val journeyId = "test-journey-id"
        whenever(propertyReactivationJourneyFactory.createJourneySteps(propertyOwnershipId))
            .thenThrow(PropertyOwnershipMismatchException("mismatch"))
        whenever(propertyReactivationJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc.get(getPropertyReactivationPath(propertyOwnershipId)).andExpect {
            status { is3xxRedirection() }
            redirectedUrl("${getPropertyReactivationPath(propertyOwnershipId)}?journeyId=$journeyId")
        }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `postJourneyData returns 404 when the property cannot be reactivated by the current user`() {
        mockPropertyCannotBeReactivated()

        mvc
            .post(getPropertyReactivationPath(propertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `postJourneyData returns the step's model and view for a landlord of the inactive property`() {
        mockJourneySteps()
        whenever(mockStepLifecycleOrchestrator.postStepModelAndView(any())).thenReturn(placeholderModelAndView)

        mvc
            .post(getPropertyReactivationPath(propertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `postFileUploadJourneyData returns 404 without uploading when the property cannot be reactivated by the current user`() {
        mockPropertyCannotBeReactivated()
        val httpEntity =
            MultipartEntityBuilder
                .create()
                .addTextBody("_csrf", "any-csrf-token")
                .addBinaryBody("certificate", ResourceUtils.getFile("classpath:data/certificates/validFile.png"))
                .build()

        mvc
            .post("${getPropertyReactivationBasePath(propertyOwnershipId)}/${UploadGasCertStep.ROUTE_SEGMENT}?journeyId=any-id") {
                contentType = MediaType.parseMediaType(httpEntity.contentType)
                content = httpEntity.content.readAllBytes()
                with(csrf().asHeader())
                cookie(Cookie(FILE_UPLOAD_COOKIE_NAME, "valid-token"))
            }.andExpect {
                status { isNotFound() }
            }

        verifyNoInteractions(certificateUploadHelper)
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `getConfirmation returns the placeholder confirmation page for a landlord of the inactive property`() {
        mvc.get("${getPropertyReactivationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT").andExpect {
            status { isOk() }
            view { name("forms/todoConfirmation") }
            model { attribute("todoComment", containsString("PDJB-2044")) }
        }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `getConfirmation returns 404 when the property cannot be reactivated by the current user`() {
        mockPropertyCannotBeReactivated()

        mvc.get("${getPropertyReactivationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT").andExpect {
            status { isNotFound() }
        }
    }
}
