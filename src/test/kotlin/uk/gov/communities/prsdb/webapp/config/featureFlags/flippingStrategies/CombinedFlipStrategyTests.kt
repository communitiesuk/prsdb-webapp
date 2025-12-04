package uk.gov.communities.prsdb.webapp.config.featureFlags.flippingStrategies

import org.ff4j.strategy.time.ReleaseDateFlipStrategy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.config.flipStrategies.BooleanFlipStrategy
import uk.gov.communities.prsdb.webapp.config.flipStrategies.CombinedFlipStrategy
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CombinedFlipStrategyTests {
    @MockitoBean
    private lateinit var booleanFlipStrategy: BooleanFlipStrategy

    @MockitoBean
    private lateinit var releaseDateFlipStrategy: ReleaseDateFlipStrategy

    @BeforeEach
    fun setUp() {
        booleanFlipStrategy = mock()
        releaseDateFlipStrategy = mock()
    }

    @Test
    fun `evaluate returns true when all strategies in strategyList evaluate to true`() {
        // Arrange
        whenever(booleanFlipStrategy.evaluate(anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(true)
        whenever(releaseDateFlipStrategy.evaluate(anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(true)
        val strategyList = listOf(booleanFlipStrategy, releaseDateFlipStrategy)
        val combinedFlipStrategy = CombinedFlipStrategy(strategyList)

        // Act, Assert
        assertTrue(combinedFlipStrategy.evaluate(anyOrNull(), anyOrNull(), anyOrNull()))
    }

    @Test
    fun `evaluate returns false when a strategy in strategyList evaluate to false`() {
        // Arrange
        whenever(booleanFlipStrategy.evaluate(anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(false)
        whenever(releaseDateFlipStrategy.evaluate(anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(true)
        val strategyList = listOf(booleanFlipStrategy, releaseDateFlipStrategy)
        val combinedFlipStrategy = CombinedFlipStrategy(strategyList)

        // Act, Assert
        assertFalse(combinedFlipStrategy.evaluate(anyOrNull(), anyOrNull(), anyOrNull()))
    }
}
