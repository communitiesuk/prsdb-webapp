package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

class IfEnabledConfig<T> {
    internal var ifEnabledProvider: (() -> T)? = null
    internal var ifDisabledProvider: (() -> T)? = null

    fun ifEnabled(provider: () -> T) {
        ifEnabledProvider = provider
    }

    fun ifDisabled(provider: () -> T) {
        ifDisabledProvider = provider
    }
}
