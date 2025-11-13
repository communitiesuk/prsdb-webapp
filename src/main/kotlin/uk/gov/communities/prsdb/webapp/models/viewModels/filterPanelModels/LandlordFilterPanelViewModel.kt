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
                    headingMsgKey = "landlordSearch.filter.localCouncil.heading",
                    searchRequestProperty = "restrictToLocalcouncil",
                    options = listOf(CheckboxViewModel(value = true, labelMsgKey = "landlordSearch.filter.localCouncil.label")),
                ),
            ),
        searchRequestModel = searchRequestModel,
        httpServletRequest = httpServletRequest,
    )
