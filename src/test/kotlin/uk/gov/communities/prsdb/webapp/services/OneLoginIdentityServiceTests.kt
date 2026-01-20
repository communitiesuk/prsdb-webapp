package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import uk.gov.communities.prsdb.webapp.constants.OneLoginClaimKeys
import uk.gov.communities.prsdb.webapp.constants.VERIFIED_IDENTITY_CACHE_KEY
import uk.gov.communities.prsdb.webapp.exceptions.VerifiedCredentialParsingException
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedIdentityDataModel
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class OneLoginIdentityServiceTests {
    @Mock
    private lateinit var decoderFactory: JwtDecoderFactory<Unit>

    @Mock
    private lateinit var session: HttpSession

    @InjectMocks
    private lateinit var identityService: OneLoginIdentityService

    @Mock
    private lateinit var decoder: JwtDecoder

    @Mock
    private lateinit var user: OidcUser

    @Mock
    private lateinit var jwt: Jwt

    private val mockIdentityJwt = "mockIdentityJwt"

    @Test
    fun `getVerifiedIdentityData returns cached identity if it is present`() {
        // Arrange
        val verifiedIdentity = VerifiedIdentityDataModel("name", LocalDate.now())
        whenever(session.getAttribute(VERIFIED_IDENTITY_CACHE_KEY)).thenReturn(verifiedIdentity.toMap())

        // Act
        val returnedVerifiedIdentity = identityService.getVerifiedIdentityData(user)

        // Assert
        assertEquals(verifiedIdentity, returnedVerifiedIdentity)
    }

    @Test
    fun `getVerifiedIdentityData returns null if the user claims do not have the core identity claim`() {
        // Arrange
        whenever(session.getAttribute(VERIFIED_IDENTITY_CACHE_KEY)).thenReturn(null)

        whenever(user.claims).thenReturn(mapOf())

        // Act
        val returnedVerifiedIdentity = identityService.getVerifiedIdentityData(user)

        // Assert
        assertNull(returnedVerifiedIdentity)
    }

    @Test
    fun `getVerifiedIdentityData caches and returns the correct model if the user claims have the core identity claim`() {
        // Arrange
        whenever(session.getAttribute(VERIFIED_IDENTITY_CACHE_KEY)).thenReturn(null)

        val verifiedIdentity = VerifiedIdentityDataModel("name", LocalDate.now())
        val verifiedCredentialMap = buildVcMap(verifiedIdentity)
        whenever(user.claims).thenReturn(mapOf(OneLoginClaimKeys.CORE_IDENTITY to mockIdentityJwt))
        whenever(decoderFactory.createDecoder(Unit)).thenReturn(decoder)
        whenever(decoder.decode(mockIdentityJwt)).thenReturn(jwt)
        whenever(jwt.claims).thenReturn(mapOf("vc" to verifiedCredentialMap))

        // Act
        val returnedVerifiedIdentity = identityService.getVerifiedIdentityData(user)

        // Assert
        verify(session).setAttribute(VERIFIED_IDENTITY_CACHE_KEY, returnedVerifiedIdentity?.toMap())
        assertEquals(verifiedIdentity, returnedVerifiedIdentity)
    }

    @Test
    fun `getVerifiedIdentityData throws an exception if decoded the core identity claim is malformed`() {
        // Arrange
        whenever(session.getAttribute(VERIFIED_IDENTITY_CACHE_KEY)).thenReturn(null)

        whenever(user.claims).thenReturn(mapOf(OneLoginClaimKeys.CORE_IDENTITY to mockIdentityJwt))
        whenever(decoderFactory.createDecoder(Unit)).thenReturn(decoder)
        whenever(decoder.decode(mockIdentityJwt)).thenReturn(jwt)
        whenever(jwt.claims).thenReturn(mapOf("vc" to mapOf("invalidKey" to "invalidValue")))

        // Act & Assert
        assertThrows<VerifiedCredentialParsingException> { identityService.getVerifiedIdentityData(user) }
    }

    private fun buildVcMap(verifiedIdentity: VerifiedIdentityDataModel): Map<String, Any?> =
        mapOf(
            "type" to listOf("exampleType"),
            "credentialSubject" to
                mapOf(
                    "name" to listOf(mapOf("nameParts" to listOf(mapOf("value" to verifiedIdentity.name, "type" to "GivenName")))),
                    "birthDate" to listOf(mapOf("value" to verifiedIdentity.birthDate.toString())),
                ),
        )
}
