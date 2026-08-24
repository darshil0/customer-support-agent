# Changelog

All notable changes to this project are documented here. For detailed upgrade instructions, see [Upgrade Guides](#upgrade-guides).

**Format:** [Keep a Changelog](https://keepachangelog.com) | **Versioning:** [Semantic Versioning](https://semver.org)

**Current Version:** 1.2.1 | **Last Updated:** June 24, 2026 | **Status:** Production-Ready · 100% Test Coverage · Zero Critical Issues

---

## Table of Contents

- [Quick Reference](#quick-reference)
- [Release History](#release-history)
- [Upgrade Guides](#upgrade-guides)
- [Known Issues](#known-issues)
- [Deprecations](#deprecations)
- [Roadmap](#roadmap)
- [Support](#support)
- [Contributing](#contributing)

---

## Quick Reference

### Current Stable Versions

| Component | Version | Status | Released |
|---|---|---|---|
| Backend | 1.2.1 | Stable | 2026-06-24 |
| Frontend | 1.2.1 | Stable | 2026-06-24 |
| Java | 17+ | Required | — |
| Spring Boot | 3.4.5 | Current | — |
| React | 19 | Current | — |
| Google ADK | 1.5.0 | Current | — |

### Upgrade Path

| From → To | Effort | Notes |
|---|---|---|
| 1.2.0 → 1.2.1 | ~5 min | Safety guardrails & dependency updates — see [guide](#upgrade-from-120-to-121) |
| 1.1.5 → 1.2.0 | ~30 min | Database setup required — see [guide](#upgrade-from-115-to-120) |
| 1.1.4 → 1.1.5 | ~5 min | Bug fixes only — see [v1.1.5](#115-2026-05-27) |
| 1.1.3 → 1.1.4 | ~10 min | No breaking changes — see [v1.1.4](#114-2026-05-25) |

---

## Release History

### 1.2.1 — 2026-06-24

**Major Enterprise Release** — Production readiness audit, multi-agent safety guardrails, 100% test coverage, and Google ADK 1.5.0 upgrade.

#### Added
- **Production safety guardrails:** anti-prompt-injection instructions and HTML tag sanitization for customer inputs; non-hallucination policy forcing agents to rely strictly on tool responses; tenant boundary enforcement preventing cross-customer data access; mandatory two-step confirmation and validation for irreversible operations (payments and refunds).
- **Comprehensive backend test suite:** 50 Java unit and integration tests covering multi-agent orchestration and routing (`CustomerSupportAgentTest`), production readiness scenarios (`ProductionReadinessScenariosTest`), GraphQL controller endpoints (`SupportGraphQLControllerTest`), and Spring Data JPA repositories (`CustomerRepositoryTest`, `TicketRepositoryTest`).
- **H2 test database environment:** `src/test/resources/application-test.properties` and `src/test/resources/schema.sql` for self-contained Spring Boot testing without external database dependencies.
- **CI/CD pipeline:** GitHub Actions workflow (`.github/workflows/ci.yml`) enforcing Spotless code formatting, Maven backend tests, and Vitest frontend tests.

#### Changed
- **ADK upgrade:** Google Agent Development Kit updated to `1.5.0`; Google Cloud AI Platform updated to `3.94.0`.
- **Version synchronization:** unified version `1.2.1` across `pom.xml`, `package.json`, `App.java`, `quick-start.sh`, `quick-start.ps1`, `Dockerfile`, and documentation.
- **Spring configuration:** removed redundant `EnvironmentConfig.java` in favor of `@Configuration`.

#### Fixed
- **Frontend test suite:** resolved Vitest compatibility issues by adding the missing `react-is` dependency and fixing component test mocks; all 15 frontend tests now pass cleanly.
- **Database schema compatibility:** ensured H2 dialect compatibility for the Spring Boot `test` profile.

#### Breaking Changes
None — all v1.2.0 APIs and database schemas remain fully functional and backward-compatible.

---

### 1.2.0 — 2026-06-13

#### Added
- **Database integration:** full PostgreSQL persistence layer using Spring Data JPA and Hibernate; Flyway manages schema versioning and auto-seeding.
- **GraphQL API:** `/graphql` endpoint for flexible queries and mutations — query customers, tickets, and analytics; mutate to create payments and update settings; GraphiQL playground at `/graphiql`.
- **Real-time WebSockets:** STOMP messaging on `/ws` with topics `/topic/tickets` (new ticket creation), `/topic/payments` (payment processing), and `/topic/analytics` (refresh signals).
- **Analytics dashboard:** Recharts-based frontend visualization of support metrics, ticket status, and revenue trends.
- **Custom logger service:** unified logging across frontend and backend for production diagnostics.

#### Changed
- **Backend architecture:** all customer and ticket operations now use database persistence.
- **Frontend integration:** migrated to `urql` for GraphQL queries; added native WebSocket support.
- **Test infrastructure:** isolated test profile with H2 in-memory database and mocked external services.

#### Fixed
- **State decoupling:** tests no longer couple to external API keys, via improved configuration management.

#### Breaking Changes
None — all v1.1.5 APIs remain functional; new endpoints are additive.

---

### 1.1.5 — 2026-05-27

#### Fixed
- **Type alignment (`types.ts`):** corrected `StockData.price` and `StockData.change` from `string` to `number`; fixed `MarketReport` field names (`content` → `text`, `sources` → `groundingSources`).
- **History hook (`useHistory.ts`):** fixed import of `MarketReport` from `'../types'` to `'../services/geminiService'` (source of truth); resolved a deduplication bug caused by a non-existent `report.timestamp` field; introduced a `HistoryEntry` type extending `MarketReport`.
- **Maven dependencies (`pom.xml`):** corrected `google-adk.version` to `1.3.0` and `google-cloud-ai.version` to `3.93.0`.
- **Health check version (`App.java`):** updated `/api/health` response version from `1.1.2` to `1.1.4`.
- **Quick start scripts:** updated JAR filename references from `customer-support-agent-1.1.2.jar` to `customer-support-agent-1.1.4.jar`.

**Impact:** resolves TypeScript compilation errors, type mismatches in components, and Maven build failures.

---

### 1.1.4 — 2026-05-25

#### Added
- **Markdown support:** `react-markdown` integration in `ReportView` for rich text rendering.
- **Lucide icons:** consistent iconography across the dashboard.
- **Performance optimizations:** refactored `MarketChart` and `StockComparison` with `useCallback` and `useMemo` to eliminate component-inside-render anti-patterns.
- **Lazy state initialization:** enhanced `App` component state hydration.

#### Changed
- **Version sync:** project version unified to `1.1.4` across `pom.xml`, `package.json`, and docs.
- **Dependency upgrades:** all frontend packages updated to latest compatible versions.

#### Fixed
- **Maven build:** migrated from `fmt-maven-plugin` to `spotless-maven-plugin` with `google-java-format`, resolving JDK 17+ module access errors.
- **ADK dependency:** updated to version `1.3.0`.
- **Frontend tests:** improved regex matching in `App.test.tsx` for markdown content.

#### Breaking Changes
None — backward compatible with v1.1.3 APIs.

---

### 1.1.3 — 2026-05-24

#### Fixed
- **`ReportView` props mismatch:** corrected the interface from `{title, content, groundingSources, timestamp}` to `{report, sources}`, preventing undefined prop errors.
- **Missing `axios` dependency:** added `axios@^1.6.5` to `package.json` (`geminiService.ts` was importing axios without declaring it).
- **`geminiService` refactor:** replaced the axios REST API call with the official `@google/generative-ai` SDK for Gemini 2.0 Flash integration, providing better type safety and built-in retry (3 attempts with 1s, 2s, 4s delays).

**Impact:** resolves build failures, component rendering errors, and API communication issues.

---

### 1.1.2 — 2026-05-24

#### Changed
- **Gemini model:** switched from experimental `gemini-2.0-flash-exp` to stable `gemini-2.0-flash`.
- **Dependency upgrades:** Spring Boot → 3.4.5, Google ADK → 1.3.0, Google Cloud AI → 3.93.0, Mockito → 5.23.0.

#### Fixed
- **Frontend crash:** replaced global `process.env.NODE_ENV` with Vite-standard `import.meta.env.DEV` in `ErrorBoundary.tsx`.
- **Sparkline SVG:** removed `#` from gradient IDs in `Sparkline.tsx` to fix invalid SVG structure.
- **Grounding sources:** added support for nested `web.title` and `web.uri` structures in `ReportView.tsx`.

---

### 1.1.1 — 2026-03-17

#### Changed
- Upgraded the default Gemini model to `gemini-2.0-flash` in `AgentConfiguration.java`.

#### Fixed
- Improved error handling in `geminiService.ts` to prevent unexpected `TypeError`s during API failures; corrected frontend test mocks for consecutive failures.

---

### 1.1.0 — 2026-02-02

#### Added
- **Report persistence:** implemented `localStorage` for chat history and analysis reports.
- **Copy report feature:** clipboard integration in the `ReportView` component.
- **Tailwind CSS 4:** migrated the entire styling system for improved performance.

#### Changed
- **Major framework upgrades:** React 18 → 19.0.0, Vite 6 → 7.0.0, Vitest 3 → 4.0.0, Spring Boot 3.4.2 → 3.4.5, Google ADK 0.5.0 → 1.3.0.
- **Component refactoring:** moved sub-components outside the render cycle to prevent performance degradation.
- **Hook optimization:** enhanced `useHistory` with lazy initialization.

#### Fixed
- **State persistence bug:** fixed incorrect history hydration on initial load.
- **Vitest mocking:** corrected environment variable and class constructor mocks for Vitest 4 and React 19 compatibility.
- **JDK 17 compatibility:** reverted `fmt-maven-plugin` to 2.9.1.

---

### 1.0.6 — 2025-12-22

#### Changed
- Full codebase formatting via `mvn fmt:format` and `npm run lint:fix`.

#### Fixed
- Maven dependencies, Java test null pointer exceptions, and frontend service mocks.

---

### 1.0.5 — 2025-12-19

#### Added
- `quick-start.ps1` PowerShell script and a `.env` template.

#### Fixed
- Restored corrupted `App.java`, fixed invalid Java in `AgentConfiguration.java`, and updated the test suite for Vitest.

---

### 1.0.4 — 2025-12-14

#### Added
- 35 comprehensive unit tests with 100% coverage, input validation utilities, integration tests, and deployment configs.

#### Fixed
- All compilation errors, parameter type mismatches, amount validation, the refund workflow, and context caching.

---

### 1.0.3 — 2025-12-12

#### Added
- Initial multi-agent architecture, 7 core tools, a Spring Boot REST API, and 33 test methods.

---

### 1.0.2 – 1.0.0 — 2025-12-05 to 2025-12-10

**Project Initialization:** scaffolding, Maven configuration, Spring Boot skeleton, basic tests.

---

## Upgrade Guides

### Upgrade from 1.2.0 to 1.2.1

**Effort:** ~5 minutes | **Downtime:** None | **Rollback:** Simple

**Prerequisites:** Java 17+, Maven 3.8+, Node.js 18+

1. **Pull latest changes**
   ```bash
   git pull origin main
   ```
2. **Update dependencies**
   ```bash
   mvn clean install
   npm install --legacy-peer-deps
   ```
3. **Run tests**
   ```bash
   mvn test
   npm test -- --watch=false
   ```
4. **Restart application**
   ```bash
   mvn spring-boot:run
   ```

**Breaking Changes:** None — all v1.2.0 APIs and database schemas remain fully functional and backward-compatible.

---

### Upgrade from 1.1.5 to 1.2.0

**Effort:** ~30 minutes | **Downtime:** ~5 minutes | **Rollback:** Database required

**Prerequisites:** a PostgreSQL instance (local or remote); `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` configured

1. **Back up current state** — in-memory data is lost on shutdown, so export any critical data first.
2. **Update dependencies**
   ```bash
   mvn clean install
   npm install
   ```
3. **Configure the database**
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
5. **Verify the GraphQL endpoint**
   ```bash
   curl http://localhost:8000/graphql
   # Status: 200
   ```
6. **Test WebSocket connectivity**
   ```bash
   websocat ws://localhost:8000/ws
   # Should connect successfully
   ```

**Breaking Changes:** None — all v1.1.5 APIs remain functional; new endpoints are additive.

**Migration path for in-memory data:** if you have critical customer data in the in-memory store, export it via the REST API before upgrading, run v1.2.0 so Flyway creates the schema, then re-insert the data via the `/api/customer` endpoints or direct database inserts.

---

### Upgrade from 1.1.4 to 1.1.5

**Effort:** ~5 minutes | **Downtime:** None | **Rollback:** Simple

Four files changed: `pom.xml` (dependency versions), `App.java` (health check version), `types.ts` (field types & names), `hooks/useHistory.ts` (import source), plus `quick-start.sh` and `quick-start.ps1` (JAR filenames).

1. **Pull latest changes**
   ```bash
   git pull origin main
   ```
2. **Verify the build**
   ```bash
   mvn clean install
   npm install
   npm run build
   ```
3. **Deploy**
   ```bash
   mvn spring-boot:run
   ```

**Breaking Changes:** None — all changes are backward compatible.

---

### Upgrade from 1.1.3 to 1.1.4

**Effort:** ~10 minutes | **Downtime:** None

1. **Update frontend**
   ```bash
   npm install  # installs react-markdown, lucide-react
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

**Breaking Changes:** None — backward compatible with v1.1.3 APIs.

---

## Known Issues

### Current (v1.2.1)
None reported.

### Fixed in v1.2.1
- Unhandled prompt injection vulnerabilities in customer inputs
- Test failures when running without a live API key or PostgreSQL
- Version inconsistencies across project files
- Frontend test environment missing the `react-is` peer dependency

### Fixed in v1.2.0
- In-memory storage limitations
- Lack of real-time updates
- Absence of a GraphQL API

### Fixed in v1.1.5
- Type mismatches in `StockData` and `MarketReport`
- History deduplication bug
- Maven dependency version mismatches
- Health check version mismatch
- Quick start script JAR references

---

## Deprecations

- **v1.1.5 and earlier:** no longer supported; upgrade to v1.2.1 recommended.

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
- Performance target: <50ms p99 latency, 1,000 req/sec throughput

---

## Support

- **Issues:** [GitHub Issues](https://github.com/darshil0/customer-support-agent/issues)
- **Discussions:** [GitHub Discussions](https://github.com/darshil0/customer-support-agent/discussions)
- **Email:** support@example.com
- **Documentation:** [Wiki](https://github.com/darshil0/customer-support-agent/wiki)

---

## Contributing

See `CONTRIBUTING.md` for guidelines on submitting issues, feature requests, and pull requests.

---

Made with care by Darshil.
