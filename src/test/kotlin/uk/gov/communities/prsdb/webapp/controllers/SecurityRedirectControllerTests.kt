package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.communities.prsdb.webapp.constants.SECURITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SECURITY_TXT_REDIRECT
import uk.gov.communities.prsdb.webapp.constants.WELL_KNOWN_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService

@WebMvcTest(SecurityRedirectController::class)
class SecurityRedirectControllerTests {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var backUrlStorageService: BackUrlStorageService

    @Test
    @WithMockUser
    fun `GET security txt endpoint returns 302 redirect to correct url`() {
        val url = "/${WELL_KNOWN_PATH_SEGMENT}/${SECURITY_PATH_SEGMENT}"
        mockMvc
            .get(url)
            .andExpect {
                status { isFound() }
                redirectedUrl(SECURITY_TXT_REDIRECT)
            }
    }
}
