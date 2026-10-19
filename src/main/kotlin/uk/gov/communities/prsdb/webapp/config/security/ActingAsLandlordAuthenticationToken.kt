package uk.gov.communities.prsdb.webapp.config.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import uk.gov.communities.prsdb.webapp.constants.ROLE_ACTING_AS_LANDLORD

class ActingAsLandlordAuthenticationToken(
    private val actingUserId: String,
    landlordRoles: List<String>,
    val operatorId: String,
) : AbstractAuthenticationToken((landlordRoles + ROLE_ACTING_AS_LANDLORD).map { SimpleGrantedAuthority(it) }) {
    init {
        isAuthenticated = true
    }

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): String = actingUserId

    override fun getName(): String = actingUserId
}
