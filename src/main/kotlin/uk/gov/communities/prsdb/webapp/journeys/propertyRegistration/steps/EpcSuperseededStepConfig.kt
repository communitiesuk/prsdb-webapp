package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.EpcLookupService

// TODO PDJB-664: Update and use this StepConfig for the epc superseded step.
// Update names / route segments if clearer
@JourneyFrameworkComponent
class EpcSuperseededStepConfig(
    private val epcLookupService: EpcLookupService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, EpcState>() {
    override val formModelClass = NoInputFormModel::class

    lateinit var latestEpcForProperty: EpcDataModel

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "todoComment" to "TODO PDJB-664: Implement EPC Superseded page",
            "supersededEpcDetails" to state.epcRetrievedByCertificateNumber,
            "latestEpcDetails" to state.updatedEpcRetrievedByCertificateNumber,
        )

    override fun chooseTemplate(state: EpcState) = "forms/todo"

    override fun mode(state: EpcState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: EpcState) {
        state.acceptedEpc = state.updatedEpcRetrievedByCertificateNumber
    }
}

@JourneyFrameworkComponent
final class EpcSuperseededStep(
    stepConfig: EpcSuperseededStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-superseded"
    }
}
