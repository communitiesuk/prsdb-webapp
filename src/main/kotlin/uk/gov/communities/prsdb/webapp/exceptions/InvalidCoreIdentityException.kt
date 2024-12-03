package uk.gov.communities.prsdb.webapp.exceptions

import org.springframework.security.oauth2.jwt.JwtException

class InvalidCoreIdentityException(
    innerException: JwtException,
) : Exception(innerException.message, innerException)
