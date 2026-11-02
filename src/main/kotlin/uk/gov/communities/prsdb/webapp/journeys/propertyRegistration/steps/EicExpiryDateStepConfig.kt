package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.shared.ElectricalCertExpiryStatus
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyDateFormModel

@JourneyFrameworkComponent
class EicExpiryDateStepConfig :
    AbstractRequestableStepConfig<ElectricalCertExpiryStatus, AnyDateFormModel, ElectricalSafetyDetailState>() {
    override val formModelClass = AnyDateFormModel::class

    override fun getStepSpecificContent(state: ElectricalSafetyDetailState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "propertyCompliance.electricalSafetyTask.electricalCertExpiryDate.eic.fieldSetHeading",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: ElectricalSafetyDetailState): String = "forms/dateForm"

    override fun mode(state: ElectricalSafetyDetailState) =
        state.getEicIsOutdated()?.let {
            when (it) {
                true -> ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED
                false -> ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE
            }
        }
}

@JourneyFrameworkComponent
final class EicExpiryDateStep(
    stepConfig: EicExpiryDateStepConfig,
) : RequestableStep<ElectricalCertExpiryStatus, AnyDateFormModel, ElectricalSafetyDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "eic-expiry-date"
    }
}
