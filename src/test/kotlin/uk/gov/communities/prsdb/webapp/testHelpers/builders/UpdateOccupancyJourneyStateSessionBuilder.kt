package uk.gov.communities.prsdb.webapp.testHelpers.builders

class UpdateOccupancyJourneyStateSessionBuilder :
    JourneyStateSessionBuilder<UpdateOccupancyJourneyStateSessionBuilder>(),
    OccupancyStateBuilder<UpdateOccupancyJourneyStateSessionBuilder> {
    companion object {
        fun withNoTenants() = UpdateOccupancyJourneyStateSessionBuilder().withNoTenants()

        fun withTenants() = UpdateOccupancyJourneyStateSessionBuilder().withTenants()
    }
}
