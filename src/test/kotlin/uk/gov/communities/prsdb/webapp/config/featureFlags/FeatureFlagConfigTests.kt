package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.test.context.ActiveProfiles
import uk.gov.communities.prsdb.webapp.config.FeatureFlagConfig
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.RELEASE_1_0
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagConfigModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseConfigModel
import java.time.LocalDate

class FeatureFlagConfigTests : FeatureFlagTest() {
    val expectedFeatureFlagsFromDefaultApplicationYaml =
        listOf(
            FeatureFlagConfigModel(
                name = "example-feature-flag-one",
                enabled = true,
                expiryDate = LocalDate.of(2030, 1, 12),
            ),
            FeatureFlagConfigModel(
                name = "example-feature-flag-two",
                enabled = true,
                expiryDate = LocalDate.of(2030, 1, 7),
                release = "release-1-0",
            ),
            FeatureFlagConfigModel(
                name = "example-feature-flag-three",
                enabled = false,
                expiryDate = LocalDate.of(2030, 1, 7),
                release = "release-1-0",
            ),
            FeatureFlagModel(
                name = "failover-test-endpoints",
                enabled = true,
                expiryDate = LocalDate.of(2026, 12, 31),
            ),
        )

    @Test
    fun `features and releases from application yaml are loaded`() {
        val expectedReleases =
            listOf(
                FeatureReleaseConfigModel(
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
                    FeatureReleaseConfigModel(
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
                    FeatureFlagConfigModel(
                        name = testFlagName,
                        enabled = true,
                        expiryDate = LocalDate.now().plusWeeks(5),
                        release = testReleaseName,
                    ),
                )
            featureFlagConfigWithMockAllowedValues.releases =
                listOf(
                    FeatureReleaseConfigModel(
                        name = testReleaseName,
                        enabled = true,
                    ),
                )

            // Act & Assert
            assertDoesNotThrow { featureFlagConfigWithMockAllowedValues.afterPropertiesSet() }
        }

        @Test
        fun `afterPropertiesSet throws for a feature name that is not in the featureFlagNamesList`() {
            featureFlagConfigWithMockAllowedValues.featureFlags =
                listOf(
                    FeatureFlagConfigModel(
                        name = "unlisted-feature-name",
                        enabled = true,
                        expiryDate = LocalDate.now().plusWeeks(5),
                        null,
                    ),
                )

            assertThrows<IllegalStateException> { featureFlagConfigWithMockAllowedValues.afterPropertiesSet() }
        }

        @Test
        fun `afterPropertiesSet throws for a release name that is not in the releaseNamesList`() {
            featureFlagConfigWithMockAllowedValues.featureFlags =
                listOf(
                    FeatureFlagConfigModel(
                        name = EXAMPLE_FEATURE_FLAG_ONE,
                        enabled = true,
                        expiryDate = LocalDate.now().plusWeeks(5),
                        release = RELEASE_1_0,
                    ),
                )
            featureFlagConfigWithMockAllowedValues.releases =
                listOf(
                    FeatureReleaseConfigModel(
                        name = "unlisted-release-name",
                        enabled = true,
                    ),
                )

            assertThrows<IllegalStateException> { featureFlagConfigWithMockAllowedValues.afterPropertiesSet() }
        }

        @Test
        fun `afterPropertiesSet includes all missing feature and release names in the error message`() {
            featureFlagConfigWithMockAllowedValues.featureFlags =
                listOf(
                    FeatureFlagConfigModel(
                        name = "unlisted-feature-name",
                        enabled = true,
                        expiryDate = LocalDate.now().plusWeeks(5),
                    ),
                    FeatureFlagConfigModel(
                        name = "another-unlisted-feature-name",
                        enabled = true,
                        expiryDate = LocalDate.now().plusWeeks(5),
                        release = "unlisted-release-name",
                    ),
                )
            featureFlagConfigWithMockAllowedValues.releases =
                listOf(
                    FeatureReleaseConfigModel(
                        name = "unlisted-release-name",
                        enabled = true,
                    ),
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
