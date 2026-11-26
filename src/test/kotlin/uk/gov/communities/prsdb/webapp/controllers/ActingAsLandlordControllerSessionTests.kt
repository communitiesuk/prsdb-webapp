package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
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
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.SystemOperatorRepository
import uk.gov.communities.prsdb.webapp.services.ActingAsLandlordService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPrsdbUser

// Uses the real services, which the unit tests mock, to check that the acting user ID ends up in the new session
// TODO PDJB-1911: Replace with an integration test of acting as a landlord
@WebMvcTest(ActingAsLandlordController::class)
@Import(ActingAsLandlordService::class, SecurityContextService::class)
class ActingAsLandlordControllerSessionTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var landlordService: LandlordService

    @MockitoBean
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    @MockitoBean
    private lateinit var systemOperatorRepository: SystemOperatorRepository

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `act-as starts a new session holding the landlord's user ID`() {
        // Arrange
        val landlord = createIndividualLandlord(baseUser = createPrsdbUser(LANDLORD_USER_ID))
        whenever(landlordService.retrieveLandlordById(landlord.id)).thenReturn(landlord)
        val oldSession = MockHttpSession().apply { setAttribute(STALE_ATTRIBUTE, "stale") }

        // Act
        val result =
            mvc
                .post(getActAsLandlordPath(landlord.id)) {
                    with(csrf())
                    session = oldSession
                }.andExpect { status { is3xxRedirection() } }
                .andReturn()

        // Assert
        val newSession = result.request.getSession(false)!!
        assertTrue(oldSession.isInvalid)
        assertNotEquals(oldSession.id, newSession.id)
        assertEquals(LANDLORD_USER_ID, newSession.getAttribute(ACTING_AS_USER_ID))
        assertNull(newSession.getAttribute(STALE_ATTRIBUTE))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `stop-acting starts a new session without the acting user ID`() {
        // Arrange
        val oldSession = MockHttpSession().apply { setAttribute(ACTING_AS_USER_ID, LANDLORD_USER_ID) }

        // Act
        val result =
            mvc
                .post(STOP_ACTING_ROUTE) {
                    with(csrf())
                    session = oldSession
                }.andExpect { status { is3xxRedirection() } }
                .andReturn()

        // Assert
        assertTrue(oldSession.isInvalid)
        assertNull(result.request.getSession(false)!!.getAttribute(ACTING_AS_USER_ID))
    }

    companion object {
        private const val LANDLORD_USER_ID = "landlord-user-id"
        private const val STALE_ATTRIBUTE = "staleAttribute"
    }
}
