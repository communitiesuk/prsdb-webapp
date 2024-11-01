package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordRegistrationJourney

@WebMvcTest(RegisterLandlordController::class)
class RegisterLandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockBean
    lateinit var mockLandlordRegistrationJourney: LandlordRegistrationJourney

    @Test
    fun `RegisterLandlordController returns 200 for unauthenticated user`() {
        mvc.get("/register-as-a-landlord").andExpect {
            status { isOk() }
        }
    }
}
