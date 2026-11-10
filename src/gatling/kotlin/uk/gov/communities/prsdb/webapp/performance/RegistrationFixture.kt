package uk.gov.communities.prsdb.webapp.performance

import com.fasterxml.jackson.databind.ObjectMapper

data class RegistrationFixture(
    val subject: String,
    val address: String,
    val postcode: String,
    val building: String,
    val localCouncilId: Int,
    val uprn: Long,
) {
    companion object {
        fun parse(
            json: String,
            required: Int,
        ): List<RegistrationFixture> {
            require(required in 1..MAX_FIXTURES) { "Registration allocations must contain 1 to $MAX_FIXTURES actors" }
            val root = ObjectMapper().readTree(json)
            require(root.isArray && root.size() <= MAX_FIXTURES) { "Registration fixtures must be a bounded JSON array" }
            val fixtures =
                root.map { fields ->
                    fun text(name: String): String {
                        val node = fields.get(name)
                        require(node != null && node.isTextual && node.asText().isNotBlank()) { "Missing registration fixture: $name" }
                        return node.asText().also { require(it.none { char -> char.isISOControl() }) }
                    }

                    fun number(name: String): Long {
                        val node = fields.get(name)
                        require(node != null && node.isIntegralNumber && node.canConvertToLong() && node.asLong() > 0) {
                            "Registration fixture $name must be a positive integer"
                        }
                        return node.asLong()
                    }
                    val subject = text("subject")
                    require(subject.matches(Regex("[A-Za-z0-9:._-]+"))) { "Unsafe registration subject" }
                    val council = number("localCouncilId")
                    require(council <= Int.MAX_VALUE)
                    RegistrationFixture(subject, text("address"), text("postcode"), text("building"), council.toInt(), number("uprn"))
                }
            require(fixtures.size >= required) { "Need $required distinct registration allocations; found ${fixtures.size}" }
            require(fixtures.map { it.subject }.distinct().size == fixtures.size) { "Registration actors must not be shared" }
            require(fixtures.map { it.uprn }.distinct().size == fixtures.size) { "Registration UPRNs must not be reused" }
            require(fixtures.map { it.address.trim().lowercase() }.distinct().size == fixtures.size) {
                "Registration addresses must not be reused"
            }
            return fixtures.take(required)
        }

        private const val MAX_FIXTURES = 456
    }
}
