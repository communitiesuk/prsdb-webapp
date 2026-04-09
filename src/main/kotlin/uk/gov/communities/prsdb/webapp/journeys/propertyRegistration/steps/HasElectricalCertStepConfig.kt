package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasElectricalCertFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel

@JourneyFrameworkComponent
class HasElectricalCertStepConfig : AbstractRequestableStepConfig<HasElectricalCertMode, HasElectricalCertFormModel, JourneyState>() {
    override val formModelClass = HasElectricalCertFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "propertyCompliance.electricalSafetyTask.electricalCert.heading",
            "fieldSetHint" to "propertyCompliance.electricalSafetyTask.electricalCert.hint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "secondarySubmitButtonText" to "forms.buttons.provideThisLater",
            "submitButtonAction" to CONTINUE_BUTTON_ACTION_NAME,
            "secondarySubmitButtonAction" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME,
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = HasElectricalSafetyCertificate.HAS_EIC,
                        labelMsgKey = "propertyCompliance.electricalSafetyTask.electricalCert.radios.eicLabel",
                        hintMsgKey = "propertyCompliance.electricalSafetyTask.electricalCert.radios.eicHint",
                    ),
                    RadiosButtonViewModel(
                        value = HasElectricalSafetyCertificate.HAS_EICR,
                        labelMsgKey = "propertyCompliance.electricalSafetyTask.electricalCert.radios.eicrLabel",
                        hintMsgKey = "propertyCompliance.electricalSafetyTask.electricalCert.radios.eicrHint",
                    ),
                    RadiosDividerViewModel(
                        labelMsgKey = "propertyCompliance.electricalSafetyTask.electricalCert.radios.divider",
                    ),
                    RadiosButtonViewModel(
                        value = HasElectricalSafetyCertificate.NO_CERTIFICATE,
                        labelMsgKey = "propertyCompliance.electricalSafetyTask.electricalCert.radios.noneLabel",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/hasElectricalCertForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            if (it.action == PROVIDE_THIS_LATER_BUTTON_ACTION_NAME) {
                HasElectricalCertMode.PROVIDE_THIS_LATER
            } else {
                when (it.electricalCertType) {
                    HasElectricalSafetyCertificate.HAS_EIC -> HasElectricalCertMode.HAS_EIC
                    HasElectricalSafetyCertificate.HAS_EICR -> HasElectricalCertMode.HAS_EICR
                    HasElectricalSafetyCertificate.NO_CERTIFICATE -> HasElectricalCertMode.NO_CERTIFICATE
                    else -> null
                }
            }
        }
}

@JourneyFrameworkComponent
final class HasElectricalCertStep(
    stepConfig: HasElectricalCertStepConfig,
) : RequestableStep<HasElectricalCertMode, HasElectricalCertFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "has-electrical-safety"
    }
}

enum class HasElectricalCertMode {
    HAS_EIC,
    HAS_EICR,
    NO_CERTIFICATE,
    PROVIDE_THIS_LATER,
}
