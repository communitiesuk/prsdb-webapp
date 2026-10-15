package uk.gov.communities.prsdb.webapp.performance

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

class GatlingTestRunner(private val outputDirectory: Path) {
    data class Result(
        val exitCode: Int,
        val output: String,
    )

    fun run(
        simulation: Class<*>,
        properties: Map<String, String>,
    ): Result {
        val classpath = requireNotNull(System.getProperty("performance.test.classpath"))
        val logFile = outputDirectory.resolve("gatling.log").toFile()
        val command =
            listOf(Path.of(System.getProperty("java.home"), "bin", "java").toString()) +
                properties.map { (key, value) -> "-D$key=$value" } +
                listOf(
                    "--add-opens=java.base/java.lang=ALL-UNNAMED",
                    "-cp",
                    classpath,
                    "io.gatling.app.Gatling",
                    "-s",
                    simulation.name,
                    "-rf",
                    outputDirectory.toString(),
                    "-nr",
                )
        val process =
            ProcessBuilder(command)
                .redirectErrorStream(true)
                .redirectOutput(logFile)
                .start()
        try {
            assertTrue(process.waitFor(60, TimeUnit.SECONDS), "Gatling timed out; see $logFile")
            return Result(process.exitValue(), Files.readString(logFile.toPath()))
        } finally {
            if (process.isAlive) {
                process.destroyForcibly()
                process.waitFor(10, TimeUnit.SECONDS)
            }
        }
    }
}
