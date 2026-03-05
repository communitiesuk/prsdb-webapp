package uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationReasonFormModel

@JourneyFrameworkComponent("landlordDeregistrationReasonStepConfig")
class ReasonStepConfig :
    AbstractRequestableStepConfig<Complete, LandlordDeregistrationReasonFormModel, LandlordDeregistrationJourneyState>() {
    override val formModelClass = LandlordDeregistrationReasonFormModel::class

    override fun getStepSpecificContent(state: LandlordDeregistrationJourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.reason.landlordDeregistration.fieldSetHeading",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: LandlordDeregistrationJourneyState) = "forms/deregistrationReasonForm"

    override fun mode(state: LandlordDeregistrationJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("landlordDeregistrationReasonStep")
final class ReasonStep(
    stepConfig: ReasonStepConfig,
) : RequestableStep<Complete, LandlordDeregistrationReasonFormModel, LandlordDeregistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "reason"
    }
}
