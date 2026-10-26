"""Üz qabığının illüstrasiyası: elmi qravüra üslubunda ahtapot (Octopus vulgaris).

Niyə ahtapot: neyronlarının təxminən üçdə ikisi qollarındadır — hər qol yerli qərar verir, mərkəzi beyin isə
istiqamət verir. Bu, gateway + müstəqil servislər modelinin canlı analoqudur. Üç ürək dayanıqlığı, rəng
hüceyrələri (xromatoforlar) siqnalları, vantuzlar isə kontraktları xatırladır.

Qravüra texnikaları (hamısı vektor, filtrsiz):
  * qollar — spline mərkəz xətti, uzunluq boyu incələn en, ucda spiral; köndələn kontur ştrixləri kölgə tərəfdə,
    daxili tərəfdə ölçüsü kiçilən vantuz cərgəsi;
  * mantiya — forma boyu kontur xətləri, nöqtəli kölgə və xromatofor ləkələri;
  * gözlər — üfüqi bəbək (ahtapotun xarakterik əlaməti), göz qapağı və parıltı.
"""
import math
import random

INK = "#1c1b19"
PAPER = "#f5f0e6"
GOLD = "#dca43a"
GOLD_SOFT = "#efc773"
MUTED = "#76695a"
NET = "#bfae8c"

CX = 1050
rng = random.Random(7)

# Gövdə həndəsəsi
MANTLE_CY = 560
HEAD_CY = 862
BASE_CY = 930


def _p(d, w=3.0, color=INK, opacity=1.0, extra=""):
    return (f'<path d="{d}" stroke="{color}" stroke-width="{w}" fill="none" stroke-linecap="round" '
            f'stroke-linejoin="round" opacity="{opacity}" {extra}/>')


# --------------------------------------------------------------------------- Qollar
class Arm:
    """Bir qolun həndəsəsi: mərkəz xətti, en, normallar."""

    def __init__(self, base, heading_deg, length, turn_deg, width, wiggle=0.0, phase=0.0, n=260):
        self.n = n
        self.length = length
        self.turn = math.radians(turn_deg)
        x, y = base
        phi = math.radians(heading_deg)
        ds = length / n
        self.pts, self.phis, self.ws = [], [], []
        for i in range(n + 1):
            s = i / n
            self.pts.append((x, y))
            self.phis.append(phi)
            self.ws.append(width * (1 - s) ** 1.05 + 4.0)
            # Əyrilik: ümumi dönmənin 20%-i bərabər, 80%-i uca yaxın (s^4) paylanır; üstəgəl yumşaq dalğalanma.
            density = 0.2 + 0.8 * 5 * s ** 4
            kappa = self.turn * density / length + wiggle * math.sin(2 * math.pi * 1.3 * s + phase) * (1 - s) / length
            phi += kappa * ds
            x += math.cos(phi) * ds
            y += math.sin(phi) * ds

    def side(self, i, sign, frac):
        """Mərkəz xəttindən `sign` tərəfə enin `frac` hissəsi qədər sürüşmüş nöqtə."""
        x, y = self.pts[i]
        phi = self.phis[i]
        nx, ny = -math.sin(phi), math.cos(phi)
        off = self.ws[i] / 2 * frac * sign
        return x + nx * off, y + ny * off

    def outline(self):
        left = [self.side(i, 1, 1) for i in range(self.n + 1)]
        right = [self.side(i, -1, 1) for i in range(self.n + 1)]
        tip = self.pts[-1]
        d = "M" + " L".join(f"{x:.1f},{y:.1f}" for x, y in left)
        d += f" Q{tip[0]:.1f},{tip[1]:.1f} "
        d += " L".join(f"{x:.1f},{y:.1f}" for x, y in reversed(right)) + " Z"
        return d


def draw_arm(arm, idx):
    """Qolu qravüra kimi çəkir: dolğu, kölgə ştrixləri, kontur və vantuzlar."""
    out = []
    outline = arm.outline()
    clip_id = f"armClip{idx}"
    out.append(f'<clipPath id="{clip_id}"><path d="{outline}"/></clipPath>')
    out.append(f'<path d="{outline}" fill="{PAPER}"/>')
    out.append(f'<path d="{outline}" fill="url(#fleshWash)"/>')
    # Kölgə tərəfi: işıq yuxarı-soldan gəlir; normalı aşağı-sağa baxan tərəf kölgədədir.
    hatch = []
    step = 3
    for i in range(4, arm.n - 3, step):
        phi = arm.phis[i]
        nx, ny = -math.sin(phi), math.cos(phi)
        shade_sign = 1 if (nx + ny) > 0 else -1
        a = arm.side(i, shade_sign, 0.05)
        b = arm.side(i, shade_sign, 0.98)
        hatch.append(f"M{a[0]:.1f},{a[1]:.1f} L{b[0]:.1f},{b[1]:.1f}")
    out.append(f'<path d="{" ".join(hatch)}" stroke="{INK}" stroke-width="1.5" opacity="0.75" fill="none" '
               f'clip-path="url(#{clip_id})"/>')
    # Uzununa forma xətti kölgə tərəfində
    long_line = []
    for i in range(2, arm.n - 10):
        phi = arm.phis[i]
        shade_sign = 1 if (-math.sin(phi) + math.cos(phi)) > 0 else -1
        long_line.append(arm.side(i, shade_sign, 0.55))
    out.append(_p("M" + " L".join(f"{x:.1f},{y:.1f}" for x, y in long_line), 1.6, INK, 0.6))
    out.append(_p(outline, 4.2))
    # Vantuzlar: spiralın daxili tərəfində, uca doğru kiçilir
    sucker_side = 1 if arm.turn > 0 else -1
    i = 10
    while i < arm.n - 6:
        w = arm.ws[i]
        r = 0.21 * w
        if r < 2.6:
            break
        cx, cy = arm.side(i, sucker_side, 0.9)
        out.append(f'<circle cx="{cx:.1f}" cy="{cy:.1f}" r="{r:.1f}" fill="{PAPER}" stroke="{INK}" '
                   f'stroke-width="{max(1.2, r * 0.16):.1f}"/>')
        out.append(f'<circle cx="{cx:.1f}" cy="{cy:.1f}" r="{r * 0.42:.1f}" fill="none" stroke="{INK}" '
                   f'stroke-width="{max(0.9, r * 0.11):.1f}" opacity="0.8"/>')
        spacing = (2.25 * r + 2.5) / (arm.length / arm.n)
        i += max(2, int(spacing))
    return "\n".join(out)


ARM_SPECS = [  # (başlanğıc istiqaməti°, uzunluq, son dönmə°, en, S-dalğa amplitudu, faza) — sağ tərəf
    (-38, 1000, 250, 78, 6.5, 0.0),
    (2, 1080, 330, 76, -6.0, 1.2),
    (32, 960, -290, 72, 7.0, 2.4),
    (70, 800, 360, 66, -7.0, 0.5),
]


def build_arms():
    arms = []
    for k, (h, length, turn, w, wig, ph) in enumerate(ARM_SPECS):
        for side in (1, -1):
            jitter = rng.uniform(-5, 5)
            heading = h + jitter if side == 1 else 180 - h + jitter
            ang = math.radians(h)
            # Qolun kökü başın altında gizlənir (baş qollardan sonra çəkilir)
            base_x = CX + side * (35 + 55 * math.cos(ang))
            base_y = HEAD_CY + 10 + 26 * math.sin(ang)
            arms.append((k, Arm((base_x, base_y), heading, length * rng.uniform(0.94, 1.05),
                                turn * side * rng.uniform(0.88, 1.1), w, wig * side * rng.uniform(0.85, 1.15),
                                ph + (0.5 if side < 0 else 0))))
    arms.sort(key=lambda t: -t[0])  # mərkəzi qollar arxada, yan qollar önə düşür
    return [a for _, a in arms]


def web(arms):
    """Qolların dibini birləşdirən zar (interbrachial web)."""
    pts = []
    order = sorted(arms, key=lambda a: math.atan2(a.pts[int(a.n * 0.16)][1] - BASE_CY,
                                                   a.pts[int(a.n * 0.16)][0] - CX))
    for a in order:
        pts.append(a.pts[int(a.n * 0.16)])
    d = f"M{CX},{BASE_CY - 40}"
    for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
        mx, my = (x0 + x1) / 2, (y0 + y1) / 2
        qx, qy = CX + (mx - CX) * 0.82, BASE_CY + (my - BASE_CY) * 0.82
        d += f" L{x0:.1f},{y0:.1f} Q{qx:.1f},{qy:.1f} {x1:.1f},{y1:.1f}"
    d += " Z"
    return (f'<path d="{d}" fill="{PAPER}"/><path d="{d}" fill="url(#hatchMembrane)" opacity="0.7"/>'
            + _p(d, 2.2, INK, 0.7))


# --------------------------------------------------------------------------- Baş və mantiya
MANTLE = ("M1036,300 C1186,292 1280,430 1278,572 C1276,706 1202,794 1122,822 C1096,830 1004,830 978,822 "
          "C898,794 822,708 820,574 C818,430 896,306 1036,300 Z")


def mantle():
    out = ['<g transform="translate(1050 826) rotate(-4) scale(0.92) translate(-1050 -826)">',
           f'<clipPath id="mantleClip"><path d="{MANTLE}"/></clipPath>',
           f'<path d="{MANTLE}" fill="{PAPER}"/>',
           f'<path d="{MANTLE}" fill="url(#mantleWash)"/>']
    # Köndələn kontur ştrixləri: formanı izləyən qövslər, kölgə tərəfində (sağda) daha uzun və sıx
    arcs = []
    for k in range(0, 34):
        y = 340 + k * 14.5
        frac = (y - 322) / 508
        half = 232 * math.sin(math.pi * min(0.98, max(0.04, frac)))  # mantiyanın təxmini yarım eni
        x_start = 1050 + half * (0.15 - 0.5 * (k % 2))
        arcs.append(f"M{x_start:.1f},{y:.1f} Q{1050 + half * 0.7:.1f},{y + 26:.1f} {1050 + half * 1.05:.1f},{y - 6:.1f}")
    out.append(f'<path d="{" ".join(arcs)}" stroke="{INK}" stroke-width="1.6" fill="none" opacity="0.55" '
               f'clip-path="url(#mantleClip)"/>')
    # Dəri qırışları mantiyanın aşağısında
    folds = []
    for k, y in enumerate((742, 768, 792)):
        folds.append(f"M{900 + k * 18},{y} C{990},{y + 22} {1110},{y + 22} {1200 - k * 18},{y}")
    out.append(f'<path d="{" ".join(folds)}" stroke="{INK}" stroke-width="2.4" fill="none" opacity="0.6" '
               f'clip-path="url(#mantleClip)"/>')
    # Nöqtəli kölgə, sağ-aşağı tərəfdə sıx
    dots = []
    for _ in range(2600):
        x = rng.uniform(818, 1282)
        y = rng.uniform(322, 830)
        shade = (x - 980) / 300 + (y - 500) / 420
        if rng.random() < max(0.03, min(0.9, shade * 0.6)):
            dots.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{rng.uniform(1.1, 2.4):.1f}"/>')
    out.append(f'<g fill="{INK}" opacity="0.75" clip-path="url(#mantleClip)">{"".join(dots)}</g>')
    # Xromatoforlar
    spots = []
    for _ in range(95):
        x = rng.uniform(840, 1260)
        y = rng.uniform(360, 800)
        r = rng.uniform(4, 11)
        spots.append(f'<ellipse cx="{x:.1f}" cy="{y:.1f}" rx="{r:.1f}" ry="{r * rng.uniform(0.6, 1):.1f}" '
                     f'transform="rotate({rng.uniform(0, 180):.0f} {x:.1f} {y:.1f})" '
                     f'fill="{INK if rng.random() < 0.7 else "#8a5a2b"}" opacity="{rng.uniform(0.35, 0.8):.2f}"/>')
    out.append(f'<g clip-path="url(#mantleClip)">{"".join(spots)}</g>')
    out.append(f'<ellipse cx="985" cy="470" rx="70" ry="110" transform="rotate(-18 985 470)" fill="#ffffff" '
               f'opacity="0.35" clip-path="url(#mantleClip)"/>')
    out.append(_p(MANTLE, 6.5))
    out.append("</g>")
    return "\n".join(out)


def head():
    d = "M876,846 C900,800 980,788 1050,788 C1120,788 1200,800 1224,846 C1236,890 1170,934 1050,940 C930,934 864,890 876,846 Z"
    out = [f'<clipPath id="headClip"><path d="{d}"/></clipPath>',
           f'<path d="{d}" fill="{PAPER}"/>', f'<path d="{d}" fill="url(#fleshWash)"/>']
    lines = []
    for k in range(10):
        y = 800 + k * 14
        lines.append(f"M860,{y} Q1050,{y + 38} 1240,{y}")
    out.append(f'<path d="{" ".join(lines)}" stroke="{INK}" stroke-width="1.4" fill="none" opacity="0.5" '
               f'clip-path="url(#headClip)"/>')
    out.append(_p(d, 5))
    return "\n".join(out)


def eye(cx, cy, rot):
    """Badam formalı göz: qabarıq göz yuvası, qızılı iris, üfüqi bəbək, dəri qırışları."""
    socket = (f"M{cx - 62},{cy + 4} C{cx - 48},{cy - 44} {cx + 48},{cy - 44} {cx + 62},{cy + 4} "
              f"C{cx + 46},{cy + 40} {cx - 46},{cy + 40} {cx - 62},{cy + 4} Z")
    lid = f"M{cx - 50},{cy - 2} C{cx - 30},{cy - 30} {cx + 30},{cy - 30} {cx + 50},{cy - 2}"
    return (f'<g transform="rotate({rot} {cx} {cy})">'
            f'<path d="{socket}" fill="{PAPER}" stroke="{INK}" stroke-width="4"/>'
            f'<path d="{socket}" fill="url(#fleshWash)"/>'
            f'<ellipse cx="{cx}" cy="{cy + 6}" rx="34" ry="24" fill="url(#iris)" stroke="{INK}" stroke-width="2.6"/>'
            f'<ellipse cx="{cx}" cy="{cy + 6}" rx="34" ry="24" fill="url(#hatchMembrane)" opacity="0.6"/>'
            f'<rect x="{cx - 22}" y="{cy + 1}" width="44" height="10" rx="5" fill="{INK}"/>'
            f'<circle cx="{cx - 13}" cy="{cy - 4}" r="4.5" fill="#ffffff" opacity="0.95"/>'
            + _p(lid, 3.4)
            + _p(f"M{cx - 64},{cy - 18} C{cx - 36},{cy - 58} {cx + 36},{cy - 58} {cx + 64},{cy - 18}", 1.8, INK, 0.7)
            + _p(f"M{cx - 54},{cy + 30} C{cx - 24},{cy + 50} {cx + 24},{cy + 50} {cx + 54},{cy + 30}", 1.6, INK, 0.6)
            + "</g>")


def octopus():
    arms = build_arms()
    parts = [web(arms)]
    for i, a in enumerate(arms):
        parts.append(draw_arm(a, i))
    parts.append(head())
    parts.append(mantle())
    parts.append(eye(CX - 152, 846, -5))
    parts.append(eye(CX + 152, 846, 5))
    return "\n".join(parts), arms


# --------------------------------------------------------------------------- Fon şəbəkəsi və vstavka
def network(x0, y0, x1, y1, avoid_center, avoid_r):
    """Servis şəbəkəsini xatırladan zəif qovşaqlar və əlaqələr (mərkəzdən uzaqlaşdıqca görünür)."""
    nodes = []
    while len(nodes) < 70:
        x, y = rng.uniform(x0 + 30, x1 - 30), rng.uniform(y0 + 30, y1 - 30)
        if math.hypot(x - avoid_center[0], (y - avoid_center[1]) * 1.3) > avoid_r:
            nodes.append((x, y))
    out = []
    for i, (x, y) in enumerate(nodes):
        near = sorted(nodes, key=lambda n: math.hypot(n[0] - x, n[1] - y))[1:3]
        for nx, ny in near:
            out.append(f'<line x1="{x:.1f}" y1="{y:.1f}" x2="{nx:.1f}" y2="{ny:.1f}" stroke="{NET}" '
                       f'stroke-width="1.6" opacity="0.55"/>')
    for x, y in nodes:
        r = rng.choice((4, 5, 6, 8))
        fill = GOLD if rng.random() < 0.18 else PAPER
        out.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{r}" fill="{fill}" stroke="{NET}" stroke-width="2"/>')
    return "\n".join(out)


def hearts_inset(cx, cy, r):
    """Şəkil 2: qəlsəmələr, iki qəlsəmə ürəyi və sistem ürəyi (anatomik sxem)."""
    out = [f'<circle cx="{cx}" cy="{cy}" r="{r + 10}" fill="none" stroke="{INK}" stroke-width="1.6"/>',
           f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="#fbf8f1" stroke="{INK}" stroke-width="4"/>',
           f'<g transform="translate({cx} {cy - 14}) scale(0.98) translate({-cx} {-cy})">']
    for side in (-1, 1):
        # Lələkvari qəlsəmə: mərkəz xətti və yan lövhəciklər
        x0, y0 = cx + side * 44, cy + 6
        x1, y1 = cx + side * 112, cy - 70
        out.append(_p(f"M{x0},{y0} Q{cx + side * 96},{cy - 6} {x1},{y1}", 3))
        lam = []
        for k in range(1, 16):
            t = k / 16
            px = (1 - t) ** 2 * x0 + 2 * (1 - t) * t * (cx + side * 96) + t * t * x1
            py = (1 - t) ** 2 * y0 + 2 * (1 - t) * t * (cy - 6) + t * t * y1
            ln = 26 * (1 - 0.6 * t)
            lam.append(f"M{px:.1f},{py:.1f} l{side * ln * 0.55:.1f},{ln * 0.45:.1f}")
            lam.append(f"M{px:.1f},{py:.1f} l{-side * ln * 0.35:.1f},{-ln * 0.65:.1f}")
        out.append(_p(" ".join(lam), 1.6, INK, 0.8))
        # Qəlsəmə ürəyi
        hx, hy = cx + side * 50, cy + 22
        out.append(f'<circle cx="{hx}" cy="{hy}" r="17" fill="{GOLD_SOFT}" fill-opacity="0.7" stroke="{INK}" '
                   f'stroke-width="3"/><circle cx="{hx}" cy="{hy}" r="17" fill="url(#hatchCross)" opacity="0.3"/>')
        # Damar: qəlsəmə → sistem ürəyi
        out.append(_p(f"M{cx + side * 82},{cy - 30} C{cx + side * 60},{cy + 40} {cx + side * 30},{cy + 52} "
                      f"{cx + side * 22},{cy + 50}", 2.6, INK, 0.75))
    # Sistem ürəyi və aorta
    out.append(f'<ellipse cx="{cx}" cy="{cy + 52}" rx="30" ry="22" fill="{GOLD}" fill-opacity="0.75" stroke="{INK}" '
               f'stroke-width="3.4"/><ellipse cx="{cx}" cy="{cy + 52}" rx="30" ry="22" fill="url(#hatchCross)" '
               f'opacity="0.3"/>')
    out.append(_p(f"M{cx},{cy + 30} C{cx - 6},{cy - 10} {cx + 6},{cy - 50} {cx},{cy - 96}", 3.2, INK, 0.85))
    out.append(_p(f"M{cx},{cy + 74} L{cx},{cy + 96}", 3, INK, 0.8))
    out.append("</g>")
    out.append(f'<rect x="{cx - 220}" y="{cy + r + 18}" width="440" height="42" rx="6" fill="{PAPER}" opacity="0.92"/>')
    out.append(f'<text x="{cx}" y="{cy + r + 48}" text-anchor="middle" font-family="Guide Serif, serif" '
               f'font-style="italic" font-size="26" fill="{MUTED}">Şəkil 2 · qəlsəmələr və üç ürək</text>')
    return "\n".join(out)


DEFS = f"""
<radialGradient id="fleshWash" cx="35%" cy="30%" r="80%">
  <stop offset="0%" stop-color="{GOLD_SOFT}" stop-opacity="0.10"/>
  <stop offset="100%" stop-color="#c98a4b" stop-opacity="0.28"/></radialGradient>
<radialGradient id="mantleWash" cx="38%" cy="30%" r="75%">
  <stop offset="0%" stop-color="{GOLD_SOFT}" stop-opacity="0.12"/>
  <stop offset="100%" stop-color="#b9713e" stop-opacity="0.32"/></radialGradient>
<radialGradient id="iris" cx="40%" cy="35%" r="70%">
  <stop offset="0%" stop-color="{GOLD_SOFT}"/><stop offset="100%" stop-color="{GOLD}"/></radialGradient>
"""
