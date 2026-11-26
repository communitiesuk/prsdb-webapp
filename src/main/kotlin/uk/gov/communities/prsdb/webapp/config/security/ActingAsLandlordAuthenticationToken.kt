package uk.gov.communities.prsdb.webapp.config.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import uk.gov.communities.prsdb.webapp.constants.ROLE_ACTING_AS_LANDLORD

// TODO PDJB-1903: Build this token for each request in the acting as landlord filter, from ActingAsLandlordService.getActingAsUserId()
class ActingAsLandlordAuthenticationToken(
    private val actingAsUserId: String,
    landlordRoles: List<String>,
    val systemOperatorId: String,
) : AbstractAuthenticationToken((landlordRoles + ROLE_ACTING_AS_LANDLORD).map { SimpleGrantedAuthority(it) }) {
    init {
        isAuthenticated = true
    }

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): String = actingAsUserId

    override fun getName(): String = actingAsUserId
}
