package uk.gov.communities.prsdb.webapp.testHelpers.builders

class JourneyPageDataBuilder {
    companion object {
        fun beforeLandlordDetailsUpdateSelectAddress() = JourneyDataBuilder().withLookupAddress()
    }
}
