package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.config.conditions.InverseFeatureFlaggedRequestCondition
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import kotlin.test.assertEquals

class InverseFeatureFlaggedRequestConditionTests {
    @MockitoBean
    private var mockFeatureFlagManager: FeatureFlagManager = mock()

    private val flagName = "test-flag"

    private val inverseFeatureFlaggedRequestCondition =
        InverseFeatureFlaggedRequestCondition(
            flagName,
            mockFeatureFlagManager,
        )

    @Test
    fun `getMatchingCondition returns null when feature flag is enabled`() {
        // Arrange
        whenever(mockFeatureFlagManager.checkFeature(flagName)).thenReturn(true)

        // Act & Assert
        assertNull(inverseFeatureFlaggedRequestCondition.getMatchingCondition(mock()))
    }

    @Test
    fun `getMatchingCondition returns this when feature flag is disabled`() {
        // Arrange
        whenever(mockFeatureFlagManager.checkFeature(flagName)).thenReturn(false)

        // Act
        val result = inverseFeatureFlaggedRequestCondition.getMatchingCondition(mock())

        assertEquals(inverseFeatureFlaggedRequestCondition, result)
    }
}
