package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-1447: replace this placeholder with the real Companies House change interruption page(s).
//  This step deliberately uses the generic JourneyState so it can be reused across journeys whose states differ
//  (registration check-your-answers vs the standalone update journey). The journey-specific input needed to decide
//  whether the interruption should be shown (the landlord's original Companies House answer) is supplied via
//  stepSpecificInitialisation from each journey's own state rather than by binding this step to a state type.
@JourneyFrameworkComponent
class OrgCompaniesHouseInterruptionStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    var originalIsRegisteredCompany: YesOrNo? = null

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "registerAsALandlord.title",
            "todoComment" to "TODO PDJB-1447: Companies House change interruption page",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgCompaniesHouseInterruptionStep(
    private val interruptionStepConfig: OrgCompaniesHouseInterruptionStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(interruptionStepConfig) {
    val originalIsRegisteredCompany: YesOrNo?
        get() = interruptionStepConfig.originalIsRegisteredCompany

    companion object {
        const val ROUTE_SEGMENT = "organisation-companies-house-interruption"
    }
}
