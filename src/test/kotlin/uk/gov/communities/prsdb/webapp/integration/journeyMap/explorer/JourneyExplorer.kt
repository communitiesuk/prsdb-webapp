package uk.gov.communities.prsdb.webapp.integration.journeyMap.explorer

import uk.gov.communities.prsdb.webapp.integration.journeyMap.JourneyMapHarness
import uk.gov.communities.prsdb.webapp.integration.journeyMap.JourneyMapSettings
import uk.gov.communities.prsdb.webapp.integration.journeyMap.descriptors.JourneyDescriptor
import uk.gov.communities.prsdb.webapp.integration.journeyMap.registry.JourneyMapRegistry
import uk.gov.communities.prsdb.webapp.integration.journeyMap.registry.Scenario
import uk.gov.communities.prsdb.webapp.integration.journeyMap.registry.ScenarioOption
import uk.gov.communities.prsdb.webapp.integration.journeyMap.walker.JourneyWalker
import uk.gov.communities.prsdb.webapp.integration.journeyMap.walker.VariantRenderer
import uk.gov.communities.prsdb.webapp.journeyMap.dependencies.DependencyGraph
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.CandidateGenerator
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.PageLink
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.ValueGenerator
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedStep
import uk.gov.communities.prsdb.webapp.journeyMap.model.EntryRecord
import uk.gov.communities.prsdb.webapp.journeyMap.model.Exploration
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestCondition
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestDependency
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestNode

class JourneyExplorer(
    private val walker: JourneyWalker,
    private val descriptor: JourneyDescriptor,
    private val harness: JourneyMapHarness,
    values: ValueGenerator,
    renderer: VariantRenderer,
    private val settings: JourneyMapSettings,
) {
    private val urls = JourneyUrls(descriptor.basePath)
    private val candidateGenerator = CandidateGenerator(values)
    private val recorder =
        ExplorationRecorder(
            descriptor.name,
            urls,
            renderer,
            descriptor.singleRenderPaths,
            settings.maxVariantsPerPage,
            walker::structuralSignature,
        )
    private val actionFactory = ActionFactory(candidateGenerator, urls)
    private var seedScripts = descriptor.seedScripts
    private val performer =
        ActionPerformer(walker, urls, recorder, candidateGenerator, actionFactory, ViaStepResolver(urls)) { resetDatabase() }
    private val budget = ActionBudget(settings.maxActions)
    private val changeFlows = ChangeFlowExplorer(walker, urls, recorder, actionFactory, performer, budget)
    private val submitted = mutableSetOf<String>()
    private val preferredContinuation = mutableMapOf<String, String>()
    private val taskListSteps = mutableSetOf<String>()
    private val branchesByAction = mutableMapOf<String, Int>()
    private val queue = ArrayDeque<WalkStart>()
    private val resumePoints = ArrayDeque<WalkStart>()
    private val resumesByOrigin = mutableMapOf<String?, Int>()
    private val scenarioBaseActions = mutableMapOf<String, MutableMap<String, String>>()
    private var openingStepConfigs = emptySet<String>()
    private var otherEntryLinks: List<PageLink>? = null

    // Each opening explores every action again, because its data or scenario can change any page
    private var openingScope = ""
    val usedScenarioOptions = mutableSetOf<String>()

    // Where walks start: the default, each starting point, each other link into the journey, and their scenario options
    private class Opening(
        val conditions: List<ManifestCondition> = emptyList(),
        val seedScripts: List<String>? = null,
        val scenario: Scenario? = null,
        val option: ScenarioOption? = null,
        val entryPath: String? = null,
    )

    fun explore(): Exploration {
        val openings = ArrayDeque(listOf(Opening()))
        descriptor.startingPoints.forEach { openings += Opening(listOf(ManifestCondition(STARTING_POINT, it.label)), it.seedScripts) }
        var openingScenarios: List<Scenario>? = null
        while (openings.isNotEmpty() && !budget.exceeded) {
            val opening = openings.removeFirst()
            openingScope = opening.conditions.joinToString("; ") { "${it.step}=${it.answer}" }
            opening.seedScripts?.let {
                seedScripts = it
                resetDatabase()
                walker.clearCookies()
            }
            opening.option?.let { option ->
                option.arrange(harness)
                usedScenarioOptions += "${opening.scenario?.name}: ${option.label}"
            }
            try {
                val start = startJourney(opening) ?: continue
                if (opening.conditions.isEmpty()) openings += entryOpenings()
                if (opening.scenario == null) {
                    val scenarios = openingScenarios ?: JourneyMapRegistry.scenariosFor(openingStepConfigs).also { openingScenarios = it }
                    openings.addAll(0, scenarios.flatMap { scenarioOpenings(opening, it) })
                }
                if (settings.structureOnly) continue
                queue.clear()
                resumePoints.clear()
                queue += start
                drainQueue()
                resumeBranchesForCoverage()
            } finally {
                opening.scenario?.options?.first()?.arrange(harness)
            }
        }
        if (budget.exceeded) {
            val reason = "Stopped after ${settings.maxActions} actions; ${queue.size} branches were not explored"
            recorder.gap(descriptor.basePath, null, reason)
        }
        recordDependencies()
        return recorder.toExploration()
    }

    private fun scenarioOpenings(
        opening: Opening,
        scenario: Scenario,
    ): List<Opening> {
        usedScenarioOptions += "${scenario.name}: ${scenario.options.first().label}"
        return scenario.options.drop(1).map { option ->
            val conditions = opening.conditions + ManifestCondition(scenario.name, option.label)
            Opening(conditions, opening.seedScripts ?: descriptor.seedScripts, scenario, option, opening.entryPath)
        }
    }

    private fun entryOpenings() =
        otherEntryLinks.orEmpty().map { link ->
            Opening(listOf(ManifestCondition(ENTRY_POINT, link.text)), descriptor.seedScripts, entryPath = urls.stepPathOf(link.href))
        }

    private fun resetDatabase() {
        harness.resetDatabase(seedScripts)
        walker.databaseWasReset()
    }

    private fun recordDependencies() {
        val dependencies = walker.dependencies()
        val graph = DependencyGraph(dependencies)
        recorder.outcomesPassed = dependencies.outcomesPassedOutside(changeFlows.journeyIds)
        recorder.dependencies =
            recorder
                .decisionSteps()
                .map { ManifestDependency(it, graph.writersOf(it).toList(), graph.readsAllStepData(it)) }
                .filter { it.writers.isNotEmpty() }
    }

    private fun drainQueue() {
        while (queue.isNotEmpty() && !budget.exceeded) {
            val next = queue.removeFirst()
            try {
                walk(next)
            } catch (exception: Exception) {
                recorder.error(urls.stepPathOf(next.url) ?: next.url, "exception", ActionPerformer.describe(exception))
            }
            log("${budget.used} actions, ${recorder.pageCount} pages, ${queue.size} branches waiting")
        }
    }

    // Branches stop at the task list, so later tasks only see the main walk's answers. While gaps remain, branches are
    // resumed from the task list, which covers later steps that depend on earlier answers, such as occupancy. Branches
    // are taken in turn from each step they diverged at, and resuming stops after a full round closes no gaps
    private fun resumeBranchesForCoverage() {
        var unproductive = 0
        while (resumePoints.isNotEmpty() && !budget.exceeded) {
            val gaps = recorder.coverageGaps().size
            if (gaps == 0) return
            val origins = (resumePoints.map { it.origin } + resumesByOrigin.keys).toSet().size
            if (unproductive >= origins) return
            val next = resumePoints.minBy { resumesByOrigin.getOrDefault(it.origin, 0) }
            resumePoints.remove(next)
            resumesByOrigin.merge(next.origin, 1, Int::plus)
            queue += next
            drainQueue()
            if (recorder.coverageGaps().size < gaps) unproductive = 0 else unproductive++
        }
    }

    private fun startJourney(opening: Opening): WalkStart? {
        val passedBefore = walker.dependencies().outcomesPassed
        descriptor.start(harness.navigator)
        val entryLink = if (descriptor.startsFromLink) entryLinkFor(opening.entryPath) else null
        if (entryLink != null && !follow(entryLink)) return null
        val url = walker.currentUrl()
        val path = urls.stepPathOf(url)
        val introspection = path?.let { walker.introspect(url) }
        if (path == null || introspection == null) {
            recorder.error(descriptor.basePath, "start", "The journey did not start on one of its pages: $url")
            return null
        }
        recorder.registerStructure(introspection)
        recorder.observeStart(introspection)
        val passedSteps =
            newlyPassedSteps(passedBefore, walker.dependencies().outcomesPassed).mapNotNull { introspection.stepById(it) }
        openingStepConfigs = passedSteps.map { it.stepConfigClass }.toSet()
        introspection.stepByPath(path)?.let { firstPage ->
            val startingDecision =
                passedSteps.firstOrNull { it.initial } ?: introspection.steps.firstOrNull { it.initial && it.leadsStraightTo(firstPage) }
            recorder.recordEntry(EntryRecord(startingDecision?.id ?: firstPage.id, firstPage.id, entryLink?.text ?: descriptor.entryLabel))
        }
        return WalkStart(walker.snapshot(), url, opening.conditions, emptySet(), isMainWalk = true)
    }

    // Opening a journey at its first page still checks the decision it starts with, such as whether an identity is verified
    private fun IntrospectedStep.leadsStraightTo(page: IntrospectedStep) =
        (redirecting || !requestable) && outcome != null && destinations.any { it.mode == outcome && it.step == page.id }

    private fun newlyPassedSteps(
        before: Map<String, Map<String, List<String>>>,
        after: Map<String, Map<String, List<String>>>,
    ) = after
        .flatMap { (journeyId, steps) ->
            steps.filter { (step, modes) -> modes.any { it !in before[journeyId]?.get(step).orEmpty() } }.keys
        }.toSet()

    // Every link into the journey gets an opening; without any, the journey is reported as not starting on one of its pages
    private fun entryLinkFor(entryPath: String?): PageLink? {
        val page = walker.discover()
        val links =
            (page.changeLinks + page.links + page.contentLinks)
                .filter { urls.stepPathOf(it.href) != null }
                .distinctBy { urls.stepPathOf(it.href) }
        val link = links.firstOrNull { entryPath == null || urls.stepPathOf(it.href) == entryPath } ?: return null
        if (otherEntryLinks == null) otherEntryLinks = links - link
        return link
    }

    private fun follow(link: PageLink): Boolean {
        val navigation = walker.navigate(link.href)
        if (navigation.status < 400) return true
        recorder.error(descriptor.basePath, "http-${navigation.status}", "\"${link.text}\" led to ${navigation.url}")
        return false
    }

    private fun walk(start: WalkStart) {
        val trail = mutableListOf<String>()
        try {
            walk(start, trail)
        } finally {
            if (settings.verbose) log("walked ${trail.joinToString(" > ")}")
        }
    }

    // A walk always handles its first step, so that a branch which loops back to an earlier step (such as uploading
    // another file) submits it again, and later steps only when they were chosen as its continuation
    private fun walk(
        start: WalkStart,
        trail: MutableList<String>,
    ) {
        walker.restore(start.sessionSnapshot)
        walker.navigate(start.url)
        val visited = start.visited.toMutableSet()
        val visits = mutableMapOf<String, Int>()
        var expectedStepId: String? = null
        repeat(MAX_WALK_LENGTH) {
            val url = walker.currentUrl()
            val path = urls.stepPathOf(url) ?: return
            val introspection = walker.introspect(url) ?: return recorder.error(path, "introspection", "Could not introspect $url")
            recorder.registerStructure(introspection)
            val step = introspection.stepByPath(path) ?: return
            trail += path
            if (expectedStepId != null && step.id != expectedStepId && step.id in visited) return
            val page = walker.discover()
            recorder.recordRender(step.id, ManifestNode.PAGE, path, page, walker.signature(), start.conditions, ManifestNode.ORIGIN_MAIN)
            if (page.hasTaskList) taskListSteps += step.id
            visited += step.id
            visits.merge(step.id, 1, Int::plus)
            val actions = actionFactory.actionsFor(page, step.fields, introspection)
            if (actions.isEmpty()) return recorder.recordExitLinks(step.id, page)

            val context = ActionContext(walker.snapshot(), url, step, introspection, page, start.conditions)
            val firstEncounter = actions.any { submissionKey(step.id, it) !in submitted }
            val results = linkedMapOf<String, Landing>()
            val alternativeKeys = mutableSetOf<String>()
            for (action in actions) {
                val key = submissionKey(step.id, action)
                if (key in submitted) continue
                if (!budget.spend()) return
                submitted += key
                val landing = performer.perform(context, action, actionFactory.deviation(page, action, actions))
                recorder.recordTransition(step.id, landing)
                results[action.key] = landing
            }
            if (firstEncounter) {
                val before = results.keys.toSet()
                exploreValueAlternatives(context, actions, results)
                exploreScenarios(context, actions, results)
                alternativeKeys += results.keys - before
            }
            if (start.resumed) {
                val before = results.keys.toSet()
                repeatScenarios(context, actions, results)
                alternativeKeys += results.keys - before
            }
            if (settings.changeFlows) changeFlows.explore(context)

            val choice = chooseContinuation(context, actions, results, visited, visits)
            val continuation = choice?.second
            val onTaskList = step.id in taskListSteps
            results.forEach { (key, landing) ->
                when {
                    key == choice?.first?.key -> Unit
                    // The main walk starts each task it unlocks by returning to the task list
                    (onTaskList && key !in alternativeKeys) || landing.stepId in taskListSteps -> Unit
                    // A page reached here and nowhere else yet, such as one a link opens in a new journey, is always walked
                    (landing.propagatesFrom(continuation) || landing.kind == LandingKind.PAGE && !recorder.hasRendered(landing.nodeId)) &&
                        claimBranch(step.id, key) -> {
                        trail += "[queued ${urls.stepPathOf(landing.url)}]"
                        queue +=
                            WalkStart(
                                landing.sessionSnapshot!!,
                                landing.url,
                                start.conditions + listOfNotNull(landing.condition),
                                visited.toSet(),
                                origin = step.id,
                            )
                    }
                    continuation != null && landing.isContentVariantOf(continuation) -> renderContentVariant(landing, start.conditions)
                }
            }
            if (choice == null) return
            val next = choice.second
            if (next.stepId in taskListSteps && !start.isMainWalk) {
                trail += "[resume point]"
                resumePoints +=
                    WalkStart(
                        next.sessionSnapshot!!,
                        next.url,
                        start.conditions,
                        visited.toSet(),
                        isMainWalk = true,
                        resumed = true,
                        origin = start.origin,
                    )
                return
            }
            // The first continuation chosen at a step is kept, so that later walks give the main walk's answers elsewhere
            preferredContinuation.putIfAbsent(step.id, choice.first.key)
            expectedStepId = next.stepId
            walker.restore(next.sessionSnapshot!!)
            walker.navigate(next.url)
        }
    }

    // An unvisited step is preferred. Failing that, a walk may revisit a step once more when its content has changed,
    // such as a list of uploaded files, or return to a task list to start the next task
    private fun chooseContinuation(
        context: ActionContext,
        actions: List<WalkAction>,
        results: Map<String, Landing>,
        visited: Set<String>,
        visits: Map<String, Int>,
    ): Pair<WalkAction, Landing>? {
        val preferred = preferredContinuation[context.step.id]
        val journeyId = urls.journeyIdOf(context.sourceUrl)
        var revisit: Pair<WalkAction, Landing>? = null
        var taskListReturn: Pair<WalkAction, Landing>? = null
        for (action in actions.sortedBy { if (it.key == preferred) 0 else 1 }) {
            val landing =
                results[action.key] ?: run {
                    if (!budget.spend()) return revisit ?: taskListReturn
                    performer
                        .perform(context, action, actionFactory.deviation(context.page, action, actions))
                        .also { recorder.recordTransition(context.step.id, it) }
                }
            if (landing.kind != LandingKind.PAGE || landing.journeyId != journeyId) continue
            if (landing.stepId !in visited) return action to landing
            when {
                landing.stepId in taskListSteps -> if (taskListReturn == null) taskListReturn = action to landing
                revisit == null && canRevisit(landing, visits) -> revisit = action to landing
            }
        }
        return revisit ?: taskListReturn
    }

    private fun canRevisit(
        landing: Landing,
        visits: Map<String, Int>,
    ) = (visits[landing.stepId] ?: 0) < MAX_VISITS_PER_WALK && !recorder.hasSeen(landing.nodeId, landing.hash)

    // Links to add or remove list items carry the item's id, so without a limit each pass round the list adds another
    private fun claimBranch(
        stepId: String,
        resultKey: String,
    ): Boolean {
        val key = "$openingScope|$stepId|${resultKey.substringBefore('?')}"
        val count = branchesByAction.getOrDefault(key, 0)
        if (count >= MAX_BRANCHES_PER_ACTION) return false
        branchesByAction[key] = count + 1
        return true
    }

    private fun renderContentVariant(
        landing: Landing,
        conditions: List<ManifestCondition>,
    ) {
        walker.restore(landing.sessionSnapshot!!)
        walker.navigate(landing.url)
        recorder.recordRender(
            landing.nodeId!!,
            ManifestNode.PAGE,
            urls.stepPathOf(landing.url),
            walker.discover(),
            walker.signature(),
            conditions + listOfNotNull(landing.condition),
            ManifestNode.ORIGIN_MAIN,
        )
    }

    // Branches that depend on typed values, such as dates or postcodes, are found by trying alternative values
    private fun exploreValueAlternatives(
        context: ActionContext,
        actions: List<WalkAction>,
        results: MutableMap<String, Landing>,
    ) {
        val coveredSteps = (listOf(context.step.id) + results.values.flatMap { landing -> landing.via.map { it.id } }).distinct()

        fun hasUnseenModes() = coveredSteps.any { recorder.unseenModes(it).isNotEmpty() }

        if (!hasUnseenModes()) return
        val base = actions.filterIsInstance<WalkAction.Submit>().firstOrNull() ?: return
        for (alternative in candidateGenerator.alternatives(context.page, base.candidate, context.step.fields)) {
            if (!hasUnseenModes()) return
            val action = WalkAction.Submit(alternative, alternative.label.ifBlank { base.label })
            val key = submissionKey(context.step.id, action)
            if (key in submitted) continue
            if (!budget.spend()) return
            submitted += key
            val condition = ManifestCondition(ExplorationRecorder.titleOf(context.page), action.label)
            val landing = performer.perform(context, action, condition, reportValidationFailures = false)
            if (landing.newModes) {
                recorder.recordTransition(context.step.id, landing)
                results[action.key] = landing
            }
        }
    }

    // Branches that depend on external services are found by switching the registry's scenario options
    private fun exploreScenarios(
        context: ActionContext,
        actions: List<WalkAction>,
        results: MutableMap<String, Landing>,
    ) {
        fun stepConfigsOf(landings: Collection<Landing>) =
            (listOf(context.step.stepConfigClass) + landings.flatMap { it.via }.mapNotNull { recorder.step(it.id)?.stepConfigClass })
                .toSet()

        val stepConfigs = stepConfigsOf(results.values)
        val scenarios = JourneyMapRegistry.scenariosFor(stepConfigs)
        if (scenarios.isEmpty()) return
        val firstPage = actions.firstOrNull { results[it.key]?.kind == LandingKind.PAGE } ?: actions.first()
        for (scenario in scenarios) {
            // On a task list, the scenario is explored through the link to the task that uses it
            val base =
                actions.firstOrNull { action ->
                    results[action.key]?.let { scenario.appliesTo(stepConfigsOf(listOf(it))) } ?: false
                } ?: firstPage
            scenarioBaseActions.getOrPut(context.step.id) { mutableMapOf() }[scenario.name] = base.key
            usedScenarioOptions += "${scenario.name}: ${scenario.options.first().label}"
            if (!runScenarioOptions(context, scenario, base, results, keySuffix = "")) return
        }
    }

    // A resumed walk repeats the scenarios explored on a step in its own context while a gap remains in the part of the
    // journey that the scenario affects, such as an expired EPC for a property that is not occupied
    private fun repeatScenarios(
        context: ActionContext,
        actions: List<WalkAction>,
        results: MutableMap<String, Landing>,
    ) {
        val baseActions = scenarioBaseActions[context.step.id] ?: return
        val gapContainers = recorder.coverageGaps().flatMap { gap -> recorder.step(gap.node)?.containers.orEmpty().map { it.id } }.toSet()
        if (gapContainers.isEmpty()) return
        val contextKey = context.conditions.joinToString("; ") { "${it.step}=${it.answer}" }
        for (scenario in JourneyMapRegistry.scenarios) {
            val base = baseActions[scenario.name]?.let { key -> actions.firstOrNull { it.key == key } } ?: continue
            if (recorder.containersOfStepConfigs(scenario.stepConfigNames).none { it in gapContainers }) continue
            if (!runScenarioOptions(context, scenario, base, results, keySuffix = "|$contextKey")) return
        }
    }

    private fun runScenarioOptions(
        context: ActionContext,
        scenario: Scenario,
        base: WalkAction,
        results: MutableMap<String, Landing>,
        keySuffix: String,
    ): Boolean {
        for (option in scenario.options.drop(1)) {
            val key = "$openingScope|${context.step.id}|scenario|${scenario.name}|${option.label}$keySuffix"
            if (key in submitted) continue
            if (!budget.spend()) return false
            submitted += key
            usedScenarioOptions += "${scenario.name}: ${option.label}"
            option.arrange(harness)
            val landing =
                try {
                    val condition = ManifestCondition(scenario.name, option.label)
                    performer.perform(context, base, condition)
                } finally {
                    scenario.options.first().arrange(harness)
                }
            recorder.recordTransition(context.step.id, landing)
            results[key] = landing
        }
        return true
    }

    private fun submissionKey(
        stepId: String,
        action: WalkAction,
    ) = "$openingScope|$stepId|${action.key}"

    private fun log(message: String) = println("[journey-map] ${descriptor.name}: $message")

    companion object {
        const val STARTING_POINT = "Starting point"
        const val ENTRY_POINT = "Entry point"
        private const val MAX_WALK_LENGTH = 200
        private const val MAX_VISITS_PER_WALK = 2
        private const val MAX_BRANCHES_PER_ACTION = 2
    }
}

class ActionBudget(
    private val maxActions: Int,
) {
    var used = 0
        private set
    var exceeded = false
        private set

    fun spend(): Boolean {
        if (used >= maxActions) {
            exceeded = true
            return false
        }
        used++
        return true
    }
}
