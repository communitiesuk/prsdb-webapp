package uk.gov.communities.prsdb.webapp.exceptions

class TransientLetterSendException : PrsdbWebException {
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
}
