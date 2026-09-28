<!--
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
-->

<div align="center">

<h1>KuYiji · cosmo-hhim-open</h1>

<h3>Making every product, every work log and every settlement on the shop floor traceable</h3>

English | [简体中文](README.md)

[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-1.8%2B-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.3.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x-42b883.svg)](https://vuejs.org/)
[![uni-app](https://img.shields.io/badge/uni--app-H5%20%2B%20MiniProgram-2b9939.svg)](https://uniapp.dcloud.net.cn/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-4479A1.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-6%2B-DC382D.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://docs.docker.com/compose/)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-ff69b4.svg)](CONTRIBUTING.md)

</div>

---

<div align="center">

<img width="1000" src="docs/images/KuReadme.png" alt="KuYiji at a glance: role collaboration, business flow and where analytics / AI plug in">

<br>

<sub><b>KuYiji at a glance</b> — one tenant-isolated workspace, <b>five roles</b> each doing their part (operator logs → supervisor audits → inspector inspects → WIP inventory → admin settles), all pushing the same chain forward: <b>log → audit → pass inspection → complete</b>, with every step leaving data behind. At the bottom, that data ends up in <b>analytics</b> (pass-rate drill-down / work-log ranking / inventory analysis) and <b>AI Query</b> (ask in plain language, answers cite their source).</sub>

</div>

---

**KuYiji** (`Ku易记`, "easy to record") is an open-source lean production management platform built for **discrete manufacturing shop floors**. It puts the entire frontline loop — *operator work logging → supervisor audit → quality inspection → finished-goods receipt → piece-rate settlement* — into a **WeChat Mini Program and a mobile H5 web app**, then layers **business analytics** and **natural-language AI querying** on top. Production data that used to be scattered across paper slips, spreadsheets and WeChat groups finally becomes structured, traceable, analysable and settleable.

**This is not just another admin dashboard.** It starts from the operator's phone: products and processes are picked from lists — no codes to memorise, no routing to remember, one work log in three steps. An audit flow a supervisor will actually use every day. A dashboard an owner understands at a glance.

<div align="center">

<img width="1000" src="docs/images/screens-main.png" alt="KuYiji's four main screens: logging · audit · WIP inventory · dashboard">

<br><br>

**📱 Logging**　Pick product & process, done in three steps　　　**✅ Audit**　Over-report / low pass rate / over-capacity flagged automatically<br>
**📦 WIP inventory**　Product & process views with health alerts　　**📊 Dashboard**　Pass rate, output and audit progress in one screen

</div>

---

## 🚀 Core Features

### 🏭 A closed loop across the whole shop floor
> *From a single tap on the operator's phone to a settlement sheet on the finance desk — every step leaves a trace.*

- **Work Logging / Production Reporting** — Operators log output by product + process. Supports **batch logging**, **backfilling (last 7 days)**, photo evidence, and process-level inventory coupling. Every record can be edited, deleted, and traced through its change history.
- **Production Audit** — Supervisors work through *pending / rejected / approved* states with **batch approval**, **approval revocation**, and a **"My Follows"** filter. On submission, the backend automatically applies **three risk flags**: **over-report risk** (predicts whether the log would drive WIP inventory negative, based on the good-units-only flow), **abnormally low pass rate** (compared against the daily average for that product + process), and **output above daily capacity**. Flagged records are marked inline and can be pivoted into a **"Work Log Risk"** governance list for type-based tracking and correction.
- **Quality Inspection** — Pending/inspected tabs, good/defective quantity split, defect-type tagging, a full **rework & re-inspection** flow, and an inspection timeline.
- **WIP Inventory** — Dual views for product-level and process-level inventory, with both **direct adjustment** and **change-request** modes. Every quantity change lands in **change history**, backed by a health-score indicator.
- **Completion & Warehousing** — Completion report (by day / by product) → finished-goods receipt → outbound, forming a finished-goods ledger that reconciles automatically against reported production.
- **Piece-Rate Settlement** — Settlement by product and by employee, with settlement history and a clean separation between *editable* and *settled* states. **All figures derive from the completion report**, so there is exactly one source of truth.

### 📊 Analytics that let the numbers speak
> *Pass rate dropped three points — which machine, which process, which person?*

- **Pass-rate analysis** — Drill freely across **product / process / employee** dimensions, with period-over-period deltas and trend curves.
- **Quality trend** — Day/month quality trends, unified with the pass-rate view.
- **Work-log ranking** — Top-N operator output leaderboard, with export and email delivery.
- **WIP query** — Search work-in-progress by product or process and drill into per-process detail.
- **Inventory analysis** — Inventory ranking, turnover, and anomalies (including correction markers and over-report risk).
- **Finished-goods statistics** — Completion volume and trends by day and by product.
- **Production daily report** — Pushed via **WeChat subscription messages** (templates are applied for in the WeChat MP console).

### 🤖 AI Query — ask your business in plain language
> *"Which product has the lowest pass rate this month?" — ask it the way you'd ask a colleague, and the answer tells you where it came from.*

- **Ask a question, get an answer** — output, pass rate, defect detail, work-log ranking, inventory, delay risk… **12 families of business questions asked in everyday language**, with no need to first work out which report holds the number or which filters to set.
- **Answers come with their provenance** — every figure expands into three levels of evidence: **definition / source / snapshot**. What it was calculated from, where it came from, and what range it covered — so you can judge whether to trust it, instead of being handed a bare number.
- **Plain language, not jargon** — field names are localised, ratios become percentages, key figures are highlighted, long answers collapse; each answer ends with a few "you might also ask" follow-ups.
- **If it can't answer, it says so** — questions about salary, settlement, or anything outside its capabilities get a clear reply (often with a suggested alternative). **It never invents a plausible-looking number.**
- **Conversations persist** — multi-session management (search / rename / delete), in-conversation retry on failure, full history.
- **Works without an LLM key** — rule-based parsing plus templated answers by default. Add a key and it upgrades to stronger understanding and polishing, **with numeric consistency validation that falls back to the template whenever it fails.**
- **You can also *see* the data** — the companion [Business Ontology graph](#-business-ontology-visualisation) draws "who/what → did what → what's left → what the AI can answer" as a relationship graph; tap a capability to start asking immediately.

<details>
<summary><strong>🔧 Technical implementation (for developers / extenders)</strong></summary>

- **Agent loop orchestration (Plan → Act → Reflect → Replan)**
  - **Deterministic router** (no LLM call): empty/over-length input or **blocked terms** (salary / payroll / settlement / fine / commission) → boundary response; small talk → dedicated reply; everything else enters the main loop;
  - **Plan** (LLM-first intent resolution, rule-based fallback) → **code normalisation** (time, entities, combination whitelist) → **Act** (execute the registered implementation) → **Reflect** (rule-based first-level review) → decision **PASS / CLARIFY / REPLAN / STOP**;
  - **Four anti-loop guards**: ① failed-metric tracking ② deterministic errors are never retried ③ stop when everything failed with no data ④ emit as soon as data exists;
  - The chain version is printed at startup, so "did my change take effect?" is answered at a glance instead of inferred from behaviour.
- **Three-channel capability routing with trust tiering** (the key design):
  | Channel | How data is fetched | Trust level |
  | :--- | :--- | :--- |
  | **Registered metrics / operators** | Hand-written SQL — reviewable, regression-testable | **Authoritative** (always wins when present) |
  | **Analysis operators** | Deterministic aggregation + attribution (answers "why" questions) | **Authoritative** |
  | **Generated SQL** | LLM generates → passes the safety gate → read-only execution | **Exploratory** (`exploratory=true`, labelled as such in the answer) |
- **LLM-generated SQL is still safe.** This channel is protected by **`SqlGuard`'s three gates** (parsed via the Druid AST, not string matching):
  1. **Statement-type gate**: after parsing there must be **exactly one SELECT**. Any DML/DDL (INSERT/UPDATE/DELETE/DROP/TRUNCATE/ALTER/CALL) or multi-statement concatenation is **rejected in milliseconds** — no LLM call, no retry. A dangerous-function fallback additionally blocks `sleep` / `benchmark` / `load_file` / `into outfile` / `information_schema` and friends.
  2. **Object whitelist gate**: every table collected from the AST must be whitelisted, and no column may hit a sensitive column (both table-level and column-level exclusion).
  3. **Enforced constraints (AST rewriting)**: every table in every query block is force-ANDed with `tenant_code = '<current tenant>'`; a missing or oversized `LIMIT` is rewritten to the row cap (default 500 rows / at most 3 statements per generation).
  - **Full audit trail**: every generated statement's `SUCCESS` / `REJECTED` / `FAILED` status, row count and elapsed time is persisted. Execution runs **at most 2 rounds** — a second round only happens when the first returned too little, with the results fed back in, and a hard cap prevents unbounded fetching.
- **Ontology-driven, not prompt-driven** — `ai-ontology/` (`metrics.json` with 12 metrics + `entities.json` + `relations.json`) is the **single source of truth**. One command, `node ai-ontology/sync.js`, projects it into both backend and frontend.
- **Numeric-consistency invariant** — AI answers are forced to match the UI's rounding convention (uniform `truncate(...,3)`), eliminating the trust-destroying "AI says 95.7%, the page says 95.6%" class of bug.
- **Misstatement is forbidden** — all-NULL aggregates are explicitly annotated: **"no value retrieved ≠ does not exist in the business"**. Execution failures are strictly distinguished from "no data", and the polishing layer may not conclude "no records / no work logs".
- **Coverage self-check** — `ExecutionCoverageChecker` diffs the ontology's promises against the executor's registration ledger at startup; gaps are logged as `[AI缺口]` and can be aggregated directly into a to-register list.

</details>

### 🕸️ Business ontology visualisation
> *Don't just read the numbers — see how they connect.*

A dedicated **Business Ontology** page draws "who / what → did what → how much is left → what the AI can answer with it" as an **interactive relationship graph**:

<div align="center">

<img width="880" src="docs/images/screens-ai-ontology.png" alt="AI Query and the Business Ontology graph">

<br>

<sub><b>Left</b>: AI Query — "How is the pass rate for flange DN15 at each process?" The answer comes with the figures, the metric definitions and the data provenance, plus one-tap follow-ups<br>
<b>Right</b>: Business Ontology — real business records as a live graph (products in blue · processes in purple · employees in green); it tells the AI what it can answer and where the data comes from. Tap a capability to start asking immediately</sub>

</div>

- **Data view (default)**: **real business records** as nodes in four concentric rings — master data (outer) / shop-floor actions (middle) / results (inner) / capabilities (outermost). Node size is graded by work-log volume.
- **Structure view**: the ontology's type relationships (entity / relation / capability scale).
- **Capability layer and lineage toggles**: enabling lineage automatically enables the capability layer, avoiding the "toggle seems broken" illusion when endpoints would otherwise be invisible.
- **Click a capability to ask directly**: each entity node shows its ontology entity plus related capabilities; tapping one fires a query using that capability's registered example question.
- **Process flow**: the actual routing chain a product followed, ordered by real work logs.

### 📱 One codebase, two delivery targets
> *Owners want a zero-install web app; operators want an install-free Mini Program. One codebase delivers both.*

- **H5 + WeChat Mini Program** from a single `uni-app + Vue 3` source tree, using platform conditional compilation (`#ifdef H5` / `#ifndef H5`) to adapt drag behaviour, popups, video and gestures per platform.
- **Graceful degradation of WeChat-only capabilities on H5** (already handled in code, no extra config): one-tap WeChat login → **SMS code login**; invite poster → **generated and downloadable locally**; share → **copy link**; subscription messages / purchase redirects → **guided prompts**.
- **Mini Program package-size discipline** — videos (~61.6 MB) and images are **excluded from the Mini Program bundle**. They ship with the H5 build and load remotely via `VITE_MP_VIDEO_BASE`, keeping the main package comfortably under the 2 MB limit.

### 🏢 Multi-tenancy with fine-grained roles
- **Tenant isolation** at the data level (`tenant_code`): one deployment can serve multiple workshops, teams or plants, with **invite-code onboarding** and **tenant switching**.
- **Five roles**: `10` Enterprise Admin / `20` Auditor / `25` Quality Inspector / `30` Operator / `40` Trial. Navigation and capabilities adapt to the role — operators never see admin menus, managers never see the operator-only entry points.
- **Guided trial mode** — Role `40` walks through a **four-step onboarding journey** (log work → audit → inventory → analytics) with **instructional videos** and a completion prompt, so newcomers get going without reading a manual first.

### 🎯 One-tap demo data toggle
> *You want a populated dashboard for a demo — without polluting real data.*

A floating button on the workbench switches between **demo data** and **live data**. Demo mode serves realistic bundled payloads (`.json` shipped in the image, lazily loaded into Redis) with zero database writes and full backend transparency.

### 🔌 Third-party integrations out of the box
- **WeChat ecosystem** — Mini Program login, invite QR codes / Mini Program codes, subscription messages (production daily report), WeChat Official Account.
- **Messaging** — GeTui (uni-push), SMS (any gateway), email (with attachment upload).
- **Team notifications** — Feishu (Lark) bot webhooks across three channels: production, test, and feedback collection.
- **Object storage** — MinIO as a self-hosted CDN replacement; buckets are created automatically.
- **Asynchronous reliability** — Third-party calls run through **Redis Streams**; failures are persisted and retried by the `/external/retryTask` compensation scanner.

### ⚡ Full stack in one command
```bash
cp .env.example .env && docker compose up -d --build
```
Backend ① and ②, H5 (nginx), MySQL (auto-migrated), Redis and MinIO — all up. See [Quick Start](#-quick-start).

---

## 📣 What's New

<details open>
<summary><strong>🔥 AI Query — ontology-driven business Q&A</strong></summary>

- **12 metric families** spanning overview, three-dimensional pass rate, work-log ranking, defect detail, inventory, delivery-delay risk, entity lists, change attribution, concentration analysis and period-over-period comparison;
- **Agent loop orchestration**: deterministic router (blocked terms / small talk / over-length) → Plan-Act-Reflect-Replan, backed by four anti-loop guards;
- **Three-channel routing with trust tiering**: registered metrics/operators are **authoritative**; LLM-generated SQL is **exploratory** and labelled as such;
- **Safety gate for generated SQL**: `SqlGuard`'s three gates (exactly one SELECT / table whitelist and sensitive-column exclusion / AST-rewritten tenant isolation and row cap) plus a full audit trail;
- **Tiered lineage evidence** (definition / source / snapshot) — no more black-box AI numbers;
- **Single-source ontology**: `ai-ontology/` → projected to backend resources and the frontend capability manifest via `sync.js`;
- **Business ontology visualisation**: a dedicated relationship graph showing "who/what → did what → what's left → what the AI can answer", with one-tap queries from any capability;
- **Startup self-check and gap ledger**: `ExecutionCoverageChecker` surfaces combinations the ontology promises but the executor has not implemented — no fake capabilities;
- **Usable with zero LLM**: rule parsing plus templates by default; upgrade automatically when a key is supplied (with numeric validation and fallback);
- **Conversation features**: multi-session management (search / rename / delete), ontology-projected follow-up suggestions, retryable failure bubbles, and soft-keyboard avoidance on H5.

</details>

<details>
<summary><strong>📦 Shop-floor core loop — fully closed</strong></summary>

- **Completion report → finished-goods receipt → outbound**: the finished-goods ledger reconciles automatically against reported production;
- **Piece-rate settlement**: dual product/employee dimensions, settlement history, and a single source of truth from the completion report;
- **Full defect & rework flow**: re-inspection, defect-type tagging, inspection timeline;
- **Dual-mode WIP inventory**: direct adjustment plus change requests, with full change history and health alerts;
- **Stronger audit guardrails**: three automatic risk flags, batch approval, approval revocation, follow-based filtering.

</details>

<details>
<summary><strong>📱 Dual-platform experience & open-source readiness</strong></summary>

- **Comprehensive H5 adaptation**: draggable floating controls, custom popups, native `<video>`, `visualViewport` keyboard avoidance, hash routing with last-visited-page restore;
- **Mini Program compliance**: main package under the 2 MB limit, large assets loaded remotely, self-service subscription templates;
- **One-command Docker deployment**: full-stack compose orchestration including middleware and automatic schema creation;
- **Open-source cleanup**: private JARs bundled offline, every secret converted to an environment-variable placeholder, complete Apache-2.0 licensing and third-party notices.

</details>

---

## 🚀 Quick Start

### Step 1: Just want to see it running?

Three zero-friction options — pick one:

| Option | Best for | How |
| :--- | :--- | :--- |
| **🐳 One-command Docker** | Seeing the whole system fastest | See [Step 2](#step-2-docker-deployment-recommended) below |
| **🎭 Demo data mode** | Product demos to stakeholders | Start the stack, then tap the floating button on the workbench to switch to **bundled demo data** — no data entry needed |
| **💻 Local development** | Modifying or extending the code | See [Step 3](#step-3-local-development) below |

> 💡 **Suggested first run**: start the stack, log in as the initial admin, toggle demo data, then browse the *Workbench / Analytics / Master Data* tabs. Thirty seconds is enough to get the full picture.

### Step 2: Docker deployment (recommended)

**Prerequisites**: Docker 20.10+ and Docker Compose v2 (`docker compose` available). 4 CPU / 8 GB RAM recommended.

```bash
# 1. Copy the environment template
#    Only two values are mandatory: the database password and the JWT secret.
#    Everything else can stay empty — core features still start.
cp .env.example .env
#    Edit .env, at minimum:
#      MYSQL_ROOT_PASSWORD=<your database password>
#      JWT_SECRET=<random string, at least 32 chars; try: openssl rand -hex 32>

# 2. Build images and start all services
#    (First build downloads Maven / npm dependencies — expect 10–20 minutes.)
docker compose up -d --build

# 3. Wait for MySQL initialisation
#    (62 + 21 tables are created automatically; this can take several minutes.)
docker compose ps

# 4. Watch the backend log until "微应用服务启动成功" appears
docker compose logs -f hhim-micro-be
```

**Endpoints after startup**:

| Entry point | URL | Notes |
| :--- | :--- | :--- |
| 🖥️ **H5 frontend** | http://localhost | Change `H5_PORT=8080` in `.env` to use http://localhost:8080 |
| 🔧 Backend ① (business) | http://localhost:9010 | Work logging / inspection / pass rate / inventory |
| 🔧 Backend ② (integrations) | http://localhost:8899 | WeChat / GeTui / SMS / email |
| 📦 MinIO console | http://localhost:9001 | Default `minioadmin` / `minioadmin` |

**Default demo accounts**:

| Item | Value |
| :--- | :--- |
| Initial tenant | `A9K3Q7` |
| Initial admin | phone `13800000000`, nickname "演示管理员" — the SMS login code is printed in the backend console log |
| Roles | `10` Enterprise Admin / `20` Auditor / `25` Quality Inspector / `30` Operator / `40` Trial |
| Default processes | Cutting / Turning / Tapping |

> ⚠️ **For production, delete the demo account** and create your own admin. Never keep the default phone number.

### Step 3: Local development

**Requirements**: JDK 1.8 · Maven 3.6+ · Node 16/18 · MySQL 8.0+ · Redis · MinIO

#### 3.1 Backend

```bash
# 1) Install the private JARs into your local Maven repository
#    (needed once; the repo intentionally does not point at any private registry)
./install-lib.sh          # Linux / macOS / Git Bash
install-lib.bat           # Windows

# 2) Create the databases and import the schema
mysql -h 127.0.0.1 -u root -p -e "
  CREATE DATABASE im_micro DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
  CREATE DATABASE \`im-portal\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
mysql -h 127.0.0.1 -u root -p im_micro   < init.sql        # micro_* business tables (62, incl. demo data)
mysql -h 127.0.0.1 -u root -p im-portal  < hyzz-schema.sql # hyzz_* integration tables (21)

# 3) Copy the config templates and fill in real values
cp cosmo-hhim-micro/hhim-micro-be/micro-interface/src/main/resources/application-local.yml.example \
   cosmo-hhim-micro/hhim-micro-be/micro-interface/src/main/resources/application-local.yml
cp hhim-third-platform/thirdplat-web/src/main/resources/application-local.yml.example \
   hhim-third-platform/thirdplat-web/src/main/resources/application-local.yml
#    Replace every please_set_xxx with a real value.

# 4) Compile and start
mvn clean compile -DskipTests
cd cosmo-hhim-micro/hhim-micro-be/micro-interface && mvn spring-boot:run   # backend ① :9010
cd hhim-third-platform/thirdplat-web && mvn spring-boot:run                # backend ② :8899 (separate terminal)
```

> 💡 **Faster first build**: configure the Aliyun Maven mirror — see [Maven Configuration](#maven-configuration).
> 💡 **IntelliJ IDEA users**: open `cosmo-hhim-micro/hhim-micro-be` and run `LittleGiantsApplication`; open `hhim-third-platform/thirdplat-web` and run `ThirdpartWebApplication`.

#### 3.2 Frontend

```bash
cd cosmo-hhim-micro/hhim-micro-app
npm install --legacy-peer-deps

# H5 — http://localhost:8082 (from a phone on the same LAN: http://<your-ip>:8082)
npm run dev:h5

# WeChat Mini Program — import dist/dev/mp-weixin into WeChat DevTools
npm run dev:mp-weixin
```

#### 3.3 Two mandatory WeChat Mini Program settings

**① Mini Program AppID**

Edit `mp-weixin.appid` in `cosmo-hhim-micro/hhim-micro-app/src/manifest.json`:

- The repo ships the placeholder `touristappid` (tourist mode, which **does not support same-layer canvas 2d rendering — charts will appear blank**);
- When debugging charts locally, use your own AppID, then **switch back to the placeholder before committing**;
- Real-device preview, trial builds and publishing all require your real AppID.

**② Mini Program credentials in the database (easy to miss!)**

The `hyzz_micro_miniapp_config` table (in the `im-portal` database) is **created empty**. You must insert your AppId/AppSecret after deployment:

```sql
-- A template INSERT is at the end of hyzz-schema.sql — replace the placeholders with your real values.
-- ⚠️ application_sign MUST be 'micro_process' (not the 'wechat' value in the comments)
-- ⚠️ platform_type MUST be 'wechatMiniApp'
-- ⚠️ app_id MUST match mp-weixin.appid in the frontend manifest.json
```

**Consequence of skipping this**: backend ② cannot resolve the Mini Program identity, so **WeChat one-tap login, invite QR codes and Mini Program code generation will fail with HTTP 500**.

#### 3.4 Mini Program static assets (video / images)

The Mini Program package is capped at 2 MB, so videos (~61.6 MB) and images are **not bundled**. They ship with the H5 build and load remotely:

1. **Deploy H5 first**;
2. Point `VITE_MP_VIDEO_BASE` in `.env.production` at your H5 domain (e.g. `https://your-domain.com/static/micro_app`), then **rebuild the Mini Program**;
3. **Configure WeChat legal domains** (required for real-device preview / trial / production; all need **HTTPS + an ICP-filed domain**):
   - `request` legal domain → your backend address
   - `downloadFile` legal domain → your H5 static asset address (during development you can use DevTools' *real-device debugging*, which skips domain validation);
4. **Watch the package size**: the main package is currently ~1.8 MB. After adding images to `src/static/` or a large dependency, check it in WeChat DevTools.

#### 3.5 Frontend environment variables

| Variable | Default | Description |
| :--- | :--- | :--- |
| `VITE_API_BASE` | `/api` | **H5** API prefix (vite dev proxy / nginx reverse-proxies to 9010) |
| `VITE_MP_API_BASE` | `http://localhost:9010` | **Mini Program / App** backend. **Real-device preview and trial builds must use a publicly reachable HTTPS domain.** Do not reuse `VITE_API_BASE` — a relative path is not supported by `wx.request`. |
| `VITE_MP_VIDEO_BASE` | empty in `.env.example` / `https://your-domain.com/static/micro_app` in `.env.production` | Mini Program static asset base (video + images). Point it at your deployed H5 static directory. **When empty, it falls back to in-package paths** (only some assets work; not guaranteed on real devices). |
| `VITE_EXPERIENCE_USERNAME` | `15888888888` | Fixed trial-mode login account |
| `VITE_EXPERIENCE_PASSWORD` | `cosmoplat` | Fixed trial-mode login password |
| `VITE_CDN_BASE` | `/static/micro_app` | Static image CDN prefix (localised by default; override for your own object storage) |
| `VITE_MP_TARGET_APPID` | `wx0000000000000000` (placeholder) | Target Mini Program AppID for jumps (put the real value in `.env.development.local`, which is gitignored) |

---

## 🧩 Feature Matrix

<details open>
<summary><strong>📋 Click to expand the full feature matrix</strong></summary>

| Module | Capabilities | Role |
| :--- | :--- | :--- |
| **Work logging / reporting** | Single entry, batch entry, backfill (last 7 days), photo evidence, edit/delete, daily rollup | Operator `30` |
| **Production audit** | Pending / rejected / approved, batch approval, revocation, "My Follows" filter, three automatic risk flags (over-report / low pass rate / over-capacity) | Auditor `20` |
| **Quality inspection** | Pending/inspected, good/defective split, defect-type tagging, rework & re-inspection, inspection timeline | Inspector `25` |
| **WIP inventory** | Product/process dual view, direct adjustment, change requests, change history, health indicator | Admin `10` |
| **Completion & warehousing** | Completion report (day/product), finished-goods receipt, outbound | Admin `10` |
| **Piece-rate settlement** | Product/employee dimensions, settlement history, editable vs settled state separation | Admin `10` |
| **Defect management** | Defect list, full handling flow, rework, scrap-type tagging | Inspector `25` |
| **Analytics** | Pass rate (product/process/employee), quality trend, work-log ranking, WIP query, inventory analysis, finished-goods statistics | Admin `10` / Auditor `20` |
| **Daily report** | WeChat subscription message push (subscribe entry on the workbench; apply for a "production daily report" template in the MP console) | Admin `10` |
| **AI Query** | 12 metric families in natural language, agent loop orchestration, three-channel trust tiering, tiered lineage evidence, follow-up suggestions, multi-session management | Admin `10` / Auditor `20` |
| **Business ontology** | Interactive relationship graph (data view / structure view), capability layer and lineage, one-tap queries from a capability, process-flow chains | Admin `10` / Auditor `20` |
| **Data governance** | Data health dashboard, work-log risk governance (over-report / low pass rate / over-capacity), WIP inventory governance, routing-anomaly governance (missing first/last process, multiple last processes) | Admin `10` |
| **Master data** | Product management (incl. **routing editing**: parallel/sequential ordering, similar-product recommendation), process management, employee management | Admin `10` |
| **Support chat** | Haiyun support conversation, FAQ, session history | All roles |
| **My account** | Profile, message orders, SMS subscription, feedback, invite colleagues, switch/join tenant, dissolve enterprise | All roles |
| **Help centre** | 25 instructional videos, onboarding journey, help entry points | All roles |
| **Demo data** | One-tap switch between demo and live data | Demo scenarios |

</details>

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│  Frontend: uni-app + Vue 3 (one codebase, two targets)               │
│    ├── H5            → nginx reverse proxy /api → backend ①          │
│    └── WeChat Mini Program → wx.request (HTTPS legal domain) → ①    │
└──────────────────────────────────────────────────────────────────────┘
                                 │
        ┌────────────────────────┴────────────────────────┐
        ▼                                                  ▼
┌───────────────────┐  Feign (direct) ┌──────────────────────────────┐
│ Backend ①          │ ─────────────► │ Backend ②                     │
│ hhim-micro-be      │                │ hhim-third-platform          │
│ :9010              │                │ :8899                        │
│ logging/audit/QC   │                │ WeChat / GeTui / SMS /        │
│ pass rate/inventory│                │ email / SQM                  │
│ settlements        │                │                              │
│ AI Query           │                │                              │
└───────────────────┘                └──────────────────────────────┘
        │                                          │
        │                                          ▼
        │                             Redis Streams async queue
        │                             (failures persisted + /external/retryTask)
        ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Middleware                                                           │
│   MySQL 8.0 (db0 = im_micro business / db1 = im-portal integration,   │
│              dual datasource)                                         │
│   Redis (cache + Streams queue) · MinIO (object storage)              │
└──────────────────────────────────────────────────────────────────────┘
```

**Technology stack**:

| Layer | Technology |
| :--- | :--- |
| Backend | Spring Boot 2.3.7.RELEASE · Spring Cloud Hoxton.SR9 · Spring Cloud Alibaba 2.2.5 · MyBatis-Plus 3.4.0 · Druid 1.2.6 · Fastjson 1.2.83 · JWT |
| Frontend | uni-app 3.0 (alpha) · Vue 3.2 · Vuex 4 · ECharts 5 / uCharts · dayjs 1.11 · BigNumber.js |
| Storage | MySQL 8.0 (dual datasource, multi-tenant) · Redis (cache + Redis Streams) · MinIO |
| Service discovery | Nacos (**optional** — disabled by default; services talk to each other over direct Feign calls) |
| Deployment | Docker Compose (full-stack orchestration with automatic middleware initialisation) |

---

## 📦 Deployment Details

### Services included

| Service | Description | Port |
| :--- | :--- | :--- |
| `hhim-micro-be` | Backend ① (business) | 9010 |
| `hhim-third-platform` | Backend ② (integrations) | 8899 |
| `hhim-h5` | H5 frontend (nginx) | `${H5_PORT:-80}` |
| `mysql` | Database (auto-runs `init.sql` for 62 `micro_*` tables + `hyzz-schema.sql` for 21 `hyzz_*` tables) | 3306 |
| `redis` | Cache + Redis Streams queue | 6379 |
| `minio` | Object storage | 9000 / 9001 |
| `minio-init` | One-shot job: creates the `hhim` / `hhim-micro` / `hyzz-site-test` buckets | — |

> Nacos is disabled by default (service discovery / config centre is not used). Uncomment the relevant block in `docker-compose.yml` to enable it.

### Configuration notes

- **Every option** is documented inline in `.env.example`. Never commit `.env`.
- **MinIO image display**: `MINIO_URL` defaults to `http://minio:9000` (container-to-container). Browsers cannot resolve the container name `minio` — if you need images to render in the browser, change it to `http://<host-ip>:9000` (port 9000 is already mapped to the host, so both containers and browsers can reach it).
- **Backend ② database**: `THIRDPLAT_DB_*` defaults to the `im-portal` database inside the compose MySQL instance. Usually no change needed.
- **Using existing middleware**: if you already run MySQL/Redis/MinIO, just point `DB_HOST`, `REDIS_HOST`, `MINIO_URL` etc. in `.env` at your services. No compose changes required.
- **Image build arguments**: `VITE_API_BASE`, `VITE_EXPERIENCE_USERNAME` / `VITE_EXPERIENCE_PASSWORD` can be overridden in `.env`.

### Optional features (empty = feature unavailable; startup and other features are unaffected)

| Feature | Environment variables |
| :--- | :--- |
| SMS | `COSMO_SMS_ACCESS_KEY` · `COSMO_SMS_URL` · `COSMO_SMS_TEMPLATE` · `SMS_LOGIN_CODE` · `SMS_WEEK_REPORT` · `SMS_LOGIN_TEMPLATE` · `SMS_WEEK_TEMPLATE` |
| GeTui push | `UNIPUSH_APP_ID` · `UNIPUSH_APP_KEY` · `UNIPUSH_APP_SECRET` · `UNIPUSH_MASTER_SECRET` · `UNIPUSH_APP_PACKAGE` · `UNIPUSH_BASE_URL` |
| WeChat Official Account | `WXMP_APP_ID` · `WXMP_APP_SECRET` · `WXMP_SERVER_TOKEN` · `WXNO_SERVER_AES_KEY` |
| To-do push | `TODOPUSH_APP_ID` · `TODOPUSH_APP_SECRET` · `TODOPUSH_GATEWAY_URL` · `TODOPUSH_PARENT_ID` |
| Feishu notifications | `FEISHU_PROD_WEBHOOK` · `FEISHU_TEST_WEBHOOK` · `FEISHU_SUGGEST_BOT_URL` |
| Email | `MAIL_ACCESS_KEY` · `MAIL_UPLOAD_URL` · `MAIL_SEND_URL` |
| AI Query | `AI_BASE_URL` (OpenAI-compatible gateway) · `AI_API_KEY` · `AI_CHAT_MODEL` · `AI_ENABLED` · `AI_TIMEOUT_MS` · `AI_MONTHLY_BUDGET` · `AI_REFINE_ENABLED` |

### AI Query configuration

```yaml
# micro-interface/src/main/resources/application-local.yml (gitignored)
ai:
  base-url: ${AI_BASE_URL:please_set_ai_base_url}     # OpenAI-compatible gateway (DeepSeek / Qwen / OpenAI, ...)
  api-key: ${AI_API_KEY:please_set_ai_api_key}        # Real keys stay local — never commit them
  chat-model: ${AI_CHAT_MODEL:please_set_ai_chat_model}
```

- **The feature works without a key** (rule-based intent parsing + templated answers). Supplying a key upgrades to LLM intent parsing and answer polishing, with numeric consistency validation and automatic fallback.
- Sessions are capped at 200 messages (~100 turns) and persisted in `micro_ai_chat_session` / `micro_ai_chat_message`.
- After editing the ontology, run `node ai-ontology/sync.js` to project it into backend and frontend.

---

## 🔐 Secrets & Security

### Design principle: zero plaintext in the repository

This project contains **no** plaintext passwords, keys or internal IP addresses. Every secret is injected through a `${ENV_VAR:default}` placeholder:

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:please_set_db_password}
```

```java
@Value("${mail.access-key:please_set_mail_access_key}")
private String mailAccessKey;
```

- With no environment variable set, the `please_set_*` defaults are used — database/Redis connections then **fail loudly** instead of silently running on weak defaults.
- Files involved: `MailService` · `SendIhaierMsg` · `TenantWebInterceptorConfig` · `OpenFeishuEventListener` · `CosmoConfig` · `CustomerSuggestionFacadeService` · `MicroFAQFacadeServiceImpl`.

### Injection methods

**Option 1: environment variables (recommended)**

```bash
# Linux / macOS
export DB_PASSWORD=your_real_password
export REDIS_PASSWORD=your_redis_password
export JWT_SECRET=$(openssl rand -hex 32)
mvn spring-boot:run
```

```powershell
# Windows PowerShell
$env:DB_PASSWORD="your_real_password"
$env:JWT_SECRET=[guid]::NewGuid().ToString().Replace("-","")
mvn spring-boot:run
```

**Option 2: copy the example files and edit**

```bash
cp <module>/src/main/resources/application-local.yml.example \
   <module>/src/main/resources/application-local.yml
# Replace every please_set_xxx with a real value (this file is gitignored).
```

### Pre-production security checklist

- [ ] Change `MYSQL_ROOT_PASSWORD` (never leave it empty)
- [ ] Set `JWT_SECRET` (**at least 32 random characters**)
- [ ] Delete or disable the demo account (`13800000000`)
- [ ] Never commit `.env` / `application-local.yml`
- [ ] In production, remap or remove middleware `ports:` entries you do not need to expose
- [ ] Rotate Feishu webhooks / SMS access keys as needed

---

## ❓ FAQ

<details>
<summary><strong>The first build is slow or seems stuck</strong></summary>

The first `docker compose up -d --build` downloads Maven and npm dependencies — **10–20 minutes is normal**. Both Dockerfiles are configured with the Aliyun Maven mirror, and the frontend pins `node:16-alpine`.
</details>

<details>
<summary><strong>The MySQL container stays unhealthy for a long time</strong></summary>

The first start creates 83 tables, which can take several minutes. The healthcheck `start_period` is already relaxed to 600s — just wait for `healthy`.
</details>

<details>
<summary><strong>Port 80 is already in use</strong></summary>

Set `H5_PORT=8080` in `.env`, then run `docker compose up -d`.
</details>

<details>
<summary><strong>Middleware port conflicts (MySQL/Redis/MinIO already installed locally)</strong></summary>

The compose file maps middleware ports (3306/6379/9000/9001) to the host. Either stop the local services, remap the compose `ports:` (e.g. `"3307:3306"`), or — if middleware only needs container-to-container access — **delete that service's `ports:` block entirely**.
</details>

<details>
<summary><strong>Charts render blank in WeChat DevTools</strong></summary>

`mp-weixin.appid` in `manifest.json` is the placeholder `touristappid`, and **tourist mode does not support same-layer canvas 2d rendering**. Fill in your own AppID and rebuild (switch it back to the placeholder before committing).
</details>

<details>
<summary><strong>WeChat login / invite QR codes return HTTP 500</strong></summary>

The `hyzz_micro_miniapp_config` table (in `im-portal`) is **created empty** — you must insert your AppId/AppSecret. Make sure `application_sign` is `micro_process`, `platform_type` is `wechatMiniApp`, and `app_id` matches the frontend `manifest.json`. See [3.3 Two mandatory WeChat Mini Program settings](#33-two-mandatory-wechat-mini-program-settings).
</details>

<details>
<summary><strong>Real-device preview requests fail</strong></summary>

Two likely causes: ① `VITE_MP_API_BASE` still points at `http://localhost:9010` (a phone cannot reach your computer's localhost) — change it to a publicly reachable HTTPS domain; ② the WeChat MP console has no `request` / `downloadFile` legal domain configured (both need **HTTPS + an ICP-filed domain**).
</details>

<details>
<summary><strong>How do I add a new AI Query metric?</strong></summary>

Edit `ai-ontology/metrics.json` (definition + `questionTemplates`) → register a hand-written SQL statement in `MicroAiDailyMapper.xml` (must include `tenant_code` isolation, a definition comment, `nullif` to avoid division by zero, and a `limit`) → run `node ai-ontology/sync.js`.

**Do not add metrics by editing prompts** — the ontology is the single source of truth. Registered metrics belong to the **authoritative** channel; questions the ontology does not cover fall back to the **exploratory** generated-SQL channel, whose answers state their trust level. See [Core Features → AI Query](#-ai-query--ask-your-business-in-plain-language).
</details>

<details>
<summary><strong>Why does an AI percentage differ from the page by 0.1%?</strong></summary>

The platform-wide convention is `BigDecimal.divide(..., 3, RoundingMode.DOWN)` (**truncation** to 3 decimals). Registered SQL must use `truncate(...,3)`, **not `round(...,4)`**, and `AnswerComposer.rate()` truncates to 3 decimals before multiplying by 100. This is the numeric-consistency invariant.
</details>

### Maven configuration

We recommend configuring a mirror before the first build (especially from mainland China):

```xml
<!-- ~/.m2/settings.xml -->
<mirrors>
  <mirror>
    <id>aliyun-central</id>
    <name>Aliyun Maven Central</name>
    <url>https://maven.aliyun.com/repository/central</url>
    <mirrorOf>central</mirrorOf>
  </mirror>
</mirrors>
```

> An existing Maven setup using the default Central repository also works — downloads are just slower.

---

## 📂 Project Structure

```
cosmo-hhim-open/
├── cosmo-hhim-micro/                 # Micro-application (the business core)
│   ├── hhim-micro-app/               # Frontend: uni-app (H5 + WeChat Mini Program)
│   └── hhim-micro-be/                # Backend ① hhim-micro-be :9010
│       ├── micro-interface/          # Application entry (Spring Boot fat jar)
│       ├── micro-application/        # Application layer
│       ├── micro-infrastructure/     # Infrastructure layer
│       └── micro-*-domain/           # Domain modules (base/storage/planning/submit/complete/ng…)
├── hhim-third-platform/              # Backend ② integrations :8899
│   ├── thirdplat-api/                # API definitions (WeChat / GeTui / SMS / email…)
│   ├── thirdplat-common/             # Common module
│   ├── thirdplat-core/               # Core module
│   ├── thirdplat-modules/            # Feature modules
│   └── thirdplat-web/                # Web entry point
├── cosmo-himm-commom/                # Shared component library (security/cache/log/datasource/Excel…)
├── ai-ontology/                      # 🤖 AI Query ontology assets (single source of truth)
│   ├── metrics.json                  #   Metric layer (12 metrics)
│   ├── entities.json                 #   Entity layer
│   ├── relations.json                #   Relation layer
│   └── sync.js / validate.js / capability.js / export-frontend.js / add-metric-tables.js / add-operator-routing.js
├── docker-compose.yml                # Full-stack orchestration (one command)
├── .env.example                      # Environment template (cp to .env and edit)
├── init.sql                          # micro_* business tables (62, incl. AI session tables and demo data)
├── hyzz-schema.sql                   # hyzz_* integration tables (21)
├── install-lib.sh / install-lib.bat  # Private JAR install scripts
├── lib/                              # 32 private dependency JARs (offline bundle)
├── .github/workflows/                # CI (backend Maven build + frontend H5 build)
├── LICENSE / NOTICE                  # Apache-2.0 licence and third-party notices
└── CONTRIBUTING.md                   # Contribution guide
```

### About the private JARs in `lib/`

The project depends on 32 internally published JARs (groupId `com.cosmo.plugins` / `com.cosmo.hhim.thirdplat` / `com.cosmo.hhim.micro`) that are **not on Maven Central**. To avoid depending on any private registry, they are **shipped as an offline bundle** with the repository:

- `pom.xml` declares their groupId / artifactId / version via `<dependencyManagement>` but **points at no private registry**;
- Maven resolves them from the local repository (`~/.m2/repository`), so **you must run `install-lib.sh` / `install-lib.bat` once before the first build**;
- `lib/` also contains `hhim-common-ioss-4.1.pom` (with dependency information; the install script picks it up automatically).

> Note: `hhim-common-swagger-4.1.jar` and `hhim-common-uuc` were removed during open-source cleanup (no source in the repository and no module references them — see [NOTICE](NOTICE)).

### Public dependencies

Spring Boot / Spring Cloud / MyBatis / Fastjson and other public packages have their versions managed centrally in `pom.xml`'s `<dependencyManagement>`. Maven **downloads them from Central on the first build** — no extra configuration needed.

---

## ✨ Stay Tuned

If KuYiji is useful to you — or if your shop floor needs exactly this —

⭐ **Star the repo and help more people in manufacturing digitalisation find it** ⭐

---

## 🤝 Contributing & Community

Every form of participation is welcome: issues, pull requests, documentation, bug reports, and stories about how you use it.

- **Contribution guide**: [CONTRIBUTING.md](CONTRIBUTING.md)
- **Bug reports**: [open an issue](https://github.com/cosmoplat-opensource/kuYiJi/issues)
- **Security vulnerabilities**: please do **not** open a public issue — contact the maintainers privately

**Good first contributions**:
- 🐛 Fix bugs / add tests
- 📖 Improve documentation and deployment guides
- 🌐 Additional README translations (Japanese / Korean / …)
- 🎨 Frontend UI polish and accessibility
- 🔌 Integrate more channels (DingTalk / WeCom / more SMS gateways)

---

## 📄 License

Released under the [**Apache License 2.0**](LICENSE).

Third-party component notices are in [NOTICE](NOTICE).

**Copyright 2026 Haier COSMOPlat IoT Technology Co., Ltd.**

---

<div align="center">

**KuYiji** · Making shop-floor data actually useful, for the first time

</div>
