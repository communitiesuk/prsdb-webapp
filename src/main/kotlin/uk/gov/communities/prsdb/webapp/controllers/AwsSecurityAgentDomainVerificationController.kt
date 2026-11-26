package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.AWS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.AWS_SECURITY_AGENT_DOMAIN_VERIFICATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.WELL_KNOWN_PATH_SEGMENT

@PrsdbController
@RequestMapping(AwsSecurityAgentDomainVerificationController.AWS_SECURITY_AGENT_DOMAIN_VERIFICATION_ROUTE)
class AwsSecurityAgentDomainVerificationController(
    @Value("\${aws-security-agent.domain-verification-tokens:}") private val tokens: List<String>,
) {
    @GetMapping
    fun getDomainVerificationTokens(): ResponseEntity<Map<String, List<String>>> {
        if (tokens.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("tokens" to tokens))
    }

    companion object {
        const val AWS_SECURITY_AGENT_DOMAIN_VERIFICATION_ROUTE =
            "/$WELL_KNOWN_PATH_SEGMENT/$AWS_PATH_SEGMENT/$AWS_SECURITY_AGENT_DOMAIN_VERIFICATION_PATH_SEGMENT"
    }
}
