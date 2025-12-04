package uk.gov.communities.prsdb.webapp.config.featureFlags.flippingStrategies

import org.junit.jupiter.api.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import uk.gov.communities.prsdb.webapp.config.factories.BooleanFlipStrategyFactory
import uk.gov.communities.prsdb.webapp.config.flipStrategies.BooleanFlipStrategy
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlipStrategyConfigModel
import kotlin.test.Test
import kotlin.test.assertTrue

class BooleanFlipStrategyFactoryTests {
    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `getStrategyOrNull returns BooleanFlipStrategy when enabledByStrategy is set in strategyConfig`(enabled: Boolean) {
        // Arrange
        val strategyConfig =
            FeatureFlipStrategyConfigModel(
                enabledByStrategy = enabled,
            )

        // Act
        val strategy = BooleanFlipStrategyFactory().getStrategyOrNull(strategyConfig)

        // Assert
        assertTrue(strategy is BooleanFlipStrategy)
        assertTrue(ReflectionEquals(BooleanFlipStrategy(enabled)).matches(strategy))
    }

    @Test
    fun `getStrategyOrNull returns null if the strategyConfig has no enabledByStrategy value`() {
        // Arrange
        val strategyConfig = FeatureFlipStrategyConfigModel()

        // Act, Assert
        assertNull(BooleanFlipStrategyFactory().getStrategyOrNull(strategyConfig))
    }
}
