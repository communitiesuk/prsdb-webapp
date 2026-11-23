package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.SuperiorLandlord
import uk.gov.communities.prsdb.webapp.database.repository.SuperiorLandlordRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel

@PrsdbWebService
class SuperiorLandlordService(
    private val superiorLandlordRepository: SuperiorLandlordRepository,
) {
    // Updates the saved superior landlords to match the list the user submitted, creating, updating
    // and deleting them one at a time.
    // Returns the newly added superior landlords so they can be tracked by the journey and notified.
    // TODO PDJB-2171: also return updated and removed superior landlords so they can be tracked by
    //  the journey and notified.
    @Transactional
    fun updateSuperiorLandlords(
        propertyOwnership: PropertyOwnership,
        superiorLandlords: List<SuperiorLandlordDataModel>,
    ): List<SuperiorLandlordDataModel> {
        val existingSuperiorLandlordsKeyedById =
            superiorLandlordRepository.findAllByPropertyOwnership_Id(propertyOwnership.id).associateBy { it.id }
        val submittedIds = superiorLandlords.mapNotNull { it.databaseId }.toSet()

        existingSuperiorLandlordsKeyedById.values.filter { it.id !in submittedIds }.forEach { removeSuperiorLandlord(it) }

        val added = mutableListOf<SuperiorLandlordDataModel>()

        superiorLandlords.forEach { submitted ->
            // An id that no longer matches a saved row means it was deleted elsewhere, so treat it as
            // a new superior landlord rather than losing the user's answers.
            val existingSuperiorLandlord = submitted.databaseId?.let { existingSuperiorLandlordsKeyedById[it] }
            when {
                existingSuperiorLandlord == null -> {
                    createSuperiorLandlord(propertyOwnership, submitted)
                    added.add(submitted)
                }
                detailsHaveChanged(existingSuperiorLandlord, submitted) ->
                    updateSuperiorLandlord(existingSuperiorLandlord, submitted)
                // An unchanged superior landlord matches no branch, so their row is left alone.
            }
        }

        return added
    }

    // Postal addresses aren't stored, so an address in the submitted model means the user entered one in this journey
    private fun detailsHaveChanged(
        existing: SuperiorLandlord,
        submitted: SuperiorLandlordDataModel,
    ): Boolean =
        existing.name != submitted.name ||
            existing.email != submitted.email ||
            submitted.address != null

    @Transactional
    fun createSuperiorLandlords(
        propertyOwnership: PropertyOwnership,
        superiorLandlords: List<SuperiorLandlordDataModel>,
    ) {
        superiorLandlords.forEach { createSuperiorLandlord(propertyOwnership, it) }
    }

    @Transactional
    fun createSuperiorLandlord(
        propertyOwnership: PropertyOwnership,
        superiorLandlord: SuperiorLandlordDataModel,
    ) {
        superiorLandlordRepository.save(
            SuperiorLandlord(
                propertyOwnership = propertyOwnership,
                name = superiorLandlord.name,
                email = superiorLandlord.email,
            ),
        )
    }

    @Transactional
    fun updateSuperiorLandlord(
        superiorLandlord: SuperiorLandlord,
        updatedDetails: SuperiorLandlordDataModel,
    ) {
        superiorLandlord.name = updatedDetails.name
        superiorLandlord.email = updatedDetails.email
        superiorLandlordRepository.save(superiorLandlord)
    }

    @Transactional
    fun removeSuperiorLandlord(superiorLandlord: SuperiorLandlord) {
        superiorLandlordRepository.delete(superiorLandlord)
    }

    @Transactional
    fun clearSuperiorLandlords(propertyOwnership: PropertyOwnership) {
        superiorLandlordRepository.deleteByPropertyOwnership(propertyOwnership)
    }
}
