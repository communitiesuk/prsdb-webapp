package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.ff4j.strategy.time.ReleaseDateFlipStrategy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.config.FeatureFlipStrategyInitialiser
import uk.gov.communities.prsdb.webapp.config.flipStrategies.BooleanFlipStrategy
import uk.gov.communities.prsdb.webapp.config.flipStrategies.CombinedFlipStrategy
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlipStrategyConfigModel
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeatureFlipStrategyInitialiserTests : FeatureFlagTest() {
    @Autowired
    private lateinit var featureFlipStrategyInitialiser: FeatureFlipStrategyInitialiser

    @Test
    fun `getFlipStrategyOrNull returns a CombinedFlipStrategy with a ReleaseDateFlipStrategy config includes releaseDate`() {
        // Arrange
        val strategy =
            FeatureFlipStrategyConfigModel(
                releaseDate = LocalDate.now().plusWeeks(5),
            )

        // Act
        val result = featureFlipStrategyInitialiser.getFlipStrategyOrNull(strategy)

        // Assert
        assertTrue(result is CombinedFlipStrategy)
        assertTrue(result.strategyList[0] is ReleaseDateFlipStrategy)
    }

    @Test
    fun `getFlipStrategyOrNull returns a CombinedFlipStrategy with multiple strategies if the flag has config for multiple strategies`() {
        // Arrange
        val strategy =
            FeatureFlipStrategyConfigModel(
                releaseDate = LocalDate.now().plusWeeks(5),
                enabledByStrategy = true,
            )

        // Act
        val result = featureFlipStrategyInitialiser.getFlipStrategyOrNull(strategy)

        // Assert
        assertTrue(result is CombinedFlipStrategy)
        assertEquals(2, (result as CombinedFlipStrategy).strategyList.size)
        assertTrue(result.strategyList.any { it is ReleaseDateFlipStrategy })
        assertTrue(result.strategyList.any { it is BooleanFlipStrategy })
    }

    @Test
    fun `getFlipStrategyOrNull returns null if the feature flag has no strategy related config`() {
        val strategy = FeatureFlipStrategyConfigModel()

        assertNull(featureFlipStrategyInitialiser.getFlipStrategyOrNull(strategy))
    }
}
