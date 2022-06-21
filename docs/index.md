# SlotMachine: Heuristic Optimizer
The Heuristic Optimizer of the SlotMachine system.

It accepts a JSON file for future optimization sessions, optimizes the flight departure order and returns it. This tool can be accessed via Swagger interface and there are various REST methods available. Optimizations can be done with three different frameworks: exact algorithms (Hungarian Algorithm), metaheuristics and construction heuristics (OptaPlanner) and genetic algorithms (Jenetics). Jenetics and OptaPlanner can be configured via given JSON file.

## Usage

Use `mvn clean install` to install Heuristic Optimizer from the source.

The Swagger interface of the REST server can be accessed via `http://localhost:8080/swagger-ui.html`.

## Datasets
We also provide the datasets from our experiments as downloads:
- [Run 1](https://final.at/coopis2022/run1.zip)
- [Run 2](https://final.at/coopis2022/run2.zip)
- [Run 3](https://final.at/coopis2022/run3.zip)
- [Run 4](https://final.at/coopis2022/run4.zip)

## API documentation

To explore the available REST methods, visit the [API documentation](https://jku-win-dke.github.io/SlotMachine-Optimizer/apidoc.html).


