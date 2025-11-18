package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

class FeatureFlagTestCallingEndpoints : FeatureFlagTest() {
    // TODO PRSD-1647 - make tests for endpoint availability inherit from this class
    @Autowired
    lateinit var webContext: WebApplicationContext

    lateinit var mvc: MockMvc

    @BeforeEach
    fun setup() {
        mvc =
            MockMvcBuilders
                .webAppContextSetup(webContext)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()
    }
}
