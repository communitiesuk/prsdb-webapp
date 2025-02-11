package uk.gov.communities.prsdb.webapp.models.viewModels.filterPanelModels

import jakarta.servlet.http.HttpServletRequest
import uk.gov.communities.prsdb.webapp.helpers.URIQueryBuilder
import uk.gov.communities.prsdb.webapp.models.requestModels.searchModels.SearchRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.CheckboxViewModel
import kotlin.properties.Delegates
import kotlin.reflect.full.memberProperties

abstract class FilterPanelViewModel(
    val filters: List<FilterViewModel>,
    searchRequestModel: SearchRequestModel,
    httpServletRequest: HttpServletRequest,
) {
    val clearLink =
        URIQueryBuilder
            .fromHTTPServletRequest(httpServletRequest)
            .removeParams(filters.map { it.searchRequestProperty })
            .build()
            .toUriString()

    val showFilterLink =
        URIQueryBuilder
            .fromHTTPServletRequest(httpServletRequest)
            .updateParam("showFilter", !searchRequestModel.showFilter)
            .build()
            .toUriString()

    var noFiltersSelected by Delegates.notNull<Boolean>()

    init {
        filters.forEach { it.initializeSelectedOptions(searchRequestModel, httpServletRequest) }
        noFiltersSelected = filters.all { it.selectedOptions.isEmpty() }
    }
}

class FilterViewModel(
    val headingMsgKey: String,
    val searchRequestProperty: String,
    val options: List<CheckboxViewModel<Any>>,
) {
    lateinit var selectedOptions: List<SelectedFilterOptionViewModel>

    fun initializeSelectedOptions(
        searchRequestModel: SearchRequestModel,
        httpServletRequest: HttpServletRequest,
    ) {
        val selectedOptionRequest =
            searchRequestModel::class
                .memberProperties
                .single { it.name == searchRequestProperty }
                .getter
                .call(searchRequestModel)

        selectedOptions =
            options
                .filter {
                    if (selectedOptionRequest is Collection<*>) {
                        selectedOptionRequest.contains(it.value)
                    } else {
                        selectedOptionRequest?.equals(it.value) == true
                    }
                }.map { SelectedFilterOptionViewModel(searchRequestProperty, it, httpServletRequest) }
    }
}

class SelectedFilterOptionViewModel(
    searchRequestProperty: String,
    selectedOption: CheckboxViewModel<Any>,
    httpServletRequest: HttpServletRequest,
) {
    val labelMsgOrVal = selectedOption.labelMsgKey ?: selectedOption.valueStr

    val removeLink =
        URIQueryBuilder
            .fromHTTPServletRequest(httpServletRequest)
            .removeParamValue(searchRequestProperty, selectedOption.value.toString())
            .removeParam("page")
            .build()
            .toUriString()
}
