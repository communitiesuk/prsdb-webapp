package uk.gov.communities.prsdb.webapp.performance

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object RegistrationDestination {
    const val ROUTE = "/landlord/register-property"
    private val permittedQuery = setOf("journeyId", "withBackUrl", "memberId", "paymentReference")

    fun resolve(
        value: String,
        baseUrl: String,
        journeyId: String?,
        currentUrl: String? = null,
    ): String {
        val resolved = HttpDestination.resolve(value, baseUrl, currentUrl ?: baseUrl)
        val uri = URI.create(resolved)
        require(uri.path.startsWith("$ROUTE/") && !uri.path.contains("..")) { "Unexpected registration path: ${uri.path}" }
        val parameters = parameters(uri)
        require(parameters.keys.all { it in permittedQuery }) { "Unexpected registration query: ${uri.rawQuery}" }
        require(parameters.values.all { it.size == 1 }) { "Duplicated registration query parameter" }
        for (name in listOf("withBackUrl", "memberId")) {
            parameters[name]?.single()?.let { require(it.toIntOrNull()?.let { value -> value >= 0 } == true) { "Invalid $name" } }
        }
        parameters["journeyId"]?.single()?.let { require(it.matches(Regex("[A-Za-z0-9._-]+"))) { "Unsafe registration journey ID" } }
        if (uri.path == "$ROUTE/confirmation") {
            require(parameters.keys.all { it == "withBackUrl" }) { "Confirmation must not carry a different journey" }
        } else if (journeyId != null) {
            require(parameters["journeyId"] == listOf(journeyId)) { "Registration journey changed or disappeared" }
        }
        return resolved
    }

    fun journeyId(url: String): String? = parameters(URI.create(url))["journeyId"]?.singleOrNull()

    fun hiddenFields(
        names: List<String>,
        values: List<String>,
    ): Map<String, String> {
        require(names.size == values.size && names.distinct().size == names.size) { "Ambiguous or missing hidden fields" }
        val fields = names.zip(values).toMap()
        require(!fields["_csrf"].isNullOrBlank()) { "A non-empty correlated CSRF token is required before mutation" }
        return fields
    }

    private fun parameters(uri: URI): Map<String, List<String>> =
        uri.rawQuery?.split('&')?.filter { it.isNotBlank() }?.map { pair ->
            val parts = pair.split('=', limit = 2)
            URLDecoder.decode(parts[0], StandardCharsets.UTF_8) to
                URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
        }?.groupBy({ it.first }, { it.second }) ?: emptyMap()
}
