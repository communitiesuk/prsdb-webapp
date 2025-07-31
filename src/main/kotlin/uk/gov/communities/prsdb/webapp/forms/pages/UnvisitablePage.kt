package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

class UnvisitablePage(
    private val errorMessage: String,
) : AbstractPage(
        formModel = NoInputFormModel::class,
        templateName = "error/500",
        content = emptyMap(),
    ) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) = throw ResponseStatusException(
        HttpStatus.NOT_FOUND,
        errorMessage,
    )
}
