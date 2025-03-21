package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(controllers = [ProvideComplianceController::class])
class ProvideComplianceControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    private val validPropertyOwnershipId = 1L
    private val validProvideComplianceUrl = ProvideComplianceController.getProvideCompliancePath(validPropertyOwnershipId)

    private val invalidPropertyOwnershipId = 2L
    private val invalidProvideComplianceUrl = ProvideComplianceController.getProvideCompliancePath(invalidPropertyOwnershipId)

    @BeforeEach
    fun setUp() {
        whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(validPropertyOwnershipId), any())).thenReturn(true)
        whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(invalidPropertyOwnershipId), any())).thenReturn(false)
    }

    @Test
    fun `index returns a redirect for unauthenticated user`() {
        mvc.get(validProvideComplianceUrl).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `index returns 400 for an unauthorised user`() {
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
