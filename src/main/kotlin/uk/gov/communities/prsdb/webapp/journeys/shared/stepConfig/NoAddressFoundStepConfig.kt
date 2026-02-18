package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class NoAddressFoundStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, AddressState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: AddressState) =
        mapOf(
            "restrictToEngland" to restrictToEngland,
            "postcode" to state.lookupAddressStep.formModel.notNullValue(LookupAddressFormModel::postcode),
            "houseNameOrNumber" to state.lookupAddressStep.formModel.notNullValue(LookupAddressFormModel::houseNameOrNumber),
            "searchAgainUrl" to Destination(state.lookupAddressStep).toUrlStringOrNull(),
        )

    override fun chooseTemplate(state: AddressState) = "forms/noAddressFoundForm"

    override fun mode(state: AddressState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    private var restrictToEngland: Boolean = false

    fun restrictToEngland(): NoAddressFoundStepConfig {
        this.restrictToEngland = true
        return this
    }
}

@JourneyFrameworkComponent
final class NoAddressFoundStep(
    stepConfig: NoAddressFoundStepConfig,
) : RequestableStep<Complete, NoInputFormModel, AddressState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "no-address-found"
    }
}
