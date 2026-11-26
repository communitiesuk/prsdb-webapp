package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.ACTING_AS_USER_ID
import uk.gov.communities.prsdb.webapp.controllers.ActingAsLandlordController.Companion.STOP_ACTING_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ActingAsLandlordController.Companion.getActAsLandlordPath
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.ACTING_AS_LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.SystemOperatorRepository
import uk.gov.communities.prsdb.webapp.services.ActingAsLandlordService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPrsdbUser
import java.util.Optional

@WebMvcTest(ActingAsLandlordController::class)
@Import(ActingAsLandlordService::class, SecurityContextService::class)
class ActingAsLandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var landlordService: LandlordService

    @MockitoBean
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    @MockitoBean
    private lateinit var systemOperatorRepository: SystemOperatorRepository

    private val landlord = createIndividualLandlord(baseUser = createPrsdbUser(LANDLORD_USER_ID))

    @BeforeEach
    fun setUp() {
        whenever(landlordService.retrieveLandlordById(landlord.id)).thenReturn(landlord)
    }

    @Test
    fun `act-as redirects an unauthenticated user to sign in`() {
        mvc.post(getActAsLandlordPath(landlord.id)) { with(csrf()) }.andExpect { status { is3xxRedirection() } }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `act-as returns 403 for a user who is not a system operator`() {
        mvc.post(getActAsLandlordPath(landlord.id)) { with(csrf()) }.andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `act-as starts a new session holding the landlord's user ID and redirects to the acting dashboard`() {
        // Arrange
        val oldSession = MockHttpSession().apply { setAttribute(STALE_ATTRIBUTE, "stale") }

        // Act
        val result =
            mvc
                .post(getActAsLandlordPath(landlord.id)) {
                    with(csrf())
                    session = oldSession
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(ACTING_AS_LANDLORD_DASHBOARD_URL)
                }.andReturn()

        // Assert
        val newSession = result.request.getSession(false)!!
        assertTrue(oldSession.isInvalid)
        assertNotEquals(oldSession.id, newSession.id)
        assertEquals(LANDLORD_USER_ID, newSession.getAttribute(ACTING_AS_USER_ID))
        assertNull(newSession.getAttribute(STALE_ATTRIBUTE))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `act-as with a member ID acts as that organisation member`() {
        // Arrange
        val orgLandlord = createOrgLandlord()
        val member =
            createOrganisationalLandlordUser(
                baseUser = createPrsdbUser(MEMBER_USER_ID),
                organisationalLandlord = orgLandlord,
            )
        whenever(landlordService.retrieveLandlordById(orgLandlord.id)).thenReturn(orgLandlord)
        whenever(organisationalLandlordUserRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member))

        // Act
        val result =
            mvc
                .post(getActAsLandlordPath(orgLandlord.id, MEMBER_ID)) { with(csrf()) }
                .andExpect { status { is3xxRedirection() } }
                .andReturn()

        // Assert
        assertEquals(MEMBER_USER_ID, result.request.getSession(false)!!.getAttribute(ACTING_AS_USER_ID))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `act-as returns the rejection status and leaves the session alone when the landlord is not found`() {
        // Arrange
        val oldSession = MockHttpSession()

        // Act
        mvc
            .post(getActAsLandlordPath(UNKNOWN_LANDLORD_ID)) {
                with(csrf())
                session = oldSession
            }.andExpect { status { isNotFound() } }

        // Assert
        assertFalse(oldSession.isInvalid)
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `stop-acting starts a new session without the acting user ID and redirects to the system operator dashboard`() {
        // Arrange
        val oldSession = MockHttpSession().apply { setAttribute(ACTING_AS_USER_ID, LANDLORD_USER_ID) }

        // Act
        val result =
            mvc
                .post(STOP_ACTING_ROUTE) {
                    with(csrf())
                    session = oldSession
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(SYSTEM_OPERATOR_DASHBOARD_URL)
                }.andReturn()

        // Assert
        assertTrue(oldSession.isInvalid)
        assertNull(result.request.getSession(false)!!.getAttribute(ACTING_AS_USER_ID))
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `stop-acting returns 403 for a user who is not a system operator`() {
        mvc.post(STOP_ACTING_ROUTE) { with(csrf()) }.andExpect { status { isForbidden() } }
    }

    companion object {
        private const val LANDLORD_USER_ID = "landlord-user-id"
        private const val MEMBER_USER_ID = "member-user-id"
        private const val MEMBER_ID = 7L
        private const val UNKNOWN_LANDLORD_ID = 999L
        private const val STALE_ATTRIBUTE = "staleAttribute"
    }
}
