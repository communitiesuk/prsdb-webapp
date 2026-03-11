package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.Test

@WebMvcTest(LandlordDetailsController::class)
class LandlordDetailsControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var landlordService: LandlordService

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Nested
    inner class GetUserLandlordDetailsTests {
        @Test
        fun `getUserLandlordDetails returns a redirect for an unauthenticated user`() {
            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getUserLandlordDetails returns 403 for an unauthorized user`() {
            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getUserLandlordDetails returns 200 for a valid request from a landlord`() {
            val landlord = MockLandlordData.createLandlord()
            whenever(landlordService.retrieveLandlordByBaseUserId("user")).thenReturn(landlord)
            whenever(propertyOwnershipService.getRegisteredPropertiesForLandlordUser("user")).thenReturn(emptyList())

            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { isOk() }
                model { attribute("name", landlord.name) }
            }
        }
    }

    @Nested
    inner class GetLandlordDetailsAsLcUserTests {
        private val landlord = MockLandlordData.createLandlord()

        @BeforeEach
        fun setUp() {
            whenever(landlordService.retrieveLandlordById(landlord.id)).thenReturn(landlord)
            whenever(propertyOwnershipService.getRegisteredPropertiesForLandlord(landlord.id)).thenReturn(emptyList())
        }

        @Test
        fun `getLandlordDetails returns a redirect for an unauthenticated user`() {
            mvc.get(LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(landlord.id)).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getLandlordDetails returns 403 for an unauthorized user`() {
            mvc.get(LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(landlord.id)).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getLandlordDetails returns 200 for a valid request from an LC user`() {
            mvc.get(LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(landlord.id)).andExpect {
                status { isOk() }
                model { attribute("name", landlord.name) }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `getLandlordDetails returns 200 for a valid request from an LC admin`() {
            mvc.get(LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(landlord.id)).andExpect {
                status { isOk() }
                model { attribute("name", landlord.name) }
            }
        }
    }
}
