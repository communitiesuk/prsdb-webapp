package uk.gov.communities.prsdb.webapp.database.entity

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import kotlin.test.assertEquals

class FormContextTests {
    @Test
    fun `toJourneyData returns form context as journey data`() {
        val journeyData =
            mapOf(
                "testPage" to mapOf("testKey" to "testValue"),
            )
        val serializedJourneyData = ObjectMapper().writeValueAsString(journeyData)
        val formContext = FormContext(JourneyType.PROPERTY_REGISTRATION, serializedJourneyData, OneLoginUser())

        val result = formContext.toJourneyData()

        assertEquals(journeyData, result)
    }
}
