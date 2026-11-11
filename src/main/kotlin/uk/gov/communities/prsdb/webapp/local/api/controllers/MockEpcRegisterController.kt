package uk.gov.communities.prsdb.webapp.local.api.controllers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbRestController
import uk.gov.communities.prsdb.webapp.services.PerformanceTestEpcService
import java.util.Base64

@Profile("performance-test & nft & !production")
@PrsdbRestController
@PreAuthorize("permitAll()")
@RequestMapping("/local/epc")
class MockEpcRegisterController(
    private val service: PerformanceTestEpcService,
) {
    @GetMapping("/health")
    fun health() = mapOf("status" to "UP", "dependency" to "synthetic-epc")

    @PostMapping("/auth/oauth/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun token(
        request: HttpServletRequest,
        @RequestParam parameters: MultiValueMap<String, String>,
    ): ResponseEntity<Any> {
        if (!isLoopback(request)) return error(HttpStatus.FORBIDDEN, "FORBIDDEN")
        val credentials = Base64.getEncoder().encodeToString("performance-test:performance-test".toByteArray())
        if (request.getHeader(HttpHeaders.AUTHORIZATION) != "Basic $credentials") {
            return error(HttpStatus.UNAUTHORIZED, "INVALID_CLIENT")
        }
        if (parameters.keys != setOf("grant_type") || parameters["grant_type"] != listOf("client_credentials")) {
            return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
        }
        return ResponseEntity.ok(
            mapOf("access_token" to TOKEN, "token_type" to "Bearer", "expires_in" to 3600),
        )
    }

    @GetMapping("/api/prsdatabase/assessments/search")
    fun search(
        request: HttpServletRequest,
        @RequestParam parameters: MultiValueMap<String, String>,
    ): ResponseEntity<Any> {
        if (!isLoopback(request)) return error(HttpStatus.FORBIDDEN, "FORBIDDEN")
        if (request.getHeader(HttpHeaders.AUTHORIZATION) != "Bearer $TOKEN") {
            return error(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN")
        }
        if (parameters.size > 1 || parameters.keys.any { it !in setOf("uprn", "rrn") } ||
            parameters.values.any { it.size != 1 }
        ) {
            return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
        }
        val result =
            try {
                service.search(parameters.getFirst("uprn"), parameters.getFirst("rrn"))
            } catch (_: IllegalArgumentException) {
                return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
            }
        return result?.let { ResponseEntity.ok(it) } ?: error(HttpStatus.NOT_FOUND, "NOT_FOUND")
    }

    private fun isLoopback(request: HttpServletRequest) = request.remoteAddr in setOf("127.0.0.1", "::1", "0:0:0:0:0:0:0:1")

    private fun error(
        status: HttpStatus,
        code: String,
    ): ResponseEntity<Any> =
        ResponseEntity.status(status).body(mapOf("errors" to listOf(mapOf("code" to code, "title" to "Synthetic EPC request rejected"))))

    private companion object {
        const val TOKEN = "performance-test-epc-token"
    }
}
