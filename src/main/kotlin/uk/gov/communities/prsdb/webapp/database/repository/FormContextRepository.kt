package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext

// The underscore tells JPA to access fields relating to the referenced table
@Suppress("ktlint:standard:function-naming")
interface FormContextRepository : JpaRepository<FormContext, Long?> {
    fun findByUser_IdAndJourneyType(
        principalName: String,
        journeyType: JourneyType,
    ): FormContext?

    fun findByIdAndUser_IdAndJourneyType(
        id: Long,
        principalName: String,
        journeyType: JourneyType,
    ): FormContext?

    fun countFormContextsByUser_IdAndJourneyType(
        principalName: String,
        journeyType: JourneyType,
    ): Int

    fun findAllByUser_IdAndJourneyType(
        principalName: String,
        journeyType: JourneyType,
    ): List<FormContext>
}
