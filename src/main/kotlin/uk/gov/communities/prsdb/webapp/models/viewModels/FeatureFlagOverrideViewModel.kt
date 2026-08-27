package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.constants.enums.FeatureFlagOverrideChoice

data class FeatureFlagOverrideViewModel(
    val name: String,
    val release: String?,
    val isEnabledByDefault: Boolean,
    val choice: FeatureFlagOverrideChoice,
    val isSupersededByReleaseOverride: Boolean,
)
