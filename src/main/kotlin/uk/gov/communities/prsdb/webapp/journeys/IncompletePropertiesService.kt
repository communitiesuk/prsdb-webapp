package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository

@PrsdbWebService
class IncompletePropertiesService(
    private val landlordRepository: LandlordRepository,
) {
    fun addIncompletePropertyToLandlord(state: SavedJourneyState) {
        landlordRepository.findByBaseUser_Id(state.user.id)?.let { landlord ->
            landlord.incompleteProperties.add(state)
            landlordRepository.save(landlord)
        }
    }
}
