package uk.gov.communities.prsdb.webapp.config.featureFlags.flippingStrategies

import org.ff4j.core.FlippingStrategy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.config.flipStrategies.CombinedFlipStrategy
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CombinedFlipStrategyTests {
    @MockitoBean
    private lateinit var firstFlipStrategy: FlippingStrategy

    @MockitoBean
    private lateinit var secondFlipStrategy: FlippingStrategy

    @BeforeEach
    fun setUp() {
        firstFlipStrategy = mock()
        secondFlipStrategy = mock()
    }

    @Test
    fun `evaluate returns true when all strategies in strategyList evaluate to true`() {
        // Arrange
        whenever(firstFlipStrategy.evaluate(anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(true)
        whenever(secondFlipStrategy.evaluate(anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(true)
        val strategyList = listOf(firstFlipStrategy, secondFlipStrategy)
        val combinedFlipStrategy = CombinedFlipStrategy(strategyList)

        // Act, Assert
        assertTrue(combinedFlipStrategy.evaluate(null, null, null))
    }

    @Test
    fun `evaluate returns false when a strategy in strategyList evaluate to false`() {
        // Arrange
        whenever(firstFlipStrategy.evaluate(anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(false)
        whenever(secondFlipStrategy.evaluate(anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(true)
        val strategyList = listOf(firstFlipStrategy, secondFlipStrategy)
        val combinedFlipStrategy = CombinedFlipStrategy(strategyList)

        // Act, Assert
        assertFalse(combinedFlipStrategy.evaluate(null, null, null))
    }
}
