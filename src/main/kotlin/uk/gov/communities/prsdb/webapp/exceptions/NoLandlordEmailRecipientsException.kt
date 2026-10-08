package uk.gov.communities.prsdb.webapp.exceptions

class NoLandlordEmailRecipientsException(
    landlordId: Long,
) : PrsdbWebException("No recipients found for landlord $landlordId")
