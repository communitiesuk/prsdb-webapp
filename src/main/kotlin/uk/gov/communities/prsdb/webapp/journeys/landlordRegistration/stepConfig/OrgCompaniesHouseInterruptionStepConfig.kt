package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyDetailsMode

// TODO PDJB-1447: replace this placeholder with the real Companies House change interruption page(s).
@JourneyFrameworkComponent
class OrgCompaniesHouseInterruptionStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, OrgCompaniesHouseUpdateState>() {
    override val formModelClass = NoInputFormModel::class

    // Data key of the governing body details step this interruption stands in for when changing to a non-company.
    // Journeys that embed such a step (the registration CYA flow) supply it during wiring; it's null in the standalone
    // update journey, which has no governing body details step and so needs nothing recorded.
    private var govBodyDetailsStepDataKey: String? = null

    fun recordingGovBodyDetailsCompleteAt(stepDataKey: String): OrgCompaniesHouseInterruptionStepConfig {
        this.govBodyDetailsStepDataKey = stepDataKey
        return this
    }

    override fun getStepSpecificContent(state: OrgCompaniesHouseUpdateState) =
        mapOf(
            "title" to "registerAsALandlord.title",
            "todoComment" to "TODO PDJB-1447: Companies House change interruption page",
        )

    override fun chooseTemplate(state: OrgCompaniesHouseUpdateState) = "forms/todo"

    override fun afterStepDataIsAdded(state: OrgCompaniesHouseUpdateState) {
        // When changing to a non-company, this interruption replaces the outer governing body task's first step, so
        // recording HAS_DETAILS against that step keeps the task complete in the base journey and returns the user to
        // the check answers page. The step is supplied as a dependency, so nothing is recorded in the standalone
        // update journey, which has no governing body details step.
        if (state.orgIsRegisteredCompanyStep.outcome == YesOrNo.NO) {
            govBodyDetailsStepDataKey?.let { stepDataKey ->
                state.addStepData(
                    stepDataKey,
                    mapOf(OrgGovBodyDetailsFormModel::orgGovBodyDetailsMode.name to OrgGovBodyDetailsMode.HAS_DETAILS.name),
                )
            }
        }
    }

    override fun mode(state: OrgCompaniesHouseUpdateState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgCompaniesHouseInterruptionStep(
    interruptionStepConfig: OrgCompaniesHouseInterruptionStepConfig,
) : RequestableStep<Complete, NoInputFormModel, OrgCompaniesHouseUpdateState>(interruptionStepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-companies-house-interruption"
    }
}
