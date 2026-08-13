package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationCharity

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class UpdateOrganisationCharityCyaStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, UpdateOrganisationCharityJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: UpdateOrganisationCharityJourneyState) =
        mapOf("todoComment" to "TODO: PDJB-1463 - Organisation charity check your answers page")

    override fun chooseTemplate(state: UpdateOrganisationCharityJourneyState) = "forms/todo"

    override fun mode(state: UpdateOrganisationCharityJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class UpdateOrganisationCharityCyaStep(
    stepConfig: UpdateOrganisationCharityCyaStepConfig,
) : RequestableStep<Complete, NoInputFormModel, UpdateOrganisationCharityJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-charity-check-your-answers"
    }
}
