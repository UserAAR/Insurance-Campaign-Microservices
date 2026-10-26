#!/usr/bin/env python3
"""Kitabın üz qabığını SVG kimi yaradır: cover/cover.svg (2-ci versiya).

Kompozisiya (A4, viewBox 2100 × 2970, 1 vahid = 0,1 mm):
  1. Başlıq zolağı — pətək işarəsi, texnologiyalar.
  2. "Lövhə" — XIX əsr elmi atlas üslubunda çərçivə: ikiqat xətt, kəsim işarələri, millimetrlik tor, miqyas
     xətti. İçində əl ilə ştrixlənmiş qravüra kimi bal arısı (Apis mellifera), künclərdə pətək, A–F anatomik
     göstəricilər və onların sistem qatlarına uyğunluğunu izah edən legenda.
  3. Başlıq paneli — iki rəngli başlıq, altbaşlıq, mövzu "çipləri", kitabın göstəriciləri və müəllif.

Bütün qrafika vektordur və koddan qurulur: xarici şəkil, SVG filtri və rastr effekt istifadə olunmur ki, PDF-də
istənilən miqyasda kəskin qalsın. Göstəricilər (fəsil, diaqram, tapşırıq, istinad sayı) kitabın mənbələrindən
hesablanır.

İstifadə: python3 build/cover.py
"""
import glob
import json
import math
import os
import random
import re

HERE = os.path.dirname(os.path.abspath(__file__))
GUIDE = os.path.dirname(HERE)
OUT = os.path.join(GUIDE, "cover", "cover.svg")

W, H = 2100, 2970
CX = W / 2

# Palitra
INK = "#1c1b19"
PAPER = "#f5f0e6"
PAPER_DEEP = "#ebe2d0"
GRID = "#d9cdb5"
TEAL = "#0e4550"
TEAL_DEEP = "#082f37"
TEAL_LINE = "#2c6a74"
GOLD = "#dca43a"
GOLD_SOFT = "#efc773"
MUTED = "#76695a"

# Lövhənin sərhədləri
PLATE_X0, PLATE_X1 = 110, 1990
PLATE_Y0, PLATE_Y1 = 220, 1810
TITLE_Y0 = 1880
BEE_DY = -40  # arının şaquli sürüşməsi

rng = random.Random(25)


def az_upper(text):
    """Azərbaycan əlifbası üçün böyük hərf: i → İ, ı → I."""
    return text.replace("i", "İ").replace("ı", "I").upper()


def esc(text):
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def mirror(svg):
    """Sağ yarı üçün çəkilmiş elementi simmetrik sol yarıya da əlavə edir."""
    return f"<g>{svg}</g><g transform=\"translate({2 * CX} 0) scale(-1 1)\">{svg}</g>"


def path(d, width=3, color=INK, opacity=1.0, extra=""):
    return (f'<path d="{d}" stroke="{color}" stroke-width="{width}" fill="none" stroke-linecap="round" '
            f'stroke-linejoin="round" opacity="{opacity}" {extra}/>')


# --------------------------------------------------------------------------- Kitabın göstəriciləri
def book_stats():
    chapters = sorted(glob.glob(os.path.join(GUIDE, "chapters", "[0-9][0-9]-*.md")))
    numbered = [c for c in chapters if not os.path.basename(c).startswith("00-")]
    text = "".join(open(c, encoding="utf-8").read() for c in chapters)
    diagrams = text.count("```mermaid")
    exercises = len(re.findall(r"^\*\*T\d+ ", text, re.M))
    refs = 0
    vr = os.path.join(GUIDE, "audit", "verify-refs.json")
    if os.path.exists(vr):
        refs = json.load(open(vr, encoding="utf-8")).get("total_refs", 0)
    return len(numbered), diagrams, exercises, refs


# --------------------------------------------------------------------------- Lövhə: tor, pətək, çərçivə
def plate_background():
    out = [f'<rect x="{PLATE_X0}" y="{PLATE_Y0}" width="{PLATE_X1 - PLATE_X0}" height="{PLATE_Y1 - PLATE_Y0}" '
           f'fill="url(#plateGlow)"/>']
    # Millimetrlik tor: kiçik addım 30, böyük addım 150
    for x in range(PLATE_X0 + 30, PLATE_X1, 30):
        major = (x - PLATE_X0) % 150 == 0
        out.append(f'<line x1="{x}" y1="{PLATE_Y0}" x2="{x}" y2="{PLATE_Y1}" stroke="{GRID}" '
                   f'stroke-width="{1.6 if major else 0.8}" opacity="{0.55 if major else 0.32}"/>')
    for y in range(PLATE_Y0 + 30, PLATE_Y1, 30):
        major = (y - PLATE_Y0) % 150 == 0
        out.append(f'<line x1="{PLATE_X0}" y1="{y}" x2="{PLATE_X1}" y2="{y}" stroke="{GRID}" '
                   f'stroke-width="{1.6 if major else 0.8}" opacity="{0.55 if major else 0.32}"/>')
    return "\n".join(out)


def honeycomb_cluster(cx, cy, cols, rows, r, honey, fade_to):
    """Bir küncdə pətək qrupu; mərkəzdən uzaqlaşdıqca solğunlaşır."""
    out = []
    dx, dy = 1.5 * r, math.sqrt(3) * r
    for q in range(cols):
        for row in range(rows):
            x = cx + q * dx
            y = cy + row * dy + (dy / 2 if q % 2 else 0)
            dist = math.hypot(x - fade_to[0], y - fade_to[1])
            opacity = max(0.0, min(0.9, (dist - 380) / 520))
            if opacity < 0.05:
                continue
            pts = " ".join(f"{x + r * math.cos(math.pi / 3 * k):.1f},{y + r * math.sin(math.pi / 3 * k):.1f}"
                           for k in range(6))
            inner = " ".join(f"{x + (r - 9) * math.cos(math.pi / 3 * k):.1f},"
                             f"{y + (r - 9) * math.sin(math.pi / 3 * k):.1f}" for k in range(6))
            filled = (q, row) in honey
            out.append(f'<g opacity="{opacity:.2f}">'
                       f'<polygon points="{pts}" fill="{"url(#honey)" if filled else "none"}" stroke="#b7a37f" '
                       f'stroke-width="3"/>'
                       f'<polygon points="{inner}" fill="none" stroke="#cbbb9b" stroke-width="1.4"/></g>')
    return "\n".join(out)


def crop_marks():
    out, L, g = [], 46, 16
    for x, y, sx, sy in ((PLATE_X0, PLATE_Y0, -1, -1), (PLATE_X1, PLATE_Y0, 1, -1),
                         (PLATE_X0, PLATE_Y1, -1, 1), (PLATE_X1, PLATE_Y1, 1, 1)):
        out.append(f'<line x1="{x + sx * g}" y1="{y}" x2="{x + sx * (g + L)}" y2="{y}" stroke="{MUTED}" '
                   f'stroke-width="2"/>')
        out.append(f'<line x1="{x}" y1="{y + sy * g}" x2="{x}" y2="{y + sy * (g + L)}" stroke="{MUTED}" '
                   f'stroke-width="2"/>')
    return "\n".join(out)


def plate_frame():
    return (f'<rect x="{PLATE_X0}" y="{PLATE_Y0}" width="{PLATE_X1 - PLATE_X0}" height="{PLATE_Y1 - PLATE_Y0}" '
            f'fill="none" stroke="{INK}" stroke-width="5"/>'
            f'<rect x="{PLATE_X0 + 14}" y="{PLATE_Y0 + 14}" width="{PLATE_X1 - PLATE_X0 - 28}" '
            f'height="{PLATE_Y1 - PLATE_Y0 - 28}" fill="none" stroke="{INK}" stroke-width="1.6"/>')


def plate_texts():
    y = PLATE_Y0 + 74
    out = [f'<text x="{PLATE_X0 + 50}" y="{y}" font-family="Guide Sans, sans-serif" font-weight="700" '
           f'font-size="30" letter-spacing="8" fill="{INK}">LÖVHƏ I</text>',
           f'<text x="{PLATE_X0 + 50}" y="{y + 40}" font-family="Guide Serif, serif" font-style="italic" '
           f'font-size="28" fill="{MUTED}">Koloniyanın anatomiyası</text>',
           f'<text x="{PLATE_X1 - 50}" y="{y}" text-anchor="end" font-family="Guide Sans, sans-serif" '
           f'font-weight="700" font-size="30" letter-spacing="8" fill="{INK}">ŞƏKİL 1</text>',
           f'<text x="{PLATE_X1 - 50}" y="{y + 40}" text-anchor="end" font-family="Guide Serif, serif" '
           f'font-style="italic" font-size="28" fill="{MUTED}">dorsal görünüş, ×12</text>']
    # Miqyas xətti
    x0, yb = PLATE_X1 - 360, y + 86
    out.append(f'<line x1="{x0}" y1="{yb}" x2="{x0 + 300}" y2="{yb}" stroke="{INK}" stroke-width="3"/>')
    for i in range(6):
        xi = x0 + i * 60
        out.append(f'<line x1="{xi}" y1="{yb - (14 if i % 5 == 0 else 9)}" x2="{xi}" y2="{yb}" stroke="{INK}" '
                   f'stroke-width="3"/>')
        if i % 2 == 0 or i == 5:
            out.append(f'<text x="{xi}" y="{yb + 30}" text-anchor="middle" font-family="Guide Sans, sans-serif" '
                       f'font-size="20" fill="{MUTED}">{i}</text>')
    out.append(f'<rect x="{x0}" y="{yb - 6}" width="60" height="6" fill="{INK}"/>'
               f'<rect x="{x0 + 120}" y="{yb - 6}" width="60" height="6" fill="{INK}"/>'
               f'<rect x="{x0 + 240}" y="{yb - 6}" width="60" height="6" fill="{INK}"/>')
    out.append(f'<text x="{x0 + 326}" y="{yb + 6}" font-family="Guide Sans, sans-serif" font-size="20" '
               f'fill="{MUTED}">mm</text>')
    return "\n".join(out)


# --------------------------------------------------------------------------- Arı: qravüra texnikaları
def contour_ellipses(cx, cy, rx, ry, steps, width, clip, opacity=0.75):
    out = []
    for i in range(1, steps + 1):
        f = i / (steps + 1)
        out.append(f'<ellipse cx="{cx + rx * 0.10 * f:.1f}" cy="{cy + ry * 0.12 * f:.1f}" rx="{rx * (1 - f):.1f}" '
                   f'ry="{ry * (1 - f):.1f}" fill="none" stroke="{INK}" stroke-width="{width}" '
                   f'opacity="{opacity}" clip-path="url(#{clip})"/>')
    return "\n".join(out)


def stipple(cx, cy, rx, ry, n, clip, bias=0.55):
    """Nöqtəli kölgə: nöqtələr sağ-aşağı (işıqdan uzaq) tərəfdə sıxlaşır."""
    dots = []
    for _ in range(n):
        a = rng.uniform(0, 2 * math.pi)
        rad = math.sqrt(rng.uniform(0, 1))
        x, y = cx + rx * rad * math.cos(a), cy + ry * rad * math.sin(a)
        shade = ((x - cx) / rx + (y - cy) / ry) * 0.5 + bias
        if rng.random() < max(0.05, min(0.95, shade)):
            dots.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{rng.uniform(1.2, 2.6):.1f}"/>')
    return f'<g fill="{INK}" clip-path="url(#{clip})" opacity="0.8">{"".join(dots)}</g>'


def hairs(cx, cy, rx, ry, n, length, width, arc=(0, 2 * math.pi), jitter=0.4):
    out = []
    for i in range(n):
        a = arc[0] + (arc[1] - arc[0]) * i / n + rng.uniform(-0.015, 0.015)
        x0, y0 = cx + rx * math.cos(a), cy + ry * math.sin(a)
        nx, ny = math.cos(a) / rx, math.sin(a) / ry
        nrm = math.hypot(nx, ny)
        nx, ny = nx / nrm, ny / nrm
        t = rng.uniform(-jitter, jitter)
        dx, dy = nx * math.cos(t) - ny * math.sin(t), nx * math.sin(t) + ny * math.cos(t)
        ln = length * rng.uniform(0.5, 1.15)
        sx, sy = x0 - nx * ln * 0.3, y0 - ny * ln * 0.3
        out.append(f"M{sx:.1f},{sy:.1f} Q{x0 + dx * ln * 0.55:.1f},{y0 + dy * ln * 0.45:.1f} "
                   f"{x0 + dx * ln:.1f},{y0 + dy * ln:.1f}")
    return f'<path d="{" ".join(out)}" stroke="{INK}" stroke-width="{width}" fill="none" stroke-linecap="round"/>'


def wing(outline, veins, cells, stigma):
    out = [f'<path d="{outline}" fill="url(#wingFill)" stroke="none"/>',
           f'<path d="{outline}" fill="url(#hatchMembrane)" opacity="0.5"/>']
    for d, w in veins:
        out.append(path(d, w, INK, 0.9))
    for c in cells:
        out.append(f'<path d="{c}" fill="{INK}" opacity="0.06"/>')
    out.append(f'<path d="{stigma}" fill="{INK}" opacity="0.85"/>')
    out.append(path(outline, 4.5))
    # Kənar boyunca incə tükcüklər
    return "\n".join(out)


def leg(points, base_w=11):
    out, n = [], len(points) - 1
    for i, ((x0, y0), (x1, y1)) in enumerate(zip(points, points[1:])):
        w = base_w * (1 - 0.13 * i)
        out.append(path(f"M{x0},{y0} L{x1},{y1}", w))
        out.append(f'<circle cx="{x0}" cy="{y0}" r="{w * 0.62:.1f}" fill="{PAPER}" stroke="{INK}" '
                   f'stroke-width="2.4"/>')
        ang = math.atan2(y1 - y0, x1 - x0)
        hair = []
        for k in range(7):
            t = (k + 0.5) / 7
            px, py = x0 + (x1 - x0) * t, y0 + (y1 - y0) * t
            side = 1 if k % 2 else -1
            a2 = ang + side * 1.15
            ln = rng.uniform(9, 17)
            hair.append(f"M{px:.1f},{py:.1f} L{px + ln * math.cos(a2):.1f},{py + ln * math.sin(a2):.1f}")
        out.append(path(" ".join(hair), 2))
    # Pəncə (iki qısa çəngəl)
    (xa, ya), (xb, yb) = points[-2], points[-1]
    ang = math.atan2(yb - ya, xb - xa)
    claws = " ".join(f"M{xb},{yb} L{xb + 16 * math.cos(ang + s * 0.6):.1f},{yb + 16 * math.sin(ang + s * 0.6):.1f}"
                     for s in (-1, 1))
    out.append(path(claws, 3))
    return "\n".join(out)


def antenna():
    out = [path("M1074,626 Q1092,580 1122,520", 11)]  # sap (scape)
    pts = []
    for i in range(12):  # buğumlu qamçı (flagellum)
        t = i / 11
        x = 1122 + 230 * t
        y = 520 - 175 * t + 60 * t * t
        pts.append((x, y))
    out.append(path("M" + " L".join(f"{x:.1f},{y:.1f}" for x, y in pts), 7))
    for i, (x, y) in enumerate(pts[1:], 1):
        ang = math.atan2(pts[i][1] - pts[i - 1][1], pts[i][0] - pts[i - 1][0])
        out.append(f'<ellipse cx="{x:.1f}" cy="{y:.1f}" rx="9" ry="6" transform="rotate({math.degrees(ang):.1f} '
                   f'{x:.1f} {y:.1f})" fill="{INK}"/>')
    return "\n".join(out)


def bee():
    parts = [f'<g transform="translate(0 {BEE_DY})">']
    # ---- Qanadlar
    fore = ("M1148,832 C1300,728 1520,612 1702,578 C1806,558 1872,596 1858,660 C1846,728 1764,778 1650,812 "
            "C1478,862 1296,884 1162,872 Z")
    fore_veins = [("M1150,842 C1340,776 1560,672 1838,604", 4.2),  # kostal
                  ("M1156,856 C1360,820 1540,744 1702,690", 3),
                  ("M1160,866 C1370,858 1580,808 1770,742", 3),
                  ("M1350,812 C1416,758 1468,704 1500,656", 2.6), ("M1500,656 C1560,686 1604,698 1662,690", 2.6),
                  ("M1440,792 C1520,770 1594,752 1662,690", 2.6), ("M1662,690 C1702,700 1742,704 1792,690", 2.6),
                  ("M1560,804 C1602,772 1640,748 1684,738", 2.4), ("M1268,850 C1300,820 1330,810 1350,812", 2.4)]
    fore_cells = ["M1500,656 C1560,686 1604,698 1662,690 C1594,752 1520,770 1440,792 C1468,740 1488,700 1500,656 Z",
                  "M1662,690 C1702,700 1742,704 1792,690 C1770,722 1730,736 1684,738 C1676,722 1668,706 1662,690 Z"]
    fore_stigma = "M1452,700 C1488,684 1530,666 1566,652 C1540,676 1500,694 1458,708 Z"
    hind = "M1166,900 C1308,904 1500,926 1612,968 C1686,996 1694,1044 1632,1058 C1520,1080 1330,1012 1168,936 Z"
    hind_veins = [("M1170,914 C1330,940 1502,976 1648,1034", 3), ("M1380,948 C1420,980 1470,1010 1520,1030", 2.4),
                  ("M1480,964 C1522,976 1562,986 1612,1000", 2.4)]
    hind_stigma = "M1300,922 C1330,924 1352,928 1372,934 C1350,938 1326,934 1302,930 Z"
    parts.append(mirror(wing(hind, hind_veins, [], hind_stigma)))
    parts.append(mirror(wing(fore, fore_veins, fore_cells, fore_stigma)))

    # ---- Ayaqlar
    front = [(1112, 808), (1186, 744), (1232, 662), (1264, 606), (1282, 566), (1296, 540)]
    middle = [(1160, 902), (1288, 936), (1398, 1000), (1466, 1048), (1508, 1080), (1534, 1100)]
    hind_leg = [(1140, 975), (1262, 1060), (1332, 1190), (1386, 1308), (1414, 1372), (1430, 1410)]
    parts.append(mirror(leg(front, 10)))
    parts.append(mirror(leg(middle, 11)))
    parts.append(mirror(leg(hind_leg, 12)))
    parts.append(mirror(f'<ellipse cx="1298" cy="1124" rx="30" ry="68" transform="rotate(-28 1298 1124)" '
                        f'fill="url(#honey)" stroke="{INK}" stroke-width="4.5"/>'
                        f'<ellipse cx="1298" cy="1124" rx="30" ry="68" transform="rotate(-28 1298 1124)" '
                        f'fill="url(#hatchCross)" opacity="0.35"/>'))

    # ---- Qarın
    abd = ("M1050,986 C1166,986 1216,1080 1216,1186 C1216,1320 1150,1420 1050,1472 "
           "C950,1420 884,1320 884,1186 C884,1080 934,986 1050,986 Z")
    parts.append(f'<clipPath id="abdClip"><path d="{abd}"/></clipPath>')
    parts.append(f'<path d="{abd}" fill="{PAPER}"/>')
    bands = [(992, 1062, True), (1062, 1094, False), (1094, 1170, True), (1170, 1200, False), (1200, 1270, True),
             (1270, 1298, False), (1298, 1366, True), (1366, 1392, False), (1392, 1480, True)]
    for y0, y1, dark in bands:
        band = f"M850,{y0} Q1050,{y0 + 28} 1250,{y0} L1250,{y1} Q1050,{y1 + 28} 850,{y1} Z"
        if not dark:
            parts.append(f'<path d="{band}" fill="url(#honey)" opacity="0.75" clip-path="url(#abdClip)"/>')
        n = 9 if dark else 3
        lines = []
        for k in range(n):
            y = y0 + (y1 - y0) * (k + 0.5) / n
            lines.append(f"M850,{y:.1f} Q1050,{y + 28:.1f} 1250,{y:.1f}")
        parts.append(f'<path d="{" ".join(lines)}" stroke="{INK}" stroke-width="{2.6 if dark else 1.6}" '
                     f'fill="none" clip-path="url(#abdClip)" opacity="{0.9 if dark else 0.6}"/>')
        parts.append(path(f"M850,{y1} Q1050,{y1 + 28} 1250,{y1}", 4, INK, 1, 'clip-path="url(#abdClip)"'))
    # Kölgə tərəfində çarpaz ştrix
    shade = []
    for k in range(26):
        x = 1100 + k * 6.5
        shade.append(f"M{x:.1f},980 L{x + 60:.1f},1490")
    parts.append(f'<path d="{" ".join(shade)}" stroke="{INK}" stroke-width="1.8" fill="none" opacity="0.55" '
                 f'clip-path="url(#abdClip)"/>')
    parts.append(f'<ellipse cx="992" cy="1120" rx="22" ry="70" fill="{PAPER}" opacity="0.35" '
                 f'clip-path="url(#abdClip)"/>')
    parts.append(path(abd, 6))
    parts.append(path("M1050,1472 L1050,1504", 5))
    parts.append(hairs(1050, 1210, 168, 270, 160, 24, 2.0, arc=(-0.35, math.pi + 0.35)))

    # ---- Bel
    parts.append(f'<ellipse cx="1050" cy="986" rx="40" ry="24" fill="{PAPER}" stroke="{INK}" stroke-width="5"/>')
    parts.append(f'<ellipse cx="1050" cy="986" rx="40" ry="24" fill="url(#hatchCross)" opacity="0.8"/>')

    # ---- Döş
    parts.append(hairs(1050, 870, 140, 122, 260, 36, 2.1))
    parts.append(f'<clipPath id="thxClip"><ellipse cx="1050" cy="870" rx="138" ry="120"/></clipPath>')
    parts.append(f'<ellipse cx="1050" cy="870" rx="138" ry="120" fill="{PAPER}"/>')
    parts.append(contour_ellipses(1050, 870, 138, 120, 11, 1.8, "thxClip"))
    parts.append(stipple(1050, 870, 138, 120, 1500, "thxClip"))
    parts.append(f'<ellipse cx="1004" cy="828" rx="58" ry="40" fill="{PAPER}" opacity="0.7"/>')
    parts.append(path("M984,800 C1020,780 1080,780 1116,800", 4))
    parts.append(path("M962,926 C1010,956 1090,956 1138,926", 4))
    parts.append(path("M1050,756 L1050,986", 2, INK, 0.35))
    parts.append(f'<ellipse cx="1050" cy="870" rx="138" ry="120" fill="none" stroke="{INK}" stroke-width="6"/>')

    # ---- Baş
    parts.append(hairs(1050, 690, 96, 80, 120, 20, 1.9, arc=(math.pi * 1.02, math.pi * 1.98)))
    parts.append(f'<clipPath id="headClip"><ellipse cx="1050" cy="690" rx="96" ry="80"/></clipPath>')
    parts.append(f'<ellipse cx="1050" cy="690" rx="96" ry="80" fill="{PAPER}"/>')
    parts.append(contour_ellipses(1050, 690, 96, 80, 6, 1.6, "headClip", 0.6))
    parts.append(stipple(1050, 690, 96, 80, 500, "headClip"))
    parts.append(f'<ellipse cx="1050" cy="690" rx="96" ry="80" fill="none" stroke="{INK}" stroke-width="6"/>')
    eye = (f'<ellipse cx="1112" cy="688" rx="40" ry="66" transform="rotate(14 1112 688)" fill="{PAPER}" '
           f'stroke="{INK}" stroke-width="5"/>'
           f'<ellipse cx="1112" cy="688" rx="40" ry="66" transform="rotate(14 1112 688)" fill="url(#facets)"/>'
           f'<path d="M1126,630 C1158,662 1158,724 1124,752 C1140,712 1140,668 1126,630 Z" fill="{INK}" '
           f'opacity="0.55"/>'
           f'<ellipse cx="1098" cy="662" rx="9" ry="16" transform="rotate(14 1098 662)" fill="#ffffff" '
           f'opacity="0.9"/>')
    parts.append(mirror(eye))
    for ox, oy in ((1050, 632), (1031, 646), (1069, 646)):
        parts.append(f'<circle cx="{ox}" cy="{oy}" r="8" fill="{INK}"/><circle cx="{ox - 2}" cy="{oy - 2}" '
                     f'r="2.4" fill="#ffffff"/>')
    parts.append(mirror(path("M1062,762 C1076,778 1072,794 1056,802", 7)))
    parts.append(mirror(antenna()))
    parts.append("</g>")
    return "\n".join(parts)


# --------------------------------------------------------------------------- Göstəricilər və legenda
LEGEND = [
    ("A", "Bığcıqlar", "API Gateway — giriş, marşrut, rate limit"),
    ("B", "Gözlər", "Təhlükəsizlik — JWT RS256, rollar"),
    ("C", "Döş", "Servislər — auth · campaign · dashboard · log"),
    ("D", "Qanadlar", "İnteqrasiya — REST · gRPC · Kafka"),
    ("E", "Qarın", "Data — PostgreSQL · Redis · Elasticsearch"),
    ("F", "Tüklər", "Müşahidə — metrik · trace · log"),
]


def callouts():
    # (hədəf nöqtə, etiket mərkəzi) — hədəflər arının sürüşməsi nəzərə alınaraq
    dy = BEE_DY
    spec = {"A": ((1330, 395 + dy), (1560, 440)),
            "B": ((972, 700 + dy), (520, 470)),
            "C": ((1160, 930 + dy), (1820, 1240)),
            "D": ((1790, 640 + dy), (1925, 770)),
            "E": ((906, 1310 + dy), (380, 1400)),
            "F": ((1214, 1210 + dy), (1700, 1460))}
    out = []
    for letter, ((x0, y0), (x1, y1)) in spec.items():
        out.append(f'<line x1="{x0}" y1="{y0}" x2="{x1}" y2="{y1}" stroke="{INK}" stroke-width="2" '
                   f'opacity="0.7"/>')
        out.append(f'<circle cx="{x0}" cy="{y0}" r="6" fill="{INK}"/>')
        out.append(f'<circle cx="{x1}" cy="{y1}" r="27" fill="{PAPER}" stroke="{INK}" stroke-width="2.6"/>')
        out.append(f'<text x="{x1}" y="{y1 + 11}" text-anchor="middle" font-family="Guide Serif, serif" '
                   f'font-style="italic" font-weight="700" font-size="32" fill="{INK}">{letter}</text>')
    return "\n".join(out)


def legend():
    y_cap = 1566
    out = [f'<text x="{CX}" y="{y_cap}" text-anchor="middle" font-family="Guide Serif, serif" font-style="italic" '
           f'font-size="44" fill="{INK}">Apis mellifera L.</text>',
           f'<text x="{CX}" y="{y_cap + 38}" text-anchor="middle" font-family="Guide Sans, sans-serif" '
           f'font-size="22" letter-spacing="6" fill="{MUTED}">KOLONİYA · HÜCEYRƏ · HADİSƏ</text>',
           f'<line x1="{PLATE_X0 + 60}" y1="{y_cap + 66}" x2="{PLATE_X1 - 60}" y2="{y_cap + 66}" stroke="{INK}" '
           f'stroke-width="1.4" opacity="0.5"/>']
    col_w = (PLATE_X1 - PLATE_X0 - 120) / 3
    for i, (letter, organ, meaning) in enumerate(LEGEND):
        col, row = i % 3, i // 3
        x = PLATE_X0 + 60 + col * col_w
        y = y_cap + 110 + row * 60
        out.append(f'<circle cx="{x + 18}" cy="{y - 9}" r="17" fill="none" stroke="{INK}" stroke-width="2"/>')
        out.append(f'<text x="{x + 18}" y="{y - 1}" text-anchor="middle" font-family="Guide Serif, serif" '
                   f'font-style="italic" font-weight="700" font-size="22" fill="{INK}">{letter}</text>')
        out.append(f'<text x="{x + 48}" y="{y - 12}" font-family="Guide Sans, sans-serif" font-weight="700" '
                   f'font-size="21" letter-spacing="1" fill="{INK}">{esc(az_upper(organ))}</text>')
        out.append(f'<text x="{x + 48}" y="{y + 14}" font-family="Guide Serif, serif" font-size="21" '
                   f'fill="{MUTED}">{esc(meaning)}</text>')
    return "\n".join(out)


# --------------------------------------------------------------------------- Başlıq zolağı və paneli
def hex_icon(cx, cy, r, color):
    pts = " ".join(f"{cx + r * math.cos(math.pi / 3 * k + math.pi / 6):.1f},"
                   f"{cy + r * math.sin(math.pi / 3 * k + math.pi / 6):.1f}" for k in range(6))
    pts2 = " ".join(f"{cx + r * 0.52 * math.cos(math.pi / 3 * k + math.pi / 6):.1f},"
                    f"{cy + r * 0.52 * math.sin(math.pi / 3 * k + math.pi / 6):.1f}" for k in range(6))
    return (f'<polygon points="{pts}" fill="none" stroke="{color}" stroke-width="5"/>'
            f'<polygon points="{pts2}" fill="{color}"/>')


def header(meta):
    left, _, right = meta["coverBand"].partition(" · KAFKA")
    right = "KAFKA" + right if right else ""
    return "\n".join([
        f'<rect width="{W}" height="160" fill="{TEAL_DEEP}"/>',
        f'<rect y="160" width="{W}" height="8" fill="{GOLD}"/>',
        hex_icon(PLATE_X0 + 40, 80, 34, GOLD),
        f'<text x="{PLATE_X0 + 100}" y="94" font-family="Guide Sans, sans-serif" font-weight="700" font-size="36" '
        f'letter-spacing="8" fill="{GOLD}">{esc(left)}</text>',
        f'<text x="{PLATE_X1}" y="94" text-anchor="end" font-family="Guide Sans, sans-serif" font-weight="700" '
        f'font-size="36" letter-spacing="8" fill="{PAPER}" opacity="0.92">{esc(right)}</text>',
    ])


def chips(topics, x0, y0, max_w, font=27):
    """Mono şriftli "çiplər": eni simvol sayından dəqiq hesablanır (JetBrains Mono, 0,6 em)."""
    out, x, y = [], x0, y0
    h, pad, gap = 54, 22, 16
    for t in topics:
        w = len(t) * font * 0.6 + 2 * pad
        if x + w > x0 + max_w:
            x, y = x0, y + h + gap
        out.append(f'<rect x="{x:.1f}" y="{y}" width="{w:.1f}" height="{h}" rx="27" fill="{TEAL_DEEP}" '
                   f'fill-opacity="0.45" stroke="{GOLD_SOFT}" stroke-opacity="0.55" stroke-width="2"/>')
        out.append(f'<text x="{x + w / 2:.1f}" y="{y + 36}" text-anchor="middle" font-family="Guide Mono, '
                   f'monospace" font-size="{font}" fill="{PAPER}">{esc(t)}</text>')
        x += w + gap
    return "\n".join(out), y + h


def wrap(text, max_chars):
    words, lines, cur = text.split(), [], ""
    for w in words:
        if len(cur) + len(w) + 1 > max_chars and cur:
            lines.append(cur)
            cur = w
        else:
            cur = f"{cur} {w}".strip()
    if cur:
        lines.append(cur)
    return lines


def title_panel(meta, stats):
    n_ch, n_dia, n_ex, n_refs = stats
    x = 150
    out = [f'<rect y="{TITLE_Y0}" width="{W}" height="{H - TITLE_Y0}" fill="url(#panel)"/>',
           f'<rect y="{TITLE_Y0}" width="{W}" height="6" fill="{GOLD}"/>']
    # İncə pətək teksturası panelin sağ yuxarı küncündə
    for q in range(7):
        for r in range(4):
            cx = 1500 + q * 66
            cy = TITLE_Y0 + 70 + r * 76 + (38 if q % 2 else 0)
            pts = " ".join(f"{cx + 42 * math.cos(math.pi / 3 * k):.1f},{cy + 42 * math.sin(math.pi / 3 * k):.1f}"
                           for k in range(6))
            op = max(0.0, 0.16 - 0.025 * (r + abs(q - 5) * 0.6))
            if op > 0.01:
                out.append(f'<polygon points="{pts}" fill="none" stroke="{GOLD_SOFT}" stroke-width="2" '
                           f'opacity="{op:.2f}"/>')
    out.append(f'<text x="{x}" y="{TITLE_Y0 + 104}" font-family="Guide Sans, sans-serif" font-weight="700" '
               f'font-size="32" letter-spacing="9" fill="{GOLD}">{esc(az_upper(meta["kicker"]))}</text>')
    l1, l2 = meta["coverTitleLines"]
    out.append(f'<text x="{x - 8}" y="{TITLE_Y0 + 272}" font-family="Guide Serif, serif" font-weight="700" '
               f'font-size="172" letter-spacing="-2" fill="#ffffff">{esc(l1)}</text>')
    out.append(f'<text x="{x - 8}" y="{TITLE_Y0 + 448}" font-family="Guide Serif, serif" font-weight="700" '
               f'font-size="172" letter-spacing="-2" fill="{GOLD}">{esc(l2)}</text>')
    y = TITLE_Y0 + 510
    out.append(f'<rect x="{x}" y="{y}" width="140" height="7" fill="{GOLD}"/>')
    for i, line in enumerate(wrap(meta["subtitle"], 50)):
        out.append(f'<text x="{x}" y="{y + 72 + i * 64}" font-family="Guide Serif, serif" font-style="italic" '
                   f'font-size="50" fill="{PAPER}">{esc(line)}</text>')
    chips_svg, chips_bottom = chips(meta["coverTopics"], x, TITLE_Y0 + 690, W - 2 * x, font=25)
    out.append(chips_svg)
    y_div = chips_bottom + 40
    out.append(f'<line x1="{x}" y1="{y_div}" x2="{W - x}" y2="{y_div}" stroke="{PAPER}" stroke-width="1.6" '
               f'opacity="0.28"/>')
    stats_line = (f"{n_ch} fəsil  ·  {n_dia} diaqram  ·  {n_ex} praktiki tapşırıq  ·  "
                  f"{n_refs} yoxlanılmış kod istinadı")
    out.append(f'<text x="{x}" y="{y_div + 64}" font-family="Guide Sans, sans-serif" font-size="27" '
               f'letter-spacing="1" fill="{PAPER}" opacity="0.85">{esc(stats_line)}</text>')
    out.append(f'<text x="{x}" y="{y_div + 108}" font-family="Guide Sans, sans-serif" font-weight="700" '
               f'font-size="25" letter-spacing="6" fill="{GOLD_SOFT}" opacity="0.9">'
               f'{esc(az_upper(meta["edition"]))}</text>')
    out.append(f'<text x="{W - x}" y="{y_div + 46}" text-anchor="end" font-family="Guide Sans, sans-serif" '
               f'font-size="22" letter-spacing="8" fill="{PAPER}" opacity="0.7">MÜƏLLİF</text>')
    out.append(f'<text x="{W - x + 10}" y="{y_div + 150}" text-anchor="end" font-family="Guide Sans, sans-serif" '
               f'font-weight="700" font-size="118" letter-spacing="20" fill="{GOLD}">{esc(meta["author"])}</text>')
    assert y_div + 160 < H - 40, f"başlıq paneli səhifəyə sığmır: {y_div}"
    return "\n".join(out)


# --------------------------------------------------------------------------- Yığım
def defs():
    return f"""<defs>
<radialGradient id="plateGlow" cx="50%" cy="42%" r="75%">
  <stop offset="0%" stop-color="#fbf8f1"/><stop offset="70%" stop-color="{PAPER}"/>
  <stop offset="100%" stop-color="{PAPER_DEEP}"/></radialGradient>
<linearGradient id="panel" x1="0" y1="0" x2="1" y2="1">
  <stop offset="0%" stop-color="{TEAL}"/><stop offset="100%" stop-color="{TEAL_DEEP}"/></linearGradient>
<linearGradient id="honey" x1="0" y1="0" x2="0" y2="1">
  <stop offset="0%" stop-color="{GOLD_SOFT}" stop-opacity="0.65"/>
  <stop offset="100%" stop-color="{GOLD}" stop-opacity="0.55"/></linearGradient>
<linearGradient id="wingFill" x1="0" y1="0" x2="1" y2="0">
  <stop offset="0%" stop-color="#ffffff" stop-opacity="0.85"/>
  <stop offset="100%" stop-color="#eef3f2" stop-opacity="0.55"/></linearGradient>
<pattern id="hatchMembrane" width="10" height="10" patternUnits="userSpaceOnUse" patternTransform="rotate(28)">
  <line x1="0" y1="0" x2="0" y2="10" stroke="{INK}" stroke-width="0.9" opacity="0.35"/></pattern>
<pattern id="hatchCross" width="8" height="8" patternUnits="userSpaceOnUse" patternTransform="rotate(30)">
  <line x1="0" y1="0" x2="0" y2="8" stroke="{INK}" stroke-width="2.2"/>
  <line x1="0" y1="0" x2="8" y2="0" stroke="{INK}" stroke-width="1.4"/></pattern>
<pattern id="facets" width="13.8" height="12" patternUnits="userSpaceOnUse" patternTransform="rotate(14)">
  <polygon points="3.45,0 10.35,0 13.8,6 10.35,12 3.45,12 0,6" fill="{INK}" fill-opacity="0.18"
           stroke="{INK}" stroke-width="1.1"/></pattern>
</defs>"""


def build(meta):
    stats = book_stats()
    svg = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="210mm" height="297mm" '
           f'role="img" aria-label="{esc(meta["title"])} — {esc(meta["author"])}">',
           f'<title>{esc(meta["title"])} — {esc(meta["author"])}</title>',
           defs(),
           f'<rect width="{W}" height="{H}" fill="{PAPER}"/>',
           header(meta),
           crop_marks(),
           plate_background(),
           f'<clipPath id="plateClip"><rect x="{PLATE_X0}" y="{PLATE_Y0}" width="{PLATE_X1 - PLATE_X0}" '
           f'height="{PLATE_Y1 - PLATE_Y0}"/></clipPath>',
           '<g clip-path="url(#plateClip)">',
           honeycomb_cluster(PLATE_X0 - 40, PLATE_Y0 + 720, 6, 5, 78,
                             {(1, 1), (2, 2), (1, 3), (3, 3), (0, 2)}, (CX, 900)),
           honeycomb_cluster(PLATE_X1 - 330, PLATE_Y0 + 1000, 5, 3, 78, {(3, 0), (4, 1), (2, 1)}, (CX, 900)),
           '</g>',
           f'<rect x="{PLATE_X0 + 40}" y="{PLATE_Y1 - 290}" width="{PLATE_X1 - PLATE_X0 - 80}" height="250" '
           f'fill="{PAPER}" opacity="0.92"/>',
           bee(),
           callouts(),
           plate_texts(),
           legend(),
           plate_frame(),
           title_panel(meta, stats),
           '</svg>']
    return "\n".join(svg)


def main():
    with open(os.path.join(HERE, "book.json"), encoding="utf-8") as f:
        meta = json.load(f)["meta"]
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        f.write(build(meta))
    print(f"{OUT}: yaradıldı")


if __name__ == "__main__":
    main()
