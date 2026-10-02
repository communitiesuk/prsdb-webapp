package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.Assertion
import io.gatling.javaapi.core.CoreDsl.details
import io.gatling.javaapi.core.CoreDsl.global

object BasicPerformanceAssertions {
    fun forRequests(
        config: BasicRunConfig,
        requestNames: List<String>,
    ): List<Assertion> {
        require(requestNames.isNotEmpty() && requestNames.all { it.isNotBlank() }) {
            "At least one named application request must be measured"
        }
        val assertions = mutableListOf(global().failedRequests().count().`is`(0L))
        requestNames.forEach { name ->
            assertions.add(details(name).allRequests().count().`is`(1L))
        }
        config.limits?.let { limits ->
            requestNames.forEach { name ->
                assertions.add(details(name).responseTime().max().lte(limits.maxMs))
                assertions.add(details(name).responseTime().mean().lte(limits.meanMs))
            }
        }
        return assertions
    }
}
