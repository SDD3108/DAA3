from pathlib import Path
import csv
import json
import math

import numpy as np
import matplotlib

matplotlib.use("Agg")

import matplotlib.pyplot as plt


ROOT = Path(__file__).resolve().parents[1]
RESULTS = ROOT / "results"
PLOTS = RESULTS / "plots"

ALGORITHMS = [
    "Kruskal",
    "Prim",
    "Kosaraju",
    "Kahn",
    "DagShortestPath",
]

COLORS = [
    "#2463a6",
    "#c1512b",
    "#207b57",
    "#7e4aa8",
    "#b4840c",
]

MARKERS = ["o", "s", "^", "D", "x"]
SIZES = [1000, 3162, 10000, 31623, 100000]

with (RESULTS / "results.csv").open(
    encoding="utf-8-sig", newline=""
) as stream:
    rows = list(csv.DictReader(stream))

if len(rows) != 50:
    raise ValueError(
        f"Expected 50 benchmark rows, found {len(rows)}. "
        "Run the full benchmark before plotting."
    )

integer_fields = [
    "n",
    "m",
    "dsu_ops",
    "edge_scans",
    "comparisons",
    "max_depth",
    "result",
]

for row in rows:
    for field in integer_fields:
        row[field] = int(row[field])

    row["time_ms"] = float(row["time_ms"])
    row["operations"] = (
        row["dsu_ops"] + row["edge_scans"] + row["comparisons"]
    )


def select(algorithm, density):
    return sorted(
        [
            row
            for row in rows
            if row["algorithm"] == algorithm
            and row["graph"].endswith("_" + density)
        ],
        key=lambda row: row["n"],
    )


for algorithm in ALGORITHMS:
    for density, factor in [("sparse", 4), ("dense", 20)]:
        selected = select(algorithm, density)

        if [row["n"] for row in selected] != SIZES:
            raise ValueError(
                f"Missing or duplicate sizes: {algorithm}, {density}"
            )

        for row in selected:
            if row["m"] != factor * row["n"]:
                raise ValueError(
                    f"Incorrect edge count: {algorithm}, {density}"
                )

PLOTS.mkdir(parents=True, exist_ok=True)

plt.rcParams.update({
    "font.size": 10,
    "axes.spines.top": False,
    "axes.spines.right": False,
})

charts = [
    ("operations", "operations.png", "Counted operations"),
    ("edge_scans", "scans_per_edge.png", "Edge scans / m"),
]

for field, filename, ylabel in charts:
    fig, axes = plt.subplots(1, 2, figsize=(10, 4.25))

    for ax, density in zip(axes, ["sparse", "dense"]):
        for algorithm, color, marker in zip(
            ALGORITHMS, COLORS, MARKERS
        ):
            selected = select(algorithm, density)

            values = [
                row[field]
                if field == "operations"
                else row[field] / row["m"]
                for row in selected
            ]

            ax.plot(
                [row["n"] for row in selected],
                values,
                color=color,
                marker=marker,
                linestyle="--" if algorithm == "DagShortestPath" else "-",
                label=algorithm,
                markersize=5,
            )

        ax.set_xscale("log")

        if field == "operations":
            ax.set_yscale("log")
        else:
            ax.set_ylim(0, 2.2)

        ax.set_title(
            "Sparse m = 4n" if density == "sparse" else "Dense m = 20n"
        )
        ax.set_xlabel("Vertices n")
        ax.set_ylabel(ylabel)
        ax.grid(alpha=0.2)

    handles, labels = axes[0].get_legend_handles_labels()

    fig.legend(
        handles,
        labels,
        loc="lower center",
        ncol=5,
        fontsize=9,
    )

    fig.tight_layout(rect=(0, 0.12, 1, 1))
    fig.savefig(PLOTS / filename, dpi=200)
    plt.close(fig)

fig, ax = plt.subplots(figsize=(9, 3.7))

for density, color, marker in [
    ("sparse", COLORS[0], "o"),
    ("dense", COLORS[1], "s"),
]:
    selected = select("Kruskal", density)

    ax.plot(
        [row["n"] for row in selected],
        [row["edge_scans"] / row["m"] for row in selected],
        color=color,
        marker=marker,
        label=density,
    )

ax.set_xscale("log")
ax.set_ylim(0, 1.05)
ax.set_xlabel("Vertices n")
ax.set_ylabel("Kruskal edge scans / m")
ax.grid(alpha=0.2)
ax.legend()

fig.tight_layout()
fig.savefig(PLOTS / "kruskal_share.png", dpi=200)
plt.close(fig)

summary = {}

for algorithm in ALGORITHMS:
    summary[algorithm] = {}

    for density in ["sparse", "dense"]:
        selected = select(algorithm, density)
        log_n = np.log([row["n"] for row in selected])

        def slope(field):
            values = np.log([row[field] for row in selected])
            return float(np.polyfit(log_n, values, 1)[0])

        ratios = []

        for row in selected:
            if algorithm in ["Kruskal", "Prim"]:
                ratio = row["comparisons"] / (
                    row["m"] * math.log2(row["m"])
                )
            else:
                ratio = row["edge_scans"] / (row["n"] + row["m"])

            ratios.append(ratio)

        summary[algorithm][density] = {
            "scan_slope": slope("edge_scans"),
            "operation_slope": slope("operations"),
            "ratio_min": min(ratios),
            "ratio_max": max(ratios),
            "dsu_per_edge": [
                row["dsu_ops"] / row["m"] for row in selected
            ],
            "scans_per_edge": [
                row["edge_scans"] / row["m"] for row in selected
            ],
            "times_ms": [
                row["time_ms"] for row in selected
            ],
        }

(RESULTS / "analysis.json").write_text(
    json.dumps(summary, indent=2) + "\n",
    encoding="utf-8",
)

print("Validated 50 benchmark rows.")
print("Created operations.png, scans_per_edge.png and kruskal_share.png.")
print("Saved results/analysis.json.")