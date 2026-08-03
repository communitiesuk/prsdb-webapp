package uk.gov.communities.prsdb.webapp.integration

import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local-org-landlord")
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class WithOrgLandlordProfile
