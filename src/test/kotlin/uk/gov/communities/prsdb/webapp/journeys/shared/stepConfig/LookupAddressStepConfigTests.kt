package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.INTERNATIONAL_ADDRESSES
import uk.gov.communities.prsdb.webapp.constants.enums.AddressSearchRegion
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressSearchState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.services.AddressService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class LookupAddressStepConfigTests {
    @Mock
    lateinit var mockAddressService: AddressService

    @Mock
    lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Mock
    lateinit var mockState: AddressSearchState

    @Test
    fun `resolvePageContent pre-fills postcode and house name when prefill data is provided`() {
        val stepConfig = setupStepConfig()
        val formModel = LookupAddressFormModel()
        val content =
            mapOf(
                FORM_MODEL_ATTR_NAME to formModel,
                LookupAddressStepConfig.PREFILL_POSTCODE to "EG1 2AA",
                LookupAddressStepConfig.PREFILL_HOUSE_NAME_OR_NUMBER to "1",
            )

        val result = stepConfig.resolvePageContent(mockState, content)

        val resultFormModel = result[FORM_MODEL_ATTR_NAME] as LookupAddressFormModel
        assertEquals("EG1 2AA", resultFormModel.postcode)
        assertEquals("1", resultFormModel.houseNameOrNumber)
    }

    @Test
    fun `resolvePageContent does not overwrite existing postcode`() {
        val stepConfig = setupStepConfig()
        val formModel = LookupAddressFormModel()
        formModel.postcode = "SW1A 1AA"
        val content =
            mapOf(
                FORM_MODEL_ATTR_NAME to formModel,
                LookupAddressStepConfig.PREFILL_POSTCODE to "EG1 2AA",
                LookupAddressStepConfig.PREFILL_HOUSE_NAME_OR_NUMBER to "1",
            )

        val result = stepConfig.resolvePageContent(mockState, content)

        val resultFormModel = result[FORM_MODEL_ATTR_NAME] as LookupAddressFormModel
        assertEquals("SW1A 1AA", resultFormModel.postcode)
    }

    @Test
    fun `resolvePageContent returns default content when no prefill data is provided`() {
        val stepConfig = setupStepConfig()
        val formModel = LookupAddressFormModel()
        val content = mapOf(FORM_MODEL_ATTR_NAME to formModel)

        val result = stepConfig.resolvePageContent(mockState, content)

        val resultFormModel = result[FORM_MODEL_ATTR_NAME] as LookupAddressFormModel
        assertNull(resultFormModel.postcode)
        assertNull(resultFormModel.houseNameOrNumber)
    }

    @Test
    fun `chooseTemplate returns the default lookup address template when no override is configured`() {
        val stepConfig = setupStepConfig()

        assertEquals(LookupAddressStepConfig.DEFAULT_TEMPLATE, stepConfig.chooseTemplate(mockState))
    }

    @Test
    fun `chooseTemplate returns the configured template when one is set`() {
        val stepConfig = setupStepConfig().withTemplate("forms/correspondenceLookupAddressForm")

        assertEquals("forms/correspondenceLookupAddressForm", stepConfig.chooseTemplate(mockState))
    }

    @Nested
    inner class AfterStepDataIsAdded {
        private val houseNameOrNumber = "1"
        private val postcode = "EG1 2AA"

        @BeforeEach
        fun setUp() {
            whenever(mockState.getStepData(LookupAddressStep.ROUTE_SEGMENT))
                .thenReturn(mapOf("houseNameOrNumber" to houseNameOrNumber, "postcode" to postcode))
        }

        @Test
        fun `searches the whole UK by default`() {
            setupStepConfig().afterStepDataIsAdded(mockState)

            verify(mockAddressService).searchForAddresses(houseNameOrNumber, postcode, AddressSearchRegion.UK)
        }

        @ParameterizedTest
        @EnumSource(AddressSearchRegion::class)
        fun `searches the configured region when international addresses are enabled`(region: AddressSearchRegion) {
            lenient().`when`(mockFeatureFlagManager.checkFeature(INTERNATIONAL_ADDRESSES)).thenReturn(true)

            setupStepConfig().restrictToRegion(region).afterStepDataIsAdded(mockState)

            verify(mockAddressService).searchForAddresses(houseNameOrNumber, postcode, region)
        }

        @ParameterizedTest
        @CsvSource(
            "ENGLAND_WITH_LOCAL_COUNCIL, ENGLAND_WITH_LOCAL_COUNCIL",
            "ENGLAND_AND_WALES, UK",
            "UK, UK",
        )
        fun `searches England with local council or the whole UK when international addresses are disabled`(
            configuredRegion: AddressSearchRegion,
            searchedRegion: AddressSearchRegion,
        ) {
            lenient().`when`(mockFeatureFlagManager.checkFeature(INTERNATIONAL_ADDRESSES)).thenReturn(false)

            setupStepConfig().restrictToRegion(configuredRegion).afterStepDataIsAdded(mockState)

            verify(mockAddressService).searchForAddresses(houseNameOrNumber, postcode, searchedRegion)
        }
    }

    private fun setupStepConfig(): LookupAddressStepConfig {
        val stepConfig = LookupAddressStepConfig(mockAddressService, mockFeatureFlagManager)
        stepConfig.urlPath = LookupAddressStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
