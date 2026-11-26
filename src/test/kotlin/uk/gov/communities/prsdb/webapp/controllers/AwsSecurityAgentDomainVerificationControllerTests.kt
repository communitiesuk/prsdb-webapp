package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.controllers.AwsSecurityAgentDomainVerificationController.Companion.AWS_SECURITY_AGENT_DOMAIN_VERIFICATION_ROUTE

@WebMvcTest(AwsSecurityAgentDomainVerificationController::class)
@TestPropertySource(properties = ["aws-security-agent.domain-verification-tokens=token-one,token-two"])
class AwsSecurityAgentDomainVerificationControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `GET verification route returns the configured tokens without requiring authentication`() {
        mvc
            .get(AWS_SECURITY_AGENT_DOMAIN_VERIFICATION_ROUTE)
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { string("""{"tokens":["token-one","token-two"]}""") }
            }
    }

    @Test
    fun `GET verification route returns 404 when no tokens are configured`() {
        val controller = AwsSecurityAgentDomainVerificationController(emptyList())

        val exception = assertThrows<ResponseStatusException> { controller.getDomainVerificationTokens() }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }
}
