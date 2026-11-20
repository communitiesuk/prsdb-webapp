package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.propertyManager

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class StartPropertyManagerUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, UpdatePropertyManagerJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: UpdatePropertyManagerJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: UpdatePropertyManagerJourneyState): String = ""

    // Only complete once the existing property manager has been loaded, so later steps cannot be reached without it
    override fun mode(state: UpdatePropertyManagerJourneyState): Complete? =
        if (state.propertyManagerInitialised == true) Complete.COMPLETE else null

    override fun afterStepIsReached(state: UpdatePropertyManagerJourneyState) {
        if (state.propertyManagerInitialised == true) return

        propertyOwnershipService.getPropertyOwnership(state.propertyId).propertyManager?.let { existingPropertyManager ->
            state.originalPropertyManagerName = existingPropertyManager.name
            state.originalPropertyManagerEmail = existingPropertyManager.email

            val propertyManagerTask = state.propertyManagerTask
            state.addStepData(
                propertyManagerTask.propertyManagerStep.stepConfig.stepDataKey,
                PropertyManagerFormModel().apply { usingPropertyManager = true }.toPageData(),
            )
            state.addStepData(
                propertyManagerTask.propertyManagerDetailsStep.stepConfig.stepDataKey,
                PropertyManagerDetailsFormModel()
                    .apply {
                        name = existingPropertyManager.name
                        emailAddress = existingPropertyManager.email
                    }.toPageData(),
            )
        }
        state.propertyManagerInitialised = true
    }
}

@JourneyFrameworkComponent
class StartPropertyManagerUpdateStep(
    stepConfig: StartPropertyManagerUpdateStepConfig,
) : RequestableStep<Complete, NoInputFormModel, UpdatePropertyManagerJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "start"
    }
}
