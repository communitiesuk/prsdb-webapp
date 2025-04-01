package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(PropertyComplianceController::class)
class PropertyComplianceControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var propertyComplianceJourneyFactory: PropertyComplianceJourneyFactory

    @Mock
    private lateinit var propertyComplianceJourney: PropertyComplianceJourney

    private val validPropertyOwnershipId = 1L
    private val validPropertyComplianceUrl = PropertyComplianceController.getPropertyCompliancePath(validPropertyOwnershipId)
    private val validPropertyComplianceTaskListUrl = "$validPropertyComplianceUrl/$TASK_LIST_PATH_SEGMENT"
    private val validPropertyComplianceJourneyStepUrl = "$validPropertyComplianceUrl/${PropertyComplianceStepId.GasSafety.urlPathSegment}"

    private val invalidPropertyOwnershipId = 2L
    private val invalidPropertyComplianceUrl = PropertyComplianceController.getPropertyCompliancePath(invalidPropertyOwnershipId)
    private val invalidPropertyComplianceTaskListUrl = "$invalidPropertyComplianceUrl/$TASK_LIST_PATH_SEGMENT"
    private val invalidPropertyComplianceJourneyStepUrl =
        "$invalidPropertyComplianceUrl/${PropertyComplianceStepId.GasSafety.urlPathSegment}"

    @BeforeEach
    fun setUp() {
        whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(validPropertyOwnershipId), any())).thenReturn(true)
        whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(invalidPropertyOwnershipId), any())).thenReturn(false)

        whenever(propertyComplianceJourneyFactory.create(validPropertyOwnershipId)).thenReturn(propertyComplianceJourney)
    }

    @Nested
    inner class Index {
        @Test
        fun `index returns a redirect for unauthenticated user`() {
            mvc.get(validPropertyComplianceUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `index returns 403 for an unauthorised user`() {
            mvc.get(validPropertyComplianceUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `index returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidPropertyComplianceUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `index returns 200 for a landlord user that does own the property`() {
            mvc.get(validPropertyComplianceUrl).andExpect {
                status { isOk() }
            }
        }
    }

    @Nested
    inner class GetTaskList {
        @Test
        fun `getTaskList returns a redirect for unauthenticated user`() {
            mvc.get(validPropertyComplianceTaskListUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getTaskList returns 403 for an unauthorised user`() {
            mvc.get(validPropertyComplianceTaskListUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getTaskList returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidPropertyComplianceTaskListUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getTaskList returns 200 for a landlord user that does own the property`() {
            mvc.get(validPropertyComplianceTaskListUrl).andExpect {
                status { isOk() }
            }
        }
    }

    @Nested
    inner class GetJourneyStep {
        @Test
        fun `getJourneyStep returns a redirect for unauthenticated user`() {
            mvc.get(validPropertyComplianceJourneyStepUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getJourneyStep returns 403 for an unauthorised user`() {
            mvc.get(validPropertyComplianceJourneyStepUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidPropertyComplianceJourneyStepUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 200 for a landlord user that does own the property`() {
            mvc.get(validPropertyComplianceJourneyStepUrl).andExpect {
                status { isOk() }
            }
        }
    }
}
