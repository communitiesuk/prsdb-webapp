package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.config.conditions.FeatureFlaggedRequestCondition
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager

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
}
