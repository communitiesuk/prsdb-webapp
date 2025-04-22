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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.CONTEXT_ID
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import java.security.Principal
import java.util.Optional
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

    private val journeyDataKey = "any-key"

    @BeforeEach
    fun setup() {
        journeyDataService =
            JourneyDataServiceFactory(
                mockHttpSession,
                mockFormContextRepository,
                mockOneLoginUserRepository,
                ObjectMapper(),
            ).create(journeyDataKey)
    }

    @Nested
    inner class SessionJourneyDataTests {
        @Test
        fun `getJourneyDataFromSession returns the journey data from session`() {
            val journeyData = mapOf("key" to "value")

            whenever(mockHttpSession.getAttribute(journeyDataKey)).thenReturn(journeyData)

            val retrievedJourneyData = journeyDataService.getJourneyDataFromSession()

            assertEquals(journeyData, retrievedJourneyData)
        }

        @Test
        fun `setJourneyDataFromSession sets the given journey data in session`() {
            val journeyData = mapOf("key" to "value")

            journeyDataService.setJourneyDataInSession(journeyData)

            verify(mockHttpSession).setAttribute(journeyDataKey, journeyData)
        }

        @Test
        fun `clearJourneyDataFromSession clears the journey data from session`() {
            journeyDataService.clearJourneyDataFromSession()

            verify(mockHttpSession).setAttribute(journeyDataKey, null)
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
        lateinit var journeyData: JourneyData
        lateinit var formContext: FormContext

        @BeforeEach
        fun setUp() {
            // JourneyData
            val pageName = "testPage"
            val key = "testKey"
            val value = "testValue"
            journeyData =
                mapOf(
                    pageName to mapOf(key to value),
                )
            val serializedJourneyData = ObjectMapper().writeValueAsString(journeyData)

            // FormContext
            formContext = FormContext(JourneyType.PROPERTY_REGISTRATION, serializedJourneyData, OneLoginUser())
        }

        @Test
        fun `saves journey data to session`() {
            // Act
            journeyDataService.loadJourneyDataIntoSession(formContext)
            val formContextCaptor = captor<JourneyData>()
            verify(mockHttpSession).setAttribute(eq(journeyDataKey), formContextCaptor.capture())
            val contextIdCaptor = captor<Long>()
            verify(mockHttpSession).setAttribute(eq(CONTEXT_ID), contextIdCaptor.capture())

            // Assert
            assertEquals(journeyData, formContextCaptor.value)
            assertEquals(formContext.id, contextIdCaptor.value)
        }

        @Nested
        inner class WithOnlyContextIdTests {
            @Test
            fun `calls loadJourneyDataIntoSession when form context exists`() {
                // Arrange
                whenever(mockFormContextRepository.findById(formContext.id)).thenReturn(Optional.ofNullable(formContext))

                // Act
                journeyDataService.loadJourneyDataIntoSession(formContext.id)
                val formContextCaptor = captor<JourneyData>()
                verify(mockHttpSession).setAttribute(eq(journeyDataKey), formContextCaptor.capture())
                val contextIdCaptor = captor<Long>()
                verify(mockHttpSession).setAttribute(eq(CONTEXT_ID), contextIdCaptor.capture())

                // Assert
                assertEquals(journeyData, formContextCaptor.value)
                assertEquals(formContext.id, contextIdCaptor.value)
            }

            @Test
            fun `throws an illegal state exception if form context is missing`() {
                // Arrange
                val formContextId: Long = 123
                whenever(mockFormContextRepository.findById(formContextId)).thenReturn(Optional.ofNullable(null))

                // Act and Assert
                assertThrows<IllegalStateException> {
                    journeyDataService.loadJourneyDataIntoSession(formContextId)
                }
            }
        }

        @Nested
        inner class WithContextIdAndUserIdAndJourneyTypeTests {
            @Test
            fun `calls loadJourneyDataIntoSession when form context exists`() {
                // Arrange
                whenever(
                    mockFormContextRepository.findByIdAndUser_IdAndJourneyType(
                        formContext.id,
                        formContext.user.id,
                        formContext.journeyType,
                    ),
                ).thenReturn(formContext)

                // Act
                journeyDataService.loadJourneyDataIntoSession(formContext.id, formContext.user.id, formContext.journeyType)
                val formContextCaptor = captor<JourneyData>()
                verify(mockHttpSession).setAttribute(eq(journeyDataKey), formContextCaptor.capture())
                val contextIdCaptor = captor<Long>()
                verify(mockHttpSession).setAttribute(eq(CONTEXT_ID), contextIdCaptor.capture())

                // Assert
                assertEquals(journeyData, formContextCaptor.value)
                assertEquals(formContext.id, contextIdCaptor.value)
            }

            @Test
            fun `throws a response status exception NOT_FOUND if form context is missing`() {
                // Arrange
                val formContextId: Long = 123
                val baseUserId = "user"
                val journeyType = JourneyType.PROPERTY_REGISTRATION

                whenever(
                    mockFormContextRepository.findByIdAndUser_IdAndJourneyType(formContextId, baseUserId, journeyType),
                ).thenReturn(null)

                // Act and Assert
                val exception =
                    assertThrows<ResponseStatusException> {
                        journeyDataService.loadJourneyDataIntoSession(formContextId, baseUserId, journeyType)
                    }
                assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
            }
        }
    }

    @Test
    fun `removeJourneyDataAndContextIdFromSession removes journeyData and contextId from session`() {
        journeyDataService.removeJourneyDataAndContextIdFromSession()
        verify(mockHttpSession).removeAttribute(CONTEXT_ID)
        verify(mockHttpSession).removeAttribute(journeyDataKey)
    }
}
