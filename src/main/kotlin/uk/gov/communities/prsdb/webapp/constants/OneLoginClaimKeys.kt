package uk.gov.communities.prsdb.webapp.constants

class OneLoginClaimKeys {
    companion object {
        const val DOMAIN = "https://vocab.account.gov.uk"
        const val CORE_IDENTITY = "$DOMAIN/v1/coreIdentityJWT"
        const val RETURN_CODE = "$DOMAIN/v1/returnCode"
        const val ADDRESS = "$DOMAIN/v1/address"
    }
}
