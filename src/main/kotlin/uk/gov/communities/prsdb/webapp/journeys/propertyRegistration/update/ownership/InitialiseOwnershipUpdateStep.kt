package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownership

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class InitialiseOwnershipUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, UpdateOwnershipJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: UpdateOwnershipJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: UpdateOwnershipJourneyState): String = ""

    override fun mode(state: UpdateOwnershipJourneyState): Complete = Complete.COMPLETE

    // Addresses aren't stored (PDJB-2226), so seeded freeholders and superior landlords have none.
    override fun afterStepIsReached(state: UpdateOwnershipJourneyState) {
        if (state.ownershipPartiesInitialised == true) return
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyId)

        val freeholders = propertyOwnership.freeholders.sortedBy { it.id }.map { FreeholderDataModel.fromFreeholder(it) }
        val freeholdersTask = state.ownershipStructureTask.freeholdersTask
        freeholdersTask.freeholdersMap = freeholders.mapIndexed { index, freeholder -> (index + 1) to freeholder }.toMap()
        freeholdersTask.nextFreeholderId = freeholders.size + 1

        val superiorLandlords =
            propertyOwnership.superiorLandlords.sortedBy { it.id }.map { SuperiorLandlordDataModel.fromSuperiorLandlord(it) }
        val superiorLandlordsTask = state.ownershipStructureTask.superiorLandlordsTask
        superiorLandlordsTask.superiorLandlordsMap =
            superiorLandlords.mapIndexed { index, superiorLandlord -> (index + 1) to superiorLandlord }.toMap()
        superiorLandlordsTask.nextSuperiorLandlordId = superiorLandlords.size + 1

        state.ownershipPartiesInitialised = true
    }
}

@JourneyFrameworkComponent
class InitialiseOwnershipUpdateStep(
    stepConfig: InitialiseOwnershipUpdateStepConfig,
) : RequestableStep<Complete, NoInputFormModel, UpdateOwnershipJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "start"
    }
}
