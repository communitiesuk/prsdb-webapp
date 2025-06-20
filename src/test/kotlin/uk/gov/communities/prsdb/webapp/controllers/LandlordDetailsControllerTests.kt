package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.mockito.Mock
import org.mockito.kotlin.argThat
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
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordDetailsUpdateJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordDetailsUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordDetailsUpdateStepId
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

    @MockitoBean
    private lateinit var landlordDetailsUpdateJourneyFactory: LandlordDetailsUpdateJourneyFactory

    @Mock
    private lateinit var landlordDetailsUpdateJourney: LandlordDetailsUpdateJourney

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
    inner class LandlordDetailsUpdateStepTests {
        private val updateLandlordEmailPath =
            "${LandlordDetailsController.UPDATE_ROUTE}/${LandlordDetailsUpdateStepId.UpdateEmail.urlPathSegment}"

        @BeforeEach
        fun setUp() {
            whenever(
                landlordDetailsUpdateJourneyFactory.create(
                    "user",
                    LandlordDetailsUpdateStepId.UpdateEmail.urlPathSegment,
                ),
            ).thenReturn(landlordDetailsUpdateJourney)
        }

        @Test
        fun `getJourneyStep returns a redirect for an unauthenticated user`() {
            mvc.get(updateLandlordEmailPath).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getJourneyStep returns 403 for an unauthorized user`() {
            mvc.get(updateLandlordEmailPath).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getJourneyStep returns 200 for a valid request from a landlord`() {
            mvc.get(updateLandlordEmailPath).andExpect {
                status { isOk() }
            }
        }

        @Test
        fun `postJourneyData returns a redirect for an unauthenticated user`() {
            mvc
                .post(updateLandlordEmailPath) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "email=newEmail@example.com"
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        @WithMockUser
        fun `postJourneyData returns 403 for an unauthorized user`() {
            mvc
                .post(updateLandlordEmailPath) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "email=newEmail@example.com"
                    with(csrf())
                }.andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `postJourneyData redirects to the details page for a valid request from a landlord`() {
            whenever(
                landlordDetailsUpdateJourney.completeStep(
                    argThat { pageData -> pageData["email"] == "newEmail@example.com" },
                    argThat { principal -> principal.name == "user" },
                ),
            ).thenReturn(ModelAndView("redirect:${LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE}"))

            mvc
                .post(updateLandlordEmailPath) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "email=newEmail@example.com"
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE)
                }
        }
    }

    @Nested
    inner class GetLandlordDetailsAsLaUserTests {
        private val landlord = MockLandlordData.createLandlord()

        @BeforeEach
        fun setUp() {
            whenever(landlordService.retrieveLandlordById(landlord.id)).thenReturn(landlord)
            whenever(propertyOwnershipService.getRegisteredPropertiesForLandlord(landlord.id)).thenReturn(emptyList())
        }

        @Test
        fun `getLandlordDetails returns a redirect for an unauthenticated user`() {
            mvc.get(LandlordDetailsController.getLandlordDetailsPath(landlord.id)).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getLandlordDetails returns 403 for an unauthorized user`() {
            mvc.get(LandlordDetailsController.getLandlordDetailsPath(landlord.id)).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_USER"])
        fun `getLandlordDetails returns 200 for a valid request from an LA user`() {
            mvc.get(LandlordDetailsController.getLandlordDetailsPath(landlord.id)).andExpect {
                status { isOk() }
                model { attribute("name", landlord.name) }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `getLandlordDetails returns 200 for a valid request from an LA admin`() {
            mvc.get(LandlordDetailsController.getLandlordDetailsPath(landlord.id)).andExpect {
                status { isOk() }
                model { attribute("name", landlord.name) }
            }
        }
    }
}
