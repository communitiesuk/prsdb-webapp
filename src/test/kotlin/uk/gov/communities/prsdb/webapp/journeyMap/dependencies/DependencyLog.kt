package uk.gov.communities.prsdb.webapp.journeyMap.dependencies

import kotlinx.serialization.Serializable

@Serializable
data class Dependencies(
    val reads: Map<String, List<String>> = emptyMap(),
    val outcomeReads: Map<String, List<String>> = emptyMap(),
    val writers: Map<String, List<String>> = emptyMap(),
    val outcomesPassed: Map<String, Map<String, List<String>>> = emptyMap(),
) {
    // The outcomes each step was passed with in every journey except those given
    fun outcomesPassedOutside(journeyIds: Set<String>): Map<String, List<String>> {
        val merged = sortedMapOf<String, MutableSet<String>>()
        outcomesPassed
            .filterKeys { it !in journeyIds }
            .values
            .forEach { steps -> steps.forEach { (step, modes) -> merged.getOrPut(step) { sortedSetOf() } += modes } }
        return merged.mapValues { it.value.toList() }
    }
}

class DependencyLog {
    private val reads = mutableMapOf<String, MutableSet<String>>()
    private val outcomeReads = mutableMapOf<String, MutableSet<String>>()
    private val writers = mutableMapOf<String, MutableSet<String>>()
    private val outcomesPassed = mutableMapOf<String, MutableMap<String, MutableSet<String>>>()

    @Synchronized
    fun read(
        readingSteps: Collection<String>,
        key: String,
    ) {
        readingSteps.forEach { reads.getOrPut(it) { sortedSetOf() } += key }
    }

    @Synchronized
    fun outcomeRead(
        readingStep: String?,
        readStep: String,
    ) {
        if (readingStep != null && readingStep != readStep) outcomeReads.getOrPut(readingStep) { sortedSetOf() } += readStep
    }

    @Synchronized
    fun written(
        writingStep: String?,
        key: String,
    ) {
        if (writingStep != null) writers.getOrPut(key) { sortedSetOf() } += writingStep
    }

    // The journey moving on from a step with an outcome means it took that outcome's arrow in the routing code. Outcomes
    // are kept for each journey, so that journeys started to change an answer can be told apart from the journey mapped
    @Synchronized
    fun passed(
        journeyId: String?,
        step: String,
        mode: String?,
    ) {
        if (mode != null) outcomesPassed.getOrPut(journeyId.orEmpty()) { sortedMapOf() }.getOrPut(step) { sortedSetOf() } += mode
    }

    @Synchronized
    fun snapshot() =
        Dependencies(
            reads.mapValues { it.value.toList() },
            outcomeReads.mapValues { it.value.toList() },
            writers.mapValues { it.value.toList() },
            outcomesPassed.mapValues { (_, steps) -> steps.mapValues { it.value.toList() } },
        )

    @Synchronized
    fun clear() {
        reads.clear()
        outcomeReads.clear()
        writers.clear()
        outcomesPassed.clear()
    }
}
