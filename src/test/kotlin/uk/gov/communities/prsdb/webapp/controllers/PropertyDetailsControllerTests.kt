package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDetailsUpdateJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDetailsUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import kotlin.test.Test

@WebMvcTest(PropertyDetailsController::class)
class PropertyDetailsControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockBean
    private lateinit var propertyDetailsUpdateJourneyFactory: PropertyDetailsUpdateJourneyFactory

    @Mock
    private lateinit var propertyDetailsUpdateJourney: PropertyDetailsUpdateJourney

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

    @Nested
    inner class PropertyDetailsUpdateStepTests {
        private val propertyOwnership = createPropertyOwnership()

        private val updatePropertyDetailsPath =
            PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnership.id) +
                "/${UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment}"

        private val updatePropertyOwnershipTypePath =
            PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnership.id) +
                "/${UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment}"

        @BeforeEach
        fun setUp() {
            whenever(propertyDetailsUpdateJourneyFactory.create(propertyOwnership.id)).thenReturn(propertyDetailsUpdateJourney)
        }

        @Test
        fun `getUpdatePropertyDetails returns a redirect for an unauthenticated user`() {
            mvc.get(updatePropertyDetailsPath).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getUpdatePropertyDetails returns 403 for an unauthorized user`() {
            mvc.get(updatePropertyDetailsPath).andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getUpdatePropertyDetails returns 200 for a valid request from a landlord`() {
            whenever(
                propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()),
            ).thenReturn(propertyOwnership)

            mvc.get(updatePropertyDetailsPath).andExpect {
                status { status { isOk() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getUpdatePropertyDetails returns 404 for an invalid request from a landlord`() {
            whenever(
                propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()),
            ).thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))

            mvc.get(updatePropertyDetailsPath).andExpect {
                status { status { isNotFound() } }
            }
        }

        @Test
        fun `getJourneyStep returns a redirect for an unauthenticated user`() {
            mvc.get(updatePropertyOwnershipTypePath).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getJourneyStep returns 403 for an unauthorized user`() {
            mvc.get(updatePropertyOwnershipTypePath).andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 200 for a valid request from a landlord`() {
            whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(propertyOwnership.id), any())).thenReturn(true)

            mvc.get(updatePropertyOwnershipTypePath).andExpect {
                status { status { isOk() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 404 for an invalid request from a landlord`() {
            whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(propertyOwnership.id), any())).thenReturn(false)

            mvc.get(updatePropertyOwnershipTypePath).andExpect {
                status { status { isNotFound() } }
            }
        }

        @Test
        fun `postJourneyData returns a redirect for an unauthenticated user`() {
            mvc
                .post(updatePropertyOwnershipTypePath) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "ownershipType=FREEHOLD"
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        @WithMockUser
        fun `postJourneyData returns 403 for an unauthorized user`() {
            mvc
                .post(updatePropertyOwnershipTypePath) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "ownershipType=FREEHOLD"
                    with(csrf())
                }.andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postJourneyData redirects to the update details page for a valid request from a landlord`() {
            whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(propertyOwnership.id), any())).thenReturn(true)

            whenever(
                propertyDetailsUpdateJourney.completeStep(
                    eq(UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment),
                    argThat { pageData -> pageData["ownershipType"] == "FREEHOLD" },
                    eq(null),
                    any(),
                ),
            ).thenReturn(ModelAndView("redirect:$updatePropertyDetailsPath"))

            mvc
                .post(updatePropertyOwnershipTypePath) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "ownershipType=FREEHOLD"
                    with(csrf())
                }.andExpect {
                    status {
                        is3xxRedirection()
                        redirectedUrl(updatePropertyDetailsPath)
                    }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postJourneyData returns 404 for an invalid request from a landlord`() {
            whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(propertyOwnership.id), any())).thenReturn(false)

            mvc
                .post(updatePropertyOwnershipTypePath) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "ownershipType=FREEHOLD"
                    with(csrf())
                }.andExpect {
                    status { isNotFound() }
                }
        }
    }
}
