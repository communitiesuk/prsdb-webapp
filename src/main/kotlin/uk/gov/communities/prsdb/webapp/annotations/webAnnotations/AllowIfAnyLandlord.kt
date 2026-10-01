package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.springframework.security.access.prepost.PreAuthorize
import uk.gov.communities.prsdb.webapp.constants.ROLE_INDIVIDUAL_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_ORG_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_ORG_EDITOR

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
// The roles granted access here must be kept in sync with AnyLandlordRoles.ROLES below.
@PreAuthorize("hasAnyRole('INDIVIDUAL_LANDLORD', 'ORG_ADMIN', 'ORG_EDITOR')")
annotation class AllowIfAnyLandlord

// Programmatic equivalent of the roles granted access by @AllowIfAnyLandlord, for use where the set
// of "any landlord" authorities is needed in code (e.g. dashboard routing, orphaned-user cleanup).
object AnyLandlordRoles {
    val ROLES = setOf(ROLE_INDIVIDUAL_LANDLORD, ROLE_ORG_ADMIN, ROLE_ORG_EDITOR)
}
