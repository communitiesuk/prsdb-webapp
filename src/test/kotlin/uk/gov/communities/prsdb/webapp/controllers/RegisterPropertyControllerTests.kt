package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_NUMBER
import uk.gov.communities.prsdb.webapp.constants.RESUME_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.START_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyRegistrationFormContext

@WebMvcTest(RegisterPropertyController::class)
class RegisterPropertyControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var propertyRegistrationJourneyFactory: PropertyRegistrationJourneyFactory

    @Mock
    private lateinit var propertyRegistrationJourney: PropertyRegistrationJourney

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var propertyRegistrationService: PropertyRegistrationService

    @MockitoBean
    private lateinit var journeyDataServiceFactory: JourneyDataServiceFactory

    @Mock
    private lateinit var journeyDataService: JourneyDataService

    @BeforeEach
    fun setupMocks() {
        whenever(propertyRegistrationJourneyFactory.create()).thenReturn(propertyRegistrationJourney)
        whenever(propertyRegistrationJourney.initialStepId).thenReturn(RegisterPropertyStepId.PlaceholderPage)
        whenever(journeyDataServiceFactory.create(any())).thenReturn(journeyDataService)
        whenever(journeyDataService.journeyDataKey).thenReturn(PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY)
    }

    @Test
    fun `index returns a redirect for unauthenticated user`() {
        mvc.get(RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `index returns 403 for an unauthorised user`() {
        mvc
            .get(RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `index returns 200 for a landlord user`() {
        mvc
            .get(RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 200 if a property has been registered`() {
        val propertyRegistrationNumber = 0L
        val propertyOwnership =
            createPropertyOwnership(
                registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, propertyRegistrationNumber),
            )

        whenever(propertyRegistrationService.getLastPrnRegisteredThisSession()).thenReturn(
            propertyRegistrationNumber,
        )
        whenever(propertyOwnershipService.retrievePropertyOwnership(propertyRegistrationNumber)).thenReturn(
            propertyOwnership,
        )

        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
                    .sessionAttr(PROPERTY_REGISTRATION_NUMBER, propertyRegistrationNumber),
            ).andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 400 if there's no property ownership ID in session`() {
        val propertyRegistrationNumber = 0L
        val propertyOwnership =
            createPropertyOwnership(
                registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, propertyRegistrationNumber),
            )

        whenever(propertyRegistrationService.getLastPrnRegisteredThisSession()).thenReturn(null)
        whenever(propertyOwnershipService.retrievePropertyOwnership(propertyRegistrationNumber)).thenReturn(
            propertyOwnership,
        )

        mvc
            .get("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect { status { isBadRequest() } }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 400 if the property ownership ID in session is not valid`() {
        val propertyRegistrationNumber = 0L

        whenever(propertyRegistrationService.getLastPrnRegisteredThisSession()).thenReturn(
            propertyRegistrationNumber,
        )
        whenever(propertyOwnershipService.retrievePropertyOwnership(propertyRegistrationNumber)).thenReturn(null)

        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
                    .sessionAttr(PROPERTY_REGISTRATION_NUMBER, propertyRegistrationNumber),
            ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getStart redirects to task-list after calling clear journey data method from propertyRegistrationService`() {
        mvc
            .get("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$START_PAGE_PATH_SEGMENT")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(TASK_LIST_PATH_SEGMENT)
            }

        verify(journeyDataService).removeJourneyDataAndContextIdFromSession()
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getResume redirects to task-list after calling load journey data method from propertyRegistrationService`() {
        val contextId = "1"
        val formContext = createPropertyRegistrationFormContext()
        whenever(
            propertyRegistrationService.getIncompletePropertyFormContextForLandlordIfNotExpired(contextId.toLong(), "user"),
        ).thenReturn(formContext)
        mvc
            .get("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$RESUME_PAGE_PATH_SEGMENT?contextId=$contextId")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(TASK_LIST_PATH_SEGMENT)
            }
        verify(propertyRegistrationService).getIncompletePropertyFormContextForLandlordIfNotExpired(contextId.toLong(), "user")
        verify(journeyDataService).loadJourneyDataIntoSession(formContext)
    }
}
