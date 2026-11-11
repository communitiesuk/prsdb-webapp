package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.bodyBytes
import io.gatling.javaapi.core.CoreDsl.bodyString
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.core.CoreDsl.repeat
import io.gatling.javaapi.core.Session
import io.gatling.javaapi.http.HttpDsl.RawFileBodyPart
import io.gatling.javaapi.http.HttpDsl.StringBodyPart
import io.gatling.javaapi.http.HttpDsl.header
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Duration

object RegistrationJourney {
    val requiredRequests =
        listOf("Registration: durable confirmation", "Registration: persisted property", "Registration: persisted compliance")

    fun chain(
        config: BasicRunConfig,
        fixture: RegistrationFixture,
        variant: RegistrationVariant,
        paymentBaseUrl: String,
        certificatePath: String?,
        prefix: String = "",
        thinkTime: Duration = config.thinkTime,
    ): ChainBuilder {
        val paymentBase = PaymentJourney.baseUrl(paymentBaseUrl, config.target, config.baseUrl)
        val certificateDigest = certificatePath?.let { sha256(Files.readAllBytes(Path.of(it))) }
        val expectedAddress =
            if (variant.manual) "NFT registration ${fixture.uprn}, Performance Town, ${fixture.postcode}" else fixture.address
        val formSelector = "form[method=post]"
        val post =
            http { session -> "${prefix}Registration: POST ${step(session)}" }
                .post("#{regAction}").disableFollowRedirect()
                .formParamMap { session -> session.get<Map<String, String>>("regFields")!! }
                .check(status().`is`(302)).check(header("Location").saveAs("regRedirect"))
        val upload =
            if (certificatePath != null) {
                http { session -> "${prefix}Registration: POST ${step(session)}" }
                    .post("#{regAction}").disableFollowRedirect()
                    .bodyPart(StringBodyPart("_csrf", "#{regCsrf}"))
                    .bodyPart(
                        RawFileBodyPart(
                            "certificate",
                            certificatePath,
                        ).fileName("performance-certificate.pdf").contentType("application/pdf"),
                    )
                    .asMultipartForm()
                    .check(status().`is`(302)).check(header("Location").saveAs("regRedirect"))
            } else {
                null
            }

        return exec { session ->
            session.setAll(
                mapOf(
                    "regNext" to "${config.baseUrl}${RegistrationDestination.ROUTE}/task-list",
                    "regDone" to false, "regPost" to false, "regUpload" to false,
                    "regScanPolls" to 0, "regScannedDownloads" to 0, "regScanPending" to false, "regPendingPayments" to 0,
                    "regVisited" to emptySet<String>(),
                ),
            )
        }.exec(
            repeat(320, "regCount").on(
                exec { session ->
                    if (session.getBoolean("regDone")) {
                        session
                    } else {
                        session.removeAll(
                            "regRedirect", "regActions", "regHiddenNames", "regHiddenValues", "regTaskLinks", "regInputNames",
                            "regCookies", "regErrors", "regContentType", "regDownloads",
                        ).set(
                            "regCurrent",
                            session.getString("regNext"),
                        ).set("regPost", false).set("regUpload", false).set("regScanPending", false)
                    }
                }.doIf { session -> !session.getBoolean("regDone") }.then(
                    exec(
                        http { session -> "${prefix}Registration: GET ${step(session)}" }
                            .get("#{regCurrent}").disableFollowRedirect()
                            .check(status().`in`(200, 302).saveAs("regStatus"))
                            .check(header("Location").optional().saveAs("regRedirect"))
                            .check(header("Set-Cookie").findAll().optional().saveAs("regCookies"))
                            .check(header("Content-Type").optional().saveAs("regContentType"))
                            .check(bodyString().saveAs("regBody"))
                            .check(css(".govuk-error-summary").optional().saveAs("regErrors"))
                            .check(css(formSelector, "action").findAll().optional().saveAs("regActions"))
                            .check(css("$formSelector input[type=hidden]", "name").findAll().optional().saveAs("regHiddenNames"))
                            .check(css("$formSelector input[type=hidden]", "value").findAll().optional().saveAs("regHiddenValues"))
                            .check(
                                css("$formSelector input[name], $formSelector button[name], $formSelector select[name]", "name")
                                    .findAll().optional().saveAs("regInputNames"),
                            )
                            .check(css("a.govuk-task-list__link", "href").findAll().optional().saveAs("regTaskLinks"))
                            .check(css(HttpDestination.CERTIFICATE_LINK_SELECTOR, "href").findAll().optional().saveAs("regDownloads")),
                    ).exitHereIfFailed()
                        .exec { session ->
                            prepare(session, config, fixture, variant, expectedAddress, certificatePath)
                        }
                        .exitHereIfFailed()
                        .doIf { session -> session.getBoolean("regScanPending") }.then(exec { it }.pause(Duration.ofMillis(250)))
                        .doIf { session -> isUploadCheck(step(session)) && !session.getBoolean("regScanPending") }.then(
                            exec(
                                http("${prefix}Registration: scanned certificate download")
                                    .get { session ->
                                        HttpDestination.certificate(
                                            session.getList<String>("regDownloads").single(),
                                            config.baseUrl,
                                            config.certificateDownloadOrigin,
                                        )
                                    }
                                    .ignoreProtocolHeaders().disableUrlEncoding().disableFollowRedirect().check(status().`is`(200))
                                    .check(
                                        header("Content-Type").transform { it.substringBefore(';').trim().lowercase() }
                                            .`in`("application/pdf", "application/octet-stream"),
                                    )
                                    .check(bodyBytes().transform { sha256(it) }.`is`(certificateDigest ?: "no-certificate")),
                            )
                                .exitHereIfFailed().exec { session ->
                                    session.set("regScannedDownloads", session.getInt("regScannedDownloads") + 1)
                                },
                        )
                        .doIf { session -> step(session) == "payment-return" && session.getInt("regStatus") == 200 }.then(
                            exec { session ->
                                require(
                                    session.getString("regBody").orEmpty().contains("payment-status-no-js-content"),
                                ) { "Unexpected payment processing page" }
                                session.set("regPendingPayments", session.getInt("regPendingPayments") + 1)
                            }.exitHereIfFailed()
                                .exec(
                                    http { session ->
                                        if (session.getInt("regPendingPayments") == 1) {
                                            "${prefix}Registration: pending payment did not register"
                                        } else {
                                            "${prefix}Registration: repeated pending payment still unregistered"
                                        }
                                    }
                                        .get { HttpDestination.withCacheBuster("${config.baseUrl}/landlord/landlord-details") }
                                        .disableFollowRedirect().check(status().`is`(200))
                                        .check(bodyString().transform { html -> !html.contains(expectedAddress) }.`is`(true)),
                                )
                                .exitHereIfFailed().pause(Duration.ofMillis(250)),
                        )
                        .doIf { session -> session.getBoolean("regPost") && !session.getBoolean("regScanPending") }.then(
                            exec { it }.pause(thinkTime),
                        )
                        .doIf { session ->
                            session.getBoolean("regPost") && !session.getBoolean("regUpload")
                        }.then(exec(post).exitHereIfFailed())
                        .doIf { session -> session.getBoolean("regUpload") }.then(
                            if (upload != null) exec(upload).exitHereIfFailed() else exec { it.markAsFailed() },
                        )
                        .exec { session ->
                            if (!session.getBoolean("regPost")) {
                                session
                            } else {
                                val location = requireNotNull(session.getString("regRedirect"))
                                val destination =
                                    if (step(session) == "payment-summary") {
                                        PaymentJourney.hostedUrl(location, paymentBase)
                                    } else {
                                        RegistrationDestination.resolve(
                                            location,
                                            config.baseUrl,
                                            session.getString("regJourneyId"),
                                            session.getString("regCurrent"),
                                        )
                                    }
                                session.set("regNext", destination)
                            }
                        }.exitHereIfFailed(),
                ),
            ),
        ).exec { session ->
            require(session.getBoolean("regDone")) { "Registration exceeded bounded navigation budget" }
            val visited = session.get<Set<String>>("regVisited")!!
            val expectedDownloads =
                (if (variant.gas == "upload") 1 else 0) +
                    (if (variant.electrical.endsWith("upload")) 1 else 0)
            require(session.getInt("regScannedDownloads") == expectedDownloads) { "Real scanner and safe-file downloads were not verified" }
            require(
                setOf(
                    "property-type", "number-of-bedrooms", "correspondence-email", "occupancy", "licensing-type",
                    "check-gas-safety-answers", "check-electrical-safety-answers", "check-epc-answers", "check-answers", "payment-summary",
                )
                    .all { required -> visited.any { it.substringAfterLast('/') == required } },
            ) { "Registration skipped a required task: $visited" }
            session
        }.exitHereIfFailed()
            .exec(
                http("$prefix${requiredRequests[0]}").get {
                    HttpDestination.withCacheBuster("${config.baseUrl}${RegistrationDestination.ROUTE}/confirmation")
                }
                    .disableFollowRedirect().check(status().`is`(200))
                    .check(css(".govuk-panel h1").exists())
                    .check(css("strong").findAll().transform { values -> values.any { it.matches(Regex("P-[A-Z0-9-]+")) } }.`is`(true))
                    .check(
                        css("main").transform {
                            it.contains(fixture.postcode) &&
                                it.contains(
                                    if (variant.manual) "NFT registration ${fixture.uprn}" else fixture.address.substringBefore(','),
                                )
                        }.`is`(true),
                    ),
            ).exitHereIfFailed()
            .exec(
                http("${prefix}Registration: find registered property").get {
                    HttpDestination.withCacheBuster("${config.baseUrl}/landlord/landlord-details")
                }
                    .disableFollowRedirect().check(status().`is`(200))
                    .check(
                        bodyString().transform { html ->
                            val address = if (variant.manual) "NFT registration ${fixture.uprn}" else fixture.address.substringBefore(',')
                            val rows = Regex("(?s)<tr\\b.*?</tr>").findAll(html).map { it.value }
                            val row =
                                rows.firstOrNull { it.contains(address) }
                                    ?: error("Registered address was not persisted in landlord properties")
                            val href =
                                Regex("href=\"([^\"]*/property-details/[0-9]+(?:\\?withBackUrl=[0-9]+)?)\"").find(row)?.groupValues?.get(1)
                                    ?: error("Persisted property link was not found")
                            HttpDestination.resolve(href, config.baseUrl).also { resolved ->
                                require(URI.create(resolved).path.matches(Regex("/landlord/property-details/[0-9]+")))
                            }
                        }.saveAs("regPropertyUrl"),
                    ),
            ).exitHereIfFailed()
            .exec(
                http("$prefix${requiredRequests[1]}").get { session ->
                    HttpDestination.withCacheBuster(requireNotNull(session.getString("regPropertyUrl")))
                }
                    .disableFollowRedirect().check(status().`is`(200))
                    .check(
                        css(".govuk-summary-list__row").findAll().transform { rows ->
                            rows.any { it.contains("Number of bedrooms") && it.contains("3") } &&
                                (
                                    variant.licensing == "PROVIDE_LATER" ||
                                        rows.any { it.contains("Licensing type") && it.contains(licensingLabel(variant.licensing)) }
                                ) &&
                                rows.any { it.contains("Contact address") && it.contains("NFT correspondence ${fixture.uprn}") } &&
                                rows.any {
                                    it.contains("Is this property occupied by tenants?") &&
                                        it.contains(if (variant.occupied) "Yes" else "No")
                                } &&
                                (
                                    !variant.occupied || variant.gas == "later" ||
                                        (
                                            rows.any { it.contains("Number of households") && it.contains("1") } &&
                                                rows.any { it.contains("Number of tenants") && it.contains("2") } &&
                                                rows.any { it.contains("Rent amount") && it.contains("£950.00") }
                                        )
                                )
                        }.`is`(true),
                    ),
            ).exitHereIfFailed()
            .exec(
                http("$prefix${requiredRequests[2]}").get { session ->
                    HttpDestination.withCacheBuster(requireNotNull(session.getString("regPropertyUrl")))
                }
                    .disableFollowRedirect().check(status().`is`(200))
                    .check(css("h1").exists()).check(css(".govuk-error-summary").notExists())
                    .check(
                        css(".govuk-summary-card").findAll().transform { cards ->
                            persistedCompliance(cards, variant, fixture)
                        }.`is`(true),
                    ),
            ).exitHereIfFailed()
    }

    private fun prepare(
        session: Session,
        config: BasicRunConfig,
        fixture: RegistrationFixture,
        variant: RegistrationVariant,
        expectedAddress: String,
        certificatePath: String?,
    ): Session {
        val current = requireNotNull(session.getString("regCurrent"))
        val currentStep = step(session)
        val existingJourney = session.getString("regJourneyId")
        val visited =
            session.get<Set<String>>("regVisited").orEmpty() + URI.create(current).path.removePrefix("${RegistrationDestination.ROUTE}/")
        var result = session.set("regVisited", visited)
        if (session.getInt("regStatus") == 302) {
            val destination =
                RegistrationDestination.resolve(
                    requireNotNull(session.getString("regRedirect")),
                    config.baseUrl,
                    existingJourney,
                    current,
                )
            return result.set("regNext", destination).let { next ->
                if (existingJourney == null) {
                    next.set(
                        "regJourneyId",
                        requireNotNull(RegistrationDestination.journeyId(destination)),
                    )
                } else {
                    next
                }
            }
        }
        require(
            session.getString("regContentType").orEmpty().substringBefore(';').trim() == "text/html",
        ) { "Registration did not return HTML" }
        require(!session.contains("regErrors")) { "Registration validation error at $current" }
        if (currentStep == "confirmation") {
            require(session.getString("regBody").orEmpty().contains(fixture.postcode)) { "Confirmation shows a different property" }
            return result.set("regDone", true)
        }
        require(currentStep != "payment-failed") { "Registration payment failed" }
        if (isUploadCheck(currentStep)) {
            require(session.getString("regBody").orEmpty().contains("performance-certificate.pdf")) { "Uploaded certificate is missing" }
            if (!session.contains("regDownloads")) {
                require(
                    session.getInt("regScanPolls") < 200,
                ) { "Actual local scan-processor did not produce a safe downloadable certificate" }
                return result.set("regScanPending", true).set("regScanPolls", session.getInt("regScanPolls") + 1).set("regNext", current)
            }
        }
        if (currentStep == "task-list") {
            val links = session.getList<String>("regTaskLinks")
            val next =
                links.firstOrNull { link ->
                    URI.create(HttpDestination.resolve(link, config.baseUrl)).path
                        .removePrefix("${RegistrationDestination.ROUTE}/") !in visited
                } ?: error("No incomplete registration task remains")
            val destination = RegistrationDestination.resolve(next, config.baseUrl, existingJourney, current)
            result = result.set("regNext", destination)
            return if (existingJourney == null) {
                result.set(
                    "regJourneyId",
                    requireNotNull(RegistrationDestination.journeyId(destination)),
                )
            } else {
                result
            }
        }
        require(existingJourney != null) { "No journey ID was correlated before mutation" }
        val actions = session.getList<String>("regActions")
        require(actions.size == 1) { "Expected one registration form at $current" }
        val action = RegistrationDestination.resolve(actions.single(), config.baseUrl, existingJourney, current)
        require(URI.create(action).path == URI.create(current).path) { "Registration form posts to a different step" }
        val hidden = RegistrationDestination.hiddenFields(session.getList("regHiddenNames"), session.getList("regHiddenValues"))
        val fields = fields(current, fixture, variant, expectedAddress)
        val inputNames = session.getList<String>("regInputNames").toSet()
        require(fields.keys.all { it in inputNames }) { "Unexpected fields at $currentStep: ${fields.keys - inputNames}" }
        val upload = currentStep.endsWith("file-upload")
        if (upload) {
            require(certificatePath != null) { "gatling.registration.certificatePath is required for real multipart certificates" }
            require(hidden.keys == setOf("_csrf")) { "Unexpected multipart hidden fields; refusing to discard them" }
            require(session.getList<String>("regCookies").any { isUploadCookie(it, URI.create(current).path) }) {
                "A correlated one-use upload cookie is required"
            }
        }
        return result.set("regAction", action).set("regFields", hidden + fields).set("regCsrf", hidden.getValue("_csrf"))
            .set("regPost", true).set("regUpload", upload)
    }

    private fun fields(
        current: String,
        fixture: RegistrationFixture,
        variant: RegistrationVariant,
        expectedAddress: String,
    ): Map<String, String> {
        val step = URI.create(current).path.substringAfterLast('/')
        val correspondence = URI.create(current).path.contains("/correspondence-address/")
        ComplianceJourney.fields(step, variant, fixture)?.let { return it }
        return when (step) {
            "lookup-address" -> mapOf("postcode" to fixture.postcode, "houseNameOrNumber" to fixture.building)
            "select-address" -> mapOf("address" to if (correspondence || variant.manual) "MANUAL" else fixture.address)
            "manual-address" ->
                mapOf(
                    "addressLineOne" to if (correspondence) "NFT correspondence ${fixture.uprn}" else expectedAddress.substringBefore(','),
                    "townOrCity" to "Performance Town",
                    "postcode" to fixture.postcode,
                )

            "local-council" -> mapOf("localCouncilId" to fixture.localCouncilId.toString())
            "property-type" -> mapOf("propertyType" to "DETACHED_HOUSE")
            "number-of-bedrooms" -> mapOf("numberOfBedrooms" to "3")
            "ownership-type" -> mapOf("ownershipType" to "FREEHOLD")
            "has-joint-landlords" -> mapOf("hasJointLandlords" to "false")
            "owns-property" -> mapOf("ownsProperty" to "true")
            "ownership-kind" -> mapOf("ownershipKind" to "FREEHOLD_ONLY")
            "freehold-type" -> mapOf("freeholdType" to "SOLE_OWNERSHIP")
            "property-manager" -> mapOf("usingPropertyManager" to "false")
            "correspondence-email" -> mapOf("correspondenceEmailOption" to "ACCOUNT_EMAIL")
            "occupancy" -> mapOf("occupied" to variant.occupied.toString())
            "who-provides-rental-details" -> mapOf("whoProvides" to "LANDLORD")
            "licensing-type" -> mapOf("licensingType" to variant.licensing)
            "selective-licence" -> mapOf("licenceNumber" to "NFT-${fixture.uprn}")
            "number-of-households" -> mapOf("numberOfHouseholds" to "1")

            "number-of-people" -> mapOf("numberOfPeople" to "2")
            "rent-includes-bills" -> mapOf("rentIncludesBills" to "false")
            "property-furnished" -> mapOf("furnishedStatus" to "FURNISHED")
            "rent-frequency" -> mapOf("rentFrequency" to "MONTHLY")
            "rent-amount" -> mapOf("rentAmount" to "950")
            "confirm-missing-compliance" -> mapOf("wantsToProceed" to "true")
            "check-answers", "payment-summary", "payment-return",
            "gas-safety-certificate-file-upload", "eicr-file-upload", "no-address-found",
            -> emptyMap()

            else -> error("Unimplemented registration step: ${URI.create(current).path}")
        }
    }

    private fun step(session: Session): String = URI.create(requireNotNull(session.getString("regCurrent"))).path.substringAfterLast('/')

    internal fun isUploadCheck(step: String) = step in setOf("check-gas-safety-certificate-uploads", "check-eicr-uploads")

    // Mirrors TokenCookieService: one-use token scoped to the exact upload step path, always HttpOnly and Secure.
    internal fun isUploadCookie(
        header: String,
        uploadPath: String,
    ): Boolean {
        val parts = header.split(';').map { it.trim() }
        val attributes = parts.drop(1).map { it.substringBefore('=').lowercase() to it.substringAfter('=', "") }
        return parts.first().matches(Regex("file-upload-cookie=[A-Za-z0-9-]+")) &&
            attributes.any { it.first == "httponly" } && attributes.any { it.first == "secure" } &&
            attributes.filter { it.first == "path" }.map { it.second } == listOf(uploadPath)
    }

    internal fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance(
            "SHA-256",
        ).digest(bytes).joinToString("") { "%02x".format(it) }

    private fun licensingLabel(value: String): String =
        when (value) {
            "NO_LICENSING" -> "None"
            "SELECTIVE_LICENCE" -> "Selective licence"
            "HMO_MANDATORY_LICENCE" -> "HMO mandatory licence"
            "HMO_ADDITIONAL_LICENCE" -> "HMO additional licence"
            else -> "later"
        }

    internal const val GAS_CARD = "Gas safety certificate"
    internal const val ELECTRICAL_CARD = "Electrical safety certificate"
    internal const val EPC_CARD = "Energy performance certificate (EPC)"

    // Checks every compliance card, or only the named card after a single certificate update.
    internal fun persistedCompliance(
        cards: List<String>,
        variant: RegistrationVariant,
        fixture: RegistrationFixture?,
        only: String? = null,
    ): Boolean {
        fun card(title: String) =
            cards.singleOrNull { it.contains(title) }.orEmpty().replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ")

        val titles = only?.let { listOf(it) } ?: listOf(GAS_CARD, ELECTRICAL_CARD, EPC_CARD)
        return titles.all { title ->
            val text = card(title)
            text.isNotBlank() && expectedCard(title, variant, fixture).all { text.contains(it, ignoreCase = true) }
        }
    }

    private fun expectedCard(
        title: String,
        variant: RegistrationVariant,
        fixture: RegistrationFixture?,
    ): List<String> {
        if (title == GAS_CARD) {
            return when (variant.gas) {
                "none" -> listOf("Does the property have a gas supply or any gas appliances?", "No")
                "upload" -> listOf("Valid", "performance-certificate.pdf")
                else -> listOf("Do you have a gas safety certificate for this property?", "No")
            }
        }
        if (title == ELECTRICAL_CARD) {
            return when {
                variant.electrical.endsWith(
                    "upload",
                ) -> listOf("Valid", "performance-certificate.pdf", if (variant.electrical.startsWith("eicr")) "(EICR)" else "(EIC)")

                !variant.occupied -> listOf("Provide this later")
                else -> listOf("None")
            }
        }
        require(title == EPC_CARD) { "Unknown compliance card $title" }

        fun number() = ComplianceJourney.certificateNumber(requireNotNull(fixture) { "EPC card checks require a fixture" }, variant)
        return when (variant.epc) {
            "valid" -> listOf(number(), "Valid")
            "exempt" -> listOf("less than 4 months a year")
            else -> listOf("Do you have an EPC for this property?", "No")
        }
    }
}
