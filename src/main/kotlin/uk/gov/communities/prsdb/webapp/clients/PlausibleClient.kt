package uk.gov.communities.prsdb.webapp.clients

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.models.dataModels.plausible.PlausibleQuery
import uk.gov.communities.prsdb.webapp.models.dataModels.plausible.PlausibleQueryResponse

@PrsdbWebService
class PlausibleClient(
    @Qualifier("plausible-stats-client") private val client: RestClient,
) {
    fun query(query: PlausibleQuery): PlausibleQueryResponse =
        client
            .post()
            .uri("/api/v2/query")
            .body(query)
            .retrieve()
            .body<PlausibleQueryResponse>()
            ?: PlausibleQueryResponse()
}
