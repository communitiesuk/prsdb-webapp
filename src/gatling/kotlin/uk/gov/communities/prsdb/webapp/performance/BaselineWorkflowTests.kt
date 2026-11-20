package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BaselineWorkflowTests {
    @Test
    fun `workflow ramp supplies all mandatory basic configuration`() {
        val workflow = Files.readString(Path.of(".github/workflows/run-performance-tests.yml"))
        val command =
            workflow.substringAfter("--simulation ${BaselineLoadSimulation::class.java.name}")
                .substringBefore("\n      - name:")
        val arguments =
            command.lineSequence().map { it.trim().removeSuffix("\\").trim() }
                .filter { it.startsWith("-Dgatling.") }
                .associate {
                    val (key, value) = it.removePrefix("-D").split("=", limit = 2)
                    key to value.removeSurrounding("\"")
                }
        val config = BasicRunConfig.from(arguments)
        assertEquals(PerformanceTarget.NFT, config.target)
        assertTrue(config.landlordSubject.isNotBlank())
        val mandatory =
            listOf(
                "gatling.basic.registrationSubject",
                "gatling.saved.fixturePath",
                "gatling.property.fixturePath",
                "gatling.organisation.fixturePath",
                "gatling.organisation.operationsFixturePath",
                "gatling.organisation.runId",
                "gatling.joint.fixturePath",
                "gatling.agent.accessFixturePath",
                "gatling.agent.password",
                "gatling.operator.fixturePath",
                "gatling.offboarding.fixturePath",
                "gatling.registration.certificatePath",
                "gatling.registration.fixturePath",
                "gatling.registration.paymentBaseUrl",
                "gatling.registration.fixtureOffset",
                "gatling.council.fixturePath",
                "gatling.council.simulatorUrl",
                "gatling.council.fixtureOffset",
                "gatling.certificate.downloadOrigin",
                "gatling.ramp.rampMinutes",
                "gatling.ramp.holdMinutes",
                "gatling.ramp.registrationsPerMinute",
                "gatling.ramp.updatesPerMinute",
                "gatling.ramp.additionalPerMinute",
                "gatling.ramp.fixtureOffset",
                "gatling.ramp.operatorFixtureOffset",
                "gatling.ramp.paidRegistrationsPerMinute",
                "gatling.ramp.councilReadsPerMinute",
            )
        mandatory.forEach { key -> assertTrue(!arguments[key].isNullOrBlank(), "Missing required workflow argument $key") }
        assertEquals("true", arguments["gatling.council.emailDeliveryStub"])
        val schedules = BaselineLoadProfile.schedules(arguments).mapValues { it.value.arrivalTimesMillis().size }
        val updates = schedules.getValue(BaselineFamily.PROPERTY)
        val compliance = schedules.getValue(BaselineFamily.COMPLIANCE)
        val team = schedules.getValue(BaselineFamily.ORGANISATION_TEAM)
        val operators = schedules.getValue(BaselineFamily.OPERATOR)
        val offset = arguments.getValue("gatling.ramp.fixtureOffset").toInt()
        val operatorOffset = arguments.getValue("gatling.ramp.operatorFixtureOffset").toInt()
        assertTrue(offset >= 2 * (config.iterations + 1), "Ramp must skip property-update and certificate correctness allocations")
        assertTrue(offset + schedules.getValue(BaselineFamily.SAVED) <= 101, "Saved fixture pool must contain all ramp actors")
        assertTrue(
            offset + schedules.getValue(BaselineFamily.ORGANISATION_PROFILE) <= 101,
            "Organisation profile pool must contain all ramp actors",
        )
        assertTrue(offset + updates + compliance <= 121, "Completed property pool must include exclusive certificate actors")
        assertTrue(operatorOffset >= config.iterations + 1, "Operator ramp must skip correctness actors")
        assertTrue(operatorOffset + operators <= 28, "Operator correctness and ramp must fit the minimum exported pool")
        assertTrue(team <= 17, "Organisation team ramp must fit its exclusive exported pool")
        assertEquals(767, schedules.filterKeys { it != BaselineFamily.PAID_REGISTRATION && it != BaselineFamily.COUNCIL_READ }.values.sum())
        assertEquals(455, schedules.getValue(BaselineFamily.PAID_REGISTRATION))
        assertEquals(17, schedules.getValue(BaselineFamily.COUNCIL_READ))
        assertEquals(1239, schedules.values.sum())
        assertEquals("1", arguments["gatling.registration.fixtureOffset"])
        assertEquals("1", arguments["gatling.council.fixtureOffset"])
    }

    @Test
    fun `workflow exports paid and council fixtures before the baseline ramp`() {
        val workflow = Files.readString(Path.of(".github/workflows/run-performance-tests.yml"))
        val rampStart = workflow.indexOf("id: rampedJourneys")
        assertTrue(rampStart >= 0, "The expanded baseline ramp must have a stable step id")
        val ramp = workflow.substring(rampStart).substringBefore("\n      - name:")

        for ((id, mode) in listOf(
            "exportPaidRegistrationFixtures" to "paid-registration-fixtures",
            "exportCouncilReadFixtures" to "council-read-fixtures",
        )) {
            val exportStep = workflow.indexOf("id: $id")
            assertTrue(exportStep >= 0 && exportStep < rampStart, "$id must be exported before the ramp")
            val step = workflow.substring(exportStep, rampStart)
            assertTrue("continue-on-error: true" in step)
            assertTrue("mode: $mode" in step)
            assertTrue("steps.$id.outcome == 'success'" in ramp, "$id must gate the ramp")
        }

        val rampCommand =
            workflow.substringAfter(
                "--simulation ${BaselineLoadSimulation::class.java.name}",
            ).substringBefore("\n      - name:")
        assertTrue("gatling.registration.fixturePath" in rampCommand)
        assertTrue("gatling.registration.paymentBaseUrl" in rampCommand)
        assertTrue("gatling.council.fixturePath" in rampCommand)
        assertTrue("gatling.council.simulatorUrl" in rampCommand)

        val summary = workflow.substringAfter("- name: Summarise journey outcomes")
        assertTrue("EXPORT_PAID_REGISTRATION: \${{ steps.exportPaidRegistrationFixtures.outcome }}" in summary)
        assertTrue("EXPORT_COUNCIL_READ: \${{ steps.exportCouncilReadFixtures.outcome }}" in summary)
    }
}
