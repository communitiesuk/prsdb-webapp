package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.superiorLandlords

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyoneInOwnershipChainFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class StartSuperiorLandlordsUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, UpdateSuperiorLandlordsJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: UpdateSuperiorLandlordsJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: UpdateSuperiorLandlordsJourneyState): String = ""

    // Only complete once the existing superior landlords have been loaded, so later steps cannot be reached without them
    override fun mode(state: UpdateSuperiorLandlordsJourneyState): Complete? =
        if (state.superiorLandlordsInitialised == true) Complete.COMPLETE else null

    // Addresses aren't stored (PDJB-2226), so seeded superior landlords have none.
    override fun afterStepIsReached(state: UpdateSuperiorLandlordsJourneyState) {
        if (state.superiorLandlordsInitialised == true) return

        val superiorLandlords =
            propertyOwnershipService
                .getPropertyOwnership(state.propertyId)
                .superiorLandlords
                .sortedBy { it.id }
                .map { SuperiorLandlordDataModel.fromSuperiorLandlord(it) }

        if (superiorLandlords.isNotEmpty()) {
            val superiorLandlordsTask = state.superiorLandlordsTask
            superiorLandlordsTask.superiorLandlordsMap =
                superiorLandlords.mapIndexed { index, superiorLandlord -> (index + 1) to superiorLandlord }.toMap()
            superiorLandlordsTask.nextSuperiorLandlordId = superiorLandlords.size + 1
            state.addStepData(
                superiorLandlordsTask.anyoneInOwnershipChainStep.stepConfig.stepDataKey,
                AnyoneInOwnershipChainFormModel(anyoneInOwnershipChain = true).toPageData(),
            )
        }
        state.superiorLandlordsInitialised = true
    }
}

@JourneyFrameworkComponent
class StartSuperiorLandlordsUpdateStep(
    stepConfig: StartSuperiorLandlordsUpdateStepConfig,
) : RequestableStep<Complete, NoInputFormModel, UpdateSuperiorLandlordsJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "start"
    }
}
