package uk.gov.communities.prsdb.webapp.journeys.example.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.EpcJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class EpcSupersededStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, EpcJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EpcJourneyState) =
        mapOf(
            "title" to "propertyCompliance.title",
            "certificateNumber" to state.searchForEpc.formModelOrNull?.certificateNumber,
        )

    override fun chooseTemplate(state: EpcJourneyState): String = "forms/epcSupersededForm"

    override fun mode(state: EpcJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class EpcSupersededStep(
    stepConfig: EpcSupersededStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EpcJourneyState>(stepConfig)
