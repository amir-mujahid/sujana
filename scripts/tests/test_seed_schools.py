import pytest
import uuid as _uuid
import tempfile
from pathlib import Path
from seed_schools import is_secondary, extract_coords, make_school_id, process, write_sql


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

    def test_sekolah_kebangsaan_excluded(self):
        assert is_secondary("Sekolah Kebangsaan Taman Melati") is False

    def test_srk_excluded(self):
        assert is_secondary("SRK Bukit Jalil") is False

    def test_taska_excluded(self):
        assert is_secondary("Taska Permata Sentosa") is False

    def test_sma_included(self):
        assert is_secondary("SMA Johor") is True

    def test_sm_teknik_included(self):
        assert is_secondary("SM Teknik Kuantan") is True

    def test_sm_agama_included(self):
        assert is_secondary("SM Agama Kuala Lumpur") is True


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


class TestMakeSchoolId:
    def test_deterministic(self):
        assert make_school_id("SMK Subang Jaya", 3.0809, 101.5831) == make_school_id("SMK Subang Jaya", 3.0809, 101.5831)

    def test_different_names_give_different_ids(self):
        assert make_school_id("SMK Subang Jaya", 3.0809, 101.5831) != make_school_id("SMK Tropicana", 3.1579, 101.5959)

    def test_same_name_different_coords_give_different_ids(self):
        assert make_school_id("SMK Tunku Abdul Rahman", 1.56, 110.31) != make_school_id("SMK Tunku Abdul Rahman", 5.16, 100.48)

    def test_valid_uuid5(self):
        result = make_school_id("SMK Test", 3.0, 101.0)
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
        # 3.12341 rounds to 3.1234 at 4 decimal places — same key as 3.1234
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
        # SQL-escaped: ' becomes ''
        assert "SM All Saints''" in content

    def test_deterministic_across_two_calls(self):
        schools = [{"name": "SMK Test", "lat": 3.0, "lng": 101.0}]
        assert self._write(schools) == self._write(schools)

    def test_empty_schools_no_insert_block(self):
        content = self._write([])
        assert "INSERT INTO schools" not in content
        assert "ON CONFLICT" not in content
