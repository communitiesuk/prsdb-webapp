package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Nested
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
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import kotlin.test.Test

@WebMvcTest(PropertyDetailsController::class)
class PropertyDetailsControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Nested
    inner class GetPropertyDetailsLandlordViewTests {
        @Test
        fun `getPropertyDetails returns a redirect for an unauthenticated user`() {
            mvc.get("/property-details/1").andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getPropertyDetails returns 403 for an unauthorized user`() {
            mvc.get("/property-details/1").andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `getPropertyDetails returns 403 for an unauthorized user with la admin role`() {
            mvc.get("/property-details/1").andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_USER"])
        fun `getPropertyDetails returns 403 for an unauthorized user with la user role`() {
            mvc.get("/property-details/1").andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails returns 200 for a valid request from a landlord`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(
                    propertyOwnership,
                )

            mvc.get("/property-details/${propertyOwnership.id}").andExpect {
                status { status { isOk() } }
            }
        }
    }

    @Nested
    inner class GetPropertyDetailsLaViewTests {
        @Test
        fun `getPropertyDetailsLaView returns a redirect for an unauthenticated user`() {
            mvc.get("/local-authority/property-details/1").andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getPropertyDetailsLaView returns 403 for an unauthorized user`() {
            mvc.get("/local-authority/property-details/1").andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetailsLaView returns 403 for an unauthorized user with only the landlord role`() {
            mvc.get("/local-authority/property-details/1").andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_USER"])
        fun `getPropertyDetailsLaView returns 200 for a valid request from an LA user`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(1), any()))
                .thenReturn(
                    propertyOwnership,
                )

            mvc.get("/local-authority/property-details/1").andExpect {
                status { status { isOk() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `getPropertyDetailsLaView returns 200 for a valid request from an LA admin`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(1), any()))
                .thenReturn(
                    propertyOwnership,
                )

            mvc.get("/local-authority/property-details/1").andExpect {
                status { status { isOk() } }
            }
        }
    }
}
