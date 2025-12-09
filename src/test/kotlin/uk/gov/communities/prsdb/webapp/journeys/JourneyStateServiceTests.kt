package uk.gov.communities.prsdb.webapp.journeys

import jakarta.servlet.ServletRequest
import jakarta.servlet.http.HttpSession
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpSession
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.objectToTypedStringKeyedMap
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JourneyStateServiceTests {
    @Test
    fun `when constructed from a request, journeyId returns the value from the url parameter if present`() {
        // Arrange
        val expectedJourneyId = "test-journey-id"
        val request = mock<ServletRequest>()
        whenever(request.getParameter("journeyId")).thenReturn(expectedJourneyId)
        val service = JourneyStateService(mock(), request)
        // Act
        val actualJourneyId = service.journeyId

        // Assert
        assertEquals(expectedJourneyId, actualJourneyId)
    }

    @Test
    fun `when constructed from a request, journeyId throws if no url parameter is present`() {
        // Arrange
        val request = mock<ServletRequest>()
        whenever(request.getParameter("journeyId")).thenReturn(null)
        val service = JourneyStateService(mock(), request)

        // Act & Assert
        assertThrows<NoSuchJourneyException> { service.journeyId }
    }

    @Test
    fun `getJourneyStateMetadataMap retrieves the metadata map from the session, if present`() {
        // Arrange
        val session = MockHttpSession()
        val expectedMetadataMap = mapOf("journey-1" to JourneyMetadata("data-key-1"), "journey-2" to JourneyMetadata("data-key-2"))
        session.setJourneyStateMetadataMap(expectedMetadataMap)
        val service = JourneyStateService(session, "null")

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
        val service = JourneyStateService(session, "null")

        // Act
        val actualMetadataMap = service.journeyStateMetadataMap

        // Assert
        assertTrue(actualMetadataMap.isEmpty())
    }

    @Test
    fun `getJourneyDataKey returns the corresponding value from the metadata map, if present`() {
        // Arrange
        val session = MockHttpSession()
        val journeyId = "journey-1"
        val expectedMetadata = JourneyMetadata("data-key-1")
        val metadataMap = mapOf(journeyId to expectedMetadata)
        session.setJourneyStateMetadataMap(metadataMap)
        val service = JourneyStateService(session, journeyId)

        // Act
        val actualDataKey = service.journeyMetadata

        // Assert
        assertEquals(expectedMetadata, actualDataKey)
    }

    @Test
    fun `getJourneyDataKey throws if the corresponding value is not in the metadata map`() {
        // Arrange
        val session = mock<HttpSession>()
        val journeyId = "journey-1"
        val expectedDataKey = "data-key-1"
        val metadataMap = mapOf(journeyId to expectedDataKey)
        whenever(session.getAttribute("journeyStateKeyStore")).thenReturn(metadataMap)
        val service = JourneyStateService(session, "journey-2")

        // Act & Assert
        assertThrows<NoSuchJourneyException> { service.journeyMetadata }
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
        val service = JourneyStateService(session, journeyId)

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
    fun `initialiseJourneyWithId sets up a new journey in the session with the provided id`() {
        // Arrange
        val session = MockHttpSession()
        val service = JourneyStateService(session, null)
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
        val parentService = JourneyStateService(session, parentJourneyId)
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

    private fun createJourneyStateServiceWithMetadata(
        session: HttpSession,
        existingData: JourneyData?,
    ): JourneyStateService {
        val dataKey = "data-key"
        val journeyId = "journey"
        val metadataMap = mapOf(journeyId to JourneyMetadata(dataKey))
        session.setAttribute(dataKey, existingData)
        session.setJourneyStateMetadataMap(metadataMap)
        return JourneyStateService(session, journeyId)
    }

    private fun HttpSession.setJourneyStateMetadataMap(metadataMap: Map<String, JourneyMetadata>) {
        setAttribute("journeyStateKeyStore", Json.encodeToString(serializer(), metadataMap))
    }

    private fun HttpSession.getJourneyStateMetadataMap(): Map<String, JourneyMetadata>? =
        getAttribute("journeyStateKeyStore")?.toString()?.let { Json.decodeFromString(it) }
}
