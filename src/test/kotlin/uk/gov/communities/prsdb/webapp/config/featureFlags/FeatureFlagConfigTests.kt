package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.test.context.ActiveProfiles
import uk.gov.communities.prsdb.webapp.config.FeatureFlagConfig
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFeatureFlagConfig
import java.time.LocalDate

class FeatureFlagConfigTests : FeatureFlagTest() {
    val expectedFeatureFlagsFromDefaultApplicationYaml =
        listOf(
            MockFeatureFlagConfig.createFeatureFlagConfigModel(
                name = "example-feature-flag-one",
                enabled = true,
                expiryDate = LocalDate.of(2030, 1, 12),
            ),
            MockFeatureFlagConfig.createFeatureFlagConfigModel(
                name = "example-feature-flag-two",
                enabled = true,
                expiryDate = LocalDate.of(2030, 1, 7),
                release = "release-1-0",
            ),
            MockFeatureFlagConfig.createFeatureFlagConfigModel(
                name = "example-feature-flag-three",
                enabled = false,
                expiryDate = LocalDate.of(2030, 1, 7),
                release = "release-1-0",
            ),
            MockFeatureFlagConfig.createFeatureFlagConfigModel(
                name = "failover-test-endpoints",
                enabled = true,
                expiryDate = LocalDate.of(2026, 12, 31),
            ),
        )

    @Test
    fun `features and releases from application yaml are loaded`() {
        val expectedReleases =
            listOf(
                MockFeatureFlagConfig.createFeatureReleaseConfigModel(
                    name = "release-1-0",
                    enabled = false,
                ),
            )

        assertTrue(featureFlagConfig.featureFlags == expectedFeatureFlagsFromDefaultApplicationYaml)
        assertTrue(featureFlagConfig.releases == expectedReleases)
    }

    @ActiveProfiles("integration")
    @Nested
    inner class IntegrationProfileTests : FeatureFlagTest() {
        @Test
        fun `features and releases from environment specific application yaml are loaded if available and environment profile is set`() {
            // This is only set in application.yml
            val expectedFeatureFlags = expectedFeatureFlagsFromDefaultApplicationYaml

            // This is set in application.yml with enabled=false and in application-integration.yml with enabled=true
            val expectedReleases =
                listOf(
                    MockFeatureFlagConfig.createFeatureReleaseConfigModel(
                        name = "release-1-0",
                        enabled = true,
                    ),
                )

            assertTrue(featureFlagConfig.featureFlags == expectedFeatureFlags)
            assertTrue(featureFlagConfig.releases == expectedReleases)
        }
    }

    @Nested
    inner class AfterPropertiesSetTests {
        private val testFlagName = "test-flag-name"
        private val testFlagNamesList = listOf(testFlagName)
        private val testReleaseName = "test-release-name"
        private val testReleaseNamesList = listOf(testReleaseName)

        private val featureFlagConfigWithMockAllowedValues =
            FeatureFlagConfig(
                testFlagNamesList,
                testReleaseNamesList,
            )

        @Test
        fun `afterPropertiesSet does not throw for valid feature and release names`() {
            // Arrange
            featureFlagConfigWithMockAllowedValues.featureFlags =
                listOf(
                    MockFeatureFlagConfig.createFeatureFlagConfigModel(
                        name = testFlagName,
                        release = testReleaseName,
                    ),
                )
            featureFlagConfigWithMockAllowedValues.releases =
                listOf(
                    MockFeatureFlagConfig.createFeatureReleaseConfigModel(
                        name = testReleaseName,
                    ),
                )

            // Act & Assert
            assertDoesNotThrow { featureFlagConfigWithMockAllowedValues.afterPropertiesSet() }
        }

        @Test
        fun `afterPropertiesSet throws for a feature name that is not in the featureFlagNamesList`() {
            featureFlagConfigWithMockAllowedValues.featureFlags =
                listOf(
                    MockFeatureFlagConfig.createFeatureFlagConfigModel(
                        name = "unlisted-feature-name",
                    ),
                )

            assertThrows<IllegalStateException> { featureFlagConfigWithMockAllowedValues.afterPropertiesSet() }
        }

        @Test
        fun `afterPropertiesSet throws for a release name that is not in the releaseNamesList`() {
            featureFlagConfigWithMockAllowedValues.featureFlags =
                listOf(MockFeatureFlagConfig.createFeatureFlagConfigModel())
            featureFlagConfigWithMockAllowedValues.releases =
                listOf(
                    MockFeatureFlagConfig.createFeatureReleaseConfigModel(
                        name = "unlisted-release-name",
                    ),
                )

            assertThrows<IllegalStateException> { featureFlagConfigWithMockAllowedValues.afterPropertiesSet() }
        }

        @Test
        fun `afterPropertiesSet includes all missing feature and release names in the error message`() {
            featureFlagConfigWithMockAllowedValues.featureFlags =
                listOf(
                    MockFeatureFlagConfig.createFeatureFlagConfigModel(name = "unlisted-feature-name"),
                    MockFeatureFlagConfig.createFeatureFlagConfigModel(
                        name = "another-unlisted-feature-name",
                        release = "unlisted-release-name",
                    ),
                )
            featureFlagConfigWithMockAllowedValues.releases =
                listOf(
                    MockFeatureFlagConfig.createFeatureReleaseConfigModel(name = "unlisted-release-name"),
                )

            val exception = assertThrows<IllegalStateException> { featureFlagConfigWithMockAllowedValues.afterPropertiesSet() }
            val exceptionMessage = exception.message ?: ""

            assertTrue(
                exceptionMessage.contains(
                    "Feature flag name unlisted-feature-name must be added as a const val and included in featureFlagNames",
                ),
            )
            assertTrue(
                exceptionMessage.contains(
                    "Feature flag name another-unlisted-feature-name must be added as a const val and included in featureFlagNames",
                ),
            )
            assertTrue(
                exceptionMessage.contains(
                    "Feature release name unlisted-release-name must be added as a const val and included in featureFlagReleaseNames",
                ),
            )
        }
    }
}
