package uk.gov.communities.prsdb.webapp.config.featureFlags.flippingStrategies

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.anyOrNull
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
            strategy.evaluate(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
            ),
        )
    }
}
