package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.ff4j.core.Feature
import org.ff4j.property.PropertyDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.communities.prsdb.webapp.config.FeatureFlipStrategyInitialiser
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagConfigModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseConfigModel
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
                FeatureFlagConfigModel(
                    name = "TEST_FEATURE_FLAG_1",
                    enabled = true,
                    expiryDate = LocalDate.of(2030, 2, 14),
                ),
                FeatureFlagConfigModel(
                    name = "TEST_FEATURE_FLAG_2",
                    enabled = false,
                    expiryDate = LocalDate.of(2030, 6, 14),
                ),
            )

        // Act
        featureFlagManager.initializeFeatureFlags(featureFlagsFromConfig)

        // Assert
        val captor = ArgumentCaptor.forClass(Feature::class.java)
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
    fun `checkFeature calls super check method with correct feature name`() {
        // Arrange
        featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))
        val featureName = "SOME_FEATURE_FLAG"
        featureFlagManager.createFeature(featureName)

        // Act
        featureFlagManager.checkFeature(featureName)

        // Assert
        val captor = ArgumentCaptor.forClass(String::class.java)

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
        val captor = ArgumentCaptor.forClass(String::class.java)

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
        val captor = ArgumentCaptor.forClass(String::class.java)

        verify(featureFlagManager).disable(captor.capture())
        assertEquals(featureName, captor.value)
    }

    @Nested
    inner class FeatureFlagGroupMethodsTests {
        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `initialiseFeatureFlagGroups enables or disables all features in the group based on the group's enabled value`(
            groupEnabled: Boolean,
        ) {
            // Arrange
            val flagOneName = "FEATURE_IN_RELEASE_X_Y"
            val flagTwoName = "ANOTHER_FEATURE_IN_RELEASE_X_Y"
            val groupName = "RELEASE_X_Y"
            featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))

            val featureFlagGroup =
                FeatureReleaseConfigModel(
                    name = groupName,
                    enabled = groupEnabled,
                )

            val featureInGroup1 = Feature(flagOneName, !groupEnabled)
            featureInGroup1.group = groupName
            featureFlagManager.createFeature(featureInGroup1)

            val featureInGroup2 = Feature(flagTwoName, !groupEnabled)
            featureInGroup2.group = groupName
            featureFlagManager.createFeature(featureInGroup2)

            assertEquals(!groupEnabled, featureFlagManager.checkFeature(flagOneName))
            assertEquals(!groupEnabled, featureFlagManager.checkFeature(flagTwoName))

            // Act
            featureFlagManager.initialiseFeatureReleases(listOf(featureFlagGroup))

            // Assert
            if (groupEnabled) {
                verify(featureFlagManager).enableGroup(groupName)
            } else {
                verify(featureFlagManager).disableGroup(groupName)
            }
            assertEquals(groupEnabled, featureFlagManager.checkFeature(flagOneName))
            assertEquals(groupEnabled, featureFlagManager.checkFeature(flagTwoName))
        }
    }

    @Test
    fun `initialiseFeatureReleases throws an error if empty groups are added in config`() {
        // Arrange
        featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))

        val featureFlagGroup =
            FeatureReleaseConfigModel(
                name = "EMPTY_RELEASE",
                enabled = true,
            )
        val expectedErrorMessage =
            "EMPTY_RELEASE group does not exist in store" +
                ". Check that at least one feature in the yaml config has this release's name set as release."

        // Act & Assert
        val exception = assertThrows<RuntimeException> { featureFlagManager.initialiseFeatureReleases(listOf(featureFlagGroup)) }
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun `enableFeatureRelease calls super enableGroup method with correct group name`() {
        // Arrange
        featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))
        val groupName = "SOME_FEATURE_GROUP"
        val featureFlagName = "FEATURE_FLAG"
        val feature = spy(Feature(featureFlagName, true))
        feature.group = groupName
        featureFlagManager.createFeature(feature)

        // Act
        featureFlagManager.enableFeatureRelease(groupName)

        // Assert
        val captor = ArgumentCaptor.forClass(String::class.java)

        verify(featureFlagManager).enableGroup(captor.capture())
        assertEquals(groupName, captor.value)
    }

    @Test
    fun `disableFeatureRelease calls super disableGroup method with correct group name`() {
        // Arrange
        featureFlagManager = spy(FeatureFlagManager(featureFlipStrategyInitialiser))
        val groupName = "SOME_FEATURE_GROUP"
        val featureFlagName = "FEATURE_FLAG"
        val feature = spy(Feature(featureFlagName, true))
        feature.group = groupName
        featureFlagManager.createFeature(feature)

        // Act
        featureFlagManager.disableFeatureRelease(groupName)

        // Assert
        val captor = ArgumentCaptor.forClass(String::class.java)
        verify(featureFlagManager).disableGroup(captor.capture())
        assertEquals(groupName, captor.value)
    }
}
