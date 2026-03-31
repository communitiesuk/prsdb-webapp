package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.ELECTRICAL_SAFETY_STANDARDS_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class ElectricalCertExpiredStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, ElectricalSafetyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: ElectricalSafetyState) =
        mapOf(
            "expiryDate" to state.getElectricalCertificateExpiryDateIfReachable()?.toJavaLocalDate(),
            "changeExpiryDateUrl" to Destination.VisitableStep(state.electricalCertExpiryDateStep, state.journeyId).toUrlStringOrNull(),
            "landlordElectricalSafetyUrl" to ELECTRICAL_SAFETY_STANDARDS_URL,
            "submitButtonText" to
                if (state.isOccupied == true) "forms.buttons.continueWithoutElectricalSafety" else "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: ElectricalSafetyState) =
        state.isOccupied?.let { isOccupied ->
            if (isOccupied) {
                "forms/electricalSafetyCertificateExpiredForOccupiedProperty"
            } else {
                "forms/electricalSafetyCertificateExpiredForUnoccupiedProperty"
            }
        } ?: throw IllegalStateException("ElectricalCertExpiredStep should not be reachable before isOccupied is set")

    override fun mode(state: ElectricalSafetyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ElectricalCertExpiredStep(
    stepConfig: ElectricalCertExpiredStepConfig,
) : RequestableStep<Complete, NoInputFormModel, ElectricalSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "electrical-safety-certificate-expired"
    }
}
