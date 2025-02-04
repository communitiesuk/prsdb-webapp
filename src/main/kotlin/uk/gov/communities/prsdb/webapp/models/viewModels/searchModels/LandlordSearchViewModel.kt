package uk.gov.communities.prsdb.webapp.models.viewModels.searchModels

import jakarta.servlet.http.HttpServletRequest
import uk.gov.communities.prsdb.webapp.models.requestModels.searchModels.LandlordSearchRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.CheckboxViewModel

class LandlordSearchViewModel(
    searchRequestModel: LandlordSearchRequestModel,
    httpServletRequest: HttpServletRequest,
) : SearchViewModel(
        filters =
            listOf(
                FilterViewModel(
                    headingMsgKey = "landlordSearch.filter.la.heading",
                    category = "restrictToLA",
                    options = listOf(CheckboxViewModel(value = true, labelMsgKey = "landlordSearch.filter.la.label")),
                ),
            ),
        searchRequestModel = searchRequestModel,
        httpServletRequest = httpServletRequest,
    )
