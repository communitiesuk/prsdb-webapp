package uk.gov.communities.prsdb.webapp.performance

/**
 * Resolves the base URL that Gatling simulations target.
 *
 * Pass -Dgatling.baseUrl=https://your-nft-host when running against a deployed environment.
 * Defaults to the local webapp so simulations work out of the box during local development.
 */
object BaseUrlConfig {
    const val DEFAULT_BASE_URL = "http://localhost:8080"

    val baseUrl: String = System.getProperty("gatling.baseUrl", DEFAULT_BASE_URL)
}
