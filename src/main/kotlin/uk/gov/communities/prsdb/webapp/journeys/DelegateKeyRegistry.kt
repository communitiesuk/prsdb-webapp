package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException

/**
 * A record of all fully scoped delegated property key in a journey.
 *
 * All delegate keys for a journey must be registered with a single KeyRegistry to ensure there are no collisions.
 * A single DelegateKeyRegistry should be created for the entire journey and all tasks and other sub-journeys that
 * own their own property delegate keys must register with it.
 *
 * The registry also records which keys are included in the check your answers snapshot, so the whole journey's
 * delegates can be checked for changes made since a CYA page was loaded.
 */
class DelegateKeyRegistry {
    private val resolvedKeys = mutableSetOf<String>()
    private val resolvedCyaSnapshotKeys = mutableSetOf<String>()

    val cyaSnapshotKeys: Set<String>
        get() = resolvedCyaSnapshotKeys.toSet()

    fun register(
        key: String,
        includeInCyaSnapshot: Boolean = false,
    ) {
        if (!resolvedKeys.add(key)) {
            throw JourneyInitialisationException("Delegate key '$key' is already in use in this journey")
        }
        if (includeInCyaSnapshot) {
            resolvedCyaSnapshotKeys.add(key)
        }
    }

    fun registerAll(other: DelegateKeyRegistry) {
        other.resolvedKeys.forEach { register(it, it in other.resolvedCyaSnapshotKeys) }
    }
}
