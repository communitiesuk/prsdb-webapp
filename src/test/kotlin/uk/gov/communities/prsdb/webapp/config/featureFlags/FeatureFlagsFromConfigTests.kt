package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.config.FeatureFlagsFromConfig
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.RELEASE_1_0
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseModel
import java.time.LocalDate

class FeatureFlagsFromConfigTests {
    private val testFlagName = "test-flag-name"
    private val testFlagNamesList = listOf(testFlagName)
    private val testReleaseName = "test-release-name"
    private val testReleaseNamesList = listOf(testReleaseName)

    private val featureFlagsFromConfig =
        FeatureFlagsFromConfig(
            testFlagNamesList,
            testReleaseNamesList,
        )

    @Test
    fun `afterPropertiesSet does not throw for valid feature and release names`() {
        // Arrange
        featureFlagsFromConfig.featureFlags =
            listOf(
                FeatureFlagModel(
                    name = testFlagName,
                    enabled = true,
                    expiryDate = LocalDate.now().plusWeeks(5),
                    release = testReleaseName,
                ),
            )
        featureFlagsFromConfig.releases =
            listOf(
                FeatureReleaseModel(
                    name = testReleaseName,
                    enabled = true,
                ),
            )

        // Act & Assert
        assertDoesNotThrow { featureFlagsFromConfig.afterPropertiesSet() }
    }

    @Test
    fun `afterPropertiesSet throws for unknown feature name`() {
        featureFlagsFromConfig.featureFlags =
            listOf(
                FeatureFlagModel(
                    name = "unknown-feature",
                    enabled = true,
                    expiryDate = LocalDate.now().plusWeeks(5),
                    null,
                ),
            )

        assertThrows<IllegalStateException> { featureFlagsFromConfig.afterPropertiesSet() }
    }

    @Test
    fun `afterPropertiesSet throws for unknown release name`() {
        featureFlagsFromConfig.featureFlags =
            listOf(
                FeatureFlagModel(
                    name = EXAMPLE_FEATURE_FLAG_ONE,
                    enabled = true,
                    expiryDate = LocalDate.now().plusWeeks(5),
                    release = RELEASE_1_0,
                ),
            )
        featureFlagsFromConfig.releases =
            listOf(
                FeatureReleaseModel(
                    name = "unknown-release",
                    enabled = true,
                ),
            )

        assertThrows<IllegalStateException> { featureFlagsFromConfig.afterPropertiesSet() }
    }
}
