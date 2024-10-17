package uk.gov.communities.prsdb.webapp.exceptions

class TransientEmailSentException(
    message: String,
    cause: Throwable,
) : PrsdbWebException(message, cause)
