package uk.gov.communities.prsdb.webapp.config.featureFlags

interface FeatureFlagSelector {
    fun <T> ifEnabledOrElse(branches: IfEnabledOrElseBuilder<T>.() -> Unit): T

    fun ifEnabled(action: () -> Unit)
}

class IfEnabledOrElseBuilder<T> {
    private var enabledBranch: (() -> T)? = null
    private var disabledBranch: (() -> T)? = null

    fun ifEnabled(branch: () -> T) {
        enabledBranch = branch
    }

    fun ifDisabled(branch: () -> T) {
        disabledBranch = branch
    }

    internal fun resolveEnabled(): T = (enabledBranch ?: throw IllegalStateException("ifEnabledOrElse requires an ifEnabled branch"))()

    internal fun resolveDisabled(): T = (disabledBranch ?: throw IllegalStateException("ifEnabledOrElse requires an ifDisabled branch"))()
}

abstract class EnabledFeatureFlagSelector : FeatureFlagSelector {
    final override fun <T> ifEnabledOrElse(branches: IfEnabledOrElseBuilder<T>.() -> Unit): T =
        IfEnabledOrElseBuilder<T>().apply(branches).resolveEnabled()

    final override fun ifEnabled(action: () -> Unit) = action()
}

abstract class DisabledFeatureFlagSelector : FeatureFlagSelector {
    final override fun <T> ifEnabledOrElse(branches: IfEnabledOrElseBuilder<T>.() -> Unit): T =
        IfEnabledOrElseBuilder<T>().apply(branches).resolveDisabled()

    final override fun ifEnabled(action: () -> Unit) {}
}
