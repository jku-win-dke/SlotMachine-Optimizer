# SlotMachine: Heuristic Optimizer
The Heuristic Optimizer of the SlotMachine system.

It accepts a JSON file for future optimization sessions, optimizes the flight departure order and returns it. This tool can be accessed via Swagger interface and there are various REST methods available. Optimizations can be done with three different frameworks: exact algorithms (Hungarian Algorithm), metaheuristics and construction heuristics (OptaPlanner) and genetic algorithms (Jenetics). Jenetics and OptaPlanner can be configured via given JSON file.

## Usage

Use `mvn clean install` to install Heuristic Optimizer from the source.

The Swagger interface of the REST server can be accessed via `http://localhost:8080/swagger-ui.html`.

Available REST methods include:

- `/optimizations` initializes optimization sessions and requires a JSON file. This file can include the given flights, slots, initial flight sequence, optimization identification, optimization framework, margins and additional parameters for Jenetics or OptaPlanner.
- `/optimizations/{optId}/start` or `/optimizations/{optId}/start/wait` start the optimization with the given optimization identification using the configured data from `/optimizations`. `/optimizations/{optId}/start` finishes the method call after starting and the optimization session is running in the background. `/optimizations/{optId}/start/wait` completes the method call after the optimization session is finished.
- `/optimizations/{optId}/result` returns the results of the optimization session (if results are available) using the optimization identification.
- `/optimizations/{optId}/remove` deletes all data from the optimization session with the given optimization identification.

For a full overview see [the resources](/docs/resources.md) or visit the [website](https://jku-win-dke.github.io/SlotMachine-Optimizer/).
