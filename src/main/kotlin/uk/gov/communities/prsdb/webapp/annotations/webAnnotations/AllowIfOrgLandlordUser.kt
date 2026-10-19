package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
// AllowIfOrgLandlordUserTests enforces that these stay consistent with ORG_LANDLORD_ROLES.
@PreAuthorize("hasAnyRole('ORG_ADMIN', 'ORG_EDITOR')")
annotation class AllowIfOrgLandlordUser
