package uk.gov.communities.prsd.webapp.database.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.communities.prsd.webapp.database.entity.Landlord

interface LandlordRepository : CrudRepository<Landlord?, Long?>
