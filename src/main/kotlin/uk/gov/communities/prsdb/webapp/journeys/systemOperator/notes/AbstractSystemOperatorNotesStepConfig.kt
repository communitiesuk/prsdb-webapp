package uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes

import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SystemOperatorNotesFormModel

abstract class AbstractSystemOperatorNotesStepConfig :
    AbstractRequestableStepConfig<Complete, SystemOperatorNotesFormModel, SystemOperatorNotesJourneyState>() {
    override val formModelClass = SystemOperatorNotesFormModel::class

    protected abstract val fieldSetHeadingKey: String

    protected abstract fun getExistingNotes(recordId: Long): String?

    override fun getStepSpecificContent(state: SystemOperatorNotesJourneyState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to fieldSetHeadingKey,
            "fieldSetHint" to "systemOperator.notes.fieldSetHint",
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
        )

    override fun chooseTemplate(state: SystemOperatorNotesJourneyState): String = "forms/systemOperatorNotesForm"

    override fun mode(state: SystemOperatorNotesJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepIsReached(state: SystemOperatorNotesJourneyState) {
        if (state.notesInitialised == true) return
        getExistingNotes(state.recordId)?.let { existingNotes ->
            state.addStepData(stepDataKey, SystemOperatorNotesFormModel().apply { notes = existingNotes }.toPageData())
        }
        state.notesInitialised = true
    }
}

abstract class AbstractSystemOperatorNotesStep(
    stepConfig: AbstractSystemOperatorNotesStepConfig,
) : RequestableStep<Complete, SystemOperatorNotesFormModel, SystemOperatorNotesJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "notes"
    }
}
