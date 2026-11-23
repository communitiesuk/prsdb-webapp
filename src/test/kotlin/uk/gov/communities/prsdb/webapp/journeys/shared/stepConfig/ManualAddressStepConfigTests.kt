package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.INTERNATIONAL_ADDRESSES
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ManualAddressStepConfigTests {
    @Mock
    lateinit var mockState: AddressState

    @Mock
    lateinit var mockFeatureFlagManager: FeatureFlagManager

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `getStepSpecificContent shows the country input only when international addresses is enabled`(isEnabled: Boolean) {
        // Arrange
        whenever(mockFeatureFlagManager.checkFeature(INTERNATIONAL_ADDRESSES)).thenReturn(isEnabled)
        val stepConfig = setupStepConfig()

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(isEnabled, content["isInternationalAddressesEnabled"])
        assertEquals("addressForms.manualAddress.country.label", content["countryLabel"])
    }

    // TODO: PDJB-2259: Remove the part of this test around keeping country, since the hiding code will be removed.
    @Test
    fun `enrichSubmittedDataBeforeValidation requires and keeps country when international addresses is enabled`() {
        // Arrange
        whenever(mockFeatureFlagManager.checkFeature(INTERNATIONAL_ADDRESSES)).thenReturn(true)
        val stepConfig = setupStepConfig()

        // Act
        val result = stepConfig.enrichSubmittedDataBeforeValidation(mockState, mapOf("country" to "England"))

        // Assert
        assertEquals(true, result[ManualAddressFormModel::isInternationalAddressesEnabled.name])
        assertEquals("England", result[ManualAddressFormModel::country.name])
    }

    // TODO: PDJB-2259: Remove this test
    @Test
    fun `enrichSubmittedDataBeforeValidation removes country when international addresses is disabled`() {
        // Arrange
        whenever(mockFeatureFlagManager.checkFeature(INTERNATIONAL_ADDRESSES)).thenReturn(false)
        val stepConfig = setupStepConfig()

        // Act
        val result = stepConfig.enrichSubmittedDataBeforeValidation(mockState, mapOf("country" to "England"))

        // Assert
        assertEquals(false, result[ManualAddressFormModel::isInternationalAddressesEnabled.name])
        assertFalse(result.containsKey(ManualAddressFormModel::country.name))
    }

    @Test
    fun `resolvePageContent pre-fills all manual address fields when prefill data is provided`() {
        val stepConfig = setupStepConfig()
        val formModel = ManualAddressFormModel()
        val content =
            mapOf(
                FORM_MODEL_ATTR_NAME to formModel,
                ManualAddressStepConfig.PREFILL_ADDRESS_LINE_ONE to "123 Main Street",
                ManualAddressStepConfig.PREFILL_ADDRESS_LINE_TWO to "Flat 4",
                ManualAddressStepConfig.PREFILL_TOWN_OR_CITY to "London",
                ManualAddressStepConfig.PREFILL_COUNTY to "Greater London",
                ManualAddressStepConfig.PREFILL_POSTCODE to "EG1 2AA",
                ManualAddressStepConfig.PREFILL_COUNTRY to "England",
            )

        val result = stepConfig.resolvePageContent(mockState, content)

        val resultFormModel = result[FORM_MODEL_ATTR_NAME] as ManualAddressFormModel
        assertEquals("123 Main Street", resultFormModel.addressLineOne)
        assertEquals("Flat 4", resultFormModel.addressLineTwo)
        assertEquals("London", resultFormModel.townOrCity)
        assertEquals("Greater London", resultFormModel.county)
        assertEquals("EG1 2AA", resultFormModel.postcode)
        assertEquals("England", resultFormModel.country)
    }

    @Test
    fun `resolvePageContent does not overwrite existing address line one`() {
        val stepConfig = setupStepConfig()
        val formModel = ManualAddressFormModel()
        formModel.addressLineOne = "456 Other Road"
        val content =
            mapOf(
                FORM_MODEL_ATTR_NAME to formModel,
                ManualAddressStepConfig.PREFILL_ADDRESS_LINE_ONE to "123 Main Street",
            )

        val result = stepConfig.resolvePageContent(mockState, content)

        val resultFormModel = result[FORM_MODEL_ATTR_NAME] as ManualAddressFormModel
        assertEquals("456 Other Road", resultFormModel.addressLineOne)
    }

    @Test
    fun `resolvePageContent returns default content when no prefill data is provided`() {
        val stepConfig = setupStepConfig()
        val formModel = ManualAddressFormModel()
        val content = mapOf(FORM_MODEL_ATTR_NAME to formModel)

        val result = stepConfig.resolvePageContent(mockState, content)

        val resultFormModel = result[FORM_MODEL_ATTR_NAME] as ManualAddressFormModel
        assertNull(resultFormModel.addressLineOne)
    }

    private fun setupStepConfig(): ManualAddressStepConfig {
        val stepConfig = ManualAddressStepConfig(mockFeatureFlagManager)
        stepConfig.urlPath = ManualAddressStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
