package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class ReactivationAlreadyRegisteredStepConfig :
    AbstractRequestableStepConfig<Nothing, NoInputFormModel, PropertyReactivationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyReactivationJourneyState) =
        mapOf("registerPropertyUrl" to RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE)

    override fun chooseTemplate(state: PropertyReactivationJourneyState) = "reactivationAlreadyRegisteredPropertyPage"

    override fun mode(state: PropertyReactivationJourneyState) = null
}

@JourneyFrameworkComponent
final class ReactivationAlreadyRegisteredStep(
    stepConfig: ReactivationAlreadyRegisteredStepConfig,
) : RequestableStep<Nothing, NoInputFormModel, PropertyReactivationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "already-registered"
    }
}
