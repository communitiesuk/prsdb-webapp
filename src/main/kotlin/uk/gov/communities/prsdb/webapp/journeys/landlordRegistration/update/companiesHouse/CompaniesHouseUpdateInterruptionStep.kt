package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-1447: replace this placeholder with the real interruption page(s) (switch detection + two variants).
@JourneyFrameworkComponent
class CompaniesHouseUpdateInterruptionStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    var originalIsRegisteredCompany: YesOrNo? = null

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "landlordDetails.update.title",
            "todoComment" to "TODO PDJB-1447: Companies House update interruption page",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class CompaniesHouseUpdateInterruptionStep(
    private val interruptionStepConfig: CompaniesHouseUpdateInterruptionStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(interruptionStepConfig) {
    // Read-through to the injected original answer (the base class exposes stepConfig only as its generic type).
    val originalIsRegisteredCompany: YesOrNo?
        get() = interruptionStepConfig.originalIsRegisteredCompany

    companion object {
        const val ROUTE_SEGMENT = "interruption"
    }
}
