package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.constants.enums.FeatureFlagOverrideChoice

data class FeatureReleaseOverrideViewModel(
    val name: String,
    val flagNames: List<String>,
    val choice: FeatureFlagOverrideChoice,
)
