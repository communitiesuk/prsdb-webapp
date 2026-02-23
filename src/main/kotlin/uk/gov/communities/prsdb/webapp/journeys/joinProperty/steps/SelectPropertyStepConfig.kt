package uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps

import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.PropertyAddressSearchState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectFromListFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class SelectPropertyStepConfig : AbstractRequestableStepConfig<Complete, SelectFromListFormModel, PropertyAddressSearchState>() {
    override val formModelClass = SelectFromListFormModel::class

    override fun getStepSpecificContent(state: PropertyAddressSearchState): Map<String, Any?> {
        // TODO: PDJB-274 - Replace mock data with actual search results from FindPropertyStep
        val mockProperties =
            listOf(
                "Flat 1, 9 Example Street, London, SW9 0HD",
                "Flat 2, 9 Example Street, London, SW9 0HD",
                "9 Example Street, London, SW9 0HD",
            )

        val propertyRadiosViewModel =
            mockProperties.mapIndexed { index, address ->
                RadiosButtonViewModel(address, valueStr = (index + 1).toString())
            }

        return mapOf(
            "fieldSetHeading" to "joinProperty.selectProperty.heading",
            "postcode" to "SW9 0HD",
            "houseNameOrNumber" to "9",
            "propertyCount" to mockProperties.size,
            "searchAgainUrl" to "$JOIN_PROPERTY_ROUTE/${FindPropertyStep.ROUTE_SEGMENT}",
            "prnLookupUrl" to "$JOIN_PROPERTY_ROUTE/${FindPropertyByPrnStep.ROUTE_SEGMENT}",
            "options" to propertyRadiosViewModel,
        )
    }

    override fun chooseTemplate(state: PropertyAddressSearchState) = "forms/selectPropertyForm"

    override fun mode(state: PropertyAddressSearchState) =
        state.selectPropertyStep.formModelOrNull?.selectedOption?.let { Complete.COMPLETE }

    override fun afterPrimaryValidation(
        state: PropertyAddressSearchState,
        bindingResult: BindingResult,
    ) {
        val formModel = bindingResult.target as SelectFromListFormModel
        val selectedOption = formModel.selectedOption
        if (selectedOption == null) {
            bindingResult.rejectValue(SelectFromListFormModel::selectedOption.name, "joinProperty.selectProperty.error.missing")
        } else {
            // TODO: PDJB-274 - Validate against actual search results from state
            val validSelections = listOf("1", "2", "3")
            if (selectedOption !in validSelections) {
                bindingResult.rejectValue(
                    SelectFromListFormModel::selectedOption.name,
                    "joinProperty.selectProperty.error.invalidSelection",
                )
            }
        }
    }
}

@JourneyFrameworkComponent
final class SelectPropertyStep(
    stepConfig: SelectPropertyStepConfig,
) : RequestableStep<Complete, SelectFromListFormModel, PropertyAddressSearchState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "select-property"
    }
}
