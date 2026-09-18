package uk.gov.communities.prsdb.webapp.config.featureFlags

// Common mechanism for feature-flagged strategies. A strategy interface extends this and is flipped between the
// enabled/disabled implementations via @PrsdbFlip, so callers can branch on the flag without knowing the flag name:
//
//     strategy.ifEnabledOrElse {
//         ifEnabled { enabledValue }
//         ifDisabled { disabledValue }
//     }
interface FeatureFlagStrategy {
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

abstract class EnabledFeatureFlagStrategy : FeatureFlagStrategy {
    final override fun <T> ifEnabledOrElse(branches: IfEnabledOrElseBuilder<T>.() -> Unit): T =
        IfEnabledOrElseBuilder<T>().apply(branches).resolveEnabled()

    final override fun ifEnabled(action: () -> Unit) = action()
}

abstract class DisabledFeatureFlagStrategy : FeatureFlagStrategy {
    final override fun <T> ifEnabledOrElse(branches: IfEnabledOrElseBuilder<T>.() -> Unit): T =
        IfEnabledOrElseBuilder<T>().apply(branches).resolveDisabled()

    final override fun ifEnabled(action: () -> Unit) {}
}
