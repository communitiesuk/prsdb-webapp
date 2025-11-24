package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.ff4j.core.Feature
import org.ff4j.property.PropertyDate
import org.ff4j.strategy.time.ReleaseDateFlipStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagGroupModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagModel
import java.time.LocalDate

class FeatureFlagManagerTests {
    @MockitoSpyBean
    lateinit var featureFlagManager: FeatureFlagManager

    @Test
    fun `initialiseFeatureFlags creates features from the supplied FeatureFlagModel list`() {
        // Arrange
        featureFlagManager = spy(FeatureFlagManager())

        val featureFlagsFromConfig =
            listOf(
                FeatureFlagModel(
                    name = "TEST_FEATURE_FLAG_1",
                    enabled = true,
                    expiryDate = LocalDate.of(2030, 2, 14),
                ),
                FeatureFlagModel(
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
        featureFlagManager = spy(FeatureFlagManager())
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
        featureFlagManager = spy(FeatureFlagManager())
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
        featureFlagManager = spy(FeatureFlagManager())
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
            val flagOneName = "FEATURE_IN_GROUP_1"
            val flagTwoName = "FEATURE_IN_GROUP_2"
            val groupName = "RELEASE_X_Y"
            featureFlagManager = spy(FeatureFlagManager())

            val featureFlagGroup =
                FeatureFlagGroupModel(
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
            featureFlagManager.initialiseFeatureFlagGroups(listOf(featureFlagGroup))

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
    fun `initialiseFeatureFlagGroups throws an error if empty groups are added in config`() {
        // Arrange
        featureFlagManager = spy(FeatureFlagManager())

        val featureFlagGroup =
            FeatureFlagGroupModel(
                name = "EMPTY_GROUP",
                enabled = true,
            )

        // Act & Assert
        try {
            featureFlagManager.initialiseFeatureFlagGroups(listOf(featureFlagGroup))
        } catch (e: RuntimeException) {
            assert(
                e.message!!
                    .contains("Check that at least one feature in FeatureFlagConfig.featureFlags has EMPTY_GROUP set as flagGroup."),
            )
        }
    }

    @Test
    fun `initialiseFeatureFlagGroups adds release date flipping strategy to features in the group when a release date is set`() {
        // Arrange
        val flagOneName = "FEATURE_IN_GROUP_1"
        val flagTwoName = "FEATURE_IN_GROUP_2"
        val groupName = "RELEASE_X_Y"
        val releaseDate = LocalDate.of(2025, 12, 25)
        val expectedReleaseDateFlipStrategy = ReleaseDateFlipStrategy(DateTimeHelper.getJavaDateFromLocalDate(releaseDate))

        featureFlagManager = spy(FeatureFlagManager())

        val featureFlagGroup =
            FeatureFlagGroupModel(
                name = groupName,
                enabled = true,
                releaseDate = releaseDate,
            )

        val featureInGroup1 = spy(Feature(flagOneName, true))
        featureInGroup1.group = groupName
        featureFlagManager.createFeature(featureInGroup1)

        val featureInGroup2 = spy(Feature(flagTwoName, true))
        featureInGroup2.group = groupName
        featureFlagManager.createFeature(featureInGroup2)

        // Act
        featureFlagManager.initialiseFeatureFlagGroups(listOf(featureFlagGroup))

        // Assert
        verify(featureFlagManager).getFeaturesByGroup(groupName)

        val captor1 = captor<ReleaseDateFlipStrategy>()
        verify(featureInGroup1).setFlippingStrategy(captor1.capture())
        assertTrue(ReflectionEquals(expectedReleaseDateFlipStrategy).matches(captor1.value))

        val captor2 = captor<ReleaseDateFlipStrategy>()
        verify(featureInGroup2).setFlippingStrategy(captor2.capture())
        assertTrue(ReflectionEquals(expectedReleaseDateFlipStrategy).matches(captor2.value))
    }

    @Test
    fun `updateReleaseDateOnFeaturesInFeatureGroup updates the release date on features in the group`() {
        // Arrange
        val flagOneName = "FEATURE_IN_GROUP_1"
        val groupName = "RELEASE_X_Y"
        val newReleaseDate = LocalDate.of(2026, 1, 1)
        val strategy = spy(ReleaseDateFlipStrategy(DateTimeHelper.getJavaDateFromLocalDate(LocalDate.of(2025, 12, 25))))

        featureFlagManager = FeatureFlagManager()

        val featureInGroup1 = spy(Feature(flagOneName, true))
        featureInGroup1.group = groupName
        featureInGroup1.flippingStrategy = strategy
        featureFlagManager.createFeature(featureInGroup1)

        // Act
        featureFlagManager.updateReleaseDateOnFeaturesInFeatureGroup(groupName, newReleaseDate)

        // Assert new strategy set
        verify(featureInGroup1.flippingStrategy as ReleaseDateFlipStrategy).setReleaseDate(
            DateTimeHelper.getJavaDateFromLocalDate(newReleaseDate),
        )
    }

    @Test
    fun `enableFeatureGroup calls super enableGroup method with correct group name`() {
        // Arrange
        featureFlagManager = spy(FeatureFlagManager())
        val groupName = "SOME_FEATURE_GROUP"
        val featureFlagName = "FEATURE_FLAG"
        val feature = spy(Feature(featureFlagName, true))
        feature.group = groupName
        featureFlagManager.createFeature(feature)

        // Act
        featureFlagManager.enableFeatureGroup(groupName)

        // Assert
        val captor = ArgumentCaptor.forClass(String::class.java)

        verify(featureFlagManager).enableGroup(captor.capture())
        assertEquals(groupName, captor.value)
    }

    @Test
    fun `disableFeatureGroup calls super disableGroup method with correct group name`() {
        // Arrange
        featureFlagManager = spy(FeatureFlagManager())
        val groupName = "SOME_FEATURE_GROUP"
        val featureFlagName = "FEATURE_FLAG"
        val feature = spy(Feature(featureFlagName, true))
        feature.group = groupName
        featureFlagManager.createFeature(feature)

        // Act
        featureFlagManager.disableFeatureGroup(groupName)

        // Assert
        val captor = ArgumentCaptor.forClass(String::class.java)
        verify(featureFlagManager).disableGroup(captor.capture())
        assertEquals(groupName, captor.value)
    }
}
