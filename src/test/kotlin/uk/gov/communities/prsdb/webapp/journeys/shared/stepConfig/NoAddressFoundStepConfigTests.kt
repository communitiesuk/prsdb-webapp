package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.INTERNATIONAL_ADDRESSES
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.AddressSearchRegion
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel

class NoAddressFoundStepConfigTests {
    private val mockFeatureFlagManager = mock<FeatureFlagManager>()

    @Test
    fun `afterStepDataIsAdded caches manual address selection`() {
        // Arrange
        val state = mock<AddressState>()

        // Act
        NoAddressFoundStepConfig(mockFeatureFlagManager).afterStepDataIsAdded(state)

        // Assert
        verify(state).cachedSelectedAddress = MANUAL_ADDRESS_CHOSEN
    }

    @ParameterizedTest
    @CsvSource(
        "WITH_ENGLISH_LOCAL_COUNCIL, englandPropertyDetails",
        "ENGLAND_AND_WALES, englandOrWales",
        "UK, uk",
    )
    fun `getStepSpecificContent returns the region's message keys when international addresses are enabled`(
        region: AddressSearchRegion,
        regionKey: String,
    ) {
        // Arrange
        whenever(mockFeatureFlagManager.checkFeature(INTERNATIONAL_ADDRESSES)).thenReturn(true)
        val stepConfig = NoAddressFoundStepConfig(mockFeatureFlagManager).restrictToRegion(region)

        // Act
        val content = stepConfig.getStepSpecificContent(mockState())

        // Assert
        assertEquals("addressForms.noAddressFound.heading.$regionKey", content["headingKey"])
        assertEquals("addressForms.noAddressFound.paragraph.one.beforeLink.$regionKey", content["paragraphOneBeforeLinkKey"])
        assertEquals("addressForms.noAddressFound.paragraph.two.$regionKey", content["paragraphTwoKey"])
    }

    @Test
    fun `getStepSpecificContent uses the UK message keys by default`() {
        // Arrange
        whenever(mockFeatureFlagManager.checkFeature(INTERNATIONAL_ADDRESSES)).thenReturn(true)

        // Act
        val content = NoAddressFoundStepConfig(mockFeatureFlagManager).getStepSpecificContent(mockState())

        // Assert
        assertEquals("addressForms.noAddressFound.heading.uk", content["headingKey"])
    }

    @ParameterizedTest
    @CsvSource(
        "WITH_ENGLISH_LOCAL_COUNCIL, englandPropertyDetails, englandPropertyDetails",
        "ENGLAND_AND_WALES, englandOrWales, legacy",
        "UK, englandOrWales, legacy",
    )
    fun `getStepSpecificContent returns the original message keys when international addresses are disabled`(
        region: AddressSearchRegion,
        regionKey: String,
        beforeLinkKey: String,
    ) {
        // Arrange
        whenever(mockFeatureFlagManager.checkFeature(INTERNATIONAL_ADDRESSES)).thenReturn(false)
        val stepConfig = NoAddressFoundStepConfig(mockFeatureFlagManager).restrictToRegion(region)

        // Act
        val content = stepConfig.getStepSpecificContent(mockState())

        // Assert
        assertEquals("addressForms.noAddressFound.heading.$regionKey", content["headingKey"])
        assertEquals("addressForms.noAddressFound.paragraph.one.beforeLink.$beforeLinkKey", content["paragraphOneBeforeLinkKey"])
        assertEquals("addressForms.noAddressFound.paragraph.two.$regionKey", content["paragraphTwoKey"])
    }

    @Test
    fun `getStepSpecificContent includes the searched postcode and house name or number`() {
        // Arrange
        whenever(mockFeatureFlagManager.checkFeature(INTERNATIONAL_ADDRESSES)).thenReturn(true)

        // Act
        val content = NoAddressFoundStepConfig(mockFeatureFlagManager).getStepSpecificContent(mockState())

        // Assert
        assertEquals("EG1 2AA", content["postcode"])
        assertEquals("1", content["houseNameOrNumber"])
    }

    private fun mockState(): AddressState {
        val lookupFormModel =
            LookupAddressFormModel().apply {
                postcode = "EG1 2AA"
                houseNameOrNumber = "1"
            }
        val lookupStep =
            mock<LookupAddressStep> {
                on { formModel } doReturn lookupFormModel
                on { currentJourneyId } doReturn "journeyId"
                on { isStepReachable } doReturn false
            }
        return mock { on { lookupAddressStep } doReturn lookupStep }
    }
}
