package uk.gov.communities.prsdb.webapp.clients

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder

@Service
class EpcRegisterClient(
    @Qualifier("epc-client") private val client: RestClient,
) {
    fun getByUprn(uprn: Int): String {
        val uprnString = uprn.toString().padStart(12, '0')
        val query = URLEncoder.encode(uprnString, "UTF-8")
        return searchFor("uprn", "UPRN-$query")!!
    }

    fun getByRrn(rrn: String): String {
        val query = URLEncoder.encode(rrn, "UTF-8")
        return searchFor("rrn", query)!!
    }

    private fun searchFor(
        parameterName: String,
        parameterValue: String,
    ): String? =
        client
            .get()
            .uri(
                UriComponentsBuilder
                    .fromUriString("/api/prsdatabase/assessments/search")
                    .queryParam(parameterName, parameterValue)
                    .build()
                    .toUri(),
            ).retrieve()
            .body<String>()
}
