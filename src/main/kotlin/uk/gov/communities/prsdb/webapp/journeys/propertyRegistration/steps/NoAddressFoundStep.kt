package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.AddressState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@Scope("prototype")
@PrsdbWebComponent
class NoAddressFoundStepConfig : AbstractGenericStepConfig<Complete, NoInputFormModel, AddressState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: AddressState): Map<String, Any?> {
        val lookupFormModel = state.lookupStep.formModel

        return mapOf(
            "title" to "registerProperty.title",
            "restrictToEngland" to true,
            "postcode" to lookupFormModel.notNullValue(LookupAddressFormModel::postcode),
            "houseNameOrNumber" to lookupFormModel.notNullValue(LookupAddressFormModel::houseNameOrNumber),
            "searchAgainUrl" to RegisterPropertyStepId.LookupAddress.urlPathSegment,
        )
    }

    override fun chooseTemplate(state: AddressState): String = "forms/noAddressFoundForm"

    override fun mode(state: AddressState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@Scope("prototype")
@PrsdbWebComponent
final class NoAddressFoundStep(
    stepConfig: NoAddressFoundStepConfig,
) : RequestableStep<Complete, NoInputFormModel, AddressState>(stepConfig)
