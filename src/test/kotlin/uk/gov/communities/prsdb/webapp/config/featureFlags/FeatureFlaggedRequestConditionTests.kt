package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.config.conditions.FeatureFlaggedRequestCondition
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import kotlin.test.assertEquals

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
        // Arrange
        whenever(mockFeatureFlagManager.checkFeature(flagName)).thenReturn(true)

        // Act
        val result = featureFlaggedRequestCondition.getMatchingCondition(mock())

        // Assert
        assertEquals(featureFlaggedRequestCondition, result)
    }

    @Test
    fun `getMatchingCondition returns null when feature flag is disabled`() {
        // Arrange
        whenever(mockFeatureFlagManager.checkFeature(flagName)).thenReturn(false)

        // Act & Assert
        assertNull(featureFlaggedRequestCondition.getMatchingCondition(mock()))
    }
}
