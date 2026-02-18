package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class EicrStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractRequestableStepConfig<EicrMode, EicrFormModel, EicrState>() {
    override val formModelClass = EicrFormModel::class

    override fun getStepSpecificContent(state: EicrState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "forms.eicr.fieldSetHeading",
            "fieldSetHint" to "forms.eicr.fieldSetHint",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
            "address" to propertyOwnershipService.getPropertyOwnership(state.propertyId).address.singleLineAddress,
        )

    override fun chooseTemplate(state: EicrState): String = "forms/certificateForm"

    override fun mode(state: EicrState) =
        state.eicrStep.formModelOrNull?.let {
            when (it.hasCert) {
                true -> EicrMode.HAS_CERTIFICATE
                false -> EicrMode.NO_CERTIFICATE
                null -> null
            }
        }
}

@JourneyFrameworkComponent
final class EicrStep(
    stepConfig: EicrStepConfig,
) : RequestableStep<EicrMode, EicrFormModel, EicrState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "eicr"
    }
}

enum class EicrMode {
    HAS_CERTIFICATE,
    NO_CERTIFICATE,
}
