package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.test.context.ActiveProfiles
import uk.gov.communities.prsdb.webapp.config.FeatureFlagsFromConfig
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.RELEASE_1_0
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseModel
import java.time.LocalDate

class FeatureFlagsFromConfigTests : FeatureFlagTest() {
    @Test
    fun `loads features and releases from application yaml`() {
        val expectedFeatureFlags =
            listOf(
                FeatureFlagModel(
                    name = "example-feature-flag-one",
                    enabled = true,
                    expiryDate = LocalDate.of(2030, 1, 12),
                ),
                FeatureFlagModel(
                    name = "example-feature-flag-two",
                    enabled = true,
                    expiryDate = LocalDate.of(2030, 1, 7),
                    release = "release-1-0",
                ),
                FeatureFlagModel(
                    name = "example-feature-flag-three",
                    enabled = false,
                    expiryDate = LocalDate.of(2030, 1, 7),
                    release = "release-1-0",
                ),
            )

        val expectedReleases =
            listOf(
                FeatureReleaseModel(
                    name = "release-1-0",
                    enabled = false,
                ),
            )

        assertTrue(featureFlagsFromConfig.featureFlags == expectedFeatureFlags)
        assertTrue(featureFlagsFromConfig.releases == expectedReleases)
    }

    @ActiveProfiles("integration")
    @Nested
    inner class IntegrationProfileTests : FeatureFlagTest() {
        @Test
        fun `loads features and releases from environment specific application yaml if available and environment profile is set`() {
            // This is only set in application.yml
            val expectedFeatureFlags =
                listOf(
                    FeatureFlagModel(
                        name = "example-feature-flag-one",
                        enabled = true,
                        expiryDate = LocalDate.of(2030, 1, 12),
                    ),
                )

            // This is set in application.yml with enabled=false and in application-integration.yml with enabled=true
            val expectedReleases =
                listOf(
                    FeatureReleaseModel(
                        name = "release-1-0",
                        enabled = true,
                    ),
                )

            assertTrue(featureFlagsFromConfig.featureFlags == expectedFeatureFlags)
            assertTrue(featureFlagsFromConfig.releases == expectedReleases)
        }
    }

    @Nested
    inner class AfterPropertiesSetTests {
        private val testFlagName = "test-flag-name"
        private val testFlagNamesList = listOf(testFlagName)
        private val testReleaseName = "test-release-name"
        private val testReleaseNamesList = listOf(testReleaseName)

        private val featureFlagsFromConfigWithMockAllowedValues =
            FeatureFlagsFromConfig(
                testFlagNamesList,
                testReleaseNamesList,
            )

        @Test
        fun `afterPropertiesSet does not throw for valid feature and release names`() {
            // Arrange
            featureFlagsFromConfigWithMockAllowedValues.featureFlags =
                listOf(
                    FeatureFlagModel(
                        name = testFlagName,
                        enabled = true,
                        expiryDate = LocalDate.now().plusWeeks(5),
                        release = testReleaseName,
                    ),
                )
            featureFlagsFromConfigWithMockAllowedValues.releases =
                listOf(
                    FeatureReleaseModel(
                        name = testReleaseName,
                        enabled = true,
                    ),
                )

            // Act & Assert
            assertDoesNotThrow { featureFlagsFromConfigWithMockAllowedValues.afterPropertiesSet() }
        }

        @Test
        fun `afterPropertiesSet throws for a feature name that is not in the featureFlagNamesList`() {
            featureFlagsFromConfigWithMockAllowedValues.featureFlags =
                listOf(
                    FeatureFlagModel(
                        name = "unlisted-feature-name",
                        enabled = true,
                        expiryDate = LocalDate.now().plusWeeks(5),
                        null,
                    ),
                )

            assertThrows<IllegalStateException> { featureFlagsFromConfigWithMockAllowedValues.afterPropertiesSet() }
        }

        @Test
        fun `afterPropertiesSet throws for a release name that is not in the releaseNamesList`() {
            featureFlagsFromConfigWithMockAllowedValues.featureFlags =
                listOf(
                    FeatureFlagModel(
                        name = EXAMPLE_FEATURE_FLAG_ONE,
                        enabled = true,
                        expiryDate = LocalDate.now().plusWeeks(5),
                        release = RELEASE_1_0,
                    ),
                )
            featureFlagsFromConfigWithMockAllowedValues.releases =
                listOf(
                    FeatureReleaseModel(
                        name = "unlisted-release-name",
                        enabled = true,
                    ),
                )

            assertThrows<IllegalStateException> { featureFlagsFromConfigWithMockAllowedValues.afterPropertiesSet() }
        }
    }
}
