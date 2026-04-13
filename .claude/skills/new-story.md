# Skill: new-story

Scaffold a new Gherkin feature file and register it in the backlog.

## Usage

```
/new-story <epic> "<title>"
```

Examples:
- `/new-story catalog "Edit Book Metadata"`
- `/new-story lending "Suspend a Member"`

## Steps

### 1 — Determine the story number

- List files in `docs/stories/<epic>/`
- The next number is `max(existing numbers) + 1`, zero-padded to 3 digits (e.g. `014`)

### 2 — Derive the file slug

Convert the title to lowercase kebab-case. Examples:
- "Edit Book Metadata" → `edit-book-metadata`
- "Suspend a Member" → `suspend-a-member`

File path: `docs/stories/<epic>/<NNN>-<slug>.feature`

### 3 — Write the feature file

Use this structure:

```gherkin
@epic:<epic> @context:<epic>
Feature: <Title>
  As a librarian
  I want to <action>
  So that <benefit>

  # ── Happy Path ──

  Scenario: <primary success scenario>
    Given ...
    When ...
    Then ...

  # ── Edge Cases & Failures ──

  Scenario: <first failure case>
    Given ...
    When ...
    Then ...
```

Rules:
- Tags on the first line: `@epic:<epic> @context:<epic>`
- At least one happy-path scenario and at least one failure scenario
- Use exact entity and field names from `docs/domain/glossary.md`
- Domain events referenced in `Then` steps must exist in `shared/events/` or be planned

### 4 — Estimate complexity

| Label | Meaning |
|---|---|
| S | Single aggregate, no new events, no UI |
| M | New event, cross-context listener, or UI form |
| L | New aggregate, schema migration, or external integration |

### 5 — Add to the backlog

Open `Backlog.md` and add an entry to the **Ready** column of the correct epic section:

```markdown
- [ ] [<NNN> — <Title>](docs/stories/<epic>/<NNN>-<slug>.feature) · `<epic>` · complexity: <S|M|L>
```

Insert it at the **top** of the Ready list (highest priority first).
