package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository

@JourneyFrameworkComponent
class IncompletePropertyCreatingNavigationalStepConfig(
    private val landlordRepository: LandlordRepository,
) : NavigationalStepConfig() {
    override fun afterSaveState(
        state: JourneyState,
        saveStateId: SavedJourneyState,
    ) {
        super.afterSaveState(state, saveStateId)
        landlordRepository.findByBaseUser_Id(saveStateId.user.id)?.let { landlord ->
            landlord.incompleteProperties.add(saveStateId)
            landlordRepository.save(landlord)
        }
    }
}

@JourneyFrameworkComponent
class IncompletePropertyCreatingNavigationalStep(
    config: IncompletePropertyCreatingNavigationalStepConfig,
) : NavigationalStep(config)
