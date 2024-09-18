package uk.gov.communities.prsd.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsd.webapp.config.CustomSecurityConfig
import uk.gov.communities.prsd.webapp.services.UserRolesService

@Import(CustomSecurityConfig::class)
abstract class ControllerTest(
    private val context: WebApplicationContext,
) {
    protected lateinit var mvc: MockMvc

    @BeforeEach
    fun setup() {
        mvc =
            MockMvcBuilders
                .webAppContextSetup(context)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()
    }

    @MockBean
    lateinit var mockClientRegistrationRepository: ClientRegistrationRepository

    @MockBean
    lateinit var userRolesService: UserRolesService
}
