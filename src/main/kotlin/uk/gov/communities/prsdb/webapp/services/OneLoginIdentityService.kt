package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.OneLoginClaimKeys
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedCredentialModel

private const val VERIFIED_IDENTITY_CACHE_KEY = "verified-identity-cache"

@Service
class OneLoginIdentityService(
    private val decoderFactory: JwtDecoderFactory<Unit>,
    private val session: HttpSession,
) {
    fun getVerifiedIdentityData(user: OidcUser): MutableMap<String, Any?>? {
        val cachedVerifiedIdentity = retrieveCachedVerifiedIdentity()
        if (cachedVerifiedIdentity != null) {
            return cachedVerifiedIdentity
        }

        val idClaimString = user.claims[OneLoginClaimKeys.CORE_IDENTITY] as? String
        if (idClaimString != null) {
            val idClaimJwt = decodeCoreIdentityJwt(idClaimString)
            val verifiedIdentity = extractVerifiedIdentity(idClaimJwt)
            cacheVerifiedIdentity(verifiedIdentity)
            return verifiedIdentity
        }

        return null
    }

    private fun cacheVerifiedIdentity(verifiedIdentity: MutableMap<String, Any?>) {
        session.setAttribute(VERIFIED_IDENTITY_CACHE_KEY, verifiedIdentity)
    }

    private fun extractVerifiedIdentity(idClaimJwt: Jwt): MutableMap<String, Any?> {
        val verifiableCredentialMap = idClaimJwt.claims["vc"] as? Map<*, *>
        val verifiableCredential = VerifiedCredentialModel.fromUnknownMap(verifiableCredentialMap)

        return mutableMapOf(
            "name" to verifiableCredential.credentialSubject.getCurrentName(),
            "birthDate" to verifiableCredential.credentialSubject.birthDate,
        )
    }

    private fun decodeCoreIdentityJwt(idClaimString: String): Jwt {
        val decoder = decoderFactory.createDecoder(Unit)
        return decoder.decode(idClaimString)
    }

    private fun retrieveCachedVerifiedIdentity(): MutableMap<String, Any?>? {
        val cached = session.getAttribute(VERIFIED_IDENTITY_CACHE_KEY) as? Map<*, *>
        return cached
            ?.map { (key, value) ->
                if (key !is String) return null
                key to value
            }?.toMap(mutableMapOf())
    }
}
