package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.UrlHandlerFilter
import uk.gov.communities.prsdb.webapp.config.CustomErrorConfig
import uk.gov.communities.prsdb.webapp.config.CustomSecurityConfig
import uk.gov.communities.prsdb.webapp.config.filters.TrailingSlashFilterConfiguration
import uk.gov.communities.prsdb.webapp.services.UserRolesService

@Import(CustomSecurityConfig::class, CustomErrorConfig::class, TrailingSlashFilterConfiguration::class)
abstract class ControllerTest(
    private val context: WebApplicationContext,
) {
    protected lateinit var mvc: MockMvc

    @Autowired
    private lateinit var trailingSlashFilter: UrlHandlerFilter

    @BeforeEach
    fun setup() {
        mvc =
            MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters<DefaultMockMvcBuilder>(trailingSlashFilter)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()
    }

    @MockBean
    lateinit var mockClientRegistrationRepository: ClientRegistrationRepository

    @MockBean
    lateinit var userRolesService: UserRolesService
}
