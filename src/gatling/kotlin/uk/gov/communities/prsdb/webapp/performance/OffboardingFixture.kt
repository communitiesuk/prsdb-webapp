package uk.gov.communities.prsdb.webapp.performance

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

enum class OffboardingAction(
    val value: String,
) {
    LEAVE_PROPERTY("leave-property"),
    SWITCH_TO_INDIVIDUAL("switch-to-individual"),
    DEREGISTER_PROPERTY("deregister-property"),
    DEREGISTER_LANDLORD("deregister-landlord"),
}

data class OffboardingFixture(
    val action: OffboardingAction,
    val subject: String,
    val propertyOwnershipId: Long?,
    val address: String?,
) {
    companion object {
        fun parse(
            json: String,
            required: Int,
        ): List<OffboardingFixture> {
            require(required > 0) { "At least one offboarding fixture is required" }
            val root = ObjectMapper().readTree(json)
            require(root.isArray) { "Offboarding fixtures must be a JSON array" }
            val fixtures = root.map(::parseFixture)
            require(fixtures.size >= required) {
                "Not enough distinct offboarding fixtures: need $required, found ${fixtures.size}"
            }
            require(fixtures.map { it.subject }.toSet().size == fixtures.size) {
                "Offboarding subjects must not be shared"
            }
            val propertyIds = fixtures.mapNotNull { it.propertyOwnershipId }
            require(propertyIds.toSet().size == propertyIds.size) {
                "Offboarding properties must not be reused"
            }
            return fixtures.take(required)
        }

        private fun parseFixture(node: JsonNode): OffboardingFixture {
            require(node.isObject) { "Every offboarding fixture must be an object" }
            val actionName = requiredText(node, "action")
            val action =
                OffboardingAction.entries.singleOrNull { it.value == actionName }
                    ?: throw IllegalArgumentException("Unsupported offboarding action: $actionName")
            val propertyOwnershipId = optionalPositiveLong(node, "propertyOwnershipId")
            val address = optionalText(node, "address")
            if (action == OffboardingAction.DEREGISTER_LANDLORD) {
                require(propertyOwnershipId == null && address == null) {
                    "Landlord deregistration fixtures must not include a property"
                }
            } else {
                require(propertyOwnershipId != null && !address.isNullOrBlank()) {
                    "Property offboarding fixtures must include a property and address"
                }
            }
            return OffboardingFixture(action, requiredText(node, "subject"), propertyOwnershipId, address)
        }

        private fun requiredText(
            node: JsonNode,
            name: String,
        ): String {
            val value = node.get(name)
            require(value != null && value.isTextual && value.asText().isNotBlank()) {
                "Missing or invalid fixture field: $name"
            }
            return value.asText()
        }

        private fun requiredPositiveLong(
            node: JsonNode,
            name: String,
        ): Long {
            val value = node.get(name)
            require(value != null && value.isIntegralNumber && value.canConvertToLong() && value.asLong() > 0) {
                "Missing or invalid fixture field: $name"
            }
            return value.asLong()
        }

        private fun optionalPositiveLong(
            node: JsonNode,
            name: String,
        ): Long? {
            val value = node.get(name) ?: return null
            require(value.isIntegralNumber && value.canConvertToLong() && value.asLong() > 0) {
                "Invalid fixture field: $name"
            }
            return value.asLong()
        }

        private fun optionalText(
            node: JsonNode,
            name: String,
        ): String? {
            val value = node.get(name) ?: return null
            require(value.isTextual && value.asText().isNotBlank()) { "Invalid fixture field: $name" }
            return value.asText()
        }
    }
}
