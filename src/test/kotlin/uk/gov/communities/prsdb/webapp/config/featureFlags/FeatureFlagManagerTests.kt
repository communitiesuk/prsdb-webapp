package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.ff4j.core.Feature
import org.ff4j.property.PropertyDate
import org.ff4j.strategy.time.ReleaseDateFlipStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.communities.prsdb.webapp.config.FeatureFlipStrategyInitialiser
import uk.gov.communities.prsdb.webapp.config.flipStrategies.CombinedFlipStrategy
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFeatureFlagConfig
import java.time.LocalDate

class FeatureFlagManagerTests {
    @MockitoSpyBean
    lateinit var featureFlagManager: FeatureFlagManager

    @MockitoBean
    lateinit var featureFlipStrategyInitialiser: FeatureFlipStrategyInitialiser

    @BeforeEach
    fun setup() {
        featureFlipStrategyInitialiser = mock()
    }

    @Test
    fun `initialiseFeatureFlags creates features from the supplied FeatureFlagModel list`() {
        // Arrange
        featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))

        val featureFlagsFromConfig =
            listOf(
                MockFeatureFlagConfig.createFeatureFlagConfigModel(
                    name = "TEST_FEATURE_FLAG_1",
                    enabled = true,
                    expiryDate = LocalDate.of(2030, 2, 14),
                ),
                MockFeatureFlagConfig.createFeatureFlagConfigModel(
                    name = "TEST_FEATURE_FLAG_2",
                    enabled = false,
                    expiryDate = LocalDate.of(2030, 6, 14),
                ),
            )

        // Act
        featureFlagManager.initializeFeatureFlags(featureFlagsFromConfig)

        // Assert
        val captor = captor<Feature>()
        verify(featureFlagManager, times(2)).createFeature(captor.capture())

        val createdFeatures = captor.allValues
        assertEquals(2, createdFeatures.size)

        val first = createdFeatures[0]
        assertEquals("TEST_FEATURE_FLAG_1", first.uid)
        assertTrue(first.isEnable)
        assertEquals("2030-02-14 00:00:00", first.getProperty<PropertyDate>("expiryDate").asString())

        val second = createdFeatures[1]
        assertEquals("TEST_FEATURE_FLAG_2", second.uid)
        assertFalse(second.isEnable)
        assertEquals("2030-06-14 00:00:00", second.getProperty<PropertyDate>("expiryDate").asString())
    }

    @Test
    fun `initialiseFeatureFlag sets a flippingStrategy on the flag if strategy parameters are set in the config`() {
        // Arrange
        featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))

        val releaseDate = LocalDate.now().plusMonths(1)
        val strategyConfig = MockFeatureFlagConfig.createFlipStrategyConfigModel(releaseDate = releaseDate)
        val featureFlagsFromConfig = listOf(MockFeatureFlagConfig.createFeatureFlagConfigModel(strategyConfig = strategyConfig))

        val expectedFlipStrategy =
            CombinedFlipStrategy(
                listOf(ReleaseDateFlipStrategy(DateTimeHelper.getJavaDateFromLocalDate(releaseDate))),
            )

        whenever(featureFlipStrategyInitialiser.getFlipStrategyOrNull(strategyConfig))
            .thenReturn(expectedFlipStrategy)

        // Act
        featureFlagManager.initializeFeatureFlags(featureFlagsFromConfig)

        // Assert
        verify(featureFlipStrategyInitialiser).getFlipStrategyOrNull(strategyConfig)

        val captor = captor<Feature>()
        verify(featureFlagManager).createFeature(captor.capture())

        assertEquals(expectedFlipStrategy, captor.value.flippingStrategy)
    }

    @Test
    fun `checkFeature calls super check method with correct feature name`() {
        // Arrange
        featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))
        val featureName = "SOME_FEATURE_FLAG"
        featureFlagManager.createFeature(featureName)

        // Act
        featureFlagManager.checkFeature(featureName)

        // Assert
        val captor = captor<String>()

        verify(featureFlagManager).check(captor.capture())
        assertEquals(featureName, captor.value)
    }

    @Test
    fun `enableFeature calls super enable method with correct feature name`() {
        // Arrange
        featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))
        val featureName = "SOME_FEATURE_FLAG"
        featureFlagManager.createFeature(featureName)

        // Act
        featureFlagManager.enableFeature(featureName)

        // Assert
        val captor = captor<String>()
        verify(featureFlagManager).enable(captor.capture())
        assertEquals(featureName, captor.value)
    }

    @Test
    fun `disableFeature calls super disable method with correct feature name`() {
        // Arrange
        featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))
        val featureName = "SOME_FEATURE_FLAG"
        featureFlagManager.createFeature(featureName)

        // Act
        featureFlagManager.disableFeature(featureName)

        // Assert
        val captor = captor<String>()
        verify(featureFlagManager).disable(captor.capture())
        assertEquals(featureName, captor.value)
    }

    @Nested
    inner class FeatureFlagReleaseMethodsTests {
        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `initialiseFeatureReleases enables or disables all features in the group based on the group's enabled value`(
            releaseEnabled: Boolean,
        ) {
            // Arrange
            val flagOneName = "FEATURE_IN_RELEASE_X_Y"
            val flagTwoName = "ANOTHER_FEATURE_IN_RELEASE_X_Y"
            val releaseName = "RELEASE_X_Y"
            featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))

            val featureFlagGroup = MockFeatureFlagConfig.createFeatureReleaseConfigModel(name = releaseName, enabled = releaseEnabled)
            val featureInRelease = MockFeatureFlagConfig.createFeature(name = flagOneName, enabled = !releaseEnabled, release = releaseName)
            val anotherFeatureInRelease =
                MockFeatureFlagConfig.createFeature(
                    name = flagTwoName,
                    enabled = !releaseEnabled,
                    release = releaseName,
                )

            featureFlagManager.createFeature(featureInRelease)
            featureFlagManager.createFeature(anotherFeatureInRelease)
            assertEquals(!releaseEnabled, featureFlagManager.checkFeature(flagOneName))
            assertEquals(!releaseEnabled, featureFlagManager.checkFeature(flagTwoName))

            // Act
            featureFlagManager.initialiseFeatureReleases(listOf(featureFlagGroup))

            // Assert
            if (releaseEnabled) {
                verify(featureFlagManager).enableGroup(releaseName)
            } else {
                verify(featureFlagManager).disableGroup(releaseName)
            }
            assertEquals(releaseEnabled, featureFlagManager.checkFeature(flagOneName))
            assertEquals(releaseEnabled, featureFlagManager.checkFeature(flagTwoName))
        }

        @Test
        fun `initialiseFeatureReleases throws an error if empty groups are added in config`() {
            // Arrange
            featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))

            val featureFlagGroup = MockFeatureFlagConfig.createFeatureReleaseConfigModel(name = "EMPTY_RELEASE")

            val expectedErrorMessage =
                "EMPTY_RELEASE group does not exist in store" +
                    ". Check that at least one feature in the yaml config has this release's name set as release."

            // Act & Assert
            val exception = assertThrows<RuntimeException> { featureFlagManager.initialiseFeatureReleases(listOf(featureFlagGroup)) }
            assertEquals(expectedErrorMessage, exception.message)
        }

        @Test
        fun `initialiseFeatureReleases sets a release flippingStrategy for all features in the release`() {
            // Arrange
            val releaseName = "RELEASE_WITH_STRATEGY"
            val releaseDate = LocalDate.now().plusMonths(2)
            val strategyConfig = MockFeatureFlagConfig.createFlipStrategyConfigModel(releaseDate = releaseDate)
            val release = MockFeatureFlagConfig.createFeatureReleaseConfigModel(name = releaseName, strategyConfig = strategyConfig)

            val expectedFlipStrategy =
                CombinedFlipStrategy(
                    listOf(ReleaseDateFlipStrategy(DateTimeHelper.getJavaDateFromLocalDate(releaseDate))),
                )

            whenever(featureFlipStrategyInitialiser.getFlipStrategyOrNull(strategyConfig))
                .thenReturn(expectedFlipStrategy)

            val featureInRelease = spy(MockFeatureFlagConfig.createFeature(name = "FEATURE_IN_RELEASE", release = releaseName))
            val anotherFeatureInRelease =
                spy(MockFeatureFlagConfig.createFeature(name = "ANOTHER_FEATURE_IN_RELEASE", release = releaseName))

            featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))
            featureFlagManager.createFeature(featureInRelease)
            featureFlagManager.createFeature(anotherFeatureInRelease)

            // Act
            featureFlagManager.initialiseFeatureReleases(listOf(release))

            // Assert
            verify(featureFlagManager).getFeaturesByGroup(releaseName)
            verify(featureFlipStrategyInitialiser, times(2)).getFlipStrategyOrNull(strategyConfig)

            val captor1 = captor<CombinedFlipStrategy>()
            verify(featureInRelease).setFlippingStrategy(captor1.capture())
            assertEquals(expectedFlipStrategy, captor1.value)

            val captor2 = captor<CombinedFlipStrategy>()
            verify(anotherFeatureInRelease).setFlippingStrategy(captor2.capture())
            assertEquals(expectedFlipStrategy, captor2.value)
        }

        @Test
        fun `enableFeatureRelease calls super enableGroup method with correct group name`() {
            // Arrange
            featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))
            val releaseName = "RELEASE_X_Y"
            val feature = spy(MockFeatureFlagConfig.createFeature(enabled = true, release = releaseName))
            featureFlagManager.createFeature(feature)

            // Act
            featureFlagManager.enableFeatureRelease(releaseName)

            // Assert
            val captor = captor<String>()
            verify(featureFlagManager).enableGroup(captor.capture())
            assertEquals(releaseName, captor.value)
        }

        @Test
        fun `disableFeatureRelease calls super disableGroup method with correct group name`() {
            // Arrange
            featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))
            val releaseName = "RELEASE_X_Y"
            val feature = spy(MockFeatureFlagConfig.createFeature(enabled = true, release = releaseName))
            featureFlagManager.createFeature(feature)

            // Act
            featureFlagManager.disableFeatureRelease(releaseName)

            // Assert
            val captor = captor<String>()
            verify(featureFlagManager).disableGroup(captor.capture())
            assertEquals(releaseName, captor.value)
        }
    }
}
