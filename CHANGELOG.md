# Changelog

All notable changes to this project are documented here. For detailed upgrade instructions, see the relevant section below.

**Format:** [Keep a Changelog](https://keepachangelog.com) | **Versioning:** [Semantic Versioning](https://semver.org)

---

## Latest Release: v1.2.0 (June 13, 2026)

**Major Feature Release** — Production-ready database integration with real-time updates and advanced analytics.

### What's New

**Data Persistence** — Migrated from in-memory storage to PostgreSQL with Spring Data JPA, Hibernate, and Flyway schema management.

**Real-time Updates** — STOMP-based WebSockets enable live notifications for ticket creation and payment processing. Subscribe to `/topic/tickets`, `/topic/payments`, and `/topic/analytics`.

**GraphQL API** — New flexible `/graphql` endpoint for querying customers, tickets, and analytics. Interactive schema explorer at `/graphiql`.

**Analytics Dashboard** — Frontend dashboard visualizes ticket status distribution, customer tier breakdown, and revenue metrics using Recharts.

**Unified Logging** — `CustomLogger` service provides consistent, production-ready logging across backend and frontend.

**Resilient Error Handling** — Enhanced error boundaries with automatic retry and detailed diagnostics.

---

## Quick Reference

### Current Stable Versions

| Component | Version | Status | Released |
|-----------|---------|--------|----------|
| **Backend** | 1.2.0 | ✅ Stable | 2026-06-13 |
| **Frontend** | 1.2.0 | ✅ Stable | 2026-06-13 |
| Java | 17+ | ✅ Required | — |
| Spring Boot | 3.4.5 | ✅ Current | — |
| React | 19 | ✅ Current | — |
| Google ADK | 1.3.0 | ✅ Current | — |

### Upgrade Path

- **From 1.1.5 → 1.2.0:** Database setup required; see [Upgrade Guide](#upgrade-from-115-to-120)
- **From 1.1.4 → 1.1.5:** Bug fixes only; see [v1.1.5](#115---2026-05-27)
- **From 1.1.3 → 1.1.4:** No breaking changes; see [v1.1.4](#114---2026-05-25)

---

## Detailed Release History

### 1.2.0 - 2026-06-13

#### Added

**Database Integration:** Full PostgreSQL persistence layer using Spring Data JPA and Hibernate. Flyway manages schema versioning and auto-seeding.

**GraphQL API:** `/graphql` endpoint for flexible queries and mutations.
- Query: customers, tickets, analytics
- Mutate: create payments, update settings
- GraphiQL playground at `/graphiql`

**Real-time WebSockets:** STOMP messaging on `/ws` with topics:
- `/topic/tickets` — New ticket creation
- `/topic/payments` — Payment processing
- `/topic/analytics` — Refresh signals

**Analytics Dashboard:** Recharts-based frontend visualization of support metrics, ticket status, and revenue trends.

**Custom Logger Service:** Unified logging across frontend and backend for production diagnostics.

#### Changed

**Backend Architecture:** All customer and ticket operations now use database persistence.

**Frontend Integration:** Migrated to `urql` for GraphQL queries; native WebSocket support.

**Test Infrastructure:** Isolated test profile with H2 in-memory database and mocked external services.

#### Fixed

**State Decoupling:** Tests no longer couple to external API keys through improved configuration management.

---

### 1.1.5 - 2026-05-27

#### Fixed

**Type Alignment (`types.ts`):** Corrected `StockData.price` and `StockData.change` from `string` to `number`. Fixed `MarketReport` field names: `content` → `text`, `sources` → `groundingSources`.

**History Hook (`useHistory.ts`):** Fixed import of `MarketReport` from `'../types'` to `'../services/geminiService'` (source of truth). Resolved deduplication bug where non-existent `report.timestamp` field caused false duplicates. Introduced `HistoryEntry` type extending `MarketReport`.

**Maven Dependencies (`pom.xml`):** Corrected `google-adk.version` to `1.3.0` and `google-cloud-ai.version` to `3.93.0`.

**Health Check Version (`App.java`):** Updated `/api/health` response version from `1.1.2` to `1.1.4`.

**Quick Start Scripts:** Updated JAR filename references from `customer-support-agent-1.1.2.jar` to `customer-support-agent-1.1.4.jar`.

**Impact:** Resolves TypeScript compilation errors, type mismatches in components, and Maven build failures.

---

### 1.1.4 - 2026-05-25

#### Added

**Markdown Support:** `react-markdown` integration in `ReportView` for rich text rendering.

**Lucide Icons:** Consistent iconography across dashboard.

**Performance Optimizations:** Refactored `MarketChart` and `StockComparison` with `useCallback` and `useMemo` to eliminate component-inside-render anti-patterns.

**Lazy State Initialization:** Enhanced `App` component state hydration.

#### Fixed

**Maven Build:** Migrated from `fmt-maven-plugin` to `spotless-maven-plugin` with `google-java-format` to resolve JDK 17+ module access errors.

**ADK Dependency:** Updated to version `1.3.0`.

**Frontend Tests:** Improved regex matching in `App.test.tsx` for markdown content.

#### Changed

**Version Sync:** Project version unified to `1.1.4` across `pom.xml`, `package.json`, and docs.

**Dependency Upgrades:** All frontend packages updated to latest compatible versions.

---

### 1.1.3 - 2026-05-24

#### Fixed

**ReportView Props Mismatch:** Corrected interface from `{title, content, groundingSources, timestamp}` to `{report, sources}`. Prevents undefined prop errors.

**Missing axios Dependency:** Added `axios@^1.6.5` to `package.json`. (`geminiService.ts` was importing axios without declaring it.)

**geminiService Refactor:** Replaced axios REST API with official `@google/generative-ai` SDK for Gemini 2.0 Flash integration. Provides better type safety and built-in retry (3 attempts with 1s, 2s, 4s delays).

**Impact:** Resolves build failures, component rendering errors, and API communication issues.

---

### 1.1.2 - 2026-05-24

#### Fixed

**Frontend Crash:** Replaced global `process.env.NODE_ENV` with Vite-standard `import.meta.env.DEV` in `ErrorBoundary.tsx`.

**Sparkline SVG:** Removed `#` from gradient IDs in `Sparkline.tsx` to fix invalid SVG structure.

**Grounding Sources:** Added support for nested `web.title` and `web.uri` structures in `ReportView.tsx`.

#### Changed

**Gemini Model:** Switched from experimental `gemini-2.0-flash-exp` to stable `gemini-2.0-flash`.

**Dependency Upgrades:**
- Spring Boot → 3.4.5
- Google ADK → 1.3.0
- Google Cloud AI → 3.93.0
- Mockito → 5.23.0

---

### 1.1.1 - 2026-03-17

**Changed:** Upgraded default Gemini model to `gemini-2.0-flash` in `AgentConfiguration.java`.

**Fixed:** Improved error handling in `geminiService.ts` to prevent unexpected `TypeError` during API failures. Corrected frontend test mocks for consecutive failures.

---

### 1.1.0 - 2026-02-02

#### Added

**Report Persistence:** Implemented `localStorage` for chat history and analysis reports.

**Copy Report Feature:** Clipboard integration in `ReportView` component.

**Tailwind CSS 4:** Migrated entire styling system for improved performance.

#### Changed

**Major Framework Upgrades:**
- React 18 → 19.0.0
- Vite 6 → 7.0.0
- Vitest 3 → 4.0.0
- Spring Boot 3.4.2 → 3.4.5
- Google ADK 0.5.0 → 1.3.0

**Component Refactoring:** Moved sub-components outside render cycle to prevent performance degradation.

**Hook Optimization:** Enhanced `useHistory` with lazy initialization.

#### Fixed

**State Persistence Bug:** Fixed incorrect history hydration on initial load.

**Vitest Mocking:** Corrected environment variable and class constructor mocks for Vitest 4 and React 19 compatibility.

**JDK 17 Compatibility:** Reverted `fmt-maven-plugin` to 2.9.1.

---

### 1.0.6 - 2025-12-22

**Fixed:** Maven dependencies, Java test null pointer exceptions, frontend service mocks.

**Changed:** Full codebase formatting via `mvn fmt:format` and `npm run lint:fix`.

---

### 1.0.5 - 2025-12-19

**Fixed:** Restored corrupted `App.java`, fixed invalid Java in `AgentConfiguration.java`, updated test suite for Vitest.

**Added:** `quick-start.ps1` PowerShell script and `.env` template.

---

### 1.0.4 - 2025-12-14

**Added:** 35 comprehensive unit tests with 100% coverage, input validation utilities, integration tests, deployment configs.

**Fixed:** All compilation errors, parameter type mismatches, amount validation, refund workflow, context caching.

---

### 1.0.3 - 2025-12-12

**Added:** Initial multi-agent architecture, 7 core tools, Spring Boot REST API, 33 test methods.

---

### 1.0.2 - 1.0.0 - 2025-12-05 to 2025-12-10

**Project Initialization:** Scaffolding, Maven config, Spring Boot skeleton, basic tests.

---

## Upgrade Guides

### Upgrade from 1.1.5 to 1.2.0

**Effort:** ~30 minutes | **Downtime:** ~5 minutes | **Rollback:** Database required

#### Prerequisites

- PostgreSQL instance (local or remote)
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` configured

#### Steps

1. **Backup current state** (in-memory data is lost on shutdown)
   ```bash
   # Export any critical data if needed
   ```

2. **Update dependencies**
   ```bash
   mvn clean install
   npm install
   ```

3. **Configure database**
   ```bash
   export DB_HOST="localhost"
   export DB_PORT="5432"
   export DB_NAME="customer_support"
   export DB_USER="postgres"
   export DB_PASSWORD="password"
   
   createdb customer_support
   ```

4. **Run migrations** (automatic via Flyway on startup)
   ```bash
   mvn spring-boot:run
   # Flyway creates schema and seeds test data
   ```

5. **Verify GraphQL endpoint**
   ```bash
   curl http://localhost:8000/graphql
   # Status: 200
   ```

6. **Test WebSocket connectivity**
   ```bash
   websocat ws://localhost:8000/ws
   # Should connect successfully
   ```

#### Breaking Changes

**None** — All v1.1.5 APIs remain functional. New endpoints are additive.

#### Migration Path for In-Memory Data

If you have critical customer data in the in-memory store:

1. Export via REST API before upgrading
2. Run v1.2.0 with Flyway (creates schema)
3. Re-insert data via `/api/customer` endpoints or direct database inserts

---

### Upgrade from 1.1.4 to 1.1.5

**Effort:** ~5 minutes | **Downtime:** None | **Rollback:** Simple

#### Steps

1. **Pull latest changes**
   ```bash
   git pull origin main
   ```

2. **Update files** (4 critical files changed)
   ```bash
   # Java
   # - pom.xml (dependency versions)
   # - App.java (health check version)
   
   # Frontend
   # - types.ts (field types & names)
   # - hooks/useHistory.ts (import source)
   
   # Scripts
   # - quick-start.sh, quick-start.ps1 (JAR filenames)
   ```

3. **Verify build**
   ```bash
   mvn clean install
   npm install
   npm run build
   ```

4. **Deploy**
   ```bash
   mvn spring-boot:run
   ```

#### Breaking Changes

**None** — All changes are backward compatible.

---

### Upgrade from 1.1.3 to 1.1.4

**Effort:** ~10 minutes | **Downtime:** None

#### Steps

1. **Update frontend**
   ```bash
   npm install  # Install react-markdown, lucide-react
   npm run build
   ```

2. **Update backend**
   ```bash
   mvn clean install
   ```

3. **Restart application**
   ```bash
   mvn spring-boot:run
   ```

#### Breaking Changes

**None** — Backward compatible with v1.1.3 APIs.

---

## Known Issues

### Current (v1.2.0)

- None reported

### Fixed in v1.2.0

- ✅ In-memory storage limitations
- ✅ Lack of real-time updates
- ✅ GraphQL API absence

### Fixed in v1.1.5

- ✅ Type mismatches in `StockData` and `MarketReport`
- ✅ History deduplication bug
- ✅ Maven dependency version mismatches
- ✅ Health check version mismatch
- ✅ Quick start script JAR references

---

## Deprecations

### Current

- **v1.1.0 and earlier:** No longer supported; upgrade to v1.1.5+ recommended

---

## Roadmap

### v1.3.0 (Q3 2026)

- Machine learning for intelligent ticket routing
- Slack/Teams channel integrations
- Mobile app support (React Native)
- Real-time market alerts

### v2.0.0 (Q4 2026)

- Microservices architecture (separate services per agent)
- Kubernetes deployment manifests
- Multi-tenant support with organization isolation
- Advanced authentication (OAuth2, JWT, OIDC)
- Performance: target <50ms p99 latency, 1000 req/sec throughput

---

## Support

- **Issues:** [GitHub Issues](https://github.com/darshil0/customer-support-agent/issues)
- **Discussions:** [GitHub Discussions](https://github.com/darshil0/customer-support-agent/discussions)
- **Email:** support@example.com
- **Documentation:** [Wiki](https://github.com/darshil0/customer-support-agent/wiki)

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on submitting issues, feature requests, and pull requests.

---

Made with ❤️ by Darshil

**Last Updated:** June 13, 2026  
**Current Version:** 1.2.0  
**Status:** ✅ Production-Ready | 100% Test Coverage | Zero Critical Issues
