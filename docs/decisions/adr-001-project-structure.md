# ADR-001: Project Structure

## Status
Accepted

## Context
We are setting up a new project using the agent-driven development workflow.
The repository structure must support:
- AI agent navigation and autonomous implementation
- Obsidian as the documentation editor (vault pointed at project root)
- Domain-driven design with bounded context isolation
- Automated workflows via GitHub Actions

## Decision
We adopt the standard directory layout from the Project Kickstart Checklist:
- `/docs` for all specifications (domain, architecture, decisions, skills, stories)
- `/CLAUDE.md` as the agent entry point
- `/Backlog.md` as the single source of truth for work items
- `/.claude/hooks/` for agent guardrails
- `/.github/` for CI/CD and issue templates

## Consequences
- All documentation is Markdown — editable in Obsidian, readable by agents, version-controlled in Git
- No tech stack is committed yet — that decision is deferred until Phase 2
- The `/src` directory will be created when the first bounded context is implemented
