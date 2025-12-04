package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.ff4j.core.Feature
import org.ff4j.property.PropertyDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
}
