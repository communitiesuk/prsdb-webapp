package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperty
import uk.gov.communities.prsdb.webapp.database.entity.Payment

interface PaymentRepository : JpaRepository<Payment, String> {
    fun findAllByAssociatedIncompleteProperty(associatedIncompleteProperty: LandlordIncompleteProperty): List<Payment>
}
