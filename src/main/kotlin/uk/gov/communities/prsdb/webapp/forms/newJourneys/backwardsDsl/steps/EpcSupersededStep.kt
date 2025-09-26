package uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.forms.newJourneys.Complete
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.EpcJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@Scope("prototype")
@PrsdbWebComponent
class EpcSupersededStep : BackwardsDslInitialisableStep<Complete, NoInputFormModel, EpcJourneyState>() {
    override val formModelClazz = NoInputFormModel::class

    override fun getStepContent(state: EpcJourneyState) =
        mapOf(
            "title" to "propertyCompliance.title",
            "certificateNumber" to state.searchedForEpcNumber,
        )

    override fun chooseTemplate(state: EpcJourneyState): String = "forms/epcSupersededForm"

    override fun mode(state: EpcJourneyState): Complete? = getFormModelFromState(state)?.let { Complete.COMPLETE }
}
