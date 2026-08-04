package uk.gov.communities.prsdb.webapp.journeys.organisationLandlordDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordDeregistration.OrganisationLandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("organisationLandlordDeregistrationAreYouSureStepConfig")
class AreYouSureStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, OrganisationLandlordDeregistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: OrganisationLandlordDeregistrationJourneyState) =
        mapOf("todoComment" to "TODO: PDJB-1482 - Org landlord deregistration are you sure page")

    override fun chooseTemplate(state: OrganisationLandlordDeregistrationJourneyState) = "forms/todo"

    override fun mode(state: OrganisationLandlordDeregistrationJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("organisationLandlordDeregistrationAreYouSureStep")
final class AreYouSureStep(
    stepConfig: AreYouSureStepConfig,
) : RequestableStep<Complete, NoInputFormModel, OrganisationLandlordDeregistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "are-you-sure"
    }
}
