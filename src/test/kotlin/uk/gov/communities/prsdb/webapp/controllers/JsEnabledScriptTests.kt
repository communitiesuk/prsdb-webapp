package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE

// If these fail, the govuk-frontend-supported snippet in layout.html has been moved, removed, or has lost its
// nonce. Without it applied before the page content, conditional reveals and tabs render open until the JS
// bundle loads.
@WebMvcTest(CookiesController::class)
class JsEnabledScriptTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `the govuk-frontend-supported script is rendered before any page content`() {
        val response = renderPage()

        assertTrue(
            SNIPPET_AT_TOP_OF_BODY_REGEX.containsMatchIn(response.contentAsString),
            "The govuk-frontend-supported snippet must be the first element inside <body>.",
        )
    }

    @Test
    fun `the govuk-frontend-supported script is given the nonce that the content security policy allows`() {
        val response = renderPage()
        val scriptNonce = scriptNonceOf(response)
        val policyNonce = policyNonceOf(response)

        assertTrue(
            policyNonce.isNotEmpty(),
            "The content security policy did not contain a nonce, so every inline script would be blocked.",
        )
        assertEquals(
            policyNonce,
            scriptNonce,
            "The govuk-frontend-supported snippet's nonce does not match the one the content security policy " +
                "allows, so browsers would block it and conditional reveals would stay open.",
        )
    }

    private fun renderPage() = mvc.get(COOKIES_ROUTE).andReturn().response

    private fun scriptNonceOf(response: MockHttpServletResponse): String {
        val snippet = SNIPPET_AT_TOP_OF_BODY_REGEX.find(response.contentAsString)?.value.orEmpty()
        return NONCE_ATTRIBUTE_REGEX.find(snippet)?.groupValues?.get(1).orEmpty()
    }

    private fun policyNonceOf(response: MockHttpServletResponse): String {
        val policy = response.getHeader(CONTENT_SECURITY_POLICY_HEADER).orEmpty()
        return POLICY_NONCE_REGEX.find(policy)?.groupValues?.get(1).orEmpty()
    }

    companion object {
        private const val CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy"

        private val SNIPPET_AT_TOP_OF_BODY_REGEX =
            Regex("""<body[^>]*>\s*<script[^>]*>[^<]*govuk-frontend-supported[^<]*</script>""")
        private val NONCE_ATTRIBUTE_REGEX = Regex("nonce=\"([^\"]+)\"")
        private val POLICY_NONCE_REGEX = Regex("'nonce-([^']+)'")
    }
}
