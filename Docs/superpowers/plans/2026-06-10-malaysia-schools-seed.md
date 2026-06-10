# Malaysian Secondary Schools Seed — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Write a Python script that fetches Malaysian secondary schools from the OSM Overpass API, filters to Form 1–6 school types, and generates `V7__seed_schools_malaysia.sql` — a committed Flyway migration with ~2,000–3,000 real schools including GPS coordinates.

**Architecture:** A single self-contained script (`scripts/seed_schools.py`) handles fetching, filtering, deduplication, and SQL generation. All pure logic has unit tests. The generated SQL file is committed once and never regenerated. UUIDs are deterministic (`uuid5`) so Flyway's checksum stays stable.

**Tech Stack:** Python 3.9+, `requests` (HTTP), `pytest` (tests), Flyway (migration auto-runs on backend start)

---

## File Map

| File | Status | Responsibility |
|---|---|---|
| `scripts/seed_schools.py` | Create | Fetch + filter + dedup + SQL output |
| `scripts/tests/__init__.py` | Create | Python package marker (empty) |
| `scripts/tests/conftest.py` | Create | Adds `scripts/` to `sys.path` for imports |
| `scripts/tests/test_seed_schools.py` | Create | Unit tests for all pure functions |
| `backend/src/main/resources/db/migration/V7__seed_schools_malaysia.sql` | Generate | Flyway migration (script output, committed once) |

---

### Task 1: Script skeleton — constants, `is_secondary`, `extract_coords` (TDD)

**Files:**
- Create: `scripts/tests/__init__.py`
- Create: `scripts/tests/conftest.py`
- Create: `scripts/tests/test_seed_schools.py` (filter + coord tests only)
- Create: `scripts/seed_schools.py` (constants + `is_secondary` + `extract_coords`)

- [ ] **Step 1: Create the test package support files**

Create `scripts/tests/__init__.py` — empty file.

Create `scripts/tests/conftest.py`:
```python
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent.parent))
```

- [ ] **Step 2: Write failing tests for `is_secondary` and `extract_coords`**

Create `scripts/tests/test_seed_schools.py`:
```python
import pytest
from seed_schools import is_secondary, extract_coords


class TestIsSecondary:
    def test_smk_included(self):
        assert is_secondary("SMK Subang Jaya") is True

    def test_smjk_included(self):
        assert is_secondary("SMJK Yu Hua") is True

    def test_mrsm_included(self):
        assert is_secondary("MRSM Kuala Terengganu") is True

    def test_sbp_included(self):
        assert is_secondary("SBP Integrasi Rawang") is True

    def test_kolej_vokasional_included(self):
        assert is_secondary("Kolej Vokasional Kuala Lumpur") is True

    def test_sekolah_menengah_catch_all_included(self):
        assert is_secondary("Sekolah Menengah Kebangsaan Damansara Utama") is True

    def test_sm_sains_included(self):
        assert is_secondary("SM Sains Selangor") is True

    def test_sm_prefix_included(self):
        assert is_secondary("SM Alam Shah") is True

    def test_sk_excluded(self):
        assert is_secondary("SK Damansara Jaya") is False

    def test_srjk_excluded(self):
        assert is_secondary("SRJK (C) Chung Hua") is False

    def test_sjk_excluded(self):
        assert is_secondary("SJK (T) Ladang Bikam") is False

    def test_sekolah_rendah_excluded(self):
        assert is_secondary("Sekolah Rendah Kebangsaan Seri Bintang") is False

    def test_tadika_excluded(self):
        assert is_secondary("Tadika Kemas Batu 4") is False

    def test_tabika_excluded(self):
        assert is_secondary("TABIKA KEMAS Sri Serdang") is False

    def test_empty_excluded(self):
        assert is_secondary("") is False

    def test_case_insensitive_smk(self):
        assert is_secondary("smk bandar baru bangi") is True

    def test_case_insensitive_sk(self):
        assert is_secondary("sk pandan indah") is False


class TestExtractCoords:
    def test_node_returns_lat_lon(self):
        el = {"type": "node", "lat": 3.1234, "lon": 101.5678}
        assert extract_coords(el) == (3.1234, 101.5678)

    def test_way_uses_center(self):
        el = {"type": "way", "center": {"lat": 3.5, "lon": 102.1}}
        assert extract_coords(el) == (3.5, 102.1)

    def test_relation_uses_center(self):
        el = {"type": "relation", "center": {"lat": 4.0, "lon": 103.0}}
        assert extract_coords(el) == (4.0, 103.0)

    def test_node_missing_lat_returns_none(self):
        assert extract_coords({"type": "node"}) is None

    def test_way_missing_center_returns_none(self):
        assert extract_coords({"type": "way"}) is None

    def test_way_center_missing_lat_returns_none(self):
        assert extract_coords({"type": "way", "center": {}}) is None
```

- [ ] **Step 3: Run tests — expect ImportError**

```bash
cd scripts && python -m pytest tests/ -v
```
Expected: `ModuleNotFoundError: No module named 'seed_schools'`

- [ ] **Step 4: Create `scripts/seed_schools.py` with constants and pure functions**

Create `scripts/seed_schools.py`:
```python
import uuid
from pathlib import Path

NAMESPACE = uuid.NAMESPACE_URL

INCLUDE_PREFIXES = [
    "smk ", "smjk ", "sbp ", "mrsm ",
    "sm sains", "sm teknik", "sm agama", "sma ",
    "kolej vokasional ",
    "sekolah menengah",
    "sm ",
]

EXCLUDE_PREFIXES = [
    "sk ", "srk", "srjk", "sjk",
    "sekolah rendah", "sekolah kebangsaan ",
    "tadika", "tabika", "kemas", "taska",
]

OUTPUT_PATH = (
    Path(__file__).parent.parent
    / "backend/src/main/resources/db/migration/V7__seed_schools_malaysia.sql"
)

V3_DELETE_IDS = [
    "a1b2c3d4-0000-0000-0000-000000000001",  # SK Damansara Jaya  (primary)
    "a1b2c3d4-0000-0000-0000-000000000003",  # SK Ara Damansara   (primary)
    "a1b2c3d4-0000-0000-0000-000000000004",  # SK Taman Megah     (primary)
]


def is_secondary(name: str) -> bool:
    """Return True if name matches a Malaysian secondary school type."""
    n = name.strip().lower()
    if not n:
        return False
    for prefix in EXCLUDE_PREFIXES:
        if n.startswith(prefix):
            return False
    for prefix in INCLUDE_PREFIXES:
        if n.startswith(prefix):
            return True
    return False


def extract_coords(element: dict) -> tuple[float, float] | None:
    """Return (lat, lng) from an OSM element dict, or None if coordinates are absent."""
    if element["type"] == "node":
        lat = element.get("lat")
        lng = element.get("lon")
    else:
        center = element.get("center", {})
        lat = center.get("lat")
        lng = center.get("lon")
    if lat is None or lng is None:
        return None
    return lat, lng
```

- [ ] **Step 5: Run filter + coord tests — expect all PASS**

```bash
cd scripts && python -m pytest tests/test_seed_schools.py::TestIsSecondary tests/test_seed_schools.py::TestExtractCoords -v
```
Expected: 17 passed, 0 failed

- [ ] **Step 6: Commit**

```bash
git add scripts/seed_schools.py scripts/tests/__init__.py scripts/tests/conftest.py scripts/tests/test_seed_schools.py
git commit -m "feat(seed): filter and coord extraction with tests"
```

---

### Task 2: `make_school_id`, `process`, `write_sql` (TDD)

**Files:**
- Modify: `scripts/tests/test_seed_schools.py` — append three new test classes
- Modify: `scripts/seed_schools.py` — append three new functions

- [ ] **Step 1: Append new test classes to `scripts/tests/test_seed_schools.py`**

Append after the existing test classes:
```python
import uuid as _uuid
import tempfile
from pathlib import Path
from seed_schools import make_school_id, process, write_sql


class TestMakeSchoolId:
    def test_deterministic(self):
        assert make_school_id("SMK Subang Jaya") == make_school_id("SMK Subang Jaya")

    def test_different_names_give_different_ids(self):
        assert make_school_id("SMK Subang Jaya") != make_school_id("SMK Tropicana")

    def test_valid_uuid5(self):
        result = make_school_id("SMK Test")
        parsed = _uuid.UUID(result)
        assert parsed.version == 5


class TestProcess:
    def _node(self, name, lat=3.0, lng=101.0):
        return {"type": "node", "lat": lat, "lon": lng, "tags": {"name": name}}

    def test_includes_smk(self):
        result = process([self._node("SMK Damansara")])
        assert len(result) == 1
        assert result[0]["name"] == "SMK Damansara"

    def test_excludes_primary(self):
        assert process([self._node("SK Damansara")]) == []

    def test_skips_missing_name(self):
        el = {"type": "node", "lat": 3.0, "lon": 101.0, "tags": {}}
        assert process([el]) == []

    def test_skips_missing_coords(self):
        el = {"type": "node", "tags": {"name": "SMK Test"}}
        assert process([el]) == []

    def test_deduplicates_by_rounded_coords(self):
        # 3.12341 and 3.1234 both round to 3.1234 at 4 decimal places
        elements = [
            self._node("SMK Damansara", 3.1234, 101.5678),
            self._node("SMK Damansara", 3.12341, 101.56781),
        ]
        assert len(process(elements)) == 1

    def test_keeps_distinct_locations_same_name(self):
        elements = [
            self._node("SMK Damansara", 3.1234, 101.5678),
            self._node("SMK Damansara", 3.9999, 102.9999),
        ]
        assert len(process(elements)) == 2

    def test_sorted_by_name(self):
        elements = [
            self._node("SMK Zetara", lat=3.1),
            self._node("SMK Alpha", lat=3.2),
        ]
        result = process(elements)
        assert result[0]["name"] == "SMK Alpha"
        assert result[1]["name"] == "SMK Zetara"


class TestWriteSql:
    def _write(self, schools):
        tmp = Path(tempfile.mktemp(suffix=".sql"))
        write_sql(schools, tmp)
        return tmp.read_text(encoding="utf-8")

    def test_contains_delete_block(self):
        content = self._write([])
        assert "DELETE FROM schools WHERE id IN" in content
        assert "a1b2c3d4-0000-0000-0000-000000000001" in content
        assert "a1b2c3d4-0000-0000-0000-000000000003" in content
        assert "a1b2c3d4-0000-0000-0000-000000000004" in content

    def test_contains_insert_and_conflict_clause(self):
        schools = [{"name": "SMK Test", "lat": 3.1, "lng": 101.5}]
        content = self._write(schools)
        assert "INSERT INTO schools (id, name, lat, lng) VALUES" in content
        assert "SMK Test" in content
        assert "ON CONFLICT (id) DO NOTHING;" in content

    def test_escapes_single_quotes_in_name(self):
        schools = [{"name": "SM All Saints'", "lat": 3.0, "lng": 101.0}]
        content = self._write(schools)
        # SQL-escaped: single quote becomes two single quotes
        assert "SM All Saints''" in content

    def test_deterministic_across_two_calls(self):
        schools = [{"name": "SMK Test", "lat": 3.0, "lng": 101.0}]
        assert self._write(schools) == self._write(schools)
```

- [ ] **Step 2: Run new tests — expect ImportError**

```bash
cd scripts && python -m pytest tests/ -v -k "TestMakeSchoolId or TestProcess or TestWriteSql"
```
Expected: `ImportError: cannot import name 'make_school_id' from 'seed_schools'`

- [ ] **Step 3: Append `make_school_id`, `process`, and `write_sql` to `scripts/seed_schools.py`**

Append to the bottom of `scripts/seed_schools.py`:
```python
from datetime import date


def make_school_id(name: str) -> str:
    """Deterministic UUID5 keyed on school name — stable across re-runs."""
    return str(uuid.uuid5(NAMESPACE, f"isujana:school:{name}"))


def process(elements: list[dict]) -> list[dict]:
    """Filter, deduplicate, and sort OSM elements into school dicts."""
    seen: set[tuple] = set()
    schools: list[dict] = []
    for el in elements:
        name = el.get("tags", {}).get("name", "").strip()
        if not name or not is_secondary(name):
            continue
        coords = extract_coords(el)
        if coords is None:
            continue
        lat, lng = coords
        key = (name, round(lat, 4), round(lng, 4))
        if key in seen:
            continue
        seen.add(key)
        schools.append({"name": name, "lat": lat, "lng": lng})
    schools.sort(key=lambda s: s["name"])
    return schools


def write_sql(schools: list[dict], path: Path) -> None:
    """Write Flyway-compatible SQL migration to path."""
    delete_ids = ",\n".join(f"    '{sid}'" for sid in V3_DELETE_IDS)
    rows = [
        f"    ('{make_school_id(s['name'])}', '{s['name'].replace(chr(39), chr(39)*2)}', {s['lat']}, {s['lng']})"
        for s in schools
    ] or ["    -- (no schools generated)"]

    sql = (
        f"-- Generated by scripts/seed_schools.py on {date.today()} — do not edit by hand.\n"
        f"-- Source: OpenStreetMap Overpass API (amenity=school, Malaysia, secondary only).\n"
        f"\n"
        f"DELETE FROM schools WHERE id IN (\n"
        f"{delete_ids}\n"
        f");\n"
        f"\n"
        f"INSERT INTO schools (id, name, lat, lng) VALUES\n"
        + ",\n".join(rows) + "\n"
        f"ON CONFLICT (id) DO NOTHING;\n"
    )
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(sql, encoding="utf-8")
```

- [ ] **Step 4: Run all tests — expect all PASS**

```bash
cd scripts && python -m pytest tests/ -v
```
Expected: all tests pass

- [ ] **Step 5: Commit**

```bash
git add scripts/seed_schools.py scripts/tests/test_seed_schools.py
git commit -m "feat(seed): process(), make_school_id(), write_sql() with tests"
```

---

### Task 3: Network fetch + `__main__` entry point

**Files:**
- Modify: `scripts/seed_schools.py` — add `requests` import, `OVERPASS_*` constants, `fetch_schools`, `__main__` block

No unit test for the network call — all interesting logic (`process`, `write_sql`) is already covered.

- [ ] **Step 1: Add `import requests` to the top of `scripts/seed_schools.py`**

The first line of imports currently reads `import uuid`. Change the imports block to:
```python
import uuid
from datetime import date
from pathlib import Path

import requests
```

Remove the duplicate `from datetime import date` that was appended in Task 2 Step 3.

- [ ] **Step 2: Append `OVERPASS_*` constants, `fetch_schools`, and `__main__` to `scripts/seed_schools.py`**

Append to the bottom:
```python
OVERPASS_URL = "https://overpass-api.de/api/interpreter"
OVERPASS_QUERY = """
[out:json][timeout:120];
area["ISO3166-1"="MY"][admin_level=2]->.malaysia;
(
  nwr["amenity"="school"](area.malaysia);
);
out center tags;
"""


def fetch_schools() -> list[dict]:
    """Query OSM Overpass API; retry once on HTTP 429."""
    import time
    print("Querying Overpass API... (this may take 30-60s)")
    for attempt in range(2):
        resp = requests.post(
            OVERPASS_URL,
            data={"data": OVERPASS_QUERY},
            timeout=180,
        )
        if resp.status_code == 429 and attempt == 0:
            print("Rate limited — waiting 30 s before retry...")
            time.sleep(30)
            continue
        resp.raise_for_status()
        break
    elements = resp.json()["elements"]
    print(f"Fetched {len(elements)} elements.")
    return elements


if __name__ == "__main__":
    elements = fetch_schools()
    schools = process(elements)
    print(f"After filter + dedup: {len(schools)} secondary schools.")
    write_sql(schools, OUTPUT_PATH)
    print(f"Written: {OUTPUT_PATH}")
```

- [ ] **Step 3: Run all tests — expect all still PASS**

```bash
cd scripts && python -m pytest tests/ -v
```
Expected: all tests pass (no regressions from restructuring imports)

- [ ] **Step 4: Commit**

```bash
git add scripts/seed_schools.py
git commit -m "feat(seed): add fetch_schools() and __main__ entry point"
```

---

### Task 4: Run the script and commit the generated migration

- [ ] **Step 1: Install dependency**

```bash
pip install requests
```

- [ ] **Step 2: Run the script**

From repo root:
```bash
python scripts/seed_schools.py
```

Expected console output (exact numbers will vary):
```
Querying Overpass API... (this may take 30-60s)
Fetched XXXX elements.
After filter + dedup: YYYY secondary schools.
Written: backend/src/main/resources/db/migration/V7__seed_schools_malaysia.sql
```

- **If YYYY < 1,000:** filter may be too narrow — open `seed_schools.py` and check that `INCLUDE_PREFIXES` list is correct.
- **If YYYY > 5,000:** filter may be too broad — spot-check names in the SQL file for non-secondary schools.
- **If `requests.exceptions.HTTPError: 429`:** wait 60 s and re-run; Overpass rate-limits burst traffic.

- [ ] **Step 3: Spot-check the generated SQL**

Open `backend/src/main/resources/db/migration/V7__seed_schools_malaysia.sql`. Verify:

1. Header comment present at top.
2. `DELETE FROM schools WHERE id IN (` block contains exactly 3 UUIDs (`...000001`, `...000003`, `...000004`).
3. First 5–10 INSERT rows have names like `SMK …`, `SMJK …`, `MRSM …` — no `SK `, `Tadika`, `Sekolah Rendah`.
4. Last INSERT row ends with `)` not `),` (no trailing comma before `ON CONFLICT`).
5. File ends with `ON CONFLICT (id) DO NOTHING;`.

- [ ] **Step 4: Commit script + migration together**

```bash
git add scripts/ backend/src/main/resources/db/migration/V7__seed_schools_malaysia.sql
git commit -m "feat(seed): Malaysian secondary schools Flyway migration (OSM)"
```

---

## Self-Review Notes

**Spec coverage:**
- ✅ Python script at `scripts/seed_schools.py`
- ✅ OSM Overpass API as data source
- ✅ Secondary school filter (SMK, SMJK, SBP, MRSM, SM Agama/Teknik/Sains, Kolej Vokasional, Sekolah Menengah)
- ✅ Excludes primary/kindergarten
- ✅ Deduplication by (name, rounded lat, rounded lng)
- ✅ Deterministic UUIDs via `uuid5`
- ✅ Flyway migration `V7__seed_schools_malaysia.sql`
- ✅ DELETE of V3 primary placeholder rows
- ✅ `ON CONFLICT (id) DO NOTHING` for idempotency
- ✅ `tenant_id` remains NULL (Stage 7 manages tenant assignment)

**Placeholder scan:** No TBD/TODO present.

**Type consistency:** `process()` → `list[dict]` with keys `name`, `lat`, `lng`; `write_sql()` consumes that exact shape. `make_school_id(name: str) -> str` called consistently in `write_sql`.
