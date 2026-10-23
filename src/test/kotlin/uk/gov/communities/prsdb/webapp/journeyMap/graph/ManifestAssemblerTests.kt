package uk.gov.communities.prsdb.webapp.journeyMap.graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedContainer
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedDestination
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedParentage
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedStep
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectionResponse
import uk.gov.communities.prsdb.webapp.journeyMap.model.EntryRecord
import uk.gov.communities.prsdb.webapp.journeyMap.model.Exploration
import uk.gov.communities.prsdb.webapp.journeyMap.model.JourneyInfo
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestArrow
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestContainer
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestEdge
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestEntry
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestNode
import uk.gov.communities.prsdb.webapp.journeyMap.model.PageRecord
import uk.gov.communities.prsdb.webapp.journeyMap.model.Transition
import uk.gov.communities.prsdb.webapp.journeyMap.model.VariantRecord
import uk.gov.communities.prsdb.webapp.journeyMap.model.ViaStep

class ManifestAssemblerTests {
    private val assembler = ManifestAssembler()
    private val journey = JourneyInfo("test", "Test journey", "/test", null, "now", emptyMap())

    @Test
    fun `observed transitions that pass through task exits become direct edges`() {
        val structure =
            structure(
                step("privacy-notice"),
                step("exit:task:IdentityTask", requestable = false, exit = true),
                step("email"),
            )
        val exploration =
            exploration(
                structure,
                pages = listOf(page("privacy-notice"), page("email")),
                transitions =
                    listOf(
                        Transition("privacy-notice", "email", "Agree", "COMPLETE", listOf(ViaStep("exit:task:IdentityTask", "COMPLETE"))),
                    ),
            )

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(listOf(ManifestEdge("privacy-notice", "email", "", "COMPLETE", ManifestEdge.OBSERVED)), manifest.edges)
        assertTrue(manifest.nodes.none { it.id.startsWith("exit:") })
    }

    @Test
    fun `internal decision steps passed through become decision nodes with outcome labelled edges`() {
        val decisionId = "internal:CheckUserRoleStep"
        val structure =
            structure(
                step("privacy-notice"),
                step(decisionId, requestable = false, stepClass = "CheckUserRoleStep", modeValues = listOf("LANDLORD", "NOT_A_LANDLORD")),
                step("email"),
            )
        val exploration =
            exploration(
                structure,
                pages = listOf(page("privacy-notice"), page("email")),
                transitions = listOf(Transition("privacy-notice", "email", "Agree", null, listOf(ViaStep(decisionId, "NOT_A_LANDLORD")))),
            )

        val manifest = assembler.assemble(journey, exploration)

        val decision = manifest.nodes.single { it.id == decisionId }
        assertEquals(ManifestNode.DECISION, decision.kind)
        assertEquals("Check user role", decision.title)
        assertEquals(
            listOf(
                ManifestEdge("privacy-notice", decisionId, "", null, ManifestEdge.OBSERVED),
                ManifestEdge(decisionId, "email", "Not a landlord", "NOT_A_LANDLORD", ManifestEdge.OBSERVED),
            ),
            manifest.edges,
        )
    }

    @Test
    fun `requestable steps that were never visited appear without variants, joined by labelled structural edges`() {
        val structure =
            structure(
                step("landlord-type"),
                step("organisation-name", parentage = IntrospectedParentage(IntrospectedParentage.SINGLE, "landlord-type", "ORGANISATION")),
            )
        val exploration = exploration(structure, pages = listOf(page("landlord-type")), transitions = emptyList())

        val manifest = assembler.assemble(journey, exploration)

        val unvisited = manifest.nodes.single { it.id == "organisation-name" }
        assertTrue(unvisited.variants.isEmpty())
        assertEquals(
            listOf(ManifestEdge("landlord-type", "organisation-name", "Organisation", "ORGANISATION", ManifestEdge.STRUCTURAL)),
            manifest.edges,
        )
    }

    @Test
    fun `via steps with a single mode are passed straight through`() {
        val passThroughId = "internal:FinishCyaJourneyStep"
        val structure =
            structure(
                step("email"),
                step(passThroughId, requestable = false, stepClass = "FinishCyaJourneyStep", modeValues = listOf("COMPLETE")),
                step("check-answers"),
            )
        val exploration =
            exploration(
                structure,
                pages = listOf(page("email"), page("check-answers")),
                transitions = listOf(Transition("email", "check-answers", "", "COMPLETE", listOf(ViaStep(passThroughId, "COMPLETE")))),
            )

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(listOf(ManifestEdge("email", "check-answers", "", "COMPLETE", ManifestEdge.OBSERVED)), manifest.edges)
        assertTrue(manifest.nodes.none { it.id == passThroughId })
    }

    @Test
    fun `unvisited redirecting steps are not added as nodes`() {
        val structure = structure(step("landlord-type"), step("task-route", redirecting = true))
        val exploration = exploration(structure, pages = listOf(page("landlord-type")), transitions = emptyList())

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(listOf("landlord-type"), manifest.nodes.map { it.id })
    }

    @Test
    fun `edges between the same nodes are merged, and a node with a single destination has unlabelled edges`() {
        val structure = structure(step("property-type"), step("bedrooms"), step("select-address"), step("manual-address"))
        val exploration =
            exploration(
                structure,
                pages = listOf(page("property-type"), page("bedrooms"), page("select-address"), page("manual-address")),
                transitions =
                    listOf(
                        Transition("property-type", "bedrooms", "Detached house", "COMPLETE"),
                        Transition("property-type", "bedrooms", "Flat", "COMPLETE"),
                        Transition("select-address", "property-type", "1 Example Road", "SELECTED"),
                        Transition("select-address", "manual-address", "Enter the address manually", "MANUAL"),
                        Transition("select-address", "property-type", "2 Example Road", "SELECTED"),
                    ),
            )

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(
            listOf(
                ManifestEdge("property-type", "bedrooms", "", "COMPLETE", ManifestEdge.OBSERVED),
                ManifestEdge("select-address", "property-type", "1 Example Road / 2 Example Road", "SELECTED", ManifestEdge.OBSERVED),
                ManifestEdge("select-address", "manual-address", "Enter the address manually", "MANUAL", ManifestEdge.OBSERVED),
            ),
            manifest.edges,
        )
    }

    @Test
    fun `pages only seen while changing answers are placed in a changing answers container`() {
        val structure = structure(step("check-answers"))
        val changePage =
            PageRecord(
                "change:email",
                ManifestNode.PAGE,
                "email",
                "What is your email address?",
                listOf(VariantRecord("hash", "img/a.jpg", "img/a.thumb.jpg", emptyList(), ManifestNode.ORIGIN_CHANGE)),
                ManifestNode.ORIGIN_CHANGE,
            )
        val exploration =
            exploration(
                structure,
                pages = listOf(page("check-answers"), changePage),
                transitions = listOf(Transition("check-answers", "change:email", "Change email", null, kind = ManifestEdge.CHANGE)),
            )

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(listOf(ManifestAssembler.CHANGES_CONTAINER), manifest.containers)
        assertEquals(listOf(ManifestAssembler.CHANGES_CONTAINER.id), manifest.nodes.single { it.id == "change:email" }.containers)
    }

    @Test
    fun `containers are nested in the order they are recorded on each step`() {
        val containers =
            listOf(
                IntrospectedContainer("section:register", "section", "Register"),
                IntrospectedContainer("task:IdentityTask", "task", "Identity"),
            )
        val structure = structure(step("privacy-notice", containers = containers))
        val exploration = exploration(structure, pages = listOf(page("privacy-notice")), transitions = emptyList())

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(
            listOf(
                ManifestContainer("section:register", "section", "Register", null),
                ManifestContainer("task:IdentityTask", "task", "Identity", "section:register"),
            ),
            manifest.containers,
        )
        assertEquals(listOf("section:register", "task:IdentityTask"), manifest.nodes.single().containers)
    }

    @Test
    fun `arrows in the routing code that no walk took are drawn as not walked`() {
        val structure =
            structure(
                step(
                    "landlord-type",
                    modeValues = listOf("INDIVIDUAL", "ORGANISATION"),
                    destinations =
                        listOf(
                            IntrospectedDestination("INDIVIDUAL", step = "country-of-residence"),
                            IntrospectedDestination("ORGANISATION", step = "organisation-name"),
                        ),
                ),
                step("country-of-residence"),
                step("organisation-name"),
            )
        val exploration =
            exploration(
                structure,
                pages = listOf(page("landlord-type"), page("country-of-residence")),
                transitions = listOf(Transition("landlord-type", "country-of-residence", "An individual", "INDIVIDUAL")),
                outcomesPassed = mapOf("landlord-type" to listOf("INDIVIDUAL")),
            )

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(
            listOf(
                ManifestArrow("landlord-type", "INDIVIDUAL", "country-of-residence", walked = true),
                ManifestArrow("landlord-type", "ORGANISATION", "organisation-name", walked = false),
            ),
            manifest.arrows,
        )
        assertTrue(
            ManifestEdge("landlord-type", "organisation-name", "Organisation", "ORGANISATION", ManifestEdge.NOT_WALKED) in manifest.edges,
            manifest.edges.toString(),
        )
    }

    @Test
    fun `an arrow the journey took that no transition shows, such as a decision before the first page, is drawn as walked`() {
        val structure =
            structure(
                step(
                    "deregister",
                    redirecting = true,
                    modeValues = listOf("SINGLE_LANDLORD", "HAS_JOINT_LANDLORDS"),
                    destinations =
                        listOf(
                            IntrospectedDestination("SINGLE_LANDLORD", step = "info"),
                            IntrospectedDestination("HAS_JOINT_LANDLORDS", step = "cannot-deregister"),
                        ),
                ),
                step("info"),
                step("cannot-deregister"),
            )
        val exploration =
            exploration(
                structure,
                pages = listOf(page("info")),
                transitions = emptyList(),
                outcomesPassed = mapOf("deregister" to listOf("SINGLE_LANDLORD")),
            )

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(
            listOf(
                ManifestEdge("deregister", "info", "Single landlord", "SINGLE_LANDLORD", ManifestEdge.OBSERVED),
                ManifestEdge("deregister", "cannot-deregister", "Has joint landlords", "HAS_JOINT_LANDLORDS", ManifestEdge.NOT_WALKED),
            ),
            manifest.edges,
        )
        assertEquals(ManifestNode.DECISION, manifest.nodes.single { it.id == "deregister" }.kind)
    }

    @Test
    fun `arrows not walked whose routing code reads journey state are listed but not drawn, as their destination is not known`() {
        val structure =
            structure(
                step(
                    "landlord-type",
                    modeValues = listOf("INDIVIDUAL", "ORGANISATION"),
                    destinations =
                        listOf(
                            IntrospectedDestination("INDIVIDUAL", step = "country-of-residence"),
                            IntrospectedDestination("ORGANISATION", step = "country-of-residence", reads = listOf("outcome:landlord-type")),
                        ),
                ),
                step("country-of-residence"),
            )
        val exploration =
            exploration(
                structure,
                pages = listOf(page("landlord-type"), page("country-of-residence")),
                transitions = listOf(Transition("landlord-type", "country-of-residence", "An individual", "INDIVIDUAL")),
                outcomesPassed = mapOf("landlord-type" to listOf("INDIVIDUAL")),
            )

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(listOf(false), manifest.arrows.filter { it.mode == "ORGANISATION" }.map { it.walked })
        assertEquals(listOf(null), manifest.arrows.filter { it.mode == "ORGANISATION" }.map { it.to })
        assertTrue(manifest.edges.none { it.mode == "ORGANISATION" }, manifest.edges.toString())
    }

    @Test
    fun `walked arrows whose routing code reads journey state are drawn by the walks alone`() {
        val structure =
            structure(
                step(
                    "landlord-type",
                    modeValues = listOf("INDIVIDUAL", "ORGANISATION"),
                    destinations =
                        listOf(
                            IntrospectedDestination("INDIVIDUAL", step = "country-of-residence", reads = listOf("outcome:landlord-type")),
                            IntrospectedDestination("ORGANISATION", step = "country-of-residence", reads = listOf("outcome:landlord-type")),
                        ),
                ),
                step("country-of-residence"),
                step("organisation-name"),
            )
        val exploration =
            exploration(
                structure,
                pages = listOf(page("landlord-type"), page("organisation-name")),
                transitions = listOf(Transition("landlord-type", "organisation-name", "An organisation", "ORGANISATION")),
                outcomesPassed = mapOf("landlord-type" to listOf("ORGANISATION")),
            )

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(listOf(ManifestEdge("landlord-type", "organisation-name", "", "ORGANISATION", ManifestEdge.OBSERVED)), manifest.edges)
        assertEquals(listOf(true to null), manifest.arrows.filter { it.mode == "ORGANISATION" }.map { it.walked to it.to })
    }

    @Test
    fun `steps only seen while changing answers from check your answers have no arrows of their own`() {
        val structure =
            structure(
                step("check-answers"),
                step(
                    "internal:ChangeRedirect",
                    requestable = false,
                    modeValues = listOf("CHECK_ANSWERS", "TASK"),
                    destinations =
                        listOf(
                            IntrospectedDestination("CHECK_ANSWERS", step = "check-answers"),
                            IntrospectedDestination("TASK", step = "check-answers"),
                        ),
                ),
            )
        val exploration =
            exploration(structure, pages = listOf(page("check-answers")), transitions = emptyList())
                .copy(changeFlowSteps = setOf("internal:ChangeRedirect"))

        val manifest = assembler.assemble(journey, exploration)

        assertTrue(manifest.arrows.isEmpty(), manifest.arrows.toString())
        assertEquals(listOf("check-answers"), manifest.nodes.map { it.id })
    }

    @Test
    fun `arrows pass through steps with one outcome and task exits to the next page`() {
        val structure =
            structure(
                step(
                    "email",
                    modeValues = listOf("COMPLETE"),
                    destinations = listOf(IntrospectedDestination("COMPLETE", step = "internal:Finish")),
                ),
                step(
                    "internal:Finish",
                    requestable = false,
                    modeValues = listOf("COMPLETE"),
                    destinations = listOf(IntrospectedDestination("COMPLETE", step = "exit:task:Task")),
                ),
                step(
                    "exit:task:Task",
                    requestable = false,
                    exit = true,
                    modeValues = listOf("COMPLETE"),
                    destinations = listOf(IntrospectedDestination("COMPLETE", url = "/test/check-answers")),
                ),
                step("check-answers"),
            )
        val exploration = exploration(structure, pages = listOf(page("email"), page("check-answers")), transitions = emptyList())

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(listOf(ManifestArrow("email", "COMPLETE", "check-answers", walked = false)), manifest.arrows)
    }

    @Test
    fun `journey state read by the routing code of steps passed through makes an arrow depend on earlier answers`() {
        val structure =
            structure(
                step(
                    "email",
                    modeValues = listOf("COMPLETE"),
                    destinations = listOf(IntrospectedDestination("COMPLETE", step = "internal:Finish")),
                ),
                step(
                    "internal:Finish",
                    requestable = false,
                    modeValues = listOf("COMPLETE"),
                    destinations = listOf(IntrospectedDestination("COMPLETE", step = "check-answers", reads = listOf("step:email"))),
                ),
                step("check-answers"),
            )
        val exploration = exploration(structure, pages = listOf(page("email"), page("check-answers")), transitions = emptyList())

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(listOf(ManifestArrow("email", "COMPLETE", null, walked = false, reads = listOf("step:email"))), manifest.arrows)
    }

    @Test
    fun `arrows to addresses outside the journey's pages lead to pages that leave the journey`() {
        val structure =
            structure(
                step(
                    "confirm",
                    modeValues = listOf("COMPLETE"),
                    destinations = listOf(IntrospectedDestination("COMPLETE", url = "/dashboard")),
                ),
            )
        val exploration = exploration(structure, pages = listOf(page("confirm")), transitions = emptyList())

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(ManifestNode.TERMINAL, manifest.nodes.single { it.id == "/dashboard" }.kind)
        assertEquals(listOf(ManifestArrow("confirm", "COMPLETE", "/dashboard", walked = false)), manifest.arrows)
    }

    @Test
    fun `entry points resolve to the decision the journey starts with when it is drawn, and otherwise to the first page`() {
        val structure =
            structure(
                step(
                    "deregister",
                    redirecting = true,
                    modeValues = listOf("SINGLE_LANDLORD", "HAS_JOINT_LANDLORDS"),
                    destinations =
                        listOf(
                            IntrospectedDestination("SINGLE_LANDLORD", step = "info"),
                            IntrospectedDestination("HAS_JOINT_LANDLORDS", step = "cannot-deregister"),
                        ),
                ),
                step("info"),
                step("cannot-deregister"),
            )
        val exploration =
            exploration(
                structure,
                pages = listOf(page("info")),
                transitions = emptyList(),
                outcomesPassed = mapOf("deregister" to listOf("SINGLE_LANDLORD")),
                entries = listOf(EntryRecord("deregister", "info", null), EntryRecord("internal:NotDrawn", "info", "Change type")),
            )

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(listOf(ManifestEntry("deregister", ""), ManifestEntry("info", "Change type")), manifest.entries)
    }

    @Test
    fun `entry points at the same node are listed once with their different labels joined`() {
        val exploration =
            exploration(
                structure(step("rent-frequency"), step("rent-amount")),
                pages = listOf(page("rent-frequency"), page("rent-amount")),
                transitions = emptyList(),
                entries =
                    listOf(
                        EntryRecord("rent-frequency", "rent-frequency", "Change rent frequency"),
                        EntryRecord("rent-amount", "rent-amount", "Change rent amount"),
                        EntryRecord("rent-frequency", "rent-frequency", "Change rent frequency"),
                        EntryRecord("rent-frequency", "rent-frequency", "Change how often rent is paid"),
                        EntryRecord("rent-amount", "rent-amount", null),
                    ),
            )

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(
            listOf(
                ManifestEntry("rent-frequency", "Change rent frequency or Change how often rent is paid"),
                ManifestEntry("rent-amount", "Change rent amount"),
            ),
            manifest.entries,
        )
    }

    @Test
    fun `entry points that resolve to no node on the map are dropped`() {
        val exploration =
            exploration(
                structure(step("name")),
                pages = listOf(page("name")),
                transitions = emptyList(),
                entries = listOf(EntryRecord("internal:Gone", "elsewhere", "Link")),
            )

        val manifest = assembler.assemble(journey, exploration)

        assertEquals(emptyList<ManifestEntry>(), manifest.entries)
    }

    private fun structure(vararg steps: IntrospectedStep) = IntrospectionResponse("privacy-notice", "journey-id", steps.toList())

    private fun step(
        id: String,
        requestable: Boolean = true,
        redirecting: Boolean = !requestable,
        exit: Boolean = false,
        stepClass: String = "TestStep",
        modeValues: List<String> = emptyList(),
        parentage: IntrospectedParentage = IntrospectedParentage(IntrospectedParentage.NONE),
        containers: List<IntrospectedContainer> = emptyList(),
        destinations: List<IntrospectedDestination> = emptyList(),
    ) = IntrospectedStep(
        id = id,
        path = if (requestable) id else null,
        requestable = requestable,
        redirecting = redirecting,
        stepClass = stepClass,
        stepConfigClass = stepClass + "Config",
        modeValues = modeValues,
        outcome = null,
        reachable = true,
        exit = exit,
        initial = parentage.kind == IntrospectedParentage.NONE,
        containers = containers,
        parentage = parentage,
        fields = emptyMap(),
        destinations = destinations,
    )

    private fun page(id: String) =
        PageRecord(
            id,
            ManifestNode.PAGE,
            id,
            "Title of $id",
            listOf(VariantRecord("hash-$id", "img/$id.jpg", "img/$id.thumb.jpg", emptyList())),
        )

    private fun exploration(
        structure: IntrospectionResponse,
        pages: List<PageRecord>,
        transitions: List<Transition>,
        outcomesPassed: Map<String, List<String>> = emptyMap(),
        entries: List<EntryRecord> = emptyList(),
    ) = Exploration(listOf(structure), pages, transitions, emptyList(), emptyList(), outcomesPassed = outcomesPassed, entries = entries)
}
