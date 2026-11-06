package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.LAND_REGISTRY_SEARCH_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class FreeholdersInfoStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, FreeholdersState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: FreeholdersState): Map<String, Any?> =
        mapOf("landRegistrySearchUrl" to LAND_REGISTRY_SEARCH_URL)

    override fun chooseTemplate(state: FreeholdersState) =
        if (state.isJointFreeholder) "forms/jointFreeholdersInfoForm" else "forms/freeholdersInfoForm"

    override fun mode(state: FreeholdersState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class FreeholdersInfoStep(
    stepConfig: FreeholdersInfoStepConfig,
) : RequestableStep<Complete, NoInputFormModel, FreeholdersState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "freeholders"
    }
}
