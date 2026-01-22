package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import org.springframework.security.oauth2.jwt.JwtException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.OneLoginClaimKeys
import uk.gov.communities.prsdb.webapp.constants.VERIFIED_IDENTITY_CACHE_KEY
import uk.gov.communities.prsdb.webapp.exceptions.InvalidCoreIdentityException
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedCredentialModel
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedIdentityDataModel

@PrsdbWebService
class OneLoginIdentityService(
    private val decoderFactory: JwtDecoderFactory<Unit>,
    private val session: HttpSession,
) {
    fun getVerifiedIdentityData(user: OidcUser): VerifiedIdentityDataModel? {
        val cachedVerifiedIdentity = retrieveCachedVerifiedIdentity()
        if (cachedVerifiedIdentity != null) {
            return cachedVerifiedIdentity
        }

        val idClaimString = user.claims[OneLoginClaimKeys.CORE_IDENTITY] as? String ?: return null
        val idClaimJwt = decodeCoreIdentityJwt(idClaimString)
        val verifiedIdentity = extractVerifiedIdentity(idClaimJwt)
        cacheVerifiedIdentity(verifiedIdentity)
        return verifiedIdentity
    }

    private fun retrieveCachedVerifiedIdentity(): VerifiedIdentityDataModel? {
        val verifiedIdentityMap = session.getAttribute(VERIFIED_IDENTITY_CACHE_KEY) as? Map<*, *> ?: return null
        return VerifiedIdentityDataModel.fromMap(verifiedIdentityMap)
    }

    private fun cacheVerifiedIdentity(verifiedIdentity: VerifiedIdentityDataModel) {
        session.setAttribute(VERIFIED_IDENTITY_CACHE_KEY, verifiedIdentity.toMap())
    }

    private fun decodeCoreIdentityJwt(idClaimString: String): Jwt {
        val decoder = decoderFactory.createDecoder(Unit)
        try {
            return decoder.decode(idClaimString)
        } catch (innerException: JwtException) {
            throw InvalidCoreIdentityException(innerException)
        }
    }

    private fun extractVerifiedIdentity(idClaimJwt: Jwt): VerifiedIdentityDataModel {
        val verifiableCredentialMap = idClaimJwt.claims["vc"] as? Map<*, *>
        val verifiableCredential = VerifiedCredentialModel.fromUnknownMap(verifiableCredentialMap)

        return VerifiedIdentityDataModel(
            verifiableCredential.credentialSubject.getCurrentName(),
            verifiableCredential.credentialSubject.birthDate,
        )
    }
}
