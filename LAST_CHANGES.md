## 2026-03-07 — Project housekeeping: .gitignore, README.md, and Serena onboarding

### Files changed
- `.gitignore` — full rewrite with organised sections; key additions: `.serena/cache/` and `.serena/project.local.yml` (machine-specific Serena files, must not be committed); fixed critical bug where `docker-compose.yml` was being ignored (it is a source file and must be tracked); removed `compose.yaml` blanket ignore; kept `.serena/memories/` committable for shared agent context; added `production-opts.txt`
- `README.md` — full rewrite replacing boilerplate with complete project documentation: tech stack table, setup instructions, environment variables table (including all RabbitMQ vars), project structure tree with all messaging packages, architecture diagram, RabbitMQ bridge architecture section with queue/DLQ table, domain events table linking events to queues, full API reference, database schema table, available scripts, Mailhog + RabbitMQ Management UI guides, deployment info, links to AGENTS.md / NEXT_STEPS.md / LAST_CHANGES.md
- `NEXT_STEPS.md` — created: living roadmap with High / Medium / Low priorities
- `LAST_CHANGES.md` — created: this file; session changelog going forward
- `AGENTS.md` — added Rule 11: after every agent session with code changes, agent must update LAST_CHANGES.md and NEXT_STEPS.md

### Notes
- The critical `.gitignore` bug (docker-compose.yml ignored) means the compose file may never have been tracked in git. Worth running `git status` to confirm and committing it if needed.
- This project is more advanced than `E:\code\minhavenda` — RabbitMQ producer, consumer, message DTOs, DLQ config, and V10 migration are all present here.

---
