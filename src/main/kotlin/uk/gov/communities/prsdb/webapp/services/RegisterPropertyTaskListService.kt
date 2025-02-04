package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.tasks.CheckAndSubmitPropertiesTaskList
import uk.gov.communities.prsdb.webapp.forms.tasks.RegisterPropertiesTaskList
import uk.gov.communities.prsdb.webapp.models.viewModels.RegisterPropertyTaskListViewModel

@Service
class RegisterPropertyTaskListService(
    private val journeyDataService: JourneyDataService,
    private val registerPropertiesTaskList: RegisterPropertiesTaskList,
    private val checkAndSubmitPropertiesTaskList: CheckAndSubmitPropertiesTaskList,
) {
    fun getTaskListPageViewModel(principalName: String): RegisterPropertyTaskListViewModel {
        loadJourneyDataIntoSessionIfNotLoaded(principalName)

        return RegisterPropertyTaskListViewModel(
            registerPropertiesTaskList.getTaskListViewModels(),
            checkAndSubmitPropertiesTaskList.getTaskListViewModels(),
        )
    }

    private fun loadJourneyDataIntoSessionIfNotLoaded(principalName: String) {
        val data = journeyDataService.getJourneyDataFromSession()
        if (data.isEmpty()) {
            val contextId = journeyDataService.getContextId(principalName, JourneyType.PROPERTY_REGISTRATION)
            if (contextId == null) {
                addTaskListStepDataToJourneyData(data)
            } else {
                journeyDataService.loadJourneyDataIntoSession(contextId)
            }
        }
    }

    private fun addTaskListStepDataToJourneyData(data: JourneyData) {
        data[RegisterPropertyStepId.TaskList.urlPathSegment] = mutableMapOf<String, Any>()
        journeyDataService.setJourneyData(data)
    }
}
