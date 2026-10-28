package uk.gov.communities.prsdb.webapp.exceptions

class OrganisationalLandlordUserMismatchException : PrsdbWebException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
