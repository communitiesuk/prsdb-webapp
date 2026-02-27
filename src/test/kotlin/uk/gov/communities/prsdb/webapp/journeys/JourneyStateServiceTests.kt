package uk.gov.communities.prsdb.webapp.journeys

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.anyOrNull
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
    fun `journeyMetadata returns the corresponding value from the metadata store, if present`() {
        // Arrange
        val session = MockHttpSession()
        val journeyId = "journey-1"
        val expectedMetadata = JourneyMetadata(journeyId)
        val metadataStore = JourneyMetadataStore(mapOf(journeyId to expectedMetadata))
        session.setJourneyStateMetadataStore(metadataStore)
        val service = createJourneyStateServiceWithJourneyId(session, journeyId)

        // Act
        val actualDataKey = service.journeyMetadata

        // Assert
        assertEquals(expectedMetadata, actualDataKey)
    }

    @Test
    fun `journeyMetadata restores the journey if possible, when the corresponding value is not in the metadata store`() {
        // Arrange
        val session = mock<HttpSession>()
        val journeyId = "journey-1"
        val expectedMetadata = JourneyMetadata(journeyId)
        val metadataStore = JourneyMetadataStore(mapOf(journeyId to expectedMetadata))
        val journeyStatePersistenceService = mock<JourneyStatePersistenceService>()
        val retrievedData = mapOf<String, Any?>("restoredKey" to "restoredValue")
        whenever(journeyStatePersistenceService.retrieveJourneyStateData(anyOrNull())).thenReturn(retrievedData)
        whenever(session.getAttribute("journeyStateKeyStore")).thenReturn(Json.encodeToString(metadataStore))
        val newJourneyId = "journey-2"
        val service = createJourneyStateServiceWithJourneyId(session, newJourneyId, journeyStatePersistenceService)
        // Act
        service.journeyMetadata

        // Assert
        verify(journeyStatePersistenceService).retrieveJourneyStateData(newJourneyId)
        verify(session).setAttribute(newJourneyId, retrievedData)
        verify(session).setAttribute(
            eq("journeyStateKeyStore"),
            org.mockito.kotlin.argThat<String> { encoded ->
                val store = Json.decodeFromString<JourneyMetadataStore>(encoded)
                store[journeyId] == expectedMetadata && store[newJourneyId]?.journeyId == newJourneyId
            },
        )
    }

    @Test
    fun `journeyMetadata throws an exception if the journey cannot be restored when it is not in the metadata store`() {
        // Arrange
        val session = mock<HttpSession>()
        val journeyId = "journey-1"
        val metadataStore = JourneyMetadataStore(mapOf(journeyId to JourneyMetadata(journeyId)))
        val journeyStatePersistenceService = mock<JourneyStatePersistenceService>()
        whenever(journeyStatePersistenceService.retrieveJourneyStateData(anyOrNull())).thenReturn(null)
        whenever(session.getAttribute("journeyStateKeyStore")).thenReturn(Json.encodeToString(metadataStore))
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
        val updatedJourneyData = objectToStringKeyedMap(session.getAttribute(service.journeyMetadata.journeyId))!!
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

        val updatedJourneyData = objectToStringKeyedMap(session.getAttribute(service.journeyMetadata.journeyId))!!

        assertEquals("existingValue", updatedJourneyData["existingKey"])
        assertEquals(17, updatedJourneyData["newKey"])
    }

    @Test
    fun `deleteState removes the journey data and updates the metadata store in the session`() {
        // Arrange
        val session = MockHttpSession()
        val journeyId = "journey-1"
        val metadataStore =
            JourneyMetadataStore(
                mapOf(journeyId to JourneyMetadata(journeyId), "journey-2" to JourneyMetadata("journey-2")),
            )
        session.setJourneyStateMetadataStore(metadataStore)
        val service = createJourneyStateServiceWithJourneyId(session, journeyId)

        // Act
        service.deleteState()

        // Assert
        assertNull(session.getAttribute(journeyId))
        val updatedMetadataStore = session.getJourneyStateMetadataStore()

        assertNotNull(updatedMetadataStore)
        assertNull(updatedMetadataStore[journeyId])
        assertEquals("journey-2", updatedMetadataStore["journey-2"]?.journeyId)
    }

    @Test
    fun `deleteState calls persistenceService to delete the base journey state data`() {
        // Arrange
        val session = MockHttpSession()
        val baseJourneyId = "journey-1"
        val childJourneyId = "journey-2"
        val metadataStore =
            JourneyMetadataStore(
                mapOf(
                    baseJourneyId to JourneyMetadata(baseJourneyId),
                    childJourneyId to JourneyMetadata(childJourneyId, baseJourneyId = baseJourneyId),
                ),
            )
        session.setJourneyStateMetadataStore(metadataStore)
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
        val metadataStore =
            JourneyMetadataStore(
                mapOf(
                    journeyId to JourneyMetadata(journeyId),
                    "journey-2" to JourneyMetadata("journey-2"),
                ),
            )
        session.setJourneyStateMetadataStore(metadataStore)
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
        val updatedMetadataStore = session.getJourneyStateMetadataStore()
        val newMetadata = updatedMetadataStore?.get(newJourneyId)!!
        val newJourneyData = objectToStringKeyedMap(session.getAttribute(newMetadata.journeyId))
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
    fun `copyJourneyTo creates a new journey with the current journey's data and a base journey reference`() {
        // Arrange
        val session = MockHttpSession()
        val sourceJourneyId = "source-journey"
        val sourceData = mapOf("key" to "value")
        val metadataStore = JourneyMetadataStore(mapOf(sourceJourneyId to JourneyMetadata(sourceJourneyId)))
        session.setJourneyStateMetadataStore(metadataStore)
        session.setAttribute(sourceJourneyId, sourceData)
        val service = createJourneyStateServiceWithJourneyId(session, sourceJourneyId)
        val newJourneyId = "new-journey"

        // Act
        service.copyJourneyTo(newJourneyId)

        // Assert
        val updatedMetadataStore = session.getJourneyStateMetadataStore()
        val newMetadata = updatedMetadataStore?.get(newJourneyId)
        assertNotNull(newMetadata)
        assertEquals(newJourneyId, newMetadata.journeyId)
        assertEquals(sourceJourneyId, newMetadata.baseJourneyId)

        val copiedData = objectToStringKeyedMap(session.getAttribute(newJourneyId))
        assertEquals("value", copiedData?.get("key"))
    }

    @Test
    fun `copyJourneyTo uses existing metadata if the new journey already exists in the store`() {
        // Arrange
        val session = MockHttpSession()
        val sourceJourneyId = "source-journey"
        val existingNewJourneyId = "existing-journey"
        val existingMetadata = JourneyMetadata(existingNewJourneyId, baseJourneyId = "other-base")
        val metadataStore =
            JourneyMetadataStore(
                mapOf(
                    sourceJourneyId to JourneyMetadata(sourceJourneyId),
                    existingNewJourneyId to existingMetadata,
                ),
            )
        session.setJourneyStateMetadataStore(metadataStore)
        val sourceData = mapOf("key" to "value")
        session.setAttribute(sourceJourneyId, sourceData)
        val service = createJourneyStateServiceWithJourneyId(session, sourceJourneyId)

        // Act
        service.copyJourneyTo(existingNewJourneyId)

        // Assert
        val updatedMetadataStore = session.getJourneyStateMetadataStore()
        val resultMetadata = updatedMetadataStore?.get(existingNewJourneyId)
        assertNotNull(resultMetadata)
        assertEquals("other-base", resultMetadata.baseJourneyId)

        val copiedData = objectToStringKeyedMap(session.getAttribute(existingNewJourneyId))
        assertEquals("value", copiedData?.get("key"))
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
        val journeyId = "journey"
        val metadataStore = JourneyMetadataStore(mapOf(journeyId to JourneyMetadata(journeyId)))
        session.setAttribute(journeyId, existingData)
        session.setJourneyStateMetadataStore(metadataStore)
        return createJourneyStateServiceWithJourneyId(session, journeyId)
    }

    private fun HttpSession.setJourneyStateMetadataStore(metadataStore: JourneyMetadataStore) {
        setAttribute("journeyStateKeyStore", Json.encodeToString(metadataStore))
    }

    private fun HttpSession.getJourneyStateMetadataStore(): JourneyMetadataStore? =
        getAttribute("journeyStateKeyStore")?.toString()?.let { Json.decodeFromString(it) }
}
