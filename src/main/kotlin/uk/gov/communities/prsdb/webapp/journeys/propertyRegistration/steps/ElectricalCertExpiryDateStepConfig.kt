package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyDateFormModel

@JourneyFrameworkComponent
class ElectricalCertExpiryDateStepConfig :
    AbstractRequestableStepConfig<ElectricalCertExpiryDateMode, AnyDateFormModel, ElectricalSafetyState>() {
    override val formModelClass = AnyDateFormModel::class

    override fun getStepSpecificContent(state: ElectricalSafetyState): Map<String, Any?> {
        val certType = state.hasElectricalCertStep.formModelIfReachableOrNull?.electricalCertType
        val headingKey =
            if (certType == HasElectricalSafetyCertificate.HAS_EIC) {
                "propertyCompliance.electricalSafetyTask.electricalCertExpiryDate.eic.fieldSetHeading"
            } else {
                "propertyCompliance.electricalSafetyTask.electricalCertExpiryDate.eicr.fieldSetHeading"
            }
        return mapOf(
            "fieldSetHeading" to headingKey,
            "fieldSetHint" to "propertyCompliance.electricalSafetyTask.electricalCertExpiryDate.fieldSetHint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )
    }

    override fun chooseTemplate(state: ElectricalSafetyState): String = "forms/dateForm"

    override fun mode(state: ElectricalSafetyState) =
        state.getElectricalCertificateIsOutdated()?.let {
            when (it) {
                true -> ElectricalCertExpiryDateMode.ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED
                false -> ElectricalCertExpiryDateMode.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE
            }
        }
}

@JourneyFrameworkComponent
final class ElectricalCertExpiryDateStep(
    stepConfig: ElectricalCertExpiryDateStepConfig,
) : RequestableStep<ElectricalCertExpiryDateMode, AnyDateFormModel, ElectricalSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "electrical-safety-certificate-expiry-date"
    }
}

enum class ElectricalCertExpiryDateMode {
    ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED,
    ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE,
}
