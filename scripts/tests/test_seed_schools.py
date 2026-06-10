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
