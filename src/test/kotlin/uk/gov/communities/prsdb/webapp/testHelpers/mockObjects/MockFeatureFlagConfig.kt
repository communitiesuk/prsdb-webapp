package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import org.ff4j.core.Feature
import org.ff4j.core.FlippingStrategy
import org.ff4j.property.PropertyDate
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagConfigModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlipStrategyConfigModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseConfigModel
import java.time.LocalDate

class MockFeatureFlagConfig {
    companion object {
        fun createFlipStrategyConfigModel(
            releaseDate: LocalDate? = null,
            enabledByStrategy: Boolean? = null,
        ): FeatureFlipStrategyConfigModel =
            FeatureFlipStrategyConfigModel(
                releaseDate = releaseDate,
                enabledByStrategy = enabledByStrategy,
            )

        fun createFeatureFlagConfigModel(
            name: String = "TEST_FEATURE_FLAG_1",
            enabled: Boolean = false,
            expiryDate: LocalDate = LocalDate.now().plusMonths(1),
            release: String? = null,
            strategyConfig: FeatureFlipStrategyConfigModel? = null,
        ) = FeatureFlagConfigModel(
            name = name,
            enabled = enabled,
            expiryDate = expiryDate,
            release = release,
            strategyConfig = strategyConfig,
        )

        fun createFeatureReleaseConfigModel(
            name: String = "TEST_FEATURE_RELEASE_1",
            enabled: Boolean = false,
            strategyConfig: FeatureFlipStrategyConfigModel? = null,
        ) = FeatureReleaseConfigModel(
            name = name,
            enabled = enabled,
            strategyConfig = strategyConfig,
        )

        fun createFeature(
            name: String = "TEST_FEATURE_FLAG_1",
            enabled: Boolean = false,
            expiryDate: LocalDate = LocalDate.now().plusMonths(1),
            release: String? = null,
            flippingStrategy: FlippingStrategy? = null,
        ): Feature {
            val expiryDateProperty = PropertyDate("expiryDate", DateTimeHelper.getJavaDateFromLocalDate(expiryDate))
            val feature = Feature(name, enabled)
            feature.addProperty(expiryDateProperty)
            feature.group = release
            feature.flippingStrategy = flippingStrategy
            return feature
        }
    }
}
