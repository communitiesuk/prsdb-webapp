package uk.gov.communities.prsdb.webapp.journeys

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpSession
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.objectToTypedStringKeyedMap
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JourneyStateServiceTests {
    @Test
    fun `journeyId lazily resolves from provider when not explicitly set`() {
        // Arrange
        val expectedJourneyId = "test-journey-id"
        val provider = mock<JourneyIdProvider>()
        whenever(provider.getParameterOrNull()).thenReturn(expectedJourneyId)
        val service = JourneyStateService(mock(), provider, mock())

        // Act
        val actualJourneyId = service.journeyId

        // Assert
        assertEquals(expectedJourneyId, actualJourneyId)
    }

    @Test
    fun `journeyId throws if provider returns null and no id has been set`() {
        // Arrange
        val provider = mock<JourneyIdProvider>()
        whenever(provider.getParameterOrNull()).thenReturn(null)
        val service = JourneyStateService(mock(), provider, mock())

        // Act & Assert
        assertThrows<NoSuchJourneyException> { service.journeyId }
    }

    @Test
    fun `setJourneyId sets the journey id when not yet set`() {
        // Arrange
        val service = JourneyStateService(mock(), mock(), mock())

        // Act
        service.setJourneyId("my-journey")

        // Assert
        assertEquals("my-journey", service.journeyId)
    }

    @Test
    fun `setJourneyId throws if journey id has already been set explicitly`() {
        // Arrange
        val service = JourneyStateService(mock(), mock(), mock())
        service.setJourneyId("first-id")

        // Act & Assert
        assertThrows<JourneyInitialisationException> { service.setJourneyId("second-id") }
    }

    @Test
    fun `setJourneyId throws if journey id has already been lazily resolved from provider`() {
        // Arrange
        val provider = mock<JourneyIdProvider>()
        whenever(provider.getParameterOrNull()).thenReturn("from-request")
        val service = JourneyStateService(mock(), provider, mock())
        service.journeyId // triggers lazy resolution

        // Act & Assert
        assertThrows<JourneyInitialisationException> { service.setJourneyId("new-id") }
    }

    @Test
    fun `journeyId returns explicitly set id without consulting provider`() {
        // Arrange
        val provider = mock<JourneyIdProvider>()
        whenever(provider.getParameterOrNull()).thenReturn("from-request")
        val service = JourneyStateService(mock(), provider, mock())
        service.setJourneyId("explicit-id")

        // Act
        val actualJourneyId = service.journeyId

        // Assert
        assertEquals("explicit-id", actualJourneyId)
    }

    @Test
    fun `getJourneyStateMetadataMap retrieves the metadata map from the session, if present`() {
        // Arrange
        val session = MockHttpSession()
        val expectedMetadataMap = mapOf("journey-1" to JourneyMetadata("data-key-1"), "journey-2" to JourneyMetadata("data-key-2"))
        session.setJourneyStateMetadataMap(expectedMetadataMap)
        val service = createJourneyStateServiceWithJourneyId(session, "null")

        // Act
        val actualMetadataMap = service.journeyStateMetadataMap

        // Assert
        assertEquals(expectedMetadataMap, actualMetadataMap)
    }

    @Test
    fun `getJourneyStateMetadataMap returns an empty map, if not present in the session`() {
        // Arrange
        val session = mock<HttpSession>()
        whenever(session.getAttribute("journeyStateKeyStore")).thenReturn(null)
        val service = createJourneyStateServiceWithJourneyId(session, "null")

        // Act
        val actualMetadataMap = service.journeyStateMetadataMap

        // Assert
        assertTrue(actualMetadataMap.isEmpty())
    }

    @Test
    fun `journeyMetadata returns the corresponding value from the metadata map, if present`() {
        // Arrange
        val session = MockHttpSession()
        val journeyId = "journey-1"
        val expectedMetadata = JourneyMetadata("data-key-1")
        val metadataMap = mapOf(journeyId to expectedMetadata)
        session.setJourneyStateMetadataMap(metadataMap)
        val service = createJourneyStateServiceWithJourneyId(session, journeyId)

        // Act
        val actualDataKey = service.journeyMetadata

        // Assert
        assertEquals(expectedMetadata, actualDataKey)
    }

    @Test
    fun `journeyMetadata restores the journey if possible, when the corresponding value is not in the metadata map`() {
        // Arrange
        val session = mock<HttpSession>()
        val journeyId = "journey-1"
        val expectedMetadata = JourneyMetadata("data-key-1")
        val metadataMap = mapOf(journeyId to expectedMetadata)
        val journeyStatePersistenceService = mock<JourneyStatePersistenceService>()
        val retrievedData = mapOf<String, Any?>("restoredKey" to "restoredValue")
        whenever(journeyStatePersistenceService.retrieveJourneyStateData(anyOrNull())).thenReturn(retrievedData)
        whenever(session.getAttribute("journeyStateKeyStore")).thenReturn(Json.encodeToString(metadataMap))
        val service = createJourneyStateServiceWithJourneyId(session, "journey-2", journeyStatePersistenceService)

        // Act
        service.journeyMetadata

        // Assert
        verify(journeyStatePersistenceService).retrieveJourneyStateData("journey-2")

        val dataKeyCaptor = argumentCaptor<String>()
        verify(session).setAttribute(dataKeyCaptor.capture(), eq(retrievedData))
        verify(session).setAttribute(
            "journeyStateKeyStore",
            Json.encodeToString(
                mapOf(
                    "journey-1" to expectedMetadata,
                    "journey-2" to JourneyMetadata(dataKeyCaptor.firstValue),
                ),
            ),
        )
    }

    @Test
    fun `journeyMetadata throws an exception, if the journey cannot be restored when the corresponding value is not in the metadata map`() {
        // Arrange
        val session = mock<HttpSession>()
        val journeyId = "journey-1"
        val expectedDataKey = "data-key-1"
        val metadataMap = mapOf(journeyId to expectedDataKey)
        val journeyStatePersistenceService = mock<JourneyStatePersistenceService>()
        whenever(journeyStatePersistenceService.retrieveJourneyStateData(anyOrNull())).thenReturn(null)
        whenever(session.getAttribute("journeyStateKeyStore")).thenReturn(metadataMap)
        val service = createJourneyStateServiceWithJourneyId(session, "journey-2", journeyStatePersistenceService)

        // Act & Assert
        assertThrows<NoSuchJourneyException> { service.journeyMetadata }

        // Assert
        verify(journeyStatePersistenceService).retrieveJourneyStateData("journey-2")
    }

    @Test
    fun `getValue retrieves the correct value from the journey data stored in the session`() {
        // Arrange
        val session = MockHttpSession()
        val journeyData = mapOf("key" to 17)
        val service = createJourneyStateServiceWithMetadata(session, journeyData)

        // Act
        val actualValue2 = service.getValue("key")

        // Assert
        assertEquals(17, actualValue2)
    }

    @Test
    fun `getValue returns null if the key is not present in the journey data`() {
        // Arrange
        val session = MockHttpSession()
        val journeyData = mapOf("key" to 17)
        val service = createJourneyStateServiceWithMetadata(session, journeyData)

        // Act
        val actualValue = service.getValue("non-existent-key")

        // Assert
        assertNull(actualValue)
    }

    @Test
    fun `getValue returns null if there is no journey data stored in the session`() {
        // Arrange
        val session = MockHttpSession()
        val service = createJourneyStateServiceWithMetadata(session, null)

        // Act
        val actualValue = service.getValue("any-key")

        // Assert
        assertNull(actualValue)
    }

    @Test
    fun `getSubmittedStepData retrieves the step data map from the journey data, if present`() {
        // Arrange
        val session = MockHttpSession()
        val stepData = mapOf("step-1" to mapOf("field" to "value"))
        val journeyData = mapOf("journeyData" to stepData)
        val service = createJourneyStateServiceWithMetadata(session, journeyData)

        // Act
        val actualStepData = service.getSubmittedStepData()

        // Assert
        assertEquals(stepData, actualStepData)
    }

    @Test
    fun `getSubmittedStepData returns an empty map if no step data is present in the journey data`() {
        // Arrange
        val session = MockHttpSession()
        val service = createJourneyStateServiceWithMetadata(session, null)

        // Act
        val actualStepData = service.getSubmittedStepData()

        // Assert
        assertTrue(actualStepData.isEmpty())
    }

    @Test
    fun `addSingleStepData adds the provided step data to the existing journey data in the session`() {
        // Arrange
        val session = MockHttpSession()
        val step1Data = mapOf("field1" to "value1")
        val existingStepData = mapOf("step-1" to step1Data)
        val journeyData = mapOf("journeyData" to existingStepData)
        val service = createJourneyStateServiceWithMetadata(session, journeyData)

        // Act
        val newStepDataKey = "step-2"
        val newStepDataValue = mapOf("field2" to "value2")
        service.addSingleStepData(newStepDataKey, newStepDataValue)

        // Assert
        val updatedJourneyData = objectToStringKeyedMap(session.getAttribute(service.journeyMetadata.dataKey))!!
        val stepData = objectToTypedStringKeyedMap<Map<String, String>>(updatedJourneyData["journeyData"])

        assertEquals("value1", stepData?.get("step-1")?.get("field1"))
        assertEquals("value2", stepData?.get("step-2")?.get("field2"))
    }

    @Test
    fun `setValue updates the journey data in the session with the provided key-value pair`() {
        // Arrange
        val session = MockHttpSession()
        val existingJourneyData = mapOf("existingKey" to "existingValue")
        val service = createJourneyStateServiceWithMetadata(session, existingJourneyData)

        // Act
        val newKey = "newKey"
        val newValue = 17
        service.setValue(newKey, newValue)

        // Assert

        val updatedJourneyData = objectToStringKeyedMap(session.getAttribute(service.journeyMetadata.dataKey))!!

        assertEquals("existingValue", updatedJourneyData["existingKey"])
        assertEquals(17, updatedJourneyData["newKey"])
    }

    @Test
    fun `deleteState removes the journey data and updates the metadata map in the session`() {
        // Arrange
        val session = MockHttpSession()
        val journeyId = "journey-1"
        val dataKey = "data-key-1"
        val metadataMap = mapOf(journeyId to JourneyMetadata(dataKey), "journey-2" to JourneyMetadata("data-key-2"))
        session.setJourneyStateMetadataMap(metadataMap)
        val service = createJourneyStateServiceWithJourneyId(session, journeyId)

        // Act
        service.deleteState()

        // Assert
        assertNull(session.getAttribute(dataKey))
        val updatedMetadataMap = session.getJourneyStateMetadataMap()

        assertNotNull(updatedMetadataMap)
        assertNull(updatedMetadataMap[journeyId])
        assertEquals("data-key-2", updatedMetadataMap["journey-2"]?.dataKey)
    }

    @Test
    fun `deleteState calls persistenceService to delete the base journey state data`() {
        // Arrange
        val session = MockHttpSession()
        val baseJourneyId = "journey-1"
        val childJourneyId = "journey-2"
        val dataKey = "data-key-2"
        val metadataMap =
            mapOf(
                baseJourneyId to JourneyMetadata("data-key-1"),
                childJourneyId to JourneyMetadata(dataKey, baseJourneyId = baseJourneyId),
            )
        session.setJourneyStateMetadataMap(metadataMap)
        val mockPersistenceService = mock<JourneyStatePersistenceService>()
        val service = createJourneyStateServiceWithJourneyId(session, childJourneyId, mockPersistenceService)

        // Act
        service.deleteState()

        // Assert
        verify(mockPersistenceService).deleteJourneyStateData(baseJourneyId)
    }

    @Test
    fun `deleteState calls persistenceService to delete the current journey state data if there is no base journey`() {
        // Arrange
        val session = MockHttpSession()
        val journeyId = "journey-1"
        val dataKey = "data-key-1"
        val metadataMap =
            mapOf(
                journeyId to JourneyMetadata(dataKey),
                "journey-2" to JourneyMetadata("data-key-2"),
            )
        session.setJourneyStateMetadataMap(metadataMap)
        val mockPersistenceService = mock<JourneyStatePersistenceService>()
        val service = createJourneyStateServiceWithJourneyId(session, journeyId, mockPersistenceService)

        // Act
        service.deleteState()

        // Assert
        verify(mockPersistenceService).deleteJourneyStateData(journeyId)
    }

    @Test
    fun `initialiseJourneyWithId sets up a new journey in the session with the provided id`() {
        // Arrange
        val session = MockHttpSession()
        val service = JourneyStateService(session, mock(), mock())
        val newJourneyId = "new-journey"

        // Act
        service.initialiseJourneyWithId(newJourneyId) {
            setValue("initialKey", "initialValue")
        }

        // Assert
        val updatedMetadataMap = session.getJourneyStateMetadataMap()
        val newMetadata = (updatedMetadataMap?.get(newJourneyId) as? JourneyMetadata)!!
        val newJourneyData = objectToStringKeyedMap(session.getAttribute(newMetadata.dataKey))
        assertEquals("initialValue", newJourneyData?.get("initialKey"))
    }

    @Test
    fun `urlWithJourneyState appends the journeyId parameter to the provided URL`() {
        // Arrange
        val baseUrl = "example.com/page"
        val journeyId = "test-journey-id"

        // Act
        val urlWithParam = JourneyStateService.urlWithJourneyState(baseUrl, journeyId)

        // Assert
        assertEquals("$baseUrl?journeyId=$journeyId", urlWithParam)
    }

    @Test
    fun `initialiseChildJourney sets up a child journey sharing the parent's data key`() {
        // Arrange
        val session = MockHttpSession()
        val parentJourneyId = "parent-journey"
        val dataKey = "shared-data-key"
        val metadataMap = mapOf(parentJourneyId to JourneyMetadata(dataKey))
        session.setJourneyStateMetadataMap(metadataMap)
        val parentService = createJourneyStateServiceWithJourneyId(session, parentJourneyId)
        val childJourneyId = "child-journey"
        val subJourneyName = "sub-journey"

        // Act
        parentService.initialiseChildJourney(childJourneyId, subJourneyName)

        // Assert
        val updatedMetadataMap = session.getJourneyStateMetadataMap()
        val childMetadata = updatedMetadataMap?.get(childJourneyId) as? JourneyMetadata
        assertNotNull(childMetadata)
        assertEquals(dataKey, childMetadata.dataKey)
        assertEquals(parentJourneyId, childMetadata.baseJourneyId)
        assertEquals(subJourneyName, childMetadata.childJourneyName)
    }

    private fun createJourneyStateServiceWithJourneyId(
        session: HttpSession,
        journeyId: String,
        persistenceService: JourneyStatePersistenceService = mock(),
    ): JourneyStateService {
        val service = JourneyStateService(session, mock(), persistenceService)
        service.setJourneyId(journeyId)
        return service
    }

    private fun createJourneyStateServiceWithMetadata(
        session: HttpSession,
        existingData: JourneyData?,
    ): JourneyStateService {
        val dataKey = "data-key"
        val journeyId = "journey"
        val metadataMap = mapOf(journeyId to JourneyMetadata(dataKey))
        session.setAttribute(dataKey, existingData)
        session.setJourneyStateMetadataMap(metadataMap)
        return createJourneyStateServiceWithJourneyId(session, journeyId)
    }

    private fun HttpSession.setJourneyStateMetadataMap(metadataMap: Map<String, JourneyMetadata>) {
        setAttribute("journeyStateKeyStore", Json.encodeToString(serializer(), metadataMap))
    }

    private fun HttpSession.getJourneyStateMetadataMap(): Map<String, JourneyMetadata>? =
        getAttribute("journeyStateKeyStore")?.toString()?.let { Json.decodeFromString(it) }
}
