package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import uk.gov.communities.prsdb.webapp.constants.OneLoginClaimKeys
import uk.gov.communities.prsdb.webapp.exceptions.VerifiedCredentialParsingException
import java.time.LocalDate

class OneLoginIdentityServiceTests {
    private lateinit var identityService: OneLoginIdentityService
    private lateinit var decoder: JwtDecoder
    private lateinit var decoderFactory: JwtDecoderFactory<Unit>
    private lateinit var user: OidcUser
    private lateinit var jwt: Jwt
    private val mockIdentityJwt = "mockIdentityJwt"

    @BeforeEach
    fun setup() {
        decoderFactory = mock()
        decoder = mock()
        user = mock()
        jwt = mock()
        identityService = OneLoginIdentityService(decoderFactory)

        whenever(decoderFactory.createDecoder(Unit)).thenReturn(decoder)

        whenever(decoder.decode(mockIdentityJwt)).thenReturn(jwt)
    }

    @Test
    fun `getVerifiedIdentityData returns null if the user claims do not have the core identity claim`() {
        // Arrange
        whenever(user.claims).thenReturn(mapOf())

        // Act
        val verifiedIdentityData = identityService.getVerifiedIdentityData(user)

        // Assert
        assertEquals(null, verifiedIdentityData)
    }

    @Test
    fun `getVerifiedIdentityData returns the correct map if the user claims have the core identity claim`() {
        // Arrange
        whenever(user.claims).thenReturn(mapOf(OneLoginClaimKeys.CORE_IDENTITY to mockIdentityJwt))

        val birthDate = LocalDate.now()
        val name = "name"
        val verifiedCredentialMap = buildVcMap(name, birthDate)
        whenever(jwt.claims).thenReturn(mapOf("vc" to verifiedCredentialMap))

        // Act
        val verifiedIdentityData = identityService.getVerifiedIdentityData(user)

        // Assert
        assertEquals(birthDate, verifiedIdentityData?.get("birthDate"))
        assertEquals(name, verifiedIdentityData?.get("name"))
    }

    @Test
    fun `getVerifiedIdentityData throws an exception if decoded the core identity claim is malformed`() {
        // Arrange
        whenever(user.claims).thenReturn(mapOf(OneLoginClaimKeys.CORE_IDENTITY to mockIdentityJwt))

        val verifiedCredentialMap = mapOf("key" to "value")
        whenever(jwt.claims).thenReturn(mapOf("vc" to verifiedCredentialMap))

        // Act & Assert
        assertThrows<VerifiedCredentialParsingException> { identityService.getVerifiedIdentityData(user) }
    }

    private fun buildVcMap(
        name: String,
        birthDate: LocalDate,
    ): Map<String, Any?> =
        mapOf(
            "type" to listOf("exampleType"),
            "credentialSubject" to
                mapOf(
                    "name" to
                        listOf(
                            mapOf("nameParts" to listOf(mapOf("value" to name, "type" to "GivenName"))),
                        ),
                    "birthDate" to listOf(mapOf("value" to birthDate.toString())),
                ),
        )
}
