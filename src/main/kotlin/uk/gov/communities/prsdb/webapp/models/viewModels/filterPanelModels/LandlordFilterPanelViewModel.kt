package uk.gov.communities.prsdb.webapp.models.viewModels.filterPanelModels

import jakarta.servlet.http.HttpServletRequest
import uk.gov.communities.prsdb.webapp.models.requestModels.searchModels.LandlordSearchRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxViewModel

class LandlordFilterPanelViewModel(
    searchRequestModel: LandlordSearchRequestModel,
    httpServletRequest: HttpServletRequest,
) : FilterPanelViewModel(
        filters =
            listOf(
                FilterViewModel(
                    headingMsgKey = "landlordSearch.filter.la.heading",
                    searchRequestProperty = "restrictToLA",
                    options = listOf(CheckboxViewModel(value = true, labelMsgKey = "landlordSearch.filter.la.label")),
                ),
            ),
        searchRequestModel = searchRequestModel,
        httpServletRequest = httpServletRequest,
    )
