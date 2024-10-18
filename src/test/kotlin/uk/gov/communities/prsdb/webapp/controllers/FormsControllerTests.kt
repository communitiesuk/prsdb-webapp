package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.services.JourneyService
import kotlin.test.Test

@WebMvcTest(FormsController::class)
class FormsControllerTests(
    @Autowired val webContext: WebApplicationContext,
    @Autowired val journeyService: JourneyService,
) : ControllerTest(webContext) {
    //  // TODO this test won't pass while auth is disabled for development
//  @Test
//  fun `FormsController returns a redirect for unauthenticated user`() {
//    mvc.get("/forms/registration-journey-type/name").andExpect {
//      status { is3xxRedirection() }
//    }
//  }
//
//  // TODO this test won't pass while auth is disabled for development
//  @Test
//  @WithMockUser
//  fun `FormsController returns 403 for unauthorized user`() {
//    mvc
//      .get("/forms/registration-journey-type/name")
//      .andExpect {
//        status { isForbidden() }
//      }
//  }

    // TODO this twill need to be updated before PR
    @Test
    fun `FormsController returns 200 for authorized user`() {
        mvc
            .get("/forms/registration-journey-type/name")
            .andExpect {
                status { isOk() }
            }
    }
}
