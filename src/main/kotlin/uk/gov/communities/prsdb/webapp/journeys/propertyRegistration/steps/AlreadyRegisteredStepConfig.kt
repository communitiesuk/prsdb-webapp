package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.PropertyRegistrationAddressState

@JourneyFrameworkComponent
class AlreadyRegisteredStepConfig : AbstractGenericStepConfig<Nothing, Nothing, PropertyRegistrationAddressState>() {
    override val formModelClass = Nothing::class

    override fun getStepSpecificContent(state: PropertyRegistrationAddressState) =
        mapOf(
            "title" to "registerProperty.title",
            "searchAgainUrl" to Destination(state.lookupAddressStep).toUrlStringOrNull(),
            "singleLineAddress" to state.selectAddressStep.formModel.address,
        )

    override fun chooseTemplate(state: PropertyRegistrationAddressState) = "alreadyRegisteredPropertyPage"

    override fun mode(state: PropertyRegistrationAddressState) = null
}

@JourneyFrameworkComponent
final class AlreadyRegisteredStep(
    stepConfig: AlreadyRegisteredStepConfig,
) : RequestableStep<Nothing, Nothing, PropertyRegistrationAddressState>(stepConfig)
