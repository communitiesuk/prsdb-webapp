package uk.gov.communities.prsdb.webapp.models.requestModels

import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.models.requestModels.searchModels.SearchRequestModel
import kotlin.test.assertEquals

class SearchRequestModelTests {
    class TestNoFilterSearchRequestModel : SearchRequestModel()

    class TestFilterSearchRequestModel : SearchRequestModel() {
        var filter1: String = "value1"
        var filter2: String = "value2"
    }

    companion object {
        @JvmStatic
        fun provideSearchRequestModels(): List<Arguments> {
            val testFilterSearchRequestModel = TestFilterSearchRequestModel()

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
                        "has filter properties",
                        testFilterSearchRequestModel,
                    ),
                    Named.of(
                        "a corresponding list of filter property name-value pairs",
                        listOf(
                            Pair("filter1", testFilterSearchRequestModel.filter1),
                            Pair("filter2", testFilterSearchRequestModel.filter2),
                        ),
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
