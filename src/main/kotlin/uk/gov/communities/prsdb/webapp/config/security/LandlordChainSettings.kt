package uk.gov.communities.prsdb.webapp.config.security

import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.csrf.CsrfFilter
import org.springframework.security.web.csrf.CsrfTokenRepository
import org.springframework.security.web.header.HeaderWriterFilter
import uk.gov.communities.prsdb.webapp.config.filters.CSPNonceFilter
import uk.gov.communities.prsdb.webapp.config.filters.MultipartFormDataFilter
import uk.gov.communities.prsdb.webapp.config.security.DefaultSecurityConfig.Companion.CONTENT_SECURITY_POLICY_DIRECTIVES
import uk.gov.communities.prsdb.webapp.config.security.DefaultSecurityConfig.Companion.PERMISSIONS_POLICY_DIRECTIVES

fun HttpSecurity.applyLandlordChainSettings(csrfTokenRepository: CsrfTokenRepository): HttpSecurity =
    this
        .csrf { it.csrfTokenRepository(csrfTokenRepository) }
        .addFilterBefore(MultipartFormDataFilter(csrfTokenRepository), CsrfFilter::class.java)
        .headers { headers ->
            headers
                .contentSecurityPolicy { csp ->
                    csp
                        .policyDirectives(CONTENT_SECURITY_POLICY_DIRECTIVES)
                }.permissionsPolicyHeader { permissions ->
                    permissions
                        .policy(PERMISSIONS_POLICY_DIRECTIVES)
                }
        }.addFilterBefore(CSPNonceFilter(), HeaderWriterFilter::class.java)
