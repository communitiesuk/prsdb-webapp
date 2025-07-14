package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.UrlHandlerFilter
import uk.gov.communities.prsdb.webapp.config.BackLinkInterceptorConfig
import uk.gov.communities.prsdb.webapp.config.CustomErrorConfig
import uk.gov.communities.prsdb.webapp.config.filters.TrailingSlashFilterConfiguration
import uk.gov.communities.prsdb.webapp.config.security.DefaultSecurityConfig
import uk.gov.communities.prsdb.webapp.config.security.LandlordSecurityConfig
import uk.gov.communities.prsdb.webapp.config.security.LocalAuthoritySecurityConfig
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService
import uk.gov.communities.prsdb.webapp.services.UserRolesService

@Import(
    DefaultSecurityConfig::class,
    LandlordSecurityConfig::class,
    LocalAuthoritySecurityConfig::class,
    CustomErrorConfig::class,
    TrailingSlashFilterConfiguration::class,
    BackLinkInterceptorConfig::class,
)
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

    @MockitoBean
    lateinit var backLinkStorageService: BackUrlStorageService

    @MockitoBean
    lateinit var mockClientRegistrationRepository: ClientRegistrationRepository

    @MockitoBean
    lateinit var userRolesService: UserRolesService
}
