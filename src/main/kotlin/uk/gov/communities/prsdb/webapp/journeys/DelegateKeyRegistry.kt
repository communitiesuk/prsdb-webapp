package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException

/**
 * A record of all fully scoped delegated property key in a journey.
 *
 * All delegate keys for a journey must be registered with a single KeyRegistry to ensure there are no collisions.
 * A single DelegateKeyRegistry should be created for the entire journey and all tasks and other sub-journeys that
 * own their own property delegate keys must register with it.
 *
 * The registry also records which keys hold answers shown on a check your answers page, so the whole journey's
 * answer delegates can be included in the CYA answers snapshot.
 */
class DelegateKeyRegistry {
    private val resolvedKeys = mutableSetOf<String>()
    private val resolvedCyaAnswerKeys = mutableSetOf<String>()

    val cyaAnswerKeys: Set<String>
        get() = resolvedCyaAnswerKeys.toSet()

    fun register(
        key: String,
        isCyaAnswer: Boolean = false,
    ) {
        if (!resolvedKeys.add(key)) {
            throw JourneyInitialisationException("Delegate key '$key' is already in use in this journey")
        }
        if (isCyaAnswer) {
            resolvedCyaAnswerKeys.add(key)
        }
    }

    fun registerAll(other: DelegateKeyRegistry) {
        other.resolvedKeys.forEach { register(it, it in other.resolvedCyaAnswerKeys) }
    }
}
