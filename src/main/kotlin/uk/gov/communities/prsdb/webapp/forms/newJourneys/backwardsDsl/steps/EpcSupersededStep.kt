package uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.EpcJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.theJourneyFramework.AbstractStep

@Scope("prototype")
@PrsdbWebComponent
class EpcSupersededStep : AbstractStep<Complete, NoInputFormModel, EpcJourneyState>() {
    override val formModelClazz = NoInputFormModel::class

    override fun getStepContent(state: EpcJourneyState) =
        mapOf(
            "title" to "propertyCompliance.title",
            "certificateNumber" to state.searchForEpc?.formModel?.certificateNumber,
        )

    override fun chooseTemplate(): String = "forms/epcSupersededForm"

    override fun mode(state: EpcJourneyState): Complete? = getFormModelFromState(state)?.let { Complete.COMPLETE }
}
