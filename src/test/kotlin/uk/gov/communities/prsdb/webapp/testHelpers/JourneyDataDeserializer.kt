package uk.gov.communities.prsdb.webapp.testHelpers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import java.time.LocalDate
import java.time.format.DateTimeParseException

class JourneyDataDeserializer : StdDeserializer<JourneyData>(Map::class.java) {
    override fun deserialize(
        parser: JsonParser,
        context: DeserializationContext,
    ): JourneyData {
        val node = parser.codec.readTree<JsonNode>(parser)
        return node.fields().asSequence().associate { (key, value) -> (key to value.deserializeValue(parser, context)) }
    }

    private fun JsonNode.deserializeValue(
        parser: JsonParser,
        context: DeserializationContext,
    ): Any? =
        when {
            this.isTextual -> {
                try {
                    LocalDate.parse(this.asText())
                } catch (e: DateTimeParseException) {
                    this.asText()
                }
            }
            this.isNumber -> this.numberValue()
            this.isBoolean -> this.booleanValue()
            this.isObject -> deserialize(this.traverse(parser.codec), context)
            else -> this
        }
}
