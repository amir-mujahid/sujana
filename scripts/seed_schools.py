from __future__ import annotations

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
