package uk.gov.communities.prsdb.webapp.journeys

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import kotlin.test.assertEquals

class JourneyStatePersistenceServiceTests {
    @Test
    fun `saveJourneyStateData saves a serialized journey state if that journey does not exist`() {
        // Arrange
        val userName = "test-user-without-journey"
        val testJourneyId = "journey-123"

        setSecurityContextWithUser(userName)

        val mockOneLoginUserRepository = mock<OneLoginUserRepository>()
        val oneLoginUser = OneLoginUser()
        whenever(mockOneLoginUserRepository.getReferenceById(userName)).thenReturn(oneLoginUser)

        val mockJourneyRepository = mock<SavedJourneyStateRepository>()
        whenever(
            mockJourneyRepository.findByJourneyIdAndUser_Id(
                journeyId = testJourneyId,
                principalName = userName,
            ),
        ).thenReturn(null)
        whenever(mockJourneyRepository.save(any())).thenAnswer { it.arguments[0] }

        val realObjectMapper = ObjectMapper()

        val underTest =
            JourneyStatePersistenceService(
                journeyRepository = mockJourneyRepository,
                oneLoginUserRepository = mockOneLoginUserRepository,
                objectMapper = realObjectMapper,
            )

        // Act
        underTest.saveJourneyStateData(stateData = mapOf("key" to "value"), journeyId = testJourneyId)
        val stateCaptor = captor<SavedJourneyState>()
        verify(mockJourneyRepository).save(stateCaptor.capture())

        // Assert
        stateCaptor.value.apply {
            assertEquals(journeyId, testJourneyId)
            assertEquals(user, oneLoginUser)
            assertEquals(realObjectMapper.readValue(this.serializedState, Any::class.java), mapOf("key" to "value"))
        }
    }

    @Test
    fun `saveJourneyStateData updates a serialized journey state if that journey already exists`() {
        // Arrange
        val userName = "test-user-with-journey"
        val testJourneyId = "journey-123"

        setSecurityContextWithUser(userName)

        val mockOneLoginUserRepository = mock<OneLoginUserRepository>()
        val oneLoginUser = OneLoginUser()
        whenever(mockOneLoginUserRepository.getReferenceById(userName)).thenReturn(oneLoginUser)

        val mockJourneyRepository = mock<SavedJourneyStateRepository>()
        val existingJourneyState =
            SavedJourneyState(
                serializedState = """{"oldKey":"oldValue"}""",
                user = oneLoginUser,
                journeyId = testJourneyId,
            )
        whenever(
            mockJourneyRepository.findByJourneyIdAndUser_Id(
                journeyId = testJourneyId,
                principalName = userName,
            ),
        ).thenReturn(existingJourneyState)
        whenever(mockJourneyRepository.save(any())).thenAnswer { it.arguments[0] }

        val realObjectMapper = ObjectMapper()

        val underTest =
            JourneyStatePersistenceService(
                journeyRepository = mockJourneyRepository,
                oneLoginUserRepository = mockOneLoginUserRepository,
                objectMapper = realObjectMapper,
            )

        // Act
        underTest.saveJourneyStateData(stateData = mapOf("key" to "value"), journeyId = testJourneyId)
        val stateCaptor = captor<SavedJourneyState>()
        verify(mockJourneyRepository).save(stateCaptor.capture())

        // Assert
        stateCaptor.value.apply {
            assertEquals(journeyId, testJourneyId)
            assertEquals(user, oneLoginUser)
            assertEquals(realObjectMapper.readValue(this.serializedState, Any::class.java), mapOf("key" to "value"))
        }
    }

    @Test
    fun `retrieveJourneyStateData retrieves and deserializes a saved journey state`() {
        // Arrange
        val userName = "test-user-with-journey"
        val testJourneyId = "journey-123"

        setSecurityContextWithUser(userName)

        val mockOneLoginUserRepository = mock<OneLoginUserRepository>()
        val oneLoginUser = OneLoginUser()
        whenever(mockOneLoginUserRepository.getReferenceById(userName)).thenReturn(oneLoginUser)

        val mockJourneyRepository = mock<SavedJourneyStateRepository>()
        val existingJourneyState =
            SavedJourneyState(
                serializedState = """{"key":"value"}""",
                user = oneLoginUser,
                journeyId = testJourneyId,
            )
        whenever(
            mockJourneyRepository.findByJourneyIdAndUser_Id(
                journeyId = testJourneyId,
                principalName = userName,
            ),
        ).thenReturn(existingJourneyState)

        val realObjectMapper = ObjectMapper()

        val underTest =
            JourneyStatePersistenceService(
                journeyRepository = mockJourneyRepository,
                oneLoginUserRepository = mockOneLoginUserRepository,
                objectMapper = realObjectMapper,
            )

        // Act
        val retrievedState = underTest.retrieveJourneyStateData(journeyId = testJourneyId)

        // Assert
        assertEquals(mapOf("key" to "value"), retrievedState)
    }

    private fun setSecurityContextWithUser(username: String) {
        val authentication = mock(org.springframework.security.core.Authentication::class.java)
        whenever(authentication.name).thenReturn(username)
        val securityContext = mock(org.springframework.security.core.context.SecurityContext::class.java)
        whenever(securityContext.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(securityContext)
    }
}
