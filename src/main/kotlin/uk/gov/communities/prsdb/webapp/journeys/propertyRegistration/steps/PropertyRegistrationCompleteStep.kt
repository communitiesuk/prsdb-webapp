package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.example.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@Scope("prototype")
@PrsdbWebComponent
class CompletePropertyRegistrationConfig : AbstractGenericStepConfig<Complete, NoInputFormModel, PropertyRegistrationJourneyState>() {
    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState): Map<String, Any?> = mapOf()

    override fun chooseTemplate(state: PropertyRegistrationJourneyState): String = ""

    override val formModelClass = NoInputFormModel::class

    override fun mode(state: PropertyRegistrationJourneyState) = Complete.COMPLETE

    override fun afterIsStepReached(state: PropertyRegistrationJourneyState) {
        state.deleteJourney()
    }
}

@Scope("prototype")
@PrsdbWebComponent
class CompletePropertyRegistrationStep(
    stepConfig: CompletePropertyRegistrationConfig,
) : JourneyStep.InternalStep<Complete, NoInputFormModel, PropertyRegistrationJourneyState>(stepConfig)
