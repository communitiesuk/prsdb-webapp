package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_THREE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_TWO
import uk.gov.communities.prsdb.webapp.constants.FEATURE_GROUP_RELEASE_1
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagModel
import java.time.LocalDate

@ComponentScan(basePackages = ["uk.gov.communities.prsdb.webapp"])
@Configuration
class FeatureFlagConfig {
    @Bean
    fun featureFlagManager(): FeatureFlagManager {
        val featureFlagManager = FeatureFlagManager()
        featureFlagManager.initializeFeatureFlags(featureFlags)
        return featureFlagManager
    }

    val featureFlags =
        listOf(
            FeatureFlagModel(
                name = EXAMPLE_FEATURE_FLAG_ONE,
                enabled = true,
                expiryDate = LocalDate.of(2030, 2, 14),
            ),
            FeatureFlagModel(
                name = EXAMPLE_FEATURE_FLAG_TWO,
                enabled = true,
                expiryDate = LocalDate.of(2030, 2, 14),
                flagGroup = FEATURE_GROUP_RELEASE_1,
            ),
            FeatureFlagModel(
                name = EXAMPLE_FEATURE_FLAG_THREE,
                enabled = false,
                expiryDate = LocalDate.of(2030, 2, 14),
                flagGroup = FEATURE_GROUP_RELEASE_1,
            ),
        )
}
