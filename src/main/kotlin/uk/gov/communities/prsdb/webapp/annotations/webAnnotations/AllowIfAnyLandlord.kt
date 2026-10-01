package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAnyRole('INDIVIDUAL_LANDLORD', 'ORG_ADMIN', 'ORG_EDITOR')")
annotation class AllowIfAnyLandlord
