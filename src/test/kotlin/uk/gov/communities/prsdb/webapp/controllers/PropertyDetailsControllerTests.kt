package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Nested
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
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
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails returns 404 if the requested property ownership is not found`() {
            mvc.get("/property-details/1").andExpect {
                status { status { isNotFound() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails returns error if the requested property ownership is inactive`() {
            whenever(propertyOwnershipService.retrievePropertyOwnershipById(1))
                .thenReturn(PropertyOwnership())
            mvc.get("/property-details/1").andExpect {
                status { status { isBadRequest() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails returns 200 for a valid request`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.retrievePropertyOwnershipById(1))
                .thenReturn(
                    propertyOwnership,
                )

            mvc.get("/property-details/1").andExpect {
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
        @WithMockUser(roles = ["LA_USER"])
        fun `getPropertyDetails returns 404 if the requested property ownership is not found`() {
            mvc.get("/local-authority/property-details/1").andExpect {
                status { status { isNotFound() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_USER"])
        fun `getPropertyDetails returns error if the requested property ownership is inactive`() {
            whenever(propertyOwnershipService.retrievePropertyOwnershipById(1))
                .thenReturn(PropertyOwnership())
            mvc.get("/local-authority/property-details/1").andExpect {
                status { status { isBadRequest() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_USER"])
        fun `getPropertyDetails returns 200 for a valid request from an LA user`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.retrievePropertyOwnershipById(1))
                .thenReturn(
                    propertyOwnership,
                )

            mvc.get("/local-authority/property-details/1").andExpect {
                status { status { isOk() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `getPropertyDetails returns 200 for a valid request from an LA admin`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.retrievePropertyOwnershipById(1))
                .thenReturn(
                    propertyOwnership,
                )

            mvc.get("/local-authority/property-details/1").andExpect {
                status { status { isOk() } }
            }
        }
    }
}
