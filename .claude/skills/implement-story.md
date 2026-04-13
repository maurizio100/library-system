# Skill: implement-story

Implement a backlog story end-to-end following the project's domain-first workflow.

## Usage

```
/implement-story <story-id> <epic>
```

Examples:
- `/implement-story 011 lending`
- `/implement-story 014 catalog`

## Steps

### 1 — Read the spec

- Read the `.feature` file at `docs/stories/<epic>/<NNN>-<slug>.feature`
- Read the bounded context spec at `docs/domain/bounded-contexts/<epic>.md`
- Read the glossary at `docs/domain/glossary.md`
- If anything is ambiguous: **STOP and ask. Do not guess.**

### 2 — Create a branch

```bash
git checkout -b story/<NNN>-<short-name>
```

Use the story number and a kebab-case slug derived from the feature title.

### 3 — Implement the domain layer first

Location: `<epic>/src/main/kotlin/com/library/<epic>/domain/`

- Add or update entities and value objects using exact names from the glossary
- Enforce all invariants described in the bounded context spec
- Define repository interfaces here if new persistence is needed
- **No Spring imports** — pure Kotlin only

### 4 — Write tests

- **Cucumber step definitions** for every Gherkin scenario in the `.feature` file
  - Location: `<epic>/src/test/kotlin/com/library/<epic>/bdd/`
  - Naming: `<Feature>StepDefs.kt`
- **Unit tests** for non-trivial domain logic
  - Location: `<epic>/src/test/kotlin/com/library/<epic>/domain/`
  - Naming: `<ClassName>Test.kt`

All scenarios must be wired and passing before moving on.

### 5 — Implement API and infra layers

- **API layer** (`api/`): REST controller, DTOs, explicit mapping — never expose domain entities
- **Infra layer** (`infra/`): JPA entity, repository implementation, event publisher

### 6 — Publish domain events (if required by the spec)

- Domain events extend the marker interface from `:shared`
- Publish via Spring `ApplicationEventPublisher` in the infra layer
- Check `docs/domain/context-map.md` — if the event crosses contexts, verify the listener exists or create it

### 7 — Run full verification

```bash
./gradlew clean build
```

All tests (unit, BDD, ArchUnit) must be green. Fix any failures before committing.

### 8 — Commit and open PR

```bash
git commit -m "feat(<epic>): implement story <NNN> — <short title>"
```

Open a PR referencing the story. Move the story to **Done** in `Backlog.md`.
