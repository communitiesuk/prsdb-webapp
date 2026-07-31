package uk.gov.communities.prsdb.webapp.integration

import org.springframework.test.context.ActiveProfiles

@ActiveProfiles(profiles = ["local", "local-no-auth", "local-org-landlord"])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class WithOrgLandlordUser
