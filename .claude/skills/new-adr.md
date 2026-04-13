# Skill: new-adr

Create a new Architecture Decision Record and register it in the decisions folder.

## Usage

```
/new-adr "<title>"
```

Examples:
- `/new-adr "WebSocket vs SSE for real-time updates"`
- `/new-adr "Pagination strategy for catalog endpoints"`

## Steps

### 1 — Determine the ADR number

- List files in `docs/decisions/`
- The next number is `max(existing numbers) + 1`, zero-padded to 3 digits (e.g. `008`)

### 2 — Derive the file slug

Convert the title to lowercase kebab-case.

File path: `docs/decisions/adr-<NNN>-<slug>.md`

### 3 — Write the ADR

Use this structure:

```markdown
# ADR-<NNN>: <Title>

## Status
Proposed

## Context
<What situation or constraint forces a decision? What problem are we solving?
Include relevant quality attributes from docs/architecture/quality-attributes.md if applicable.>

## Options Considered

### Option A — <name>
<Brief description. Trade-offs.>

### Option B — <name>
<Brief description. Trade-offs.>

## Decision
<Which option is chosen and why. Reference the quality attributes that drove the choice.>

## Consequences
<What becomes easier or harder as a result. Any follow-up work required.>
```

Rules:
- Status starts as **Proposed** — the user changes it to **Accepted** or **Rejected** after review
- Keep each section concise; this is a record, not an essay
- If the decision affects both bounded contexts, note it explicitly
- If it supersedes an existing ADR, add `Supersedes: ADR-<NNN>` after the Status line
