package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ManualAddressStepConfigTests {
    @Mock
    lateinit var mockState: AddressState

    @Test
    fun `resolvePageContent pre-fills all manual address fields when prefill data is provided`() {
        val stepConfig = setupStepConfig()
        val formModel = ManualAddressFormModel()
        val content =
            mapOf(
                FORM_MODEL_ATTR_NAME to formModel,
                "prefillAddressLineOne" to "123 Main Street",
                "prefillAddressLineTwo" to "Flat 4",
                "prefillTownOrCity" to "London",
                "prefillCounty" to "Greater London",
                "prefillPostcode" to "EG1 2AA",
            )

        val result = stepConfig.resolvePageContent(mockState, content)

        val resultFormModel = result[FORM_MODEL_ATTR_NAME] as ManualAddressFormModel
        assertEquals("123 Main Street", resultFormModel.addressLineOne)
        assertEquals("Flat 4", resultFormModel.addressLineTwo)
        assertEquals("London", resultFormModel.townOrCity)
        assertEquals("Greater London", resultFormModel.county)
        assertEquals("EG1 2AA", resultFormModel.postcode)
    }

    @Test
    fun `resolvePageContent does not overwrite existing address line one`() {
        val stepConfig = setupStepConfig()
        val formModel = ManualAddressFormModel()
        formModel.addressLineOne = "456 Other Road"
        val content =
            mapOf(
                FORM_MODEL_ATTR_NAME to formModel,
                "prefillAddressLineOne" to "123 Main Street",
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
        val stepConfig = ManualAddressStepConfig()
        stepConfig.routeSegment = ManualAddressStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
