package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
// The roles granted access here must be kept in sync with LANDLORD_ROLES in UserRoleConstants.
// AllowIfLandlordTests enforces that these stay consistent.
@PreAuthorize("hasAnyRole('INDIVIDUAL_LANDLORD', 'ORG_ADMIN', 'ORG_EDITOR')")
annotation class AllowIfLandlord
