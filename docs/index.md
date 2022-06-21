# SlotMachine: Heuristic Optimizer
The Heuristic Optimizer of the SlotMachine system.

It accepts a JSON file for future optimization sessions, optimizes the flight departure order and returns it. This tool can be accessed via Swagger interface and there are various REST methods available. Optimizations can be done with three different frameworks: exact algorithms (Hungarian Algorithm), metaheuristics and construction heuristics (OptaPlanner) and genetic algorithms (Jenetics). Jenetics and OptaPlanner can be configured via given JSON file.
