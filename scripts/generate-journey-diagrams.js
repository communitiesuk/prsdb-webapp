#!/usr/bin/env node

const fs = require("fs");
const path = require("path");
const { glob } = require("glob");

const JOURNEYS_ROOT = path.resolve(
  __dirname,
  "..",
  "src",
  "main",
  "kotlin",
  "uk",
  "gov",
  "communities",
  "prsdb",
  "webapp",
  "journeys",
);
const DOCS_DIR = path.join(JOURNEYS_ROOT, "docs");

// -- Brace-aware block extraction --

function findMatchingBrace(text, startIndex) {
  let depth = 0;
  for (let i = startIndex; i < text.length; i++) {
    if (text[i] === "{") depth++;
    else if (text[i] === "}") {
      depth--;
      if (depth === 0) return i;
    }
  }
  return -1;
}

function extractBlock(text, keyword) {
  const pattern = new RegExp(`\\b${keyword}\\s*\\{`);
  const match = pattern.exec(text);
  if (!match) return null;
  const openBrace = match.index + match[0].length - 1;
  const closeBrace = findMatchingBrace(text, openBrace);
  if (closeBrace === -1) return null;
  return text.substring(openBrace + 1, closeBrace);
}

function extractBlockAfterArgs(text, keyword) {
  const pattern = new RegExp(`\\b${keyword}\\s*\\([^)]*\\)\\s*\\{`);
  const match = pattern.exec(text);
  if (!match) return null;
  const openBrace = match.index + match[0].length - 1;
  const closeBrace = findMatchingBrace(text, openBrace);
  if (closeBrace === -1) return null;
  return text.substring(openBrace + 1, closeBrace);
}

function extractAllBlocks(text, keyword, argPattern) {
  const blocks = [];
  // Handle optional type parameters: step<Type1, Type2>(args) { ... }
  const typeParams = `(?:<[^>]*>)?\\s*`;
  const pattern = argPattern
    ? new RegExp(`\\b${keyword}${typeParams}\\(${argPattern}\\)\\s*\\{`, "g")
    : new RegExp(`\\b${keyword}\\s*\\{`, "g");

  let match;
  while ((match = pattern.exec(text)) !== null) {
    const openBrace = match.index + match[0].length - 1;
    const closeBrace = findMatchingBrace(text, openBrace);
    if (closeBrace === -1) continue;
    blocks.push({
      fullMatch: match[0],
      body: text.substring(openBrace + 1, closeBrace),
      start: match.index,
      end: closeBrace,
    });
  }
  return blocks;
}

// -- Step/Task name extraction --

function extractStepName(argStr) {
  const m = argStr.match(/(?:journey|state)\.([\w.]+)/);
  return m ? m[1] : null;
}

function humanize(name) {
  const abbreviations = {
    Cya: "Check Your Answers",
    Epc: "EPC",
    Eicr: "EICR",
    Hmo: "HMO",
    Prn: "PRN",
    Mees: "MEES",
  };
  const words = name
    .replace(/Step$/, "")
    .replace(/Task$/, "")
    .replace(/([a-z])([A-Z])/g, "$1 $2")
    .split(" ");
  return words
    .map((w) => {
      const capitalized = w.charAt(0).toUpperCase() + w.slice(1);
      return abbreviations[capitalized] || capitalized;
    })
    .join(" ");
}

function sanitizeId(id) {
  return id.replace(/\./g, "_").replace(/[^a-zA-Z0-9_]/g, "");
}

// -- Parse nextStep --

function parseNextStep(body) {
  const edges = [];

  const nextStepBlocks = extractAllBlocks(body, "nextStep");
  for (const block of nextStepBlocks) {
    const inner = block.body.trim();

    const whenMatch = inner.match(/when\s*\(\s*\w+\s*\)\s*\{/);
    if (whenMatch) {
      const whenBody = extractBlock(inner, "when\\s*\\(\\s*\\w+\\s*\\)");
      if (whenBody) {
        const casePattern =
          /(\w+(?:\.\w+)*)\s*->\s*(?:journey\.|state\.)?([\w.]+)/g;
        let caseMatch;
        while ((caseMatch = casePattern.exec(whenBody)) !== null) {
          const condition = caseMatch[1];
          const target = caseMatch[2];
          const label = condition.includes(".")
            ? condition.split(".").pop()
            : condition;
          edges.push({ target, label });
        }
      }
    } else {
      const simpleMatch = inner.match(
        /(?:journey\.|state\.)([\w.]+)|exitStep/,
      );
      if (simpleMatch) {
        const target = simpleMatch[0] === "exitStep"
          ? "exitStep"
          : simpleMatch[1];
        edges.push({ target, label: null });
      }
    }
  }
  return edges;
}

// -- Parse nextUrl --

function parseNextUrl(body) {
  const match = body.match(/nextUrl\s*\{/);
  if (!match) return null;
  const block = extractBlock(body, "nextUrl");
  if (!block) return null;
  const urlMatch = block.match(
    /(?:"([^"]+)"|(\w+(?:\.\w+)*(?:\([^)]*\))?))/,
  );
  return urlMatch ? (urlMatch[1] || urlMatch[2] || "external URL") : "external URL";
}

// -- Parse nextDestination --

function parseNextDestination(body) {
  const edges = [];
  const block = extractBlock(body, "nextDestination");
  if (!block) return edges;

  const stepRefs = [];
  const urlRefs = [];

  const destinationRefs =
    /Destination\s*\(\s*(?:journey\.|state\.)([\w.]+)\s*\)/g;
  let match;
  while ((match = destinationRefs.exec(block)) !== null) {
    stepRefs.push(match[1]);
  }

  const externalUrls = /Destination\.ExternalUrl\s*\(\s*([^)]+)\s*\)/g;
  while ((match = externalUrls.exec(block)) !== null) {
    urlRefs.push(match[1].trim());
  }

  // Extract conditions associated with each Destination
  // Match patterns: if (mode == EnumType.VALUE) or EnumType.VALUE -> Destination(...)
  // Exclude lambda parameter declarations like "mode ->"
  const conditionedDest =
    /(?:mode\s*==\s*(\w+(?:\.\w+)+)|(\w+\.\w+(?:\.\w+)*)\s*->)[^}]*?Destination(?:\.ExternalUrl)?\s*\(\s*(?:(?:journey\.|state\.)([\w.]+)|([^)]+))\s*\)/g;
  const conditionMap = new Map();
  while ((match = conditionedDest.exec(block)) !== null) {
    const condition = match[1] || match[2];
    const target = match[3] || `__url__${match[4]}`;
    const label = condition.includes(".")
      ? condition.split(".").pop()
      : condition;
    conditionMap.set(target, label);
  }

  // Also extract if/else-if/else patterns with state conditions
  const ifElsePattern =
    /(?:if\s*\(([^)]+)\)|else\s+if\s*\(([^)]+)\)|else)\s*\{[^}]*?Destination(?:\.ExternalUrl)?\s*\(\s*(?:(?:journey\.|state\.)([\w.]+)|([^)]+))\s*\)/g;
  while ((match = ifElsePattern.exec(block)) !== null) {
    const condition = match[1] || match[2] || "else";
    const target = match[3] || `__url__${match[4]}`;
    if (!conditionMap.has(target)) {
      let label;
      if (condition === "else") {
        label = "else";
      } else {
        // Extract readable part from conditions like "state.userHasRegisteredProperties"
        const parts = condition.replace(/[!()]/g, "").trim().split(".");
        label = parts[parts.length - 1];
      }
      conditionMap.set(target, label);
    }
  }

  for (const ref of stepRefs) {
    edges.push({ target: ref, label: conditionMap.get(ref) || null });
  }
  for (const url of urlRefs) {
    edges.push({
      target: `__url__${url}`,
      label: conditionMap.get(`__url__${url}`) || null,
    });
  }

  if (edges.length === 0 && block.trim().length > 0) {
    edges.push({ target: "__dynamic__", label: null });
  }

  return edges;
}

// -- Parse step/task blocks from a journey or subjourney body --

function parseElements(body) {
  const elements = [];

  const stepBlocks = extractAllBlocks(
    body,
    "step",
    "[^)]*(?:journey|state)\\.(\\w+)[^)]*",
  );
  for (const block of stepBlocks) {
    const nameMatch = block.fullMatch.match(
      /(?:journey|state)\.([\w]+)/,
    );
    if (!nameMatch) continue;
    const name = nameMatch[1];

    const isInitial = /\binitialStep\s*\(\s*\)/.test(block.body);
    const noNext = /\bnoNextDestination\s*\(\s*\)/.test(block.body);
    const nextStepEdges = parseNextStep(block.body);
    const nextUrl = parseNextUrl(block.body);
    const nextDestEdges = parseNextDestination(block.body);

    elements.push({
      type: "step",
      name,
      isInitial,
      noNext,
      nextStepEdges,
      nextUrl,
      nextDestEdges,
    });
  }

  const taskBlocks = extractAllBlocks(
    body,
    "task",
    "[^)]*(?:journey|state)\\.(\\w+)[^)]*",
  );
  for (const block of taskBlocks) {
    const nameMatch = block.fullMatch.match(
      /(?:journey|state)\.([\w]+)/,
    );
    if (!nameMatch) continue;
    const name = nameMatch[1];

    const isInitial = /\binitialStep\s*\(\s*\)/.test(block.body);
    const nextStepEdges = parseNextStep(block.body);
    const nextUrl = parseNextUrl(block.body);
    const nextDestEdges = parseNextDestination(block.body);

    elements.push({
      type: "task",
      name,
      isInitial,
      noNext: false,
      nextStepEdges,
      nextUrl,
      nextDestEdges,
    });
  }

  return elements;
}

// -- Parse a Task file's subJourney --

function parseTaskFile(filePath) {
  const content = fs.readFileSync(filePath, "utf-8");
  const sjBody = extractBlockAfterArgs(content, "subJourney");
  if (!sjBody) return null;

  const elements = parseElements(sjBody);

  const firstElement = elements[0];
  const firstStepName = firstElement ? firstElement.name : null;

  return { elements, firstStepName, filePath };
}

// -- Discover task files --

function findTaskFiles() {
  const taskFiles = new Map();
  const pattern = path
    .join(JOURNEYS_ROOT, "**", "*Task.kt")
    .replace(/\\/g, "/");
  const files = glob.sync(pattern);

  for (const file of files) {
    if (file.includes("forms/journeys") || file.includes("forms\\journeys"))
      continue;
    if (
      file.endsWith("TaskInitialiser.kt") ||
      file.endsWith("TaskListStepConfig.kt") ||
      file.endsWith("TaskListStep.kt")
    )
      continue;

    const content = fs.readFileSync(file, "utf-8");
    const classMatch = content.match(
      /class\s+(\w+Task)\s*(?:<[^>]*>)?\s*(?:\([^)]*\))?\s*:\s*Task/,
    );
    if (!classMatch) continue;

    const className = classMatch[1];
    const pkgMatch = content.match(/^package\s+([\w.]+)/m);
    const fullQualified = pkgMatch
      ? `${pkgMatch[1]}.${className}`
      : className;

    // Store by both full-qualified and class name
    taskFiles.set(fullQualified, file);
    // Only set class name key if not already taken (first wins; imports will disambiguate)
    if (!taskFiles.has(className)) {
      taskFiles.set(className, file);
    }
  }

  return taskFiles;
}

// -- Resolve task property name to class name --

function resolveTaskClassName(propertyName, factoryContent) {
  const propNameForType = propertyName.replace(/^(\w)/, (c) =>
    c.toUpperCase(),
  );
  const typePattern = new RegExp(
    `\\b${propertyName}\\b[^:]*:\\s*(\\w+Task)`,
  );
  const match = factoryContent.match(typePattern);
  const className = match ? match[1] : propNameForType;

  // Try to find the fully-qualified name via import statements
  const importPattern = new RegExp(
    `import\\s+([\\w.]+\\.${className})\\b`,
  );
  const importMatch = factoryContent.match(importPattern);
  if (importMatch) return importMatch[1];

  return className;
}

// -- Build the graph with task expansion --

function buildGraph(elements, factoryContent, taskFiles, prefix = "") {
  const nodes = [];
  const edges = [];
  let startNode = null;

  for (const el of elements) {
    const nodeId = prefix ? `${prefix}.${el.name}` : el.name;

    if (el.type === "task") {
      const className = resolveTaskClassName(el.name, factoryContent);
      const taskFilePath = taskFiles.get(className);

      if (taskFilePath) {
        const taskData = parseTaskFile(taskFilePath);
        if (taskData) {
          const taskPrefix = prefix ? `${prefix}.${el.name}` : el.name;
          const sub = buildGraph(
            taskData.elements,
            fs.readFileSync(taskFilePath, "utf-8"),
            taskFiles,
            taskPrefix,
          );

          nodes.push(...sub.nodes);
          edges.push(...sub.edges);

          const taskFirstStep = taskData.firstStepName
            ? `${taskPrefix}.${taskData.firstStepName}`
            : null;

          if (el.isInitial) {
            startNode = taskFirstStep;
          }

          const taskExitId = `${taskPrefix}.__exit__`;
          nodes.push({
            id: taskExitId,
            label: `${humanize(el.name)} Complete`,
            isTerminal: false,
            isInternal: true,
          });

          // Redirect exitStep references to the real exit node
          edges.forEach((edge) => {
            if (edge.to === `${taskPrefix}.exitStep`) {
              edge.to = taskExitId;
            }
          });

          addEdgesForElement(
            el,
            taskExitId,
            edges,
            prefix,
            taskPrefix,
            taskFirstStep,
          );

          continue;
        }
      }
      nodes.push({
        id: nodeId,
        label: humanize(el.name),
        isTerminal: false,
        isTask: true,
      });
    } else {
      nodes.push({
        id: nodeId,
        label: humanize(el.name),
        isTerminal: el.noNext,
      });
    }

    if (el.isInitial) {
      startNode = nodeId;
    }

    addEdgesForElement(el, nodeId, edges, prefix);
  }

  return { nodes, edges, startNode };
}

function addEdgesForElement(
  el,
  fromId,
  edges,
  prefix,
  taskPrefix,
  taskFirstStep,
) {
  const resolveTarget = (target) => {
    if (target === "exitStep") {
      return prefix ? `${prefix}.__exit__` : "__exit__";
    }
    if (target.endsWith(".firstStep")) {
      const taskProp = target.replace(".firstStep", "");
      return `__first__${prefix ? prefix + "." : ""}${taskProp}`;
    }
    return prefix ? `${prefix}.${target}` : target;
  };

  if (el.nextStepEdges && el.nextStepEdges.length > 0) {
    for (const edge of el.nextStepEdges) {
      edges.push({
        from: fromId,
        to: resolveTarget(edge.target),
        label: edge.label,
      });
    }
  }

  if (el.nextUrl) {
    const urlNodeId = `__url__${fromId}`;
    edges.push({ from: fromId, to: urlNodeId, label: null, urlTarget: el.nextUrl });
  }

  if (el.nextDestEdges && el.nextDestEdges.length > 0) {
    for (const edge of el.nextDestEdges) {
      if (edge.target === "__dynamic__") {
        edges.push({
          from: fromId,
          to: `__dynamic__${fromId}`,
          label: edge.label,
          isDynamic: true,
        });
      } else if (edge.target.startsWith("__url__")) {
        edges.push({
          from: fromId,
          to: edge.target,
          label: edge.label,
          urlTarget: edge.target.replace("__url__", ""),
        });
      } else {
        edges.push({
          from: fromId,
          to: resolveTarget(edge.target),
          label: edge.label,
        });
      }
    }
  }
}

// -- Resolve __first__ references and dangling step refs --

function resolveFirstStepRefs(edges, nodes, taskFiles, factoryContent) {
  for (const edge of edges) {
    if (edge.to && edge.to.startsWith("__first__")) {
      const taskPath = edge.to.replace("__first__", "");
      const parts = taskPath.split(".");
      const taskPropName = parts[parts.length - 1];
      const className = resolveTaskClassName(taskPropName, factoryContent);
      const taskFilePath = taskFiles.get(className);

      if (taskFilePath) {
        const taskData = parseTaskFile(taskFilePath);
        if (taskData && taskData.firstStepName) {
          edge.to = `${taskPath}.${taskData.firstStepName}`;

          const existingNode = nodes.find((n) => n.id === edge.to);
          if (!existingNode) {
            edge.to = `${taskPath}`;
          }
        }
      }
    }
  }
}

function resolveDanglingRefs(edges, nodes) {
  const nodeIds = new Set(nodes.map((n) => n.id));

  for (const edge of edges) {
    if (
      !edge.to ||
      edge.to.startsWith("__url__") ||
      edge.to.startsWith("__dynamic__") ||
      edge.to.startsWith("__first__") ||
      nodeIds.has(edge.to)
    ) {
      continue;
    }

    // Edge points to a node that doesn't exist; try to find a matching expanded node
    const targetSuffix = edge.to.split(".").pop();
    const candidates = nodes.filter((n) => {
      const nSuffix = n.id.split(".").pop();
      return nSuffix === targetSuffix && n.id !== edge.to;
    });

    if (candidates.length === 1) {
      edge.to = candidates[0].id;
    } else if (candidates.length > 1) {
      // Prefer the candidate sharing the longest common prefix with edge.from
      const fromParts = edge.from.split(".");
      let best = candidates[0];
      let bestScore = 0;
      for (const c of candidates) {
        const cParts = c.id.split(".");
        let score = 0;
        for (let i = 0; i < Math.min(fromParts.length, cParts.length); i++) {
          if (fromParts[i] === cParts[i]) score++;
          else break;
        }
        if (score > bestScore) {
          bestScore = score;
          best = c;
        }
      }
      edge.to = best.id;
    }
  }
}

// -- Generate Mermaid --

function generateMermaid(graph) {
  const lines = ["flowchart TD"];
  const nodeIds = new Set(graph.nodes.map((n) => n.id));
  const urlNodes = new Set();
  const dynamicNodes = new Set();

  if (graph.startNode) {
    const sid = sanitizeId("__start__");
    const tid = sanitizeId(graph.startNode);
    lines.push(`    ${sid}([Start]) --> ${tid}`);
  }

  for (const node of graph.nodes) {
    const id = sanitizeId(node.id);
    const label = node.label;
    if (node.isTerminal) {
      lines.push(`    ${id}[/${label}\\]`);
    } else if (node.isInternal) {
      lines.push(`    ${id}((${label}))`);
    } else if (node.isTask) {
      lines.push(`    ${id}[[${label}]]`);
    } else {
      lines.push(`    ${id}[${label}]`);
    }
  }

  for (const edge of graph.edges) {
    const fromId = sanitizeId(edge.from);
    let toId;

    if (edge.urlTarget) {
      toId = sanitizeId(edge.to || `__url__${edge.from}`);
      if (!urlNodes.has(toId)) {
        urlNodes.add(toId);
        lines.push(`    ${toId}[/External URL\\]`);
      }
    } else if (edge.isDynamic) {
      toId = sanitizeId(edge.to);
      if (!dynamicNodes.has(toId)) {
        dynamicNodes.add(toId);
        lines.push(`    ${toId}{{Dynamic Destination}}`);
      }
    } else {
      toId = sanitizeId(edge.to);
    }

    if (!nodeIds.has(edge.to) && !urlNodes.has(toId) && !dynamicNodes.has(toId)) {
      if (
        !edge.to.startsWith("__exit__") &&
        !edge.to.startsWith("__url__") &&
        !edge.to.startsWith("__dynamic__")
      ) {
        nodeIds.add(edge.to);
        lines.push(`    ${toId}[${humanize(edge.to.split(".").pop())}]`);
      }
    }

    if (edge.label) {
      lines.push(`    ${fromId} -->|${edge.label}| ${toId}`);
    } else {
      lines.push(`    ${fromId} --> ${toId}`);
    }
  }

  return lines.join("\n");
}

// -- Discover journey factories --

function findJourneyFactories(filter) {
  const pattern = path
    .join(JOURNEYS_ROOT, "**", "*JourneyFactory.kt")
    .replace(/\\/g, "/");
  let files = glob.sync(pattern);

  files = files.filter(
    (f) =>
      !f.includes("forms/journeys") && !f.includes("forms\\journeys"),
  );

  // Exclude non-"New" factories if a "New" variant exists for the same journey
  // (e.g., prefer NewPropertyRegistrationJourneyFactory over PropertyRegistrationJourneyFactory
  // if both existed, but actually "New" IS the factory in this codebase)

  if (filter) {
    const lowerFilter = filter.toLowerCase();
    files = files.filter((f) => {
      const base = path.basename(f).toLowerCase();
      return base.includes(lowerFilter);
    });
  }

  return files;
}

function deriveJourneyName(filePath) {
  const base = path.basename(filePath, ".kt");
  return base
    .replace(/^New/, "")
    .replace(/JourneyFactory$/, "")
    .replace(/([a-z])([A-Z])/g, "$1 $2");
}

// -- Main --

function processJourney(factoryPath, taskFiles) {
  const content = fs.readFileSync(factoryPath, "utf-8");
  const journeyBody = extractBlockAfterArgs(content, "journey");
  if (!journeyBody) {
    console.error(`  Could not find journey block in ${factoryPath}`);
    return null;
  }

  const elements = parseElements(journeyBody);
  const graph = buildGraph(elements, content, taskFiles);

  resolveFirstStepRefs(graph.edges, graph.nodes, taskFiles, content);
  resolveDanglingRefs(graph.edges, graph.nodes);

  // Clean up unresolved edges
  graph.edges = graph.edges.filter((e) => {
    if (e.to.startsWith("__first__")) {
      console.warn(`  Warning: Could not resolve task reference: ${e.to}`);
      return false;
    }
    return true;
  });

  return graph;
}

function main() {
  const args = process.argv.slice(2);
  let filter = null;
  for (let i = 0; i < args.length; i++) {
    if (args[i] === "--filter" && args[i + 1]) {
      filter = args[i + 1];
      i++;
    }
  }

  console.log("Discovering task files...");
  const taskFiles = findTaskFiles();
  console.log(`  Found ${taskFiles.size} task classes`);

  console.log("Discovering journey factories...");
  const factories = findJourneyFactories(filter);
  console.log(`  Found ${factories.length} journey factories`);

  if (factories.length === 0) {
    console.log("No journey factories found.");
    process.exit(0);
  }

  if (!fs.existsSync(DOCS_DIR)) {
    fs.mkdirSync(DOCS_DIR, { recursive: true });
  }

  const generatedFiles = [];

  for (const factory of factories) {
    const name = deriveJourneyName(factory);
    console.log(`\nProcessing: ${name}`);

    const graph = processJourney(factory, taskFiles);
    if (!graph) continue;

    const mermaid = generateMermaid(graph);
    const fileName = `${name.replace(/\s+/g, "")}.md`;
    const outPath = path.join(DOCS_DIR, fileName);

    const mdContent = `# ${name}\n\n\`\`\`mermaid\n${mermaid}\n\`\`\`\n`;
    fs.writeFileSync(outPath, mdContent, "utf-8");
    generatedFiles.push(outPath);
    console.log(`  Wrote ${outPath}`);
  }

  console.log(`\nGenerated ${generatedFiles.length} diagram(s)`);
}

main();
