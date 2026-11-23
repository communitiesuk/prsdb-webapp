package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.freeholders

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class InitialiseFreeholdersUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, UpdateFreeholdersJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: UpdateFreeholdersJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: UpdateFreeholdersJourneyState): String = ""

    override fun mode(state: UpdateFreeholdersJourneyState): Complete = Complete.COMPLETE

    // Addresses aren't stored (PDJB-2226), so seeded freeholders have none.
    override fun afterStepIsReached(state: UpdateFreeholdersJourneyState) {
        if (state.freeholdersInitialised == true) return
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyId)

        val freeholders = propertyOwnership.freeholders.sortedBy { it.id }.map { FreeholderDataModel.fromFreeholder(it) }
        state.freeholdersMap = freeholders.mapIndexed { index, freeholder -> (index + 1) to freeholder }.toMap()
        state.nextFreeholderId = freeholders.size + 1

        state.freeholdersInitialised = true
    }
}

@JourneyFrameworkComponent
class InitialiseFreeholdersUpdateStep(
    stepConfig: InitialiseFreeholdersUpdateStepConfig,
) : RequestableStep<Complete, NoInputFormModel, UpdateFreeholdersJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "start"
    }
}
