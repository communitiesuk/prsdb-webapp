package uk.gov.communities.prsdb.webapp.exceptions

class CannotSendEmailsException(
    message: String,
    cause: Throwable,
) : PrsdbWebException(message, cause)
