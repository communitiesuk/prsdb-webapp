package uk.gov.communities.prsdb.webapp.exceptions

class EmailWasNotSentException(
    cause: Throwable,
) : PrsdbWebException("Notify has not sent that email, but retrying may work.", cause)
