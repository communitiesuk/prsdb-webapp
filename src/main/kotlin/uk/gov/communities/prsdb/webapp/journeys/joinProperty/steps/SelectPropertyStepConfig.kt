package uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps

import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.JoinPropertyAddressSearchState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectPropertyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class SelectPropertyStepConfig : AbstractRequestableStepConfig<Complete, SelectPropertyFormModel, JoinPropertyAddressSearchState>() {
    override val formModelClass = SelectPropertyFormModel::class

    override fun getStepSpecificContent(state: JoinPropertyAddressSearchState): Map<String, Any?> {
        val lookedUpAddresses =
            state.cachedAddresses
                ?: throw NotNullFormModelValueIsNullException("No cached addresses found in AddressSearchState")

        val propertyRadiosViewModel =
            lookedUpAddresses.mapIndexed { index, address ->
                RadiosButtonViewModel(address.singleLineAddress, valueStr = (index + 1).toString())
            }

        return mapOf(
            "fieldSetHeading" to "joinProperty.selectProperty.heading",
            "submitButtonText" to "forms.buttons.continue",
            "beforeCountMessageKey" to "joinProperty.selectProperty.fieldSetHint.beforeCount",
            "resultCount" to lookedUpAddresses.size,
            "postcode" to state.lookupAddressStep.formModel.notNullValue(LookupAddressFormModel::postcode),
            "houseNameOrNumber" to state.lookupAddressStep.formModel.notNullValue(LookupAddressFormModel::houseNameOrNumber),
            "searchAgainUrl" to Destination(state.lookupAddressStep).toUrlStringOrNull(),
            "prnLookupUrl" to "$JOIN_PROPERTY_ROUTE/${FindPropertyByPrnStep.ROUTE_SEGMENT}",
            "options" to propertyRadiosViewModel,
        )
    }

    override fun chooseTemplate(state: JoinPropertyAddressSearchState) = "forms/selectFromListForm"

    override fun mode(state: JoinPropertyAddressSearchState) =
        state.selectPropertyStep.formModelOrNull?.selectedOption?.let { Complete.COMPLETE }

    override fun afterPrimaryValidation(
        state: JoinPropertyAddressSearchState,
        bindingResult: BindingResult,
    ) {
        val formModel = bindingResult.target as SelectPropertyFormModel
        val selectedOption = formModel.selectedOption ?: return
        if (state.getMatchingAddress(selectedOption) == null) {
            bindingResult.rejectValueWithMessageKey(
                SelectPropertyFormModel::selectedOption.name,
                "joinProperty.selectProperty.error.invalidSelection",
            )
        }
    }
}

@JourneyFrameworkComponent
final class SelectPropertyStep(
    stepConfig: SelectPropertyStepConfig,
) : RequestableStep<Complete, SelectPropertyFormModel, JoinPropertyAddressSearchState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "select-property"
    }
}
