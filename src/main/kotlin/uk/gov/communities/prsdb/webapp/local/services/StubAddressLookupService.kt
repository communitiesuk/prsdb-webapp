package uk.gov.communities.prsdb.webapp.local.services

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.services.AddressLookupService

@Service
@Profile("local & !use-os-places")
@Primary
class StubAddressLookupService : AddressLookupService {
    override fun searchByPostcode(postcode: String): List<String> =
        listOf("1 Example Road, $postcode", "2 Example Road, $postcode", "3 Example Road, $postcode")
}
