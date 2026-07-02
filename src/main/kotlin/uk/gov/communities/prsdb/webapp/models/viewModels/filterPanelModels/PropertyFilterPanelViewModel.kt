package uk.gov.communities.prsdb.webapp.models.viewModels.filterPanelModels

import jakarta.servlet.http.HttpServletRequest
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.models.requestModels.searchModels.PropertySearchRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxButtonViewModel

class PropertyFilterPanelViewModel(
    searchRequestModel: PropertySearchRequestModel,
    httpServletRequest: HttpServletRequest,
) : FilterPanelViewModel(
        filters =
            listOf(
                FilterViewModel(
                    headingMsgKey = "propertySearch.filter.localCouncil.heading",
                    searchRequestProperty = "restrictToLocalCouncil",
                    options = listOf(CheckboxButtonViewModel(value = true, labelMsgKey = "propertySearch.filter.localCouncil.label")),
                ),
                FilterViewModel(
                    headingMsgKey = "propertySearch.filter.licence.heading",
                    searchRequestProperty = "restrictToLicenses",
                    options =
                        listOf(
                            CheckboxButtonViewModel(
                                value = LicensingType.SELECTIVE_LICENCE,
                                labelMsgKey = "propertySearch.filter.licence.selective.label",
                            ),
                            CheckboxButtonViewModel(
                                value = LicensingType.HMO_MANDATORY_LICENCE,
                                labelMsgKey = "propertySearch.filter.licence.hmoMandatory.label",
                            ),
                            CheckboxButtonViewModel(
                                value = LicensingType.HMO_ADDITIONAL_LICENCE,
                                labelMsgKey = "propertySearch.filter.licence.hmoAdditional.label",
                            ),
                            CheckboxButtonViewModel(
                                value = LicensingType.NO_LICENSING,
                                labelMsgKey = "propertySearch.filter.licence.not.label",
                            ),
                        ),
                ),
            ),
        searchRequestModel = searchRequestModel,
        httpServletRequest = httpServletRequest,
    )
