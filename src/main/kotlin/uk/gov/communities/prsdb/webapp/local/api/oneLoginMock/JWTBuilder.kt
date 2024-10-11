package uk.gov.communities.prsdb.webapp.local.api.oneLoginMock

import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.time.Instant
import java.util.Date

class JWTBuilder {
    companion object {
        val ecKey = generateSecretKey()

        private fun generateSecretKey(): ECKey {
            val ecKey =
                ECKeyGenerator(Curve.P_256)
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID("6a4bc1e3-9530-4d5b-90c5-10dcf3ffccd0")
                    .algorithm(Algorithm("ES256"))
                    .generate()

            return ecKey
        }
    }

    fun getIdToken(): String {
        val headerBuilder: JWSHeader.Builder =
            JWSHeader
                .Builder(JWSAlgorithm.ES256)
                .type(JOSEObjectType.JWT)
                .jwk(ecKey.toPublicJWK())

        val claimSetBBuilder: JWTClaimsSet.Builder =
            JWTClaimsSet
                .Builder()
                .subject("urn:fdc:gov.uk:2022:ABCDE")
                .audience("l0AE7SbEHrEa8QeQCGdml9KQ4bk")
                .issuer("http://localhost:8080/one-login-local/")
                .issueTime(Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .claim("vot", "Cl.Cm")
                .claim("nonce", LastReceivedNonce.nonce)
                .claim("vtm", "http://localhost:8080/one-login-local/trustmark")
                .claim("sid", "Nzk0M2NiNWUtYWZhNC00ZjZmLThiOTItNzUxNjcyNjUwOGNl")

        val signedJwt: SignedJWT = SignedJWT(headerBuilder.build(), claimSetBBuilder.build())

        signedJwt.sign(ECDSASigner(ecKey))

        return signedJwt.serialize()
    }
}
