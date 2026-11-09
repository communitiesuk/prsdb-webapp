package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderProvideDetailsFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ProvideFreeholderDetailsStepConfigTests {
    @Mock
    lateinit var mockState: FreeholdersDetailsState

    @Mock
    lateinit var mockValidator: Validator

    private val stepConfig = ProvideFreeholderDetailsStepConfig()

    @ParameterizedTest
    @CsvSource("true, joint", "false, nonJoint")
    fun `Freeholders use the shared details template with their ownership-specific content`(
        isJointFreeholder: Boolean,
        headingSuffix: String,
    ) {
        whenever(mockState.isJointFreeholder).thenReturn(isJointFreeholder)

        val content = stepConfig.getStepSpecificContent(mockState)
        val template = stepConfig.chooseTemplate(mockState)

        assertEquals("forms/provideDetailsForm", template)
        assertEquals("provideDetails", content["fieldName"])
        assertEquals("registerProperty.freeholders.provideDetails.fieldSetHeading.$headingSuffix", content["fieldSetHeading"])
        assertEquals("forms.buttons.saveAndContinue", content["submitButtonText"])
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Validation uses the ownership type from journey state rather than submitted data`(isJointFreeholder: Boolean) {
        whenever(mockState.isJointFreeholder).thenReturn(isJointFreeholder)
        val formData = mapOf("isJointFreeholder" to !isJointFreeholder, "provideDetails" to true)

        val enrichedData = stepConfig.enrichSubmittedDataBeforeValidation(mockState, formData)

        assertEquals(isJointFreeholder, enrichedData["isJointFreeholder"])
        assertEquals(true, enrichedData["provideDetails"])
    }

    @ParameterizedTest
    @CsvSource("true, YES", "false, NO")
    fun `Mode maps Boolean answers to the existing journey outcomes`(
        provideDetails: Boolean,
        expectedOutcome: YesOrNo,
    ) {
        stepConfig.urlPath = ProvideFreeholderDetailsStep.ROUTE_SEGMENT
        stepConfig.validator = mockValidator
        whenever(mockValidator.supports(any())).thenReturn(true)
        whenever(mockState.getStepData(stepConfig.stepDataKey)).thenReturn(mapOf("provideDetails" to provideDetails))

        assertEquals(expectedOutcome, stepConfig.mode(mockState))
    }

    @Test
    fun `Mode remains unset when there is no saved answer`() {
        stepConfig.urlPath = ProvideFreeholderDetailsStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()

        assertNull(stepConfig.mode(mockState))
    }

    @Test
    fun `Editing a freeholder with known details preselects yes`() {
        whenever(mockState.editingFreeholder).thenReturn(FreeholderDataModel(detailsKnown = true))
        val formModel = FreeholderProvideDetailsFormModel()

        val content = stepConfig.resolvePageContent(mockState, mapOf(FORM_MODEL_ATTR_NAME to formModel))

        assertEquals(true, (content[FORM_MODEL_ATTR_NAME] as FreeholderProvideDetailsFormModel).provideDetails)
    }

    @Test
    fun `Editing a freeholder without known details preselects no`() {
        whenever(mockState.editingFreeholder).thenReturn(FreeholderDataModel(detailsKnown = false))
        val formModel = FreeholderProvideDetailsFormModel()

        val content = stepConfig.resolvePageContent(mockState, mapOf(FORM_MODEL_ATTR_NAME to formModel))

        assertEquals(false, (content[FORM_MODEL_ATTR_NAME] as FreeholderProvideDetailsFormModel).provideDetails)
    }

    @Test
    fun `ResolvePageContent does not overwrite an existing answer`() {
        val formModel = FreeholderProvideDetailsFormModel(false)

        val content = stepConfig.resolvePageContent(mockState, mapOf(FORM_MODEL_ATTR_NAME to formModel))

        assertEquals(false, (content[FORM_MODEL_ATTR_NAME] as FreeholderProvideDetailsFormModel).provideDetails)
    }

    @Test
    fun `ResolvePageContent leaves a new freeholder answer blank`() {
        whenever(mockState.editingFreeholder).thenReturn(null)
        val formModel = FreeholderProvideDetailsFormModel()

        val content = stepConfig.resolvePageContent(mockState, mapOf(FORM_MODEL_ATTR_NAME to formModel))

        assertNull((content[FORM_MODEL_ATTR_NAME] as FreeholderProvideDetailsFormModel).provideDetails)
    }
}
