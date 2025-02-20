package uk.gov.communities.prsdb.webapp.forms

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyRegistrationJourney
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.test.assertEquals

class PropertyRegistrationJourneyTests {
    @Mock
    lateinit var mockJourneyDataService: JourneyDataService

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
    }

    @Nested
    inner class JourneyDataManipulationTests {
        @Test
        fun `when there is no journey data in the session or the database, journey data is not loaded`() {
            val principalName = "principalName"
            val testJourney =
                PropertyRegistrationJourney(
                    mock(),
                    mockJourneyDataService,
                    mock(),
                    mock(),
                    mock(),
                    mock(),
                    mock(),
                    mock(),
                )

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(mutableMapOf())
            whenever(mockJourneyDataService.getContextId(principalName, JourneyType.PROPERTY_REGISTRATION)).thenReturn(
                null,
            )

            // Act
            testJourney.initialiseJourneyDataIfNotInitialised(principalName)

            // Assert
            verify(mockJourneyDataService, never()).loadJourneyDataIntoSession(any())
        }

        @Test
        fun `when the journey data is not in the session it will be loaded into the session from the database`() {
            val principalName = "principalName"
            val contextId = 67L

            val testJourney =
                PropertyRegistrationJourney(
                    mock(),
                    mockJourneyDataService,
                    mock(),
                    mock(),
                    mock(),
                    mock(),
                    mock(),
                    mock(),
                )

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(mutableMapOf())
            whenever(mockJourneyDataService.getContextId(principalName, JourneyType.PROPERTY_REGISTRATION)).thenReturn(
                contextId,
            )

            // Act
            testJourney.initialiseJourneyDataIfNotInitialised(principalName)

            // Assert
            val captor = argumentCaptor<Long>()
            verify(mockJourneyDataService).loadJourneyDataIntoSession(captor.capture())
            assertEquals(contextId, captor.allValues.single())
        }

        @Test
        fun `when the journey data is already in the session, journey data is not loaded`() {
            val principalName = "principalName"
            val testJourney =
                PropertyRegistrationJourney(
                    mock(),
                    mockJourneyDataService,
                    mock(),
                    mock(),
                    mock(),
                    mock(),
                    mock(),
                    mock(),
                )

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(mutableMapOf("anything" to "Anything else"))

            // Act
            testJourney.initialiseJourneyDataIfNotInitialised(principalName)

            // Assert
            verify(mockJourneyDataService, never()).loadJourneyDataIntoSession(any())
        }
    }
}
