package uk.gov.communities.prsdb.webapp.models.requestModels

import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.models.requestModels.searchModels.SearchRequestModel
import kotlin.test.assertEquals

class SearchRequestModelTests {
    class TestNoFilterSearchRequestModel : SearchRequestModel()

    class TestNullValueFilterSearchRequestModel : SearchRequestModel() {
        var filter: String? = null
    }

    class TestSingleValueFilterSearchRequestModel : SearchRequestModel() {
        var filter: String = "value"
    }

    class TestMultiValueFilterSearchRequestModel : SearchRequestModel() {
        var filter: List<String> = listOf("value1", "value2")
    }

    companion object {
        @JvmStatic
        fun provideSearchRequestModels(): List<Arguments> {
            val testSingleValueFilterSearchRequestModel = TestSingleValueFilterSearchRequestModel()
            val testMultiValueFilterSearchRequestModel = TestMultiValueFilterSearchRequestModel()

            return listOf(
                Arguments.of(
                    Named.of(
                        "has no filter properties",
                        TestNoFilterSearchRequestModel(),
                    ),
                    Named.of(
                        "an empty list",
                        emptyList<Pair<String, Any>>(),
                    ),
                ),
                Arguments.of(
                    Named.of(
                        "has a null-valued filter property",
                        TestNullValueFilterSearchRequestModel(),
                    ),
                    Named.of(
                        "an empty list",
                        emptyList<Pair<String, Any>>(),
                    ),
                ),
                Arguments.of(
                    Named.of(
                        "has a single-valued filter property",
                        testSingleValueFilterSearchRequestModel,
                    ),
                    Named.of(
                        "a corresponding list of one filter property name-value pair",
                        listOf(
                            Pair("filter", testSingleValueFilterSearchRequestModel.filter),
                        ),
                    ),
                ),
                Arguments.of(
                    Named.of(
                        "has a multi-valued filter property",
                        testMultiValueFilterSearchRequestModel,
                    ),
                    Named.of(
                        "a corresponding list of a filter property name-value pair per value",
                        testMultiValueFilterSearchRequestModel.filter.map { Pair("filter", it) },
                    ),
                ),
            )
        }
    }

    @ParameterizedTest(name = "{1} when the SearchRequestModel {0}")
    @MethodSource("provideSearchRequestModels")
    fun `getFilterPropertyNameValuePairs returns`(
        searchRequestModel: SearchRequestModel,
        expectedNameValuePairs: List<Pair<String, Any>>,
    ) {
        assertEquals(searchRequestModel.getFilterPropertyNameValuePairs(), expectedNameValuePairs)
    }
}
