package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.ELECTRICAL_SAFETY_STANDARDS_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class ProvideElectricalCertLaterStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, ElectricalSafetyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: ElectricalSafetyState) =
        mapOf(
            "electricalSafetyStandardsUrl" to ELECTRICAL_SAFETY_STANDARDS_URL,
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: ElectricalSafetyState) =
        state.isOccupied?.let { isOccupied ->
            if (isOccupied) {
                "forms/provideElectricalSafetyCertificateLaterForOccupiedProperty"
            } else {
                "forms/provideElectricalSafetyCertificateLaterForUnoccupiedProperty"
            }
        } ?: throw IllegalStateException(
            "ProvideElectricalCertLaterStep should not be reachable before isOccupied is set",
        )

    override fun mode(state: ElectricalSafetyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ProvideElectricalCertLaterStep(
    stepConfig: ProvideElectricalCertLaterStepConfig,
) : RequestableStep<Complete, NoInputFormModel, ElectricalSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "provide-electrical-safety-certificate-later"
    }
}
