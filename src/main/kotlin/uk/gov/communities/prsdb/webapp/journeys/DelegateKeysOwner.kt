package uk.gov.communities.prsdb.webapp.journeys

interface DelegateKeysOwner {
    /**
     * Attaches the key registry to this owner, and registers all current scoped keys.
     *
     * Must be called after any key prefixes have been set or the keys will change after they have been registered.
     */
    fun bindKeyRegistry(registry: DelegateKeyRegistry)
}
