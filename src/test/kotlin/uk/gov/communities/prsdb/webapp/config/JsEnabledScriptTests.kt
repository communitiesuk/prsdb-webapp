package uk.gov.communities.prsdb.webapp.config

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

// If this fails, the govuk-frontend-supported snippet has been moved or removed from layout.html. Without it
// applied before the page content, conditional reveals and tabs render open until the JS bundle loads.
class JsEnabledScriptTests {
    @Test
    fun `layout adds the govuk-frontend-supported class before any page content`() {
        val layout = readClasspathFile(LAYOUT_TEMPLATE)

        assertTrue(
            SNIPPET_AT_TOP_OF_BODY_REGEX.containsMatchIn(layout),
            "The govuk-frontend-supported snippet must be the first element inside <body> in $LAYOUT_TEMPLATE.",
        )
    }

    @Test
    fun `the snippet is served with a nonce so the content security policy does not block it`() {
        val layout = readClasspathFile(LAYOUT_TEMPLATE)
        val snippet = SNIPPET_AT_TOP_OF_BODY_REGEX.find(layout)?.value.orEmpty()

        assertTrue(
            snippet.contains("th:nonce"),
            "The govuk-frontend-supported snippet needs th:nonce, or the content security policy will block it.",
        )
    }

    private fun readClasspathFile(path: String) =
        javaClass.getResource(path)?.readText() ?: throw IllegalStateException("$path was not found on the classpath")

    companion object {
        private const val LAYOUT_TEMPLATE = "/templates/fragments/layout.html"

        private val SNIPPET_AT_TOP_OF_BODY_REGEX =
            Regex("""<body[^>]*>\s*<script[^>]*>[^<]*govuk-frontend-supported[^<]*</script>""")
    }
}
