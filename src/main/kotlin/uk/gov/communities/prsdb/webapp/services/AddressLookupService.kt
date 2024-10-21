package uk.gov.communities.prsdb.webapp.services

interface AddressLookupService {
    fun searchByPostcode(postcode: String): List<String>
}
