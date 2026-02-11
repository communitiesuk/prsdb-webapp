package uk.gov.communities.prsdb.webapp.config.featureFlags.flippingStrategies

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.communities.prsdb.webapp.config.flipStrategies.BooleanFlipStrategy
import kotlin.test.assertEquals

class BooleanFlipStrategyTests {
    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `evaluate returns enabledByStrategy value from config`(enabledByStrategy: Boolean) {
        // Arrange
        val strategy = BooleanFlipStrategy(enabledByStrategy)

        // Act, Assert
        assertEquals(
            enabledByStrategy,
            strategy.evaluate(null, null, null),
        )
    }
}
