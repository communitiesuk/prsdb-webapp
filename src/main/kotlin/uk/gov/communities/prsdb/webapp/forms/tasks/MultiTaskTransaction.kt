package uk.gov.communities.prsdb.webapp.forms.tasks

import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.models.viewModels.TaskSectionViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class MultiTaskTransaction<T : StepId>(
    private val journeyDataService: JourneyDataService,
) {
    protected abstract val taskLists: List<TransactionSection<T>>
    protected abstract val journeyType: JourneyType
    protected abstract val taskListUrlSegment: String

    fun getTaskListSections(principalName: String): List<TaskSectionViewModel> {
        loadJourneyDataIntoSessionIfNotLoaded(principalName)

        return taskLists.map {
            TaskSectionViewModel(
                it.headingKey,
                it.sectionTasks.getTaskListViewModels(),
            )
        }
    }

    private fun loadJourneyDataIntoSessionIfNotLoaded(principalName: String) {
        val data = journeyDataService.getJourneyDataFromSession()
        if (data.isEmpty()) {
            /* TODO PRSD-589 Currently this looks the context up from the database,
                takes the id, then passes the id to another method which retrieves it
                from the database. When this is reworked, we should just pass the whole
                context to an overload of journeyDataService.loadJourneyDataIntoSession().*/
            val contextId = journeyDataService.getContextId(principalName, journeyType)
            if (contextId == null) {
                addTaskListStepDataToJourneyData(data)
            } else {
                journeyDataService.loadJourneyDataIntoSession(contextId)
            }
        }
    }

    private fun addTaskListStepDataToJourneyData(data: JourneyData) {
        data[taskListUrlSegment] = mutableMapOf<String, Any>()
        journeyDataService.setJourneyData(data)
    }

    data class TransactionSection<T : StepId>(
        val headingKey: String,
        val sectionTasks: TaskList<T>,
    )
}
