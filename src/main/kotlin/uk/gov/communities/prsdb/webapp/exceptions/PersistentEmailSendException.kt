package uk.gov.communities.prsdb.webapp.exceptions

class PersistentEmailSendException(
    message: String,
    cause: Throwable,
) : PrsdbWebException(message, cause)
