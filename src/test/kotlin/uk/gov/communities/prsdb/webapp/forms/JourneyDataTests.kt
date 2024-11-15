package uk.gov.communities.prsdb.webapp.forms

import uk.gov.communities.prsdb.webapp.forms.journeys.objectToStringKeyedMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JourneyDataTests {
    @Test
    fun `objectToStringKeyedMap casts map from object back to map`() {
        // Arrange
        val key = "testKey"
        val value = "testValue"
        val mapAsObject: Any = mutableMapOf(key to value)

        // Act
        val reconstructedMap = objectToStringKeyedMap(mapAsObject)

        // Assert
        assertEquals(reconstructedMap?.get(key), value)
    }

    @Test
    fun `objectToStringKeyedMap returns null when the object is null`() {
        // Arrange and Act
        val reconstructedMap = objectToStringKeyedMap(null)

        // Assert
        assertNull(reconstructedMap)
    }

    @Test
    fun `objectToStringKeyedMap returns null when the object cannot be cast to a map`() {
        // Arrange
        val notAMap: Any = "not a map"

        // Act
        val reconstructedMap = objectToStringKeyedMap(notAMap)

        // Assert
        assertNull(reconstructedMap)
    }

    @Test
    fun `objectToStringKeyedMap returns null when the map keys are not strings`() {
        // Arrange
        val intKeyedMap: Any = mutableMapOf(1 to "one", 2 to "two", 3 to "three")

        // Act
        val reconstructedMap = objectToStringKeyedMap(intKeyedMap)

        // Assert
        assertNull(reconstructedMap)
    }
}
