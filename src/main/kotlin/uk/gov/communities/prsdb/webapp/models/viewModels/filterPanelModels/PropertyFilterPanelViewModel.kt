package uk.gov.communities.prsdb.webapp.models.viewModels.filterPanelModels

import jakarta.servlet.http.HttpServletRequest
import uk.gov.communities.prsdb.webapp.models.requestModels.searchModels.PropertySearchRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.CheckboxViewModel

class PropertyFilterPanelViewModel(
    searchRequestModel: PropertySearchRequestModel,
    httpServletRequest: HttpServletRequest,
) : FilterPanelViewModel(
        filters =
            listOf(
                FilterViewModel(
                    headingMsgKey = "propertySearch.filter.la.heading",
                    searchRequestProperty = "restrictToLA",
                    options = listOf(CheckboxViewModel(value = true, labelMsgKey = "propertySearch.filter.la.label")),
                ),
            ),
        searchRequestModel = searchRequestModel,
        httpServletRequest = httpServletRequest,
    )
