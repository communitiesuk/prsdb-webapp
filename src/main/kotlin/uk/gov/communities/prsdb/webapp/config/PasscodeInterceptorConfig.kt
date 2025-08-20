package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebConfiguration
import uk.gov.communities.prsdb.webapp.config.interceptors.PasscodeInterceptor
import uk.gov.communities.prsdb.webapp.services.PasscodeService

@PrsdbWebConfiguration
@Profile("require-passcode")
class PasscodeInterceptorConfig(
    private val passcodeService: PasscodeService,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(PasscodeInterceptor(passcodeService))
    }
}
