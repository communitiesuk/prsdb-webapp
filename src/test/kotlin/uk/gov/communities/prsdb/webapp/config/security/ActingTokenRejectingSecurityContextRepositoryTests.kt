package uk.gov.communities.prsdb.webapp.config.security

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.DeferredSecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.context.SecurityContextRepository
import uk.gov.communities.prsdb.webapp.constants.ROLE_INDIVIDUAL_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class ActingTokenRejectingSecurityContextRepositoryTests {
    private val delegate: SecurityContextRepository = mock()
    private val repository = ActingTokenRejectingSecurityContextRepository(delegate)
    private val request = MockHttpServletRequest()
    private val response = MockHttpServletResponse()

    @Test
    fun `saveContext throws and does not save a context holding an acting token`() {
        // Arrange
        val actingContext =
            SecurityContextImpl(
                ActingAsLandlordAuthenticationToken(LANDLORD_USER_ID, listOf(ROLE_INDIVIDUAL_LANDLORD), SYSTEM_OPERATOR_USER_ID),
            )

        // Act, Assert
        assertFailsWith<IllegalStateException> { repository.saveContext(actingContext, request, response) }
        verify(delegate, never()).saveContext(any(), any(), any())
    }

    @Test
    fun `saveContext saves any other context through the delegate`() {
        // Arrange
        val context = SecurityContextImpl(TestingAuthenticationToken(SYSTEM_OPERATOR_USER_ID, null, ROLE_SYSTEM_OPERATOR))

        // Act
        repository.saveContext(context, request, response)

        // Assert
        verify(delegate).saveContext(context, request, response)
    }

    @Test
    fun `loadDeferredContext returns the delegate's deferred context`() {
        // Arrange
        val deferredContext: DeferredSecurityContext = mock()
        whenever(delegate.loadDeferredContext(request)).thenReturn(deferredContext)

        // Act
        val result = repository.loadDeferredContext(request)

        // Assert
        assertSame(deferredContext, result)
    }

    companion object {
        private const val SYSTEM_OPERATOR_USER_ID = "system-operator-user-id"
        private const val LANDLORD_USER_ID = "landlord-user-id"
    }
}
