package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class GasSafetyStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractRequestableStepConfig<GasSafetyMode, GasSafetyFormModel, GasSafetyState>() {
    override val formModelClass = GasSafetyFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "forms.gasSafety.fieldSetHeading",
            "fieldSetHint" to "forms.gasSafety.fieldSetHint",
            "address" to propertyOwnershipService.getPropertyOwnership(state.propertyId).address.singleLineAddress,
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
        )

    override fun chooseTemplate(state: GasSafetyState): String = "forms/certificateForm"

    override fun mode(state: GasSafetyState) =
        state.gasSafetyStep.formModelOrNull?.let {
            when (it.hasCert) {
                true -> GasSafetyMode.HAS_CERTIFICATE
                false -> GasSafetyMode.NO_CERTIFICATE
                null -> null
            }
        }
}

@JourneyFrameworkComponent
final class GasSafetyStep(
    stepConfig: GasSafetyStepConfig,
) : RequestableStep<GasSafetyMode, GasSafetyFormModel, GasSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety"
    }
}

enum class GasSafetyMode {
    HAS_CERTIFICATE,
    NO_CERTIFICATE,
}
