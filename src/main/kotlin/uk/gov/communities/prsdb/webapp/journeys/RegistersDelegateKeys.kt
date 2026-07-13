package uk.gov.communities.prsdb.webapp.journeys

// Implemented by anything that owns delegate keys and can be attached to a journey-build-wide DelegateKeyRegistry.
// On binding, the implementer registers its already-collected keys (resolved to their final route-scoped form) and
// forwards any keys registered afterwards.
interface RegistersDelegateKeys {
    fun bindKeyRegistry(registry: DelegateKeyRegistry)
}
