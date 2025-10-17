package uk.gov.communities.prsdb.webapp.journeys.example.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractUninitialisableInnerStep
import uk.gov.communities.prsdb.webapp.journeys.example.EpcJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@Scope("prototype")
@PrsdbWebComponent
class EpcSupersededStep : AbstractUninitialisableInnerStep<Complete, NoInputFormModel, EpcJourneyState>() {
    override val formModelClazz = NoInputFormModel::class

    override fun getStepSpecificContent(state: EpcJourneyState) =
        mapOf(
            "title" to "propertyCompliance.title",
            "certificateNumber" to state.searchForEpc?.formModel?.certificateNumber,
        )

    override fun chooseTemplate(): String = "forms/epcSupersededForm"

    override fun mode(state: EpcJourneyState): Complete? = getFormModelFromState(state)?.let { Complete.COMPLETE }
}
