package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.config.security.ActingAsLandlordAuthenticationToken
import uk.gov.communities.prsdb.webapp.constants.ACTING_AS_USER_ID
import uk.gov.communities.prsdb.webapp.constants.ROLE_INDIVIDUAL_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR

class ActingAsLandlordServiceTests {
    private lateinit var session: MockHttpSession
    private lateinit var actingAsLandlordService: ActingAsLandlordService

    @BeforeEach
    fun setup() {
        session = MockHttpSession()
        actingAsLandlordService = ActingAsLandlordService(session)
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `getActingAsUserId returns the user ID stored in the session`() {
        // Arrange
        session.setAttribute(ACTING_AS_USER_ID, "landlord-user-id")

        // Act, Assert
        assertEquals("landlord-user-id", actingAsLandlordService.getActingAsUserId())
    }

    @Test
    fun `getActingAsUserId returns null when the session holds no acting as user ID`() {
        // Act, Assert
        assertNull(actingAsLandlordService.getActingAsUserId())
    }

    @Test
    fun `isActingAsLandlord returns true when the session holds an acting as user ID`() {
        // Arrange
        session.setAttribute(ACTING_AS_USER_ID, "landlord-user-id")

        // Act, Assert
        assertTrue(actingAsLandlordService.isActingAsLandlord())
    }

    @Test
    fun `isActingAsLandlord returns false when the session holds no acting as user ID`() {
        // Act, Assert
        assertFalse(actingAsLandlordService.isActingAsLandlord())
    }

    @Test
    fun `setActingAsUserId stores the user ID in the session`() {
        // Act
        actingAsLandlordService.setActingAsUserId("landlord-user-id")

        // Assert
        assertEquals("landlord-user-id", session.getAttribute(ACTING_AS_USER_ID))
    }

    @Test
    fun `clearActingAsUserId removes only the acting as user ID from the session`() {
        // Arrange
        session.setAttribute(ACTING_AS_USER_ID, "landlord-user-id")
        session.setAttribute("otherAttribute", "other value")

        // Act
        actingAsLandlordService.clearActingAsUserId()

        // Assert
        assertNull(session.getAttribute(ACTING_AS_USER_ID))
        assertEquals("other value", session.getAttribute("otherAttribute"))
    }

    @Test
    fun `getActingSystemOperatorId returns the system operator ID from an acting as landlord token`() {
        // Arrange
        SecurityContextHolder.getContext().authentication =
            ActingAsLandlordAuthenticationToken(
                actingAsUserId = "landlord-user-id",
                landlordRoles = listOf(ROLE_INDIVIDUAL_LANDLORD),
                systemOperatorId = "system-operator-id",
            )

        // Act, Assert
        assertEquals("system-operator-id", actingAsLandlordService.getActingSystemOperatorId())
    }

    @Test
    fun `getActingSystemOperatorId returns null when the current authentication is not acting as a landlord`() {
        // Arrange
        SecurityContextHolder.getContext().authentication =
            TestingAuthenticationToken("system-operator-id", null, ROLE_SYSTEM_OPERATOR)

        // Act, Assert
        assertNull(actingAsLandlordService.getActingSystemOperatorId())
    }

    @Test
    fun `getActingSystemOperatorId returns null when there is no authentication`() {
        // Act, Assert
        assertNull(actingAsLandlordService.getActingSystemOperatorId())
    }
}
