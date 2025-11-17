package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.config.resolvers.FeatureFlaggedRequestCondition

class FeatureFlaggedRequestConditionTests {
    @MockitoBean
    private var mockFeatureFlagManager: FeatureFlagManager = mock()

    private val flagName = "test-flag"

    private val featureFlaggedRequestCondition =
        FeatureFlaggedRequestCondition(
            flagName,
            mockFeatureFlagManager,
        )

    @Test
    fun `getMatchingCondition returns this when feature flag is enabled`() {
        whenever(mockFeatureFlagManager.checkFeature(flagName)).thenReturn(true)
        val result = featureFlaggedRequestCondition.getMatchingCondition(mock())
        assert(result === featureFlaggedRequestCondition)
    }

    @Test
    fun `getMatchingCondition returns null when feature flag is disabled`() {
        whenever(mockFeatureFlagManager.checkFeature(flagName)).thenReturn(false)
        assertNull(featureFlaggedRequestCondition.getMatchingCondition(mock()))
    }

    @Nested
    inner class CombineTests {
        private val secondFlagName = "second-test-flag"

        private val secondFeatureFlaggedRequestCondition =
            FeatureFlaggedRequestCondition(
                secondFlagName,
                mockFeatureFlagManager,
            )

        @Test
        fun `combine returns condition that matches when both flags are enabled`() {
            // Arrange
            whenever(mockFeatureFlagManager.checkFeature(flagName)).thenReturn(true)
            whenever(mockFeatureFlagManager.checkFeature(secondFlagName)).thenReturn(true)

            // Act
            val combinedCondition = featureFlaggedRequestCondition.combine(secondFeatureFlaggedRequestCondition)
            val result = combinedCondition.getMatchingCondition(mock())

            // Assert
            assert(result === combinedCondition)
        }

        @Test
        fun `combine returns null when the first flag is disabled`() {
            // Arrange

            whenever(mockFeatureFlagManager.checkFeature(flagName)).thenReturn(false)
            whenever(mockFeatureFlagManager.checkFeature(secondFlagName)).thenReturn(true)

            // Act
            val combinedCondition = featureFlaggedRequestCondition.combine(secondFeatureFlaggedRequestCondition)

            // Assert
            assertNull(combinedCondition.getMatchingCondition(mock()))
        }

        @Test
        fun `combine returns null when the second flag is disabled`() {
            // Arrange
            whenever(mockFeatureFlagManager.checkFeature(flagName)).thenReturn(true)
            whenever(mockFeatureFlagManager.checkFeature(secondFlagName)).thenReturn(false)

            // Act
            val combinedCondition = featureFlaggedRequestCondition.combine(secondFeatureFlaggedRequestCondition)

            // Assert
            assertNull(combinedCondition.getMatchingCondition(mock()))
        }

        @Test
        fun `combine can be chained to return a condition that matches when more than two flags are enabled`() {
            // Arrange
            val thirdFlagName = "third-test-flag"

            val thirdFeatureFlaggedRequestCondition =
                FeatureFlaggedRequestCondition(
                    thirdFlagName,
                    mockFeatureFlagManager,
                )

            whenever(mockFeatureFlagManager.checkFeature(flagName)).thenReturn(true)
            whenever(mockFeatureFlagManager.checkFeature(secondFlagName)).thenReturn(true)
            whenever(mockFeatureFlagManager.checkFeature(thirdFlagName)).thenReturn(true)

            // Act
            val combinedCondition =
                featureFlaggedRequestCondition
                    .combine(secondFeatureFlaggedRequestCondition)
                    .combine(thirdFeatureFlaggedRequestCondition)
            val result = combinedCondition.getMatchingCondition(mock())

            // Assert
            assert(result === combinedCondition)
        }

        @Test
        fun `combine can be chained to return a condition is null when more than two flags are checked but one is disabled`() {
            // Arrange
            val thirdFlagName = "third-test-flag"

            val thirdFeatureFlaggedRequestCondition =
                FeatureFlaggedRequestCondition(
                    thirdFlagName,
                    mockFeatureFlagManager,
                )

            whenever(mockFeatureFlagManager.checkFeature(flagName)).thenReturn(false)
            whenever(mockFeatureFlagManager.checkFeature(secondFlagName)).thenReturn(true)
            whenever(mockFeatureFlagManager.checkFeature(thirdFlagName)).thenReturn(true)

            // Act
            val combinedCondition =
                featureFlaggedRequestCondition
                    .combine(secondFeatureFlaggedRequestCondition)
                    .combine(thirdFeatureFlaggedRequestCondition)

            val result = combinedCondition.getMatchingCondition(mock())

            // Assert
            assertNull(result)
        }
    }
}
