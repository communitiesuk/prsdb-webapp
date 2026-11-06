package uk.gov.communities.prsdb.webapp.performance

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.communities.prsdb.webapp.performance.CouncilFixtureFields.boolean
import uk.gov.communities.prsdb.webapp.performance.CouncilFixtureFields.id
import uk.gov.communities.prsdb.webapp.performance.CouncilFixtureFields.text

data class CouncilFixture(
    val subject: String,
    val actorName: String,
    val councilId: Long,
    val councilName: String,
    val targetId: Long,
    val targetName: String,
    val targetEmail: String,
    val targetSubject: String,
    val targetManager: Boolean,
    val propertyId: Long,
    val address: String,
    val uprn: String,
    val propertyRegistrationNumber: Long,
    val landlordId: Long,
    val landlordName: String,
    val landlordRegistrationNumber: Long,
    val licenseType: Int?,
) {
    companion object {
        fun parse(
            json: String,
            required: Int,
        ): List<CouncilFixture> {
            val rows = CouncilFixtureFields.rows(json, required)
            val fixtures =
                rows.map {
                    CouncilFixture(
                        it.text("subject"), it.text("actorName"), it.id("councilId"), it.text("councilName"),
                        it.id("targetId"), it.text("targetName"), it.text("targetEmail"), it.text("targetSubject"),
                        it.boolean("targetManager"), it.id("propertyId"), it.text("address"), it.text("uprn"),
                        it.id("propertyRegistrationNumber"), it.id("landlordId"), it.text("landlordName"),
                        it.id("landlordRegistrationNumber"), it.get("licenseType")?.takeUnless(JsonNode::isNull)?.asInt(),
                    )
                }
            require(fixtures.map { it.councilId }.distinct().size == fixtures.size) { "Councils must not share mutated users" }
            require(fixtures.map { it.targetId }.distinct().size == fixtures.size) { "Mutation targets must be exclusive" }
            require(fixtures.none { target -> fixtures.any { it.subject == target.targetSubject } }) { "Actors cannot be mutation targets" }
            return fixtures.take(required)
        }
    }
}

internal object CouncilFixtureFields {
    fun rows(
        json: String,
        required: Int,
    ): List<JsonNode> {
        require(required > 0) { "At least one fixture is required" }
        val root = ObjectMapper().readTree(json)
        require(root.isArray) { "Fixtures must be a JSON array" }
        val rows = root.toList()
        require(rows.size >= required) { "Not enough distinct role-correct fixtures: need $required, found ${rows.size}" }
        require(rows.map { it.text("subject") }.distinct().size == rows.size) { "Actors cannot be reused" }
        return rows
    }

    fun JsonNode.text(name: String): String {
        val value = get(name)
        require(value != null && value.isTextual && value.asText().isNotBlank()) { "Missing fixture field: $name" }
        return value.asText()
    }

    fun JsonNode.id(name: String): Long {
        val value = get(name)
        require(value != null && value.isIntegralNumber && value.canConvertToLong() && value.asLong() > 0) {
            "A positive numeric $name is required"
        }
        return value.asLong()
    }

    fun JsonNode.boolean(name: String): Boolean {
        val value = get(name)
        require(value != null && value.isBoolean) { "A boolean $name is required" }
        return value.asBoolean()
    }
}
