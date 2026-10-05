package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.FORM_SUBMISSION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.GATLING_POST_ENDPOINT
import uk.gov.communities.prsdb.webapp.constants.PERFORMANCE_TEST_PATH_SEGMENT

/**
 * Public, feature-flagged endpoint used only as a Gatling performance-test target (PDJB-431).
 *
 * Enabled only in the NFT environment (performance tests only ever run there). The GET page renders a minimal
 * form with a real CSRF token so simulations can exercise the realistic extract-token-then-POST flow used by
 * genuine webapp journeys. The POST handler is a no-op (no persistence) — it exists purely to be a submission
 * target.
 */
@PrsdbController
@RequestMapping(PerformanceTestFormSubmissionController.PERFORMANCE_TEST_FORM_SUBMISSION_ROUTE)
class PerformanceTestFormSubmissionController {
    @GetMapping
    @AvailableWhenFeatureEnabled(GATLING_POST_ENDPOINT)
    fun getForm(csrfToken: CsrfToken): ResponseEntity<String> {
        val body =
            """
            <!DOCTYPE html>
            <html lang="en">
            <body>
            <form method="post" action="$PERFORMANCE_TEST_FORM_SUBMISSION_ROUTE">
                <input type="hidden" name="${csrfToken.parameterName}" value="${csrfToken.token}"/>
                <button type="submit">Submit</button>
            </form>
            </body>
            </html>
            """.trimIndent()

        return ResponseEntity
            .ok()
            .contentType(MediaType.TEXT_HTML)
            .body(body)
    }

    @PostMapping
    @AvailableWhenFeatureEnabled(GATLING_POST_ENDPOINT)
    fun submitForm(): ResponseEntity<String> =
        ResponseEntity
            .ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body("Submission received (no-op)")

    companion object {
        const val PERFORMANCE_TEST_FORM_SUBMISSION_ROUTE =
            "/$PERFORMANCE_TEST_PATH_SEGMENT/$FORM_SUBMISSION_PATH_SEGMENT"
    }
}
