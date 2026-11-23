package uk.gov.communities.prsdb.webapp.controllers

import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
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
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController.Companion.getPropertyReactivationBasePath
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController.Companion.getPropertyReactivationPath
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.helpers.CertificateUploadHelper
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasCertStep
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyReactivationService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
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

    @MockitoBean
    private lateinit var propertyReactivationService: PropertyReactivationService

    @MockitoBean
    private lateinit var userToLandlordService: UserToLandlordService

    private val propertyOwnershipId = 1L
    private val placeholderModelAndView = ModelAndView("placeholder", mapOf("title" to "placeholder"))

    private val confirmationPath = "${getPropertyReactivationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT"

    private val uploadGasCertPath =
        "${getPropertyReactivationBasePath(propertyOwnershipId)}/${UploadGasCertStep.ROUTE_SEGMENT}?journeyId=any-id"
    private val fileUploadCookie = Cookie(FILE_UPLOAD_COOKIE_NAME, "valid-token")
    private val fileUploadEntity =
        MultipartEntityBuilder
            .create()
            .addTextBody("_csrf", "any-csrf-token")
            .addBinaryBody("certificate", ResourceUtils.getFile("classpath:data/certificates/validFile.png"))
            .build()

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

        mvc
            .post(uploadGasCertPath) {
                contentType = MediaType.parseMediaType(fileUploadEntity.contentType)
                content = fileUploadEntity.content.readAllBytes()
                with(csrf().asHeader())
                cookie(fileUploadCookie)
            }.andExpect {
                status { isNotFound() }
            }

        verifyNoInteractions(certificateUploadHelper)
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `postFileUploadJourneyData returns 400 for a landlord of the inactive property without a cookie`() {
        mvc
            .post(uploadGasCertPath) {
                contentType = MediaType.parseMediaType(fileUploadEntity.contentType)
                content = fileUploadEntity.content.readAllBytes()
                with(csrf().asHeader())
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `postFileUploadJourneyData delegates to the certificate upload helper and redirects for a landlord of the inactive property`() {
        val redirectUrl = "any-url"
        whenever(propertyReactivationJourneyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(UploadGasCertStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(certificateUploadHelper.uploadFileAndReturnFormModel(any(), any(), any(), any()))
            .thenReturn(mapOf<String, Any>())
        whenever(mockStepLifecycleOrchestrator.postStepModelAndView(any()))
            .thenReturn(ModelAndView("redirect:$redirectUrl"))

        mvc
            .post(uploadGasCertPath) {
                contentType = MediaType.parseMediaType(fileUploadEntity.contentType)
                content = fileUploadEntity.content.readAllBytes()
                with(csrf().asHeader())
                cookie(fileUploadCookie)
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(redirectUrl)
            }

        verify(certificateUploadHelper).uploadFileAndReturnFormModel(any(), any(), eq(fileUploadCookie.value), any())
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `getConfirmation returns the reactivation confirmation page when the property was reactivated this session`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        whenever(propertyReactivationService.getLastPropertyOwnershipIdReactivatedThisSession()).thenReturn(propertyOwnershipId)
        whenever(propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(MockLandlordData.createIndividualLandlord())

        // Act, Assert
        mvc.get(confirmationPath).andExpect {
            status { isOk() }
            view { name("reactivatePropertyConfirmation") }
            model {
                attribute("addressParts", propertyOwnership.address.toMultiLineAddress().split("\n"))
                attribute("prn", RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString())
                attribute("hasJointLandlords", false)
                attribute("landlordReactivated", false)
                attribute("isOrganisationLandlord", false)
                attribute("landlordDashboardUrl", LANDLORD_DASHBOARD_URL)
                attribute("propertyDetailsUrl", PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId))
                attributeExists("paymentSummaryViewModel")
            }
        }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `getConfirmation shows joint landlords when the property has more than one landlord`() {
        // Arrange
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                landlords = mutableSetOf(MockLandlordData.createIndividualLandlord(), MockLandlordData.createIndividualLandlord()),
            )
        whenever(propertyReactivationService.getLastPropertyOwnershipIdReactivatedThisSession()).thenReturn(propertyOwnershipId)
        whenever(propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(MockLandlordData.createIndividualLandlord())

        // Act, Assert
        mvc.get(confirmationPath).andExpect {
            status { isOk() }
            model { attribute("hasJointLandlords", true) }
        }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `getConfirmation shows an individual landlord that their account was reactivated`() {
        // Arrange
        whenever(propertyReactivationService.getLastPropertyOwnershipIdReactivatedThisSession()).thenReturn(propertyOwnershipId)
        whenever(propertyReactivationService.wasLandlordReactivatedByLastReactivation()).thenReturn(true)
        whenever(propertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
            .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyOwnershipId))
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(MockLandlordData.createIndividualLandlord())

        // Act, Assert
        mvc.get(confirmationPath).andExpect {
            status { isOk() }
            model {
                attribute("landlordReactivated", true)
                attribute("isOrganisationLandlord", false)
            }
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"])
    fun `getConfirmation shows an organisation landlord that their organisation's account was reactivated`() {
        // Arrange
        whenever(propertyReactivationService.getLastPropertyOwnershipIdReactivatedThisSession()).thenReturn(propertyOwnershipId)
        whenever(propertyReactivationService.wasLandlordReactivatedByLastReactivation()).thenReturn(true)
        whenever(propertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
            .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyOwnershipId))
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(MockLandlordData.createOrgLandlord())

        // Act, Assert
        mvc.get(confirmationPath).andExpect {
            status { isOk() }
            model {
                attribute("landlordReactivated", true)
                attribute("isOrganisationLandlord", true)
            }
        }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `getConfirmation returns 404 when the property was not the last one reactivated this session`() {
        whenever(propertyReactivationService.getLastPropertyOwnershipIdReactivatedThisSession()).thenReturn(propertyOwnershipId + 1)

        mvc.get(confirmationPath).andExpect {
            status { isNotFound() }
        }
    }
}
