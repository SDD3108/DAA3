# DAA3

Design and Analysis of Algorithms, Assignment 3: City Network and Job Scheduler.

Student: Satimov Damir, SE-2525. Instructor: Taubakabyl Nurlybek.

## Requirements and build

Install JDK 17 or newer and Maven 3.9 or newer. Run commands from the project root.

```bash
mvn clean verify
```

The project has 24 JUnit 5 tests. They include 100 random MST comparisons, exhaustive small MSTs, DSU reference comparisons, SCC reachability checks, Bellman-Ford comparisons, and iterative DFS tests on 100,000 vertices.

## Run

```bash
java -Xmx2g -jar target/daa3-1.0.0.jar
java -Xmx2g -cp target/daa3-1.0.0.jar benchmark.BonusBenchmark
java -cp target/daa3-1.0.0.jar benchmark.Demo data/dag.txt
java -cp target/daa3-1.0.0.jar benchmark.Demo data/cyclic.txt
java -cp target/daa3-1.0.0.jar benchmark.Demo data/disconnected.txt
```

The full benchmark produces exactly 50 rows in `results/results.csv`: five measured graph algorithms, five sizes, two densities. DSU is measured inside Kruskal. Each row uses the median of five runs. All ten reference triples are checked; mismatches throw an exception. The bonus benchmark checks Tarjan against Kosaraju and saves ten rows to `results/bonus.csv`. The demo prints actual paths, components, cycle detection or a minimum spanning forest.

For a smaller reference check:

```bash
java -jar target/daa3-1.0.0.jar --quick
```

This writes `target/quick-results.csv`, preserving the full results.

## Reproduce the plots and report

Python 3.10 or newer is needed only for plotting and document generation.

```bash
python -m pip install -r scripts/requirements.txt
python scripts/plot_results.py
python scripts/build_report.py
```

After the Java commands above, these commands reproduce all report numbers, the three PNG plots, `results/analysis.json`, `REPORT.md`, and `report.docx`. Times naturally change between runs.

## Structure

- `src/main/java/graph`: shared graph representation and text-file parser.
- `src/main/java/algorithms`: DSU, Kruskal, Prim, Kosaraju, Kahn, DAG paths, Tarjan and condensation.
- `src/main/java/metrics`: counters incremented inside the algorithms.
- `src/main/java/benchmark`: deterministic generators, self-checking benchmarks and demo.
- `src/test/java`: JUnit 5 tests.
- `data`: DAG, cyclic and disconnected input fixtures, all used in tests.
- `results`: CSV results, benchmark log, normalized counters and three plots.
- `REPORT.md` and `report.docx`: analysis and correctness arguments.

Input begins with `n m directed|undirected`, followed by exactly `m` lines `u v w`. Vertices are numbered from zero; weights are signed integers. Results and distances use `long`. Loops, parallel edges and isolated vertices are supported. MST algorithms reject directed input, while SCC and scheduling algorithms require directed input.

## Counter conventions

- `dsu_ops`: public find calls plus successful unions; each attempted union makes two find calls.
- `edge_scans`: examined candidate edges in Kruskal, adjacency entries in Prim, DFS traversal edges in Kosaraju, outgoing edges in Kahn, and relaxation edges in DAG paths.
- `comparisons`: comparisons of weights during sorting or priority-queue operations.
- `max_depth`: maximum occupied explicit DFS stack; zero for algorithms without such a stack.

An undirected edge has two adjacency entries, so Prim scans `2m`. Kosaraju counts the two DFS passes (`2m`); transpose construction is excluded from that counter but included in time and complexity. In-degrees are maintained by Graph. Kahn scans `m`. DAG paths include a Kahn pass in runtime but report only the relaxation counter (`m`), as required by the brief; the internal Kahn pass has its own Metrics object. Operation plots sum the three work counters and do not include max depth or uninstrumented initialization. Counters are deterministic for the same implementation and input; comparisons performed by Java collection internals can change with their implementation.

The generator uses a fresh `Random(42)` for each graph. When endpoints coincide, it redraws only the second endpoint. This detail is required to reproduce the provided reference values. The benchmark follows the explicit ten-dataset/50-row requirement; the deliverables' phrase "four generated graphs" conflicts with that requirement.

## Bonus semantics

Tarjan uses explicit DFS frames and an active stack. Condensation removes intra-component edges and retains the largest weight for parallel component-to-component edges. The critical path maximizes the sum of these transition weights from any source, with a zero-edge path allowed. SCC-internal costs are not represented; this is not a solution to the original graph's longest simple path problem. Scheduling durations should be nonnegative.

## Git history

Implementation, tests, measurements and documentation are organized into sequential commits on `main`.

GitHub repository: https://github.com/SDD3108/DAA3

## AI use acknowledgement

ChatGPT assisted with implementation, test design, debugging the generator, benchmark execution, plots and report preparation,and .py scripts writing for reports/results