package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException

/**
 * A record of all fully scoped delegated property key in a journey.
 *
 * All delegate keys for a journey must be registered with a single KeyRegistry to ensure there are no collisions.
 * A single DelegateKeyRegistry should be created for the entire journey and all tasks and other sub-journeys that
 * own their own property delegate keys must register with it.
 */
class DelegateKeyRegistry {
    private val resolvedKeys = mutableSetOf<String>()

    fun register(key: String) {
        if (!resolvedKeys.add(key)) {
            throw JourneyInitialisationException("Delegate key '$key' is already in use in this journey")
        }
    }

    fun registerAll(other: DelegateKeyRegistry) {
        other.resolvedKeys.forEach { register(it) }
    }
}
