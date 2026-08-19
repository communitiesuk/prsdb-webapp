package uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class AreYouSureStepConfigTests {
    @Mock
    lateinit var mockState: LandlordDeregistrationJourneyState

    @Test
    fun `mode returns null when form model is not present`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(null)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Test
    fun `mode returns null when landlord has no registered properties and wantsToProceed is not set`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(emptyMap())
        whenever(mockState.userHasRegisteredProperties).thenReturn(false)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Test
    fun `mode returns WANTS_TO_PROCEED when landlord has registered properties and form is submitted`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(emptyMap())
        whenever(mockState.userHasRegisteredProperties).thenReturn(true)

        val result = stepConfig.mode(mockState)

        assertEquals(AreYouSureMode.WANTS_TO_PROCEED, result)
    }

    @Test
    fun `mode returns WANTS_TO_PROCEED when landlord has no registered properties and wantsToProceed is true`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(mapOf("wantsToProceed" to "true"))
        whenever(mockState.userHasRegisteredProperties).thenReturn(false)

        val result = stepConfig.mode(mockState)

        assertEquals(AreYouSureMode.WANTS_TO_PROCEED, result)
    }

    @Test
    fun `mode returns DOES_NOT_WANT_TO_PROCEED when landlord has no registered properties and wantsToProceed is false`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(mapOf("wantsToProceed" to "false"))
        whenever(mockState.userHasRegisteredProperties).thenReturn(false)

        val result = stepConfig.mode(mockState)

        assertEquals(AreYouSureMode.DOES_NOT_WANT_TO_PROCEED, result)
    }

    @Test
    fun `chooseTemplate returns landlordDeregistrationAreYouSure when landlord has registered properties`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.userHasRegisteredProperties).thenReturn(true)

        val result = stepConfig.chooseTemplate(mockState)

        assertEquals("forms/landlordDeregistrationAreYouSure", result)
    }

    @Test
    fun `chooseTemplate returns areYouSureForm when landlord has no registered properties`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.userHasRegisteredProperties).thenReturn(false)

        val result = stepConfig.chooseTemplate(mockState)

        assertEquals("forms/areYouSureForm", result)
    }

    @Test
    fun `enrichSubmittedDataBeforeValidation sets wantsToProceed to true when landlord has registered properties`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.userHasRegisteredProperties).thenReturn(true)

        val result = stepConfig.enrichSubmittedDataBeforeValidation(mockState, emptyMap())

        assertEquals(true, result[LandlordDeregistrationAreYouSureFormModel::wantsToProceed.name])
        assertEquals(true, result[LandlordDeregistrationAreYouSureFormModel::userHasRegisteredProperties.name])
    }

    @Test
    fun `enrichSubmittedDataBeforeValidation does not set wantsToProceed when landlord has no registered properties`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.userHasRegisteredProperties).thenReturn(false)

        val result = stepConfig.enrichSubmittedDataBeforeValidation(mockState, emptyMap())

        assertNull(result[LandlordDeregistrationAreYouSureFormModel::wantsToProceed.name])
        assertEquals(false, result[LandlordDeregistrationAreYouSureFormModel::userHasRegisteredProperties.name])
    }

    @Test
    fun `getStepSpecificContent includes radioOptions and fieldSetHeading when landlord has no registered properties`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.userHasRegisteredProperties).thenReturn(false)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertNull(content["cancelLinkUrl"])
        assertEquals("deregisterLandlord.areYouSure.noProperties.fieldSetHeading", content["fieldSetHeading"])
        assertTrue(content.containsKey("radioOptions"))
    }

    @Test
    fun `getStepSpecificContent includes cancelLinkUrl when landlord has registered properties`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.userHasRegisteredProperties).thenReturn(true)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(LANDLORD_DETAILS_FOR_LANDLORD_ROUTE, content["cancelLinkUrl"])
        assertNull(content["radioOptions"])
        assertNull(content["fieldSetHeading"])
    }

    private fun setupStepConfig(): AreYouSureStepConfig {
        val stepConfig = AreYouSureStepConfig()
        stepConfig.urlPath = AreYouSureStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
