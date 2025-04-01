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
import uk.gov.communities.prsdb.webapp.forms.journeys.ComplianceProvisionJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.ComplianceProvisionJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.ProvideComplianceStepId
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(ProvideComplianceController::class)
class ProvideComplianceControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var complianceProvisionJourneyFactory: ComplianceProvisionJourneyFactory

    @Mock
    private lateinit var complianceProvisionJourney: ComplianceProvisionJourney

    private val validPropertyOwnershipId = 1L
    private val validProvideComplianceUrl = ProvideComplianceController.getProvideCompliancePath(validPropertyOwnershipId)
    private val validProvideComplianceTaskListUrl = "$validProvideComplianceUrl/$TASK_LIST_PATH_SEGMENT"
    private val validProvideComplianceJourneyStepUrl = "$validProvideComplianceUrl/${ProvideComplianceStepId.GasSafety.urlPathSegment}"

    private val invalidPropertyOwnershipId = 2L
    private val invalidProvideComplianceUrl = ProvideComplianceController.getProvideCompliancePath(invalidPropertyOwnershipId)
    private val invalidProvideComplianceTaskListUrl = "$invalidProvideComplianceUrl/$TASK_LIST_PATH_SEGMENT"
    private val invalidProvideComplianceJourneyStepUrl = "$invalidProvideComplianceUrl/${ProvideComplianceStepId.GasSafety.urlPathSegment}"

    @BeforeEach
    fun setUp() {
        whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(validPropertyOwnershipId), any())).thenReturn(true)
        whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(invalidPropertyOwnershipId), any())).thenReturn(false)

        whenever(complianceProvisionJourneyFactory.create(validPropertyOwnershipId)).thenReturn(complianceProvisionJourney)
    }

    @Nested
    inner class Index {
        @Test
        fun `index returns a redirect for unauthenticated user`() {
            mvc.get(validProvideComplianceUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `index returns 403 for an unauthorised user`() {
            mvc.get(validProvideComplianceUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `index returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidProvideComplianceUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `index returns 200 for a landlord user that does own the property`() {
            mvc.get(validProvideComplianceUrl).andExpect {
                status { isOk() }
            }
        }
    }

    @Nested
    inner class GetTaskList {
        @Test
        fun `getTaskList returns a redirect for unauthenticated user`() {
            mvc.get(validProvideComplianceTaskListUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getTaskList returns 403 for an unauthorised user`() {
            mvc.get(validProvideComplianceTaskListUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getTaskList returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidProvideComplianceTaskListUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getTaskList returns 200 for a landlord user that does own the property`() {
            mvc.get(validProvideComplianceTaskListUrl).andExpect {
                status { isOk() }
            }
        }
    }

    @Nested
    inner class GetJourneyStep {
        @Test
        fun `getJourneyStep returns a redirect for unauthenticated user`() {
            mvc.get(validProvideComplianceJourneyStepUrl).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getJourneyStep returns 403 for an unauthorised user`() {
            mvc.get(validProvideComplianceJourneyStepUrl).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 404 for a landlord user that doesn't own the property`() {
            mvc.get(invalidProvideComplianceJourneyStepUrl).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 200 for a landlord user that does own the property`() {
            mvc.get(validProvideComplianceJourneyStepUrl).andExpect {
                status { isOk() }
            }
        }
    }
}
