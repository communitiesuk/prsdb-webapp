package uk.gov.communities.prsdb.webapp.models.viewModels.filterPanelModels

import jakarta.servlet.http.HttpServletRequest
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
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
                FilterViewModel(
                    headingMsgKey = "propertySearch.filter.licence.heading",
                    searchRequestProperty = "restrictToLicenses",
                    options =
                        listOf(
                            CheckboxViewModel(
                                value = LicensingType.SELECTIVE_LICENCE,
                                labelMsgKey = "propertySearch.filter.licence.selective.label",
                            ),
                            CheckboxViewModel(
                                value = LicensingType.HMO_MANDATORY_LICENCE,
                                labelMsgKey = "propertySearch.filter.licence.hmoMandatory.label",
                            ),
                            CheckboxViewModel(
                                value = LicensingType.HMO_ADDITIONAL_LICENCE,
                                labelMsgKey = "propertySearch.filter.licence.hmoAdditional.label",
                            ),
                            CheckboxViewModel(
                                value = LicensingType.NO_LICENSING,
                                labelMsgKey = "propertySearch.filter.licence.not.label",
                            ),
                        ),
                ),
            ),
        searchRequestModel = searchRequestModel,
        httpServletRequest = httpServletRequest,
    )
