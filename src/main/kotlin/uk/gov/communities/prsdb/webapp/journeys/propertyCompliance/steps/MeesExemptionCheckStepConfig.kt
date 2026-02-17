package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.MEES_EXEMPTION_GUIDE_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.ExemptionMode
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionCheckFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class MeesExemptionCheckStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractRequestableStepConfig<ExemptionMode, MeesExemptionCheckFormModel, EpcState>() {
    override val formModelClass = MeesExemptionCheckFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "title" to "propertyCompliance.title",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
            "meesExemptionGuideUrl" to MEES_EXEMPTION_GUIDE_URL,
            "singleLineAddress" to propertyOwnershipService.getPropertyOwnership(state.propertyId).address.singleLineAddress,
        )

    override fun chooseTemplate(state: EpcState): String = "forms/meesExemptionCheckForm"

    override fun mode(state: EpcState): ExemptionMode? =
        getFormModelFromStateOrNull(state)?.let {
            when (it.propertyHasExemption) {
                true -> ExemptionMode.HAS_EXEMPTION
                false -> ExemptionMode.NO_EXEMPTION
                null -> null
            }
        }
}

@JourneyFrameworkComponent
final class MeesExemptionCheckStep(
    stepConfig: MeesExemptionCheckStepConfig,
) : RequestableStep<ExemptionMode, MeesExemptionCheckFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "mees-exemption-check"
    }
}
