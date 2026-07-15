package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException

// A single, journey-build-wide record of every RESOLVED (route-scoped) delegate key. Created once per journey
// build and shared by every JourneyStateDelegateProvider in that build (the journey state's and each task's), so
// that keys from different providers are compared together and collisions are detected at build time.
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
