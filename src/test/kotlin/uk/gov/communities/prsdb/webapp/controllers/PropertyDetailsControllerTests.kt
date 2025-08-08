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
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDetailsUpdateJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDetailsUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.PropertyComplianceViewModelFactory
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import kotlin.test.Test

@WebMvcTest(PropertyDetailsController::class)
class PropertyDetailsControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var propertyDetailsUpdateJourneyFactory: PropertyDetailsUpdateJourneyFactory

    @Mock
    private lateinit var propertyDetailsUpdateJourney: PropertyDetailsUpdateJourney

    @MockitoBean
    private lateinit var propertyComplianceService: PropertyComplianceService

    @MockitoBean
    private lateinit var viewModelFactory: PropertyComplianceViewModelFactory

    @Nested
    inner class GetPropertyDetailsLandlordViewTests {
        @Test
        fun `getPropertyDetails returns a redirect for an unauthenticated user`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLaView = false)).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getPropertyDetails returns 403 for an unauthorized user`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLaView = false)).andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `getPropertyDetails returns 403 for an unauthorized user with la admin role`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLaView = false)).andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_USER"])
        fun `getPropertyDetails returns 403 for an unauthorized user with la user role`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLaView = false)).andExpect {
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

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLaView = false)).andExpect {
                status { status { isOk() } }
            }
        }
    }

    @Nested
    inner class GetPropertyDetailsLaViewTests {
        @Test
        fun `getPropertyDetailsLaView returns a redirect for an unauthenticated user`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLaView = true)).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getPropertyDetailsLaView returns 403 for an unauthorized user`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLaView = true)).andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetailsLaView returns 403 for an unauthorized user with only the landlord role`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLaView = true)).andExpect {
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

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLaView = true)).andExpect {
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

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLaView = true)).andExpect {
                status { status { isOk() } }
            }
        }
    }

    @Nested
    inner class PropertyDetailsUpdateStepTests {
        private val propertyOwnership = createPropertyOwnership()

        private val updatePropertyOwnershipTypePath =
            PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnership.id) +
                "/${UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment}"

        private val propertyDetailsPath = PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id)

        @BeforeEach
        fun setUp() {
            whenever(
                propertyDetailsUpdateJourneyFactory.create(
                    propertyOwnership.id,
                    UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment,
                    isCheckingAnswer = false,
                ),
            ).thenReturn(propertyDetailsUpdateJourney)
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
            whenever(propertyOwnershipService.getIsAuthorizedToEditRecord(eq(propertyOwnership.id), any())).thenReturn(true)

            mvc.get(updatePropertyOwnershipTypePath).andExpect {
                status { status { isOk() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 404 for an invalid request from a landlord`() {
            whenever(propertyOwnershipService.getIsAuthorizedToEditRecord(eq(propertyOwnership.id), any())).thenReturn(false)

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
        fun `postJourneyData redirects to the details page for a valid request from a landlord`() {
            whenever(propertyOwnershipService.getIsAuthorizedToEditRecord(eq(propertyOwnership.id), any())).thenReturn(true)

            whenever(
                propertyDetailsUpdateJourney.completeStep(
                    argThat { pageData -> pageData["ownershipType"] == "FREEHOLD" },
                    argThat { principal -> principal.name == "user" },
                    eq(null),
                ),
            ).thenReturn(ModelAndView("redirect:$propertyDetailsPath"))

            mvc
                .post(updatePropertyOwnershipTypePath) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "ownershipType=FREEHOLD"
                    with(csrf())
                }.andExpect {
                    status {
                        is3xxRedirection()
                        redirectedUrl(propertyDetailsPath)
                    }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postJourneyData returns 404 for an invalid request from a landlord`() {
            whenever(propertyOwnershipService.getIsAuthorizedToEditRecord(eq(propertyOwnership.id), any())).thenReturn(false)

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
