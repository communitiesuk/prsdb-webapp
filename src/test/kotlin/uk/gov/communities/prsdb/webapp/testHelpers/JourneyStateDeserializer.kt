package uk.gov.communities.prsdb.webapp.testHelpers

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.deser.std.StdDeserializer
import uk.gov.communities.prsdb.webapp.journeys.FormData
import java.time.LocalDate
import java.time.format.DateTimeParseException

class JourneyStateDeserializer : StdDeserializer<FormData>(Map::class.java) {
    override fun deserialize(
        parser: JsonParser,
        context: DeserializationContext,
    ): FormData {
        val node = context.readTree(parser)
        return node.properties().associate { (key, value) -> (key to value.deserializeValue(parser, context)) }
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

            this.isNumber -> {
                this.numberValue()
            }

            this.isBoolean -> {
                this.booleanValue()
            }

            this.isNull -> {
                null
            }

            this.isArray -> {
                this.mapTo(mutableListOf()) { it.deserializeValue(parser, context) }
            }

            this.isObject -> {
                deserialize(this.traverse(parser.objectReadContext()), context)
            }

            else -> {
                this
            }
        }
}
