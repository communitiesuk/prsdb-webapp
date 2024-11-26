package uk.gov.communities.prsdb.webapp.services

import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.OneLoginClaimKeys
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedCredentialModel

@Service
class OneLoginIdentityService(
    private val decoderFactory: JwtDecoderFactory<Unit>,
) : IdentityService {
    override fun getVerifiedIdentityData(user: OidcUser): MutableMap<String, Any?>? {
        val idClaimString = user.claims[OneLoginClaimKeys.CORE_IDENTITY] as? String
        if (idClaimString != null) {
            val decoder = decoderFactory.createDecoder(Unit)
            val idClaimJwt = decoder.decode(idClaimString)

            val verifiableCredentialMap = idClaimJwt.claims["vc"] as? Map<*, *>
            val verifiableCredential = VerifiedCredentialModel.fromUnknownMap(verifiableCredentialMap)

            return mutableMapOf(
                "name" to verifiableCredential.credentialSubject.getCurrentName(),
                "birthDate" to verifiableCredential.credentialSubject.birthDate,
            )
        }

        return null
    }
}

interface IdentityService {
    fun getVerifiedIdentityData(user: OidcUser): MutableMap<String, Any?>?
}
