package uk.gov.communities.prsdb.webapp.journeys.systemOperator.landlordNotes

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SystemOperatorNotesFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class SystemOperatorLandlordNotesStepConfig(
    private val landlordService: LandlordService,
) : AbstractRequestableStepConfig<Complete, SystemOperatorNotesFormModel, UpdateSystemOperatorLandlordNotesJourneyState>() {
    override val formModelClass = SystemOperatorNotesFormModel::class

    override fun getStepSpecificContent(state: UpdateSystemOperatorLandlordNotesJourneyState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "systemOperator.landlordNotes.fieldSetHeading",
            "fieldSetHint" to "systemOperator.landlordNotes.fieldSetHint",
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
        )

    override fun chooseTemplate(state: UpdateSystemOperatorLandlordNotesJourneyState): String = "forms/systemOperatorNotesForm"

    override fun mode(state: UpdateSystemOperatorLandlordNotesJourneyState): Complete? =
        getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepIsReached(state: UpdateSystemOperatorLandlordNotesJourneyState) {
        if (state.notesInitialised == true) return

        landlordService.retrieveLandlordById(state.landlordId)?.systemOperatorNotes?.let { existingNotes ->
            state.addStepData(stepDataKey, SystemOperatorNotesFormModel().apply { notes = existingNotes }.toPageData())
        }
        state.notesInitialised = true
    }
}

@JourneyFrameworkComponent
class SystemOperatorLandlordNotesStep(
    stepConfig: SystemOperatorLandlordNotesStepConfig,
) : RequestableStep<Complete, SystemOperatorNotesFormModel, UpdateSystemOperatorLandlordNotesJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "notes"
    }
}
