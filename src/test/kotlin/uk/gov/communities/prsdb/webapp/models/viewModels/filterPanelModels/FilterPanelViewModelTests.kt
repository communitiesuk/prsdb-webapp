package uk.gov.communities.prsdb.webapp.models.viewModels.filterPanelModels

import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.mock.web.MockHttpServletRequest
import uk.gov.communities.prsdb.webapp.models.requestModels.searchModels.SearchRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxViewModel
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilterPanelViewModelTests {
    enum class TestFilterOptions { One, Two, Three }

    class TestSearchRequestModel : SearchRequestModel() {
        var filter1: Boolean? = null
        var filter2: List<TestFilterOptions>? = null
    }

    class TestFilterPanelViewModel(
        searchRequestModel: TestSearchRequestModel,
        httpServletRequest: HttpServletRequest,
    ) : FilterPanelViewModel(
            filters =
                listOf(
                    FilterViewModel(
                        headingMsgKey = "filter.one.heading",
                        searchRequestProperty = "filter1",
                        options =
                            listOf(
                                CheckboxViewModel(value = true, labelMsgKey = "filter.one.label"),
                            ),
                    ),
                    FilterViewModel(
                        headingMsgKey = "filter.two.heading",
                        searchRequestProperty = "filter2",
                        options = TestFilterOptions.entries.map { CheckboxViewModel(value = it) },
                    ),
                ),
            searchRequestModel = searchRequestModel,
            httpServletRequest = httpServletRequest,
        )

    private fun FilterPanelViewModel.getSelectedOptionLabelsForFilter(filterSearchRequestProperty: String) =
        filters.single { it.searchRequestProperty == filterSearchRequestProperty }.selectedOptions.map { it.labelMsgOrVal }

    private lateinit var mockHttpServletRequest: MockHttpServletRequest
    private lateinit var searchRequestModel: TestSearchRequestModel

    @BeforeEach
    fun setUp() {
        mockHttpServletRequest = MockHttpServletRequest()
        mockHttpServletRequest.requestURI = "example.com/page"
        searchRequestModel = TestSearchRequestModel()
    }

    @Test
    fun `clearLink removes all filters`() {
        searchRequestModel.filter1 = true
        searchRequestModel.filter2 = listOf(TestFilterOptions.One, TestFilterOptions.Two)

        mockHttpServletRequest.queryString =
            "showFilter=${searchRequestModel.showFilter}&filter1=true&filter2=One&filter2=Two"
        val expectedClearFiltersLink =
            "${mockHttpServletRequest.requestURI}?showFilter=${searchRequestModel.showFilter}"

        val filterPanelViewModel = TestFilterPanelViewModel(searchRequestModel, mockHttpServletRequest)

        assertEquals(expectedClearFiltersLink, filterPanelViewModel.clearLink)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `toggleLink inverts showFilter when`(showFilter: Boolean) {
        searchRequestModel.showFilter = showFilter
        val expectedToggleLink = "${mockHttpServletRequest.requestURI}?showFilter=${!showFilter}"

        val filterPanelViewModel = TestFilterPanelViewModel(searchRequestModel, mockHttpServletRequest)

        assertEquals(expectedToggleLink, filterPanelViewModel.toggleLink)
    }

    @Test
    fun `FilterPanelViewModel generates no SelectedFilterOptionViewModels when no filters have selected options`() {
        val filterPanelViewModel = TestFilterPanelViewModel(searchRequestModel, mockHttpServletRequest)

        assertTrue(filterPanelViewModel.filters.all { it.selectedOptions.isEmpty() })
        assertTrue(filterPanelViewModel.noFiltersSelected)
    }

    @Test
    fun `FilterPanelViewModel generates the corresponding SelectedFilterOptionViewModel when a filter with one option is selected`() {
        searchRequestModel.filter1 = true
        val expectedSelectedOptionLabelsForFilter1 = listOf("filter.one.label")

        val filterPanelViewModel = TestFilterPanelViewModel(searchRequestModel, mockHttpServletRequest)

        assertEquals(
            expectedSelectedOptionLabelsForFilter1,
            filterPanelViewModel.getSelectedOptionLabelsForFilter("filter1"),
        )
        assertEquals(emptyList(), filterPanelViewModel.getSelectedOptionLabelsForFilter("filter2"))
        assertFalse(filterPanelViewModel.noFiltersSelected)
    }

    @Test
    fun `FilterPanelViewModel generates the corresponding SelectedFilterOptionViewModels when a filter has multiple options selected`() {
        searchRequestModel.filter2 = listOf(TestFilterOptions.One, TestFilterOptions.Two)
        val expectedSelectedOptionLabelsForFilter2 = listOf("One", "Two")

        val filterPanelViewModel = TestFilterPanelViewModel(searchRequestModel, mockHttpServletRequest)

        assertEquals(emptyList(), filterPanelViewModel.getSelectedOptionLabelsForFilter("filter1"))
        assertEquals(
            expectedSelectedOptionLabelsForFilter2,
            filterPanelViewModel.getSelectedOptionLabelsForFilter("filter2"),
        )
        assertFalse(filterPanelViewModel.noFiltersSelected)
    }

    @Test
    fun `FilterPanelViewModel generates the corresponding SelectedFilterOptionViewModels when multiple filters have options selected`() {
        searchRequestModel.filter1 = true
        searchRequestModel.filter2 = listOf(TestFilterOptions.One, TestFilterOptions.Two)
        val expectedSelectedOptionLabelsForFilter1 = listOf("filter.one.label")
        val expectedSelectedOptionLabelsForFilter2 = listOf("One", "Two")

        val filterPanelViewModel = TestFilterPanelViewModel(searchRequestModel, mockHttpServletRequest)

        assertEquals(
            expectedSelectedOptionLabelsForFilter1,
            filterPanelViewModel.getSelectedOptionLabelsForFilter("filter1"),
        )
        assertEquals(
            expectedSelectedOptionLabelsForFilter2,
            filterPanelViewModel.getSelectedOptionLabelsForFilter("filter2"),
        )
        assertFalse(filterPanelViewModel.noFiltersSelected)
    }

    @Test
    fun `SelectedFilterOptionViewModel generates the selected option's remove link`() {
        val searchRequestProperty = "filter"
        val selectedOption: CheckboxViewModel<Any> = CheckboxViewModel(value = "value")
        mockHttpServletRequest.queryString = "filter=value&filter=otherValue&page=2"
        val expectedRemoveLink = "${mockHttpServletRequest.requestURI}?filter=otherValue"

        val selectedFilterOptionViewModel =
            SelectedFilterOptionViewModel(searchRequestProperty, selectedOption, mockHttpServletRequest)

        assertEquals(expectedRemoveLink, selectedFilterOptionViewModel.removeLink)
    }
}
