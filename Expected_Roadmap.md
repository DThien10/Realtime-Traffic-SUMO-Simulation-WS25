# 🚦 Traffic Simulation Project - Roadmap

---

## **Phase 1 — Foundations & Architecture**

**Timeline:** 27.11.2025
**Milestone:** M1 — System Definition & Connectivity

### ✅ System Planning

* Define architecture (modules, class responsibilities)
* Create UML class diagram for wrappers (`Vehicle`, `Edge`, `TrafficLight`, `SimulationManager`)
* Determine GUI layout (map panel, control panel, dashboard)
* Define technology stack
* Plan team roles and responsibilities

### ✅ SUMO Connectivity Prototype

* Connect to SUMO using **Libtraci**
* Retrieve list of traffic lights and edges
* Implement first `stepSimulation` call
* Add basic error handling (SUMO not reachable, invalid connection, etc.)

### ✅ Initial Repository Setup

* GitHub repository initialized (README, initial commit)
* Main package structure created

---

## **Phase 2 — Core Functionality Implementation**

**Timeline:** 28.11 → 14.12.2025
**Milestone:** M2 — Functional Prototype

### ✅ Live Simulation Control

* Stable Libtraci wrapper classes
* Start/stop simulation
* Real-time stepping using a separate thread

### ✅ Vehicle Management

* Inject vehicles via GUI
* Assign route, color, and speed
* Batch injection for stress testing (v1)

### ✅ Traffic Light Management

* Display real-time traffic light phase
* Manual switching through GUI

### ✅ Map Visualization (2D)

* Show network edges (simple lines)
* Render vehicles as colored dots/icons
* Basic painting + zooming

### ✅ Documentation & User Guide Draft

* First GUI description
* Architecture technical notes
* Updated UML diagrams

---

## **Phase 3 — Full Feature Development**

**Timeline:** 15.12 → 10.01.2026

### ⭐ Advanced Visualization & Filtering

* Filter vehicles by speed range, type, color, or edge
* Highlight congested areas
* Improved map rendering (lane outlines, scaled nodes)

### ⭐ Analytics System

* Average speed computation
* Edge density tracking
* Congestion heatmap
* Travel time statistics
* Real-time charts (line + pie charts)

### ⭐ Exporting Tools

* Export statistics to CSV
* Generate PDF reports (charts + timestamps)

### ⭐ Logging & Error Handling

* Integrate `java.util.logging`
* Add custom exceptions
* Replace debug printouts with structured logging

---

## **Phase 4 — Extended / Recommended Features (Optional)**

**Timeline:** 11.01 → 16.01.2026

### ⭐ Stress-Testing Panel

* Generate heavy traffic on selected edges
* Compare static vs adaptive timing

### ⭐ Adaptive Traffic Light Logic

* Rule-based traffic light algorithm
* Evaluate performance improvements
* Visualize timing changes

### ⭐ 3D Visualization (Optional)

* JavaFX 3D camera
* Render intersections in 3D

---

## **Phase 5 — Finalization & Delivery**

**Timeline:** 17.01 → 18.01.2026

### ✅ Code Freeze & Cleanup

* Remove unused classes
* Final refactoring
* Complete JavaDoc

### ✅ Final Documentation

* User handbook
* Updated technical documentation (all diagrams)
* Milestones summary
* Work distribution table
* Signed declaration of authorship

### ✅ Presentation Preparation

* Live demo scenario
* Slides (architecture, results, teamwork)
* Exported sample reports ready for presentation

