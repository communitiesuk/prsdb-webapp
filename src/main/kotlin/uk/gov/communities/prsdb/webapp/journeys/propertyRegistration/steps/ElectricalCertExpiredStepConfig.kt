package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.ELECTRICAL_SAFETY_STANDARDS_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class ElectricalCertExpiredStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, ElectricalSafetyDetailState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: ElectricalSafetyDetailState) =
        mapOf(
            "expiryDate" to state.getElectricalCertificateExpiryDateIfReachable()?.toJavaLocalDate(),
            "changeExpiryDateUrl" to Destination.VisitableStep(state.electricalCertExpiryDateStep, state.journeyId).toUrlStringOrNull(),
            "landlordElectricalSafetyUrl" to ELECTRICAL_SAFETY_STANDARDS_URL,
            "submitButtonText" to
                if (state.isOccupied) "forms.buttons.continueWithoutElectricalSafety" else "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: ElectricalSafetyDetailState) =
        if (state.isOccupied) {
            "forms/electricalSafetyCertificateExpiredForOccupiedProperty"
        } else {
            "forms/electricalSafetyCertificateExpiredForUnoccupiedProperty"
        }

    override fun mode(state: ElectricalSafetyDetailState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ElectricalCertExpiredStep(
    stepConfig: ElectricalCertExpiredStepConfig,
) : RequestableStep<Complete, NoInputFormModel, ElectricalSafetyDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "electrical-safety-certificate-expired"
    }
}
