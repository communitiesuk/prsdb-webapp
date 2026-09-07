package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.PASSWORD_BENCHMARK_ENDPOINT
import uk.gov.communities.prsdb.webapp.constants.PASSWORD_BENCHMARK_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@PrsdbController
@RequestMapping(PasswordBenchmarkController.PASSWORD_BENCHMARK_ROUTE)
class PasswordBenchmarkController(
    private val defaultPasswordEncoder: PasswordEncoder,
    @Value("\${argon2.iterations}") private val defaultIterations: Int,
    @Value("\${argon2.memory}") private val defaultMemory: Int,
    @Value("\${argon2.parallelism}") private val defaultParallelism: Int,
) {
    @GetMapping
    @AvailableWhenFeatureEnabled(PASSWORD_BENCHMARK_ENDPOINT)
    fun benchmark(
        @RequestParam(required = false) iterations: Int?,
        @RequestParam(required = false) memory: Int?,
        @RequestParam(required = false) parallelism: Int?,
        @RequestParam(required = false) hashes: Int?,
    ): ResponseEntity<String> {
        val effectiveIterations = iterations ?: defaultIterations
        val effectiveMemory = memory ?: defaultMemory
        val effectiveParallelism = parallelism ?: defaultParallelism
        val effectiveHashCount = hashes ?: DEFAULT_HASH_COUNT

        val encoder =
            if (iterations == null && memory == null && parallelism == null) {
                defaultPasswordEncoder
            } else {
                Argon2PasswordEncoder(
                    ARGON2_SALT_LENGTH,
                    ARGON2_HASH_LENGTH,
                    effectiveParallelism,
                    effectiveMemory,
                    effectiveIterations,
                )
            }

        val durationsNanos = LongArray(effectiveHashCount)
        for (i in 0 until effectiveHashCount) {
            val start = System.nanoTime()
            encoder.encode(BENCHMARK_PASSWORD)
            durationsNanos[i] = System.nanoTime() - start
        }

        val totalSeconds = durationsNanos.sum() / 1_000_000_000.0
        val averageSeconds = totalSeconds / effectiveHashCount
        val minSeconds = durationsNanos.min() / 1_000_000_000.0
        val maxSeconds = durationsNanos.max() / 1_000_000_000.0

        val body =
            buildString {
                appendLine("Argon2 benchmark: $effectiveHashCount hashes")
                appendLine(
                    "iterations=$effectiveIterations, memory=$effectiveMemory, parallelism=$effectiveParallelism",
                )
                appendLine("total: %.3f s".format(totalSeconds))
                appendLine("average: %.3f s".format(averageSeconds))
                appendLine("min: %.3f s".format(minSeconds))
                appendLine("max: %.3f s".format(maxSeconds))
                appendLine(
                    "reproduce with ?iterations=$effectiveIterations" +
                        "&memory=$effectiveMemory" +
                        "&parallelism=$effectiveParallelism" +
                        "&hashes=$effectiveHashCount",
                )
            }

        return ResponseEntity
            .ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(body)
    }

    companion object {
        const val PASSWORD_BENCHMARK_ROUTE =
            "/$SYSTEM_OPERATOR_PATH_SEGMENT/$PASSWORD_BENCHMARK_PATH_SEGMENT"

        private const val DEFAULT_HASH_COUNT = 50
        private const val BENCHMARK_PASSWORD = "password-benchmark-input"
        private const val ARGON2_SALT_LENGTH = 16
        private const val ARGON2_HASH_LENGTH = 32
    }
}
