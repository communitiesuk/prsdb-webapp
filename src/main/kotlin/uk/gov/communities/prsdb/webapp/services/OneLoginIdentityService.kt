package uk.gov.communities.prsdb.webapp.services

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import org.springframework.boot.configurationprocessor.json.JSONArray
import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import uk.gov.communities.prsdb.webapp.constants.OneLoginClaimKeys
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedCredentialModel

@Service
class OneLoginIdentityService : IdentityService {
    override fun getVerifiedIdentityData(user: OidcUser): MutableMap<String, Any?>? {
        val idClaimString = user.claims[OneLoginClaimKeys.CORE_IDENTITY] as? String
        if (idClaimString != null) {
            val decoder = idTokenDecoder()
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

    private fun idTokenDecoder(): JwtDecoder {
        val defaultClient = RestClient.create()
        val didResponseString =
            defaultClient
                .get()
                .uri(
                    "https://identity.integration.account.gov.uk/.well-known/did.json",
                ).retrieve()
                .body(String::class.java)
        val keyArray = JSONObject(didResponseString).getJSONArray("assertionMethod")
        val ecKeyList = keyArraySequence(keyArray).toList()

        val publicKeys = ecKeyList.map { mapJsonObjectToEcKey(it) }
        val processor = DefaultJWTProcessor<SecurityContext>()
        val keySelector = JWSVerificationKeySelector(JWSAlgorithm.ES256, ImmutableJWKSet(JWKSet(publicKeys)))
        processor.jwsKeySelector = keySelector
        return NimbusJwtDecoder(processor)
    }

    private fun keyArraySequence(keyArray: JSONArray) =
        sequence {
            for (index in 0 until keyArray.length()) {
                yield(keyArray.getJSONObject(index))
            }
        }

    private fun mapJsonObjectToEcKey(keyObject: JSONObject): ECKey {
        val jsonKey = keyObject.getJSONObject("publicKeyJwk")
        jsonKey.put("kid", keyObject.getString("id"))
        return ECKey.parse(jsonKey.toString())
    }
}

interface IdentityService {
    fun getVerifiedIdentityData(user: OidcUser): MutableMap<String, Any?>?
}
