package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest

class URIQueryBuilderTests {
    private lateinit var mockHTTPServletRequest: MockHttpServletRequest

    @BeforeEach
    fun setup() {
        mockHTTPServletRequest = MockHttpServletRequest()
        mockHTTPServletRequest.requestURI = "example.com/page"
    }

    @Test
    fun `build returns the corresponding URI`() {
        mockHTTPServletRequest.queryString = "name=value"
        val expectedURI = "${mockHTTPServletRequest.requestURI}?${mockHTTPServletRequest.queryString}"

        val builtURI = URIQueryBuilder.fromHTTPServletRequest(mockHTTPServletRequest).build().toUriString()

        assertEquals(expectedURI, builtURI)
    }

    @Test
    fun `updateParam returns the updated URI (single value update)`() {
        mockHTTPServletRequest.queryString = "name1=value1&name2=value2"
        val updatedName1Value = "newValue1"
        val expectedUpdatedURI = "${mockHTTPServletRequest.requestURI}?name2=value2&name1=$updatedName1Value"

        val updatedURI =
            URIQueryBuilder
                .fromHTTPServletRequest(mockHTTPServletRequest)
                .updateParam("name1", updatedName1Value)
                .build()
                .toUriString()

        assertEquals(expectedUpdatedURI, updatedURI)
    }

    @Test
    fun `updateParam returns the updated URI (single value insert)`() {
        mockHTTPServletRequest.queryString = "name2=value2"
        val name1Value = "value1"
        val expectedUpdatedURI = "${mockHTTPServletRequest.requestURI}?name2=value2&name1=$name1Value"

        val updatedURI =
            URIQueryBuilder
                .fromHTTPServletRequest(mockHTTPServletRequest)
                .updateParam("name1", name1Value)
                .build()
                .toUriString()

        assertEquals(expectedUpdatedURI, updatedURI)
    }

    @Test
    fun `updateParam returns the updated URI (multi-value update)`() {
        mockHTTPServletRequest.queryString = "name1=value1&name2=value2"
        val updatedName1Values = listOf("newValue1a", "newValue1b")
        val expectedUpdatedURI =
            "${mockHTTPServletRequest.requestURI}?name2=value2&name1=${updatedName1Values[0]}&name1=${updatedName1Values[1]}"

        val updatedURI =
            URIQueryBuilder
                .fromHTTPServletRequest(mockHTTPServletRequest)
                .updateParam("name1", updatedName1Values)
                .build()
                .toUriString()

        assertEquals(expectedUpdatedURI, updatedURI)
    }

    @Test
    fun `updateParam returns the updated URI (multi-value insert)`() {
        mockHTTPServletRequest.queryString = "name2=value2"
        val name1Values = listOf("value1a", "value1b")
        val expectedUpdatedURI =
            "${mockHTTPServletRequest.requestURI}?name2=value2&name1=${name1Values[0]}&name1=${name1Values[1]}"

        val updatedURI =
            URIQueryBuilder
                .fromHTTPServletRequest(mockHTTPServletRequest)
                .updateParam("name1", name1Values)
                .build()
                .toUriString()

        assertEquals(expectedUpdatedURI, updatedURI)
    }

    @Test
    fun `removeParam returns the updated URI`() {
        mockHTTPServletRequest.queryString = "name1=value1&_name1=hiddenValue1&name2=value2"

        val expectedUpdatedURI = "${mockHTTPServletRequest.requestURI}?name2=value2"

        val updatedURI =
            URIQueryBuilder
                .fromHTTPServletRequest(mockHTTPServletRequest)
                .removeParam("name1")
                .build()
                .toUriString()

        assertEquals(expectedUpdatedURI, updatedURI)
    }

    @Test
    fun `removeParam returns the original URI if the param does not exist`() {
        mockHTTPServletRequest.queryString = "name2=value2"
        val expectedURI = "${mockHTTPServletRequest.requestURI}?${mockHTTPServletRequest.queryString}"

        val updatedURI =
            URIQueryBuilder
                .fromHTTPServletRequest(mockHTTPServletRequest)
                .removeParam("name1")
                .build()
                .toUriString()

        assertEquals(expectedURI, updatedURI)
    }

    @Test
    fun `removeParams returns the updated URI`() {
        mockHTTPServletRequest.queryString = "name1=value1&_name1=hiddenValue1&name2=value2"
        val expectedUpdatedURI = "${mockHTTPServletRequest.requestURI}"

        val updatedURI =
            URIQueryBuilder
                .fromHTTPServletRequest(mockHTTPServletRequest)
                .removeParams(listOf("name1", "name2"))
                .build()
                .toUriString()

        assertEquals(expectedUpdatedURI, updatedURI)
    }

    @Test
    fun `removeParamValue returns the updated URI (single value param)`() {
        mockHTTPServletRequest.queryString = "name1=value1&_name1=hiddenValue1&name2=value2"
        val expectedUpdatedURI = "${mockHTTPServletRequest.requestURI}?name2=value2"

        val updatedURI =
            URIQueryBuilder
                .fromHTTPServletRequest(mockHTTPServletRequest)
                .removeParamValue("name1", "value1")
                .build()
                .toUriString()

        assertEquals(expectedUpdatedURI, updatedURI)
    }

    @Test
    fun `removeParamValue returns the updated URI (multi-value param)`() {
        mockHTTPServletRequest.queryString = "name1=value1a&name1=value1b&_name1=hiddenValue1&name2=value2"
        val expectedUpdatedURI = "${mockHTTPServletRequest.requestURI}?_name1=hiddenValue1&name2=value2&name1=value1b"

        val updatedURI =
            URIQueryBuilder
                .fromHTTPServletRequest(mockHTTPServletRequest)
                .removeParamValue("name1", "value1a")
                .build()
                .toUriString()

        assertEquals(expectedUpdatedURI, updatedURI)
    }

    @Test
    fun `removeParamValue returns the original URI if the param does not exist`() {
        mockHTTPServletRequest.queryString = "name2=value2"
        val expectedURI = "${mockHTTPServletRequest.requestURI}?${mockHTTPServletRequest.queryString}"

        val updatedURI =
            URIQueryBuilder
                .fromHTTPServletRequest(mockHTTPServletRequest)
                .removeParamValue("name1", "value2")
                .build()
                .toUriString()

        assertEquals(expectedURI, updatedURI)
    }

    @Test
    fun `removeParamValue returns the original URI if the param does not have the value`() {
        mockHTTPServletRequest.queryString = "name1=value1&name2=value2"
        val expectedURI = "${mockHTTPServletRequest.requestURI}?name2=value2&name1=value1"

        val updatedURI =
            URIQueryBuilder
                .fromHTTPServletRequest(mockHTTPServletRequest)
                .removeParamValue("name1", "value2")
                .build()
                .toUriString()

        assertEquals(expectedURI, updatedURI)
    }
}
