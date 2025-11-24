package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FLAG_WITH_RELEASE_DATE_ONE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FLAG_WITH_RELEASE_DATE_TWO
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_GROUPED_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_GROUPED_FEATURE_FLAG_TWO
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_SINGLE_FEATURE_FLAG
import uk.gov.communities.prsdb.webapp.constants.RELEASE_1_0
import uk.gov.communities.prsdb.webapp.constants.RELEASE_2_0
import uk.gov.communities.prsdb.webapp.constants.RELEASE_3_0
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagGroupModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagModel
import java.time.LocalDate

@ComponentScan(basePackages = ["uk.gov.communities.prsdb.webapp"])
@Configuration
class FeatureFlagConfig {
    @Bean
    fun featureFlagManager(): FeatureFlagManager {
        val featureFlagManager = FeatureFlagManager()
        featureFlagManager.initializeFeatureFlags(featureFlags)
        // If a flag is in a flag group, the flag group's enabled value overrides the individual flag's enabled value
        featureFlagManager.initialiseFeatureFlagGroups(featureGroups)
        return featureFlagManager
    }

    companion object {
        val featureFlags =
            listOf(
                FeatureFlagModel(
                    name = EXAMPLE_SINGLE_FEATURE_FLAG,
                    enabled = true,
                    expiryDate = LocalDate.of(2030, 2, 14),
                ),
                FeatureFlagModel(
                    name = EXAMPLE_GROUPED_FEATURE_FLAG_ONE,
                    // Please leave this flag enabled to demo the value being overridden by the flag group
                    enabled = true,
                    expiryDate = LocalDate.of(2030, 2, 14),
                    flagGroup = RELEASE_1_0,
                ),
                FeatureFlagModel(
                    name = EXAMPLE_GROUPED_FEATURE_FLAG_TWO,
                    // Please leave this flag disabled to demo the value being overridden by the flag group
                    enabled = false,
                    expiryDate = LocalDate.of(2030, 2, 14),
                    flagGroup = RELEASE_1_0,
                ),
                FeatureFlagModel(
                    name = EXAMPLE_FLAG_WITH_RELEASE_DATE_ONE,
                    enabled = true,
                    expiryDate = LocalDate.of(2030, 2, 14),
                    flagGroup = RELEASE_2_0,
                ),
                FeatureFlagModel(
                    name = EXAMPLE_FLAG_WITH_RELEASE_DATE_TWO,
                    enabled = false,
                    expiryDate = LocalDate.of(2030, 2, 14),
                    flagGroup = RELEASE_3_0,
                ),
            )

        val featureGroups =
            listOf(
                FeatureFlagGroupModel(
                    name = RELEASE_1_0,
                    enabled = true,
                ),
                FeatureFlagGroupModel(
                    name = RELEASE_2_0,
                    // Please leave this flag enabled to demo the release date flipping strategy
                    enabled = true,
                    releaseDate = LocalDate.of(2025, 6, 1),
                ),
                FeatureFlagGroupModel(
                    name = RELEASE_3_0,
                    // Please leave this flag enabled to demo that features are only enabled if "enabled" is true AND the flipping strategy allows it.
                    enabled = false,
                    releaseDate = LocalDate.of(2025, 6, 1),
                ),
            )
    }
}
