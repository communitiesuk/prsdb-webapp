package uk.gov.communities.prsdb.webapp.config.featureFlags.flippingStrategies

import org.ff4j.strategy.time.ReleaseDateFlipStrategy
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import uk.gov.communities.prsdb.webapp.config.factories.ReleaseDateFlipStrategyFactory
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFeatureFlagConfig
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReleaseDateFlipStrategyFactoryTests {
    @Test
    fun `getStrategyOrNull returns ReleaseDateFlipStrategy when releaseDate is set in strategyConfig`() {
        // Arrange
        val releaseDate = LocalDate.now().plusWeeks(5)
        val strategyConfig = MockFeatureFlagConfig.createFlipStrategyConfigModel(releaseDate = releaseDate)

        // Act
        val strategy = ReleaseDateFlipStrategyFactory().getStrategyOrNull(strategyConfig)

        // Assert
        assertTrue(strategy is ReleaseDateFlipStrategy)
        assertTrue(
            ReflectionEquals(
                ReleaseDateFlipStrategy(
                    DateTimeHelper.getJavaDateFromLocalDate(releaseDate),
                ),
            ).matches(strategy),
        )
    }

    @Test
    fun `getStrategyOrNull returns null when releaseDate is not set in strategyConfig`() {
        // Arrange
        val strategyConfig = MockFeatureFlagConfig.createFlipStrategyConfigModel()

        // Act, Assert
        assertNull(ReleaseDateFlipStrategyFactory().getStrategyOrNull(strategyConfig))
    }
}
