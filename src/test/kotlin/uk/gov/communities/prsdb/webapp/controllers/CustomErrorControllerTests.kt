package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.CustomErrorController.Companion.FILE_TOO_LARGE_ERROR_ROUTE

@WebMvcTest(CustomErrorController::class)
class CustomErrorControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Nested
    inner class CyaErrorPageTests {
        @Test
        fun `returns 200 for unauthenticated users`() {
            mvc
                .get(CustomErrorController.CYA_ERROR_ROUTE)
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        @WithMockUser
        fun `returns 200 for authenticated users`() {
            mvc
                .get(CustomErrorController.CYA_ERROR_ROUTE)
                .andExpect {
                    status { isOk() }
                }
        }
    }

    @Nested
    inner class FileTooLargeErrorPageTests {
        @Test
        fun `returns 200 for unauthenticated users`() {
            mvc
                .get(FILE_TOO_LARGE_ERROR_ROUTE)
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        @WithMockUser
        fun `returns 200 for authenticated users`() {
            mvc
                .get(FILE_TOO_LARGE_ERROR_ROUTE)
                .andExpect {
                    status { isOk() }
                }
        }
    }
}
