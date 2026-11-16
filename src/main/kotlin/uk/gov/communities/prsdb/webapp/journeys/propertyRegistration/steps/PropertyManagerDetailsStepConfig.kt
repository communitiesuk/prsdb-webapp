package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerDetailsFormModel

@JourneyFrameworkComponent
class PropertyManagerDetailsStepConfig :
    AbstractRequestableStepConfig<Complete, PropertyManagerDetailsFormModel, JourneyState>() {
    override val formModelClass = PropertyManagerDetailsFormModel::class

    override fun getStepSpecificContent(state: JourneyState) = emptyMap<String, Any?>()

    override fun chooseTemplate(state: JourneyState) = "forms/propertyManagerDetailsForm"

    override fun mode(state: JourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class PropertyManagerDetailsStep(
    stepConfig: PropertyManagerDetailsStepConfig,
) : RequestableStep<Complete, PropertyManagerDetailsFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "property-manager-details"
    }
}
