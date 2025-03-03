package uk.gov.communities.prsdb.webapp.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import java.security.Principal
import java.util.Optional
import kotlin.test.assertContains
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class JourneyDataServiceTests {
    @Mock
    private lateinit var mockHttpSession: HttpSession

    @Mock
    private lateinit var mockFormContextRepository: FormContextRepository

    @Mock
    private lateinit var mockOneLoginUserRepository: OneLoginUserRepository

    private lateinit var journeyDataService: JourneyDataService

    @BeforeEach
    fun setup() {
        journeyDataService =
            JourneyDataService(
                mockHttpSession,
                mockFormContextRepository,
                mockOneLoginUserRepository,
                ObjectMapper(),
            )
    }

    @Nested
    inner class JourneyDataKeyTests {
        @Test
        fun `getJourneyDataKey returns journeyDataKey if it is initialized`() {
            val journeyDataKey = "journeyDataKey"

            journeyDataService.getJourneyDataFromSession(journeyDataKey)

            val retrievedJourneyDataKey = journeyDataService.getJourneyDataKey()

            assertEquals(journeyDataKey, retrievedJourneyDataKey)
        }

        @Test
        fun `getJourneyDataKey throws an error if journeyDataKey is not initialized`() {
            val exception = assertThrows<PrsdbWebException> { journeyDataService.getJourneyDataKey() }
            assertContains(exception.message!!, "journeyDataKey has not been set")
        }
    }

    @Nested
    inner class SessionJourneyDataTests {
        @Suppress("ktlint:standard:max-line-length")
        @Test
        fun `getJourneyDataFromSession sets the given journeyDataKey and returns the journey data from session if journeyDataKey is not initialized`() {
            val journeyDataKey = "journeyDataKey"
            val journeyData = mapOf("key" to "value")

            whenever(mockHttpSession.getAttribute(journeyDataKey)).thenReturn(journeyData)

            val retrievedJourneyData = journeyDataService.getJourneyDataFromSession(journeyDataKey)

            verify(mockHttpSession).getAttribute(journeyDataKey)
            assertEquals(journeyData, retrievedJourneyData)
        }

        @Test
        fun `getJourneyDataFromSession throws an error if the given journeyDataKey does not match the stored one`() {
            val journeyDataKey = "journeyDataKey"
            val differentJourneyDataKey = "differentJourneyDataKey"

            whenever(mockHttpSession.getAttribute(journeyDataKey)).thenReturn(emptyMap<String, Any?>())
            journeyDataService.getJourneyDataFromSession(journeyDataKey)

            val exception =
                assertThrows<PrsdbWebException> { journeyDataService.getJourneyDataFromSession(differentJourneyDataKey) }
            assertContains(exception.message!!, "journeyDataKey has already been set to $journeyDataKey")
        }

        @Test
        fun `getJourneyDataFromSession returns the journey data from session if journeyDataKey is initialized`() {
            val journeyDataKey = "journeyDataKey"
            val journeyData = mapOf("key" to "value")

            whenever(mockHttpSession.getAttribute(journeyDataKey)).thenReturn(journeyData)
            journeyDataService.getJourneyDataFromSession(journeyDataKey)

            val retrievedJourneyData = journeyDataService.getJourneyDataFromSession()

            verify(mockHttpSession, times(2)).getAttribute(journeyDataKey)
            assertEquals(journeyData, retrievedJourneyData)
        }

        @Test
        fun `getJourneyDataFromSession throws an error if the journeyDataKey is not initialized`() {
            val exception = assertThrows<PrsdbWebException> { journeyDataService.getJourneyDataFromSession() }
            assertContains(exception.message!!, "journeyDataKey has not been set")
        }

        @Test
        fun `setJourneyDataFromSession sets the given journey data in session if journeyDataKey is initialized`() {
            val journeyDataKey = "journeyDataKey"
            val journeyData = mapOf("key" to "value")

            journeyDataService.getJourneyDataFromSession(journeyDataKey)

            journeyDataService.setJourneyDataInSession(journeyData)

            verify(mockHttpSession).setAttribute(journeyDataKey, journeyData)
        }

        @Test
        fun `setJourneyDataFromSession throws an error if the journeyDataKey is not initialized`() {
            val exception = assertThrows<PrsdbWebException> { journeyDataService.setJourneyDataInSession(emptyMap()) }
            assertContains(exception.message!!, "journeyDataKey has not been set")
        }

        @Test
        fun `clearJourneyDataFromSession clears the journey data from session if journeyDataKey is initialized`() {
            val journeyDataKey = "journeyDataKey"

            journeyDataService.getJourneyDataFromSession(journeyDataKey)

            journeyDataService.clearJourneyDataFromSession()

            verify(mockHttpSession).setAttribute(journeyDataKey, null)
        }

        @Test
        fun `clearJourneyDataFromSession throws an error if the journeyDataKey is not initialized`() {
            val exception = assertThrows<PrsdbWebException> { journeyDataService.clearJourneyDataFromSession() }
            assertContains(exception.message!!, "journeyDataKey has not been set")
        }
    }

    @Nested
    inner class SaveJourneyDataTests {
        @Test
        fun `creates new form context if contextId is null`() {
            // Arrange
            // Function Args
            val journeyType = JourneyType.LANDLORD_REGISTRATION
            val principalId = "testPrincipleSub"
            val principal = Principal { principalId }

            // JourneyData
            val pageName = "testPage"
            val key = "testKey"
            val value = "testValue"
            val journeyData: JourneyData =
                mapOf(
                    pageName to mapOf(key to value),
                )
            val serializedJourneyData = ObjectMapper().writeValueAsString(journeyData)

            // OneLoginUser
            val oneLoginUser = OneLoginUser()
            whenever(mockOneLoginUserRepository.getReferenceById(principalId)).thenReturn(
                oneLoginUser,
            )

            // FormContext
            val contextId: Long = 123
            val spiedOnFormContext = spy(FormContext())
            whenever(spiedOnFormContext.id).thenReturn(contextId)
            whenever(mockFormContextRepository.save(any())).thenReturn(spiedOnFormContext)

            // Act
            val result = journeyDataService.saveJourneyData(null, journeyData, journeyType, principal)
            val formContextCaptor = captor<FormContext>()
            verify(mockFormContextRepository).save(formContextCaptor.capture())
            val oneLoginCaptor = captor<String>()
            verify(mockOneLoginUserRepository).getReferenceById(oneLoginCaptor.capture())

            // Assert
            assertEquals(contextId, result)
            assertEquals(journeyType, formContextCaptor.value.journeyType)
            assertEquals(oneLoginUser, formContextCaptor.value.user)
            assertEquals(serializedJourneyData, formContextCaptor.value.context)
            assertEquals(principalId, oneLoginCaptor.value)
        }

        @Test
        fun `updates existing form context if it exists`() {
            // Arrange
            // Function Args
            val journeyType = JourneyType.LANDLORD_REGISTRATION
            val principalId = "testPrincipleSub"
            val principal = Principal { principalId }

            // Original JourneyData
            val pageName = "testPage"
            val journeyData: JourneyData =
                mapOf(
                    pageName to mutableMapOf<String, Any>(),
                )
            val serializedJourneyData = ObjectMapper().writeValueAsString(journeyData)

            // New JourneyData
            val newKey = "newTestKey"
            val newValue = "newTestValue"
            val newJourneyData: JourneyData =
                mapOf(
                    pageName to mapOf(newKey to newValue),
                )
            val newSerializedJourneyData = ObjectMapper().writeValueAsString(newJourneyData)

            // OneLoginUser
            val oneLoginUser = OneLoginUser()

            // FormContext
            val contextId: Long = 123
            val spiedOnFormContext = spy(FormContext())
            whenever(spiedOnFormContext.id).thenReturn(contextId)
            whenever(mockFormContextRepository.save(any())).thenReturn(spiedOnFormContext)
            val originalFormContext = FormContext(journeyType, serializedJourneyData, oneLoginUser)
            whenever(mockFormContextRepository.findById(contextId)).thenReturn(Optional.ofNullable(originalFormContext))

            // Act
            val result = journeyDataService.saveJourneyData(contextId, newJourneyData, journeyType, principal)
            val formContextCaptor = captor<FormContext>()
            verify(mockFormContextRepository).save(formContextCaptor.capture())

            // Assert
            assertEquals(contextId, result)
            assertEquals(journeyType, formContextCaptor.value.journeyType)
            assertEquals(oneLoginUser, formContextCaptor.value.user)
            assertEquals(newSerializedJourneyData, formContextCaptor.value.context)
        }

        @Test
        fun `throws an illegal state exception if form context is missing`() {
            // Arrange
            val journeyType = JourneyType.LANDLORD_REGISTRATION
            val principalId = "testPrincipleSub"
            val principal = Principal { principalId }
            val journeyData: JourneyData = mapOf()

            // FormContext
            val contextId: Long = 123
            whenever(mockFormContextRepository.findById(contextId)).thenReturn(Optional.ofNullable(null))

            // Act and Assert
            assertThrows<IllegalStateException> {
                journeyDataService.saveJourneyData(
                    contextId,
                    journeyData,
                    journeyType,
                    principal,
                )
            }
        }
    }

    @Nested
    inner class LoadJourneyDataIntoSessionTests {
        @Test
        fun `stores the new journey data in the session`() {
            // Function Args
            val journeyType = JourneyType.LANDLORD_REGISTRATION

            // JourneyData
            val journeyDataKey = "journey-data-key"
            val pageName = "testPage"
            val key = "testKey"
            val value = "testValue"
            val journeyData: JourneyData =
                mapOf(
                    pageName to mapOf(key to value),
                )
            val serializedJourneyData = ObjectMapper().writeValueAsString(journeyData)

            // OneLoginUser
            val oneLoginUser = OneLoginUser()

            // FormContext
            val contextId: Long = 123
            val formContext = FormContext(journeyType, serializedJourneyData, oneLoginUser)
            whenever(mockFormContextRepository.findById(contextId)).thenReturn(Optional.ofNullable(formContext))

            // Act
            journeyDataService.getJourneyDataFromSession(journeyDataKey)
            journeyDataService.loadJourneyDataIntoSession(contextId)
            val formContextCaptor = captor<JourneyData>()
            verify(mockHttpSession).setAttribute(eq(journeyDataKey), formContextCaptor.capture())
            val contextIdCaptor = captor<Long>()
            verify(mockHttpSession).setAttribute(eq("contextId"), contextIdCaptor.capture())

            // Assert
            assertEquals(journeyData, formContextCaptor.value)
            assertEquals(contextId, contextIdCaptor.value)
        }

        @Test
        fun `throws an illegal state exception if form context is missing`() {
            // Arrange
            val contextId: Long = 123
            whenever(mockFormContextRepository.findById(contextId)).thenReturn(Optional.ofNullable(null))

            // Act and Assert
            assertThrows<IllegalStateException> {
                journeyDataService.loadJourneyDataIntoSession(contextId)
            }
        }
    }
}
