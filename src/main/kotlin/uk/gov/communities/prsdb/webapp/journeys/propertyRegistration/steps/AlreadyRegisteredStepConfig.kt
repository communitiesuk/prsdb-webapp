package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.PropertyRegistrationAddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class AlreadyRegisteredStepConfig : AbstractRequestableStepConfig<Nothing, NoInputFormModel, PropertyRegistrationAddressState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyRegistrationAddressState) =
        mapOf(
            "searchAgainUrl" to Destination(state.lookupAddressStep).toUrlStringOrNull(),
            "singleLineAddress" to state.selectAddressStep.formModel.address,
        )

    override fun chooseTemplate(state: PropertyRegistrationAddressState) = "alreadyRegisteredPropertyPage"

    override fun mode(state: PropertyRegistrationAddressState) = null
}

@JourneyFrameworkComponent
final class AlreadyRegisteredStep(
    stepConfig: AlreadyRegisteredStepConfig,
) : RequestableStep<Nothing, NoInputFormModel, PropertyRegistrationAddressState>(stepConfig)
