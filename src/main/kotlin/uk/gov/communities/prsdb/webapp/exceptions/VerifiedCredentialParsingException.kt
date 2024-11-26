package uk.gov.communities.prsdb.webapp.exceptions

class VerifiedCredentialParsingException(
    innerException: Exception,
) : Exception(
        "The verifiedCredential map returned in the IdentityJWT did not match the model of a verified credential. " +
            "See https://docs.sign-in.service.gov.uk/" +
            "integrate-with-integration-environment/prove-users-identity/" +
            "#understand-your-user-s-core-identity-claim",
        innerException,
    )
