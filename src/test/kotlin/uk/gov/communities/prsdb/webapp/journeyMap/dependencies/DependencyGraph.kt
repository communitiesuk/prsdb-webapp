package uk.gov.communities.prsdb.webapp.journeyMap.dependencies

import uk.gov.communities.prsdb.webapp.journeys.JourneyStateAccessObserver.Companion.ALL_STEP_DATA
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateAccessObserver.Companion.STEP_KEY_PREFIX

class DependencyGraph(
    private val dependencies: Dependencies,
) {
    // Keys the step reads while working out its mode, including keys read through the outcomes of other steps
    fun keysRead(stepId: String): Set<String> {
        val keys = sortedSetOf<String>()
        val seen = mutableSetOf<String>()
        val queue = ArrayDeque(listOf(stepId))
        while (queue.isNotEmpty()) {
            val id = queue.removeFirst()
            if (!seen.add(id)) continue
            keys += dependencies.reads[id].orEmpty()
            queue += dependencies.outcomeReads[id].orEmpty()
        }
        return keys
    }

    fun readsAllStepData(stepId: String) = ALL_STEP_DATA in keysRead(stepId)

    fun writersOf(stepId: String): Set<String> =
        keysRead(stepId)
            .flatMap { key -> if (key == ALL_STEP_DATA) stepAnswerWriters() else dependencies.writers[key].orEmpty() }
            .toSortedSet()
            .apply { remove(stepId) }

    private fun stepAnswerWriters() =
        dependencies.writers
            .filterKeys { it.startsWith(STEP_KEY_PREFIX) && it != ALL_STEP_DATA }
            .values
            .flatten()
}
