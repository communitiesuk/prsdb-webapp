package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.ELECTRICAL_SAFETY_STANDARDS_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class ElectricalCertMissingStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, ElectricalSafetyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: ElectricalSafetyState) =
        mapOf(
            "electricalSafetyStandardsUrl" to ELECTRICAL_SAFETY_STANDARDS_URL,
            "submitButtonText" to
                if (state.isOccupied == true) "forms.buttons.continueWithoutElectricalSafety" else "forms.buttons.continue",
        )

    override fun chooseTemplate(state: ElectricalSafetyState) =
        state.isOccupied?.let { isOccupied ->
            if (isOccupied) {
                "forms/electricalSafetyCertificateMissingForOccupiedProperty"
            } else {
                "forms/electricalSafetyCertificateMissingForUnoccupiedProperty"
            }
        } ?: throw IllegalStateException("ElectricalCertMissingStep should not be reachable before isOccupied is set")

    override fun mode(state: ElectricalSafetyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ElectricalCertMissingStep(
    stepConfig: ElectricalCertMissingStepConfig,
) : RequestableStep<Complete, NoInputFormModel, ElectricalSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "electrical-safety-certificate-missing"
    }
}
