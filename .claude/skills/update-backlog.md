# Skill: update-backlog

Move a story between columns in Backlog.md.

## Usage

```
/update-backlog <story-id> <epic> <status>
```

Status values: `ready` | `in-progress` | `done`

Examples:
- `/update-backlog 006 lending in-progress`
- `/update-backlog 006 lending done`

## Steps

### 1 — Find the story line

Read `Backlog.md` and locate the line for `<story-id>` in the `<epic>` section.
The line format is:
```
- [ ] [<NNN> — <Title>](...) · `<epic>` · complexity: <S|M|L>
```

If the story is not found, report it and stop.

### 2 — Determine source and target columns

| Status argument | Target heading | Checkbox |
|---|---|---|
| `ready` | `### Ready` | `- [ ]` |
| `in-progress` | `### In Progress` | `- [ ]` |
| `done` | `### Done` | `- [x]` |

### 3 — Update the file

1. Remove the story line from its current column
2. Update the checkbox: `- [ ]` for ready/in-progress, `- [x]` for done
3. Insert the line at the **top** of the target column's list
   - Exception: Done column — insert at the top (most recently completed first)

### 4 — Confirm

Print the updated line and its new location so the user can verify the change.
