package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import kotlin.test.Test

@WebMvcTest(FormsController::class)
class FormsControllerTests(
    @Autowired val webContext: WebApplicationContext,
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

    @Test
    @WithMockUser
    fun `FormsController returns 200 for authorized user`() {
        mvc
            .get("/forms/landlord_registration/start")
            .andExpect {
                status { isOk() }
            }
    }

    // TODO this will need to be updated before PR - with Role and correct redirect
//    @Test
//    @WithMockUser
//    fun `FormsController returns 200 and redirect for authorized user with CORRECT INPUT????`() {
//        Mockito.`when`(
//            journeyService.updateFormContextAndGetNextStep(
//                JourneyType.valueOf("landlord_registration"),
//                JourneyStep.valueOf("phone_number"),
//                "user",
//                "{\"email\": \"test@test.com\"}",
//            ),
//        )
//        mvc
//            .post("/forms/landlord_registration/phone_number")
//            .andExpect {
//                redirectedUrl("/forms/landlord_registration/NEXT-STEP-HERE")
//                status { isOk() }
//            }
//    }
}
