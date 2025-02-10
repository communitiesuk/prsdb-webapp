package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_LANDLORDS_SEARCH_PAGE
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_PROPERTIES_SEARCH_PAGE
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.models.viewModels.LandlordSearchResultViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PropertySearchResultViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import kotlin.test.Test

@WebMvcTest(SearchRegisterController::class)
class SearchRegisterControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockBean
    private lateinit var landlordService: LandlordService

    @MockBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Test
    fun `SearchRegisterController returns a redirect for unauthenticated user`() {
        mvc.get("/search/landlord").andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `SearchRegisterController returns 403 for unauthorized user`() {
        mvc
            .get("/search/landlord")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_USER"])
    fun `SearchRegisterController returns 200 for authorized user`() {
        mvc
            .get("/search/landlord")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_USER"])
    fun `searchForLandlords returns 200 for a valid page request`() {
        whenever(landlordService.searchForLandlords("PRSDB", "user", requestedPageIndex = 1))
            .thenReturn(
                PageImpl(
                    listOf(
                        LandlordSearchResultViewModel(
                            123.toLong(),
                            "Test name",
                            "L-123ABC",
                            "1 Street Address",
                            "test@example.com",
                            "01223 123456",
                        ),
                    ),
                    PageRequest.of(
                        1,
                        MAX_ENTRIES_IN_LANDLORDS_SEARCH_PAGE,
                    ),
                    2,
                ),
            )

        mvc.get("/search/landlord?searchTerm=PRSDB&page=2").andExpect {
            status { isOk() }
        }
    }

    @Test
    @WithMockUser(roles = ["LA_USER"])
    fun `searchForLandlords returns 404 if the requested page number is less than 1`() {
        mvc.get("/search/landlord?searchTerm=PRSDB&page=0").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["LA_USER"])
    fun `searchForLandlords redirects if the requested page number is more than the total pages`() {
        whenever(landlordService.searchForLandlords("PRSDB", "user", requestedPageIndex = 2))
            .thenReturn(
                PageImpl(
                    emptyList<LandlordSearchResultViewModel>(),
                    PageRequest.of(
                        2,
                        MAX_ENTRIES_IN_LANDLORDS_SEARCH_PAGE,
                    ),
                    1,
                ),
            )

        mvc.get("/search/landlord?searchTerm=PRSDB&page=3").andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser(roles = ["LA_USER"])
    fun `searchForProperties returns 200 for a valid page request`() {
        whenever(propertyOwnershipService.searchForProperties("PRSDB", requestedPageIndex = 1))
            .thenReturn(
                PageImpl(
                    listOf(PropertySearchResultViewModel.fromPropertyOwnership(MockLandlordData.createPropertyOwnership())),
                    PageRequest.of(1, MAX_ENTRIES_IN_PROPERTIES_SEARCH_PAGE),
                    2,
                ),
            )

        mvc.get("/search/property?searchTerm=PRSDB&page=2").andExpect {
            status { isOk() }
        }
    }

    @Test
    @WithMockUser(roles = ["LA_USER"])
    fun `searchForProperties returns 404 if the requested page number is less than 1`() {
        mvc.get("/search/property?searchTerm=PRSDB&page=0").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["LA_USER"])
    fun `searchForProperties redirects if the requested page number is more than the total pages`() {
        whenever(propertyOwnershipService.searchForProperties("PRSDB", requestedPageIndex = 2))
            .thenReturn(
                PageImpl(
                    emptyList<PropertySearchResultViewModel>(),
                    PageRequest.of(2, MAX_ENTRIES_IN_LANDLORDS_SEARCH_PAGE),
                    1,
                ),
            )

        mvc.get("/search/property?searchTerm=PRSDB&page=3").andExpect {
            status { is3xxRedirection() }
        }
    }
}
