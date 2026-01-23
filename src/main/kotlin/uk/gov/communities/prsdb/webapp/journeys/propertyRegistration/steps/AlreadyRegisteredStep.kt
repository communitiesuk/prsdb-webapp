package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class AlreadyRegisteredStepConfig : AbstractRequestableStepConfig<Nothing, NoInputFormModel, AddressState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: AddressState): Map<String, Any?> =
        mapOf(
            "title" to "registerProperty.title",
            "searchAgainUrl" to Destination(state.lookupStep).toUrlStringOrNull(),
            "singleLineAddress" to state.selectAddressStep.formModel.address,
        )

    override fun chooseTemplate(state: AddressState): String = "alreadyRegisteredPropertyPage"

    override fun mode(state: AddressState): Nothing? = null
}

@JourneyFrameworkComponent
final class AlreadyRegisteredStep(
    stepConfig: AlreadyRegisteredStepConfig,
) : RequestableStep<Nothing, NoInputFormModel, AddressState>(stepConfig)
