package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-654: Implement Remove Electrical Cert Upload page
@JourneyFrameworkComponent
class RemoveElectricalCertUploadStepConfig : AbstractRequestableStepConfig<AnyMembers, NoInputFormModel, ElectricalSafetyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: ElectricalSafetyState) =
        mapOf("todoComment" to "TODO PDJB-654: Implement Remove Electrical Cert Upload page")

    override fun chooseTemplate(state: ElectricalSafetyState) = "forms/todo"

    override fun mode(state: ElectricalSafetyState) =
        if (state.electricalUploadMap.isNotEmpty()) {
            AnyMembers.SOME_MEMBERS
        } else {
            AnyMembers.NO_MEMBERS
        }
}

@JourneyFrameworkComponent
final class RemoveElectricalCertUploadStep(
    stepConfig: RemoveElectricalCertUploadStepConfig,
) : RequestableStep<AnyMembers, NoInputFormModel, ElectricalSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "remove-electrical-safety-certificate-upload"
    }
}
