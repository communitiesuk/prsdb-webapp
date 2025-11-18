package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.config.conditions.InverseFeatureFlaggedRequestCondition
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager

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
        whenever(mockFeatureFlagManager.checkFeature(flagName)).thenReturn(true)
        assertNull(inverseFeatureFlaggedRequestCondition.getMatchingCondition(mock()))
    }

    @Test
    fun `getMatchingCondition returns this when feature flag is disabled`() {
        whenever(mockFeatureFlagManager.checkFeature(flagName)).thenReturn(false)
        val result = inverseFeatureFlaggedRequestCondition.getMatchingCondition(mock())
        assert(result === inverseFeatureFlaggedRequestCondition)
    }
}
