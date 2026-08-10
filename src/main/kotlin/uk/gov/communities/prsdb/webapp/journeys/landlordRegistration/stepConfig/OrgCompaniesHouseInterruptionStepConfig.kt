package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

enum class OrgCompaniesHouseInterruptionOutcome {
    TO_COMPANY,
    TO_NON_COMPANY,
}

// TODO PDJB-1447: replace this placeholder with the real Companies House change interruption page(s).
@JourneyFrameworkComponent
class OrgCompaniesHouseInterruptionStepConfig :
    AbstractRequestableStepConfig<OrgCompaniesHouseInterruptionOutcome, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    private lateinit var changingToCompany: () -> Boolean

    fun usingChangingToCompany(changingToCompany: () -> Boolean): OrgCompaniesHouseInterruptionStepConfig {
        this.changingToCompany = changingToCompany
        return this
    }

    override fun isSubClassInitialised() = ::changingToCompany.isInitialized

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "registerAsALandlord.title",
            "todoComment" to "TODO PDJB-1447: Companies House change interruption page",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todo"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            if (changingToCompany()) {
                OrgCompaniesHouseInterruptionOutcome.TO_COMPANY
            } else {
                OrgCompaniesHouseInterruptionOutcome.TO_NON_COMPANY
            }
        }
}

@JourneyFrameworkComponent
final class OrgCompaniesHouseInterruptionStep(
    interruptionStepConfig: OrgCompaniesHouseInterruptionStepConfig,
) : RequestableStep<OrgCompaniesHouseInterruptionOutcome, NoInputFormModel, JourneyState>(interruptionStepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-companies-house-interruption"
    }
}
