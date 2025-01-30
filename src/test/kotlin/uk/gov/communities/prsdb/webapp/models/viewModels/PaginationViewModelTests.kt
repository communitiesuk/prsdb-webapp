package uk.gov.communities.prsdb.webapp.models.viewModels

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import kotlin.test.assertEquals

class PaginationViewModelTests {
    private lateinit var mockHTTPServletRequest: MockHttpServletRequest

    @BeforeEach
    fun setup() {
        mockHTTPServletRequest = MockHttpServletRequest()
        mockHTTPServletRequest.requestURI = "example.com/eg"
    }

    @Test
    fun `getPageLink returns a link to the given page number (page insert)`() {
        mockHTTPServletRequest.setParameter("name", "value")
        val paginationViewModel = PaginationViewModel(page = 1, totalPages = 10, mockHTTPServletRequest)
        val expectedPageLink = "${mockHTTPServletRequest.requestURI}?name=value&page=5"

        val pageLink = paginationViewModel.getPageLink(5)

        assertEquals(expectedPageLink, pageLink)
    }

    @Test
    fun `getPageLink returns a link to the given page number (page update)`() {
        mockHTTPServletRequest.setParameters(mapOf("name" to "value", "page" to "2"))
        val paginationViewModel = PaginationViewModel(page = 2, totalPages = 10, mockHTTPServletRequest)
        val expectedPageLink = "${mockHTTPServletRequest.requestURI}?name=value&page=5"

        val pageLink = paginationViewModel.getPageLink(5)

        assertEquals(expectedPageLink, pageLink)
    }

    @Test
    fun `getPreviousPageLink returns a link to the previous page`() {
        mockHTTPServletRequest.setParameters(mapOf("name" to "value", "page" to "2"))
        val paginationViewModel = PaginationViewModel(page = 2, totalPages = 10, mockHTTPServletRequest)
        val expectedPageLink = "${mockHTTPServletRequest.requestURI}?name=value&page=1"

        val pageLink = paginationViewModel.getPreviousPageLink()

        assertEquals(expectedPageLink, pageLink)
    }

    @Test
    fun `getNextPageLink returns a link to the next page`() {
        mockHTTPServletRequest.setParameters(mapOf("name" to "value", "page" to "2"))
        val paginationViewModel = PaginationViewModel(page = 2, totalPages = 10, mockHTTPServletRequest)
        val expectedPageLink = "${mockHTTPServletRequest.requestURI}?name=value&page=3"

        val pageLink = paginationViewModel.getNextPageLink()

        assertEquals(expectedPageLink, pageLink)
    }
}
