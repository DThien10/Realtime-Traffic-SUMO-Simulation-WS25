# 🚦 Realtime-Traffic-SUMO-Simulation-WS25 

📌 Overview

- This project is a real-time traffic simulation platform built in Java that connects to the SUMO traffic simulator using the Libtraci API.
- The platform visualizes traffic flow, controls vehicles and traffic lights, and provides live analytics.


🎯 Core Features
✔ Live SUMO Integration

- Connect to a running SUMO instance using TraaS
- Step-by-step real-time simulation
- Programmatic access to telemetry, traffic lights, and vehicles

✔ Interactive Map Visualization

- 2D rendering of the road network
- Real-time vehicle rendering with colors 
- Traffic light visualization with current phase
- Zoom, pan, and camera movement
- Vehicle filtering (speed, color, location...)

✔ Vehicle Injection & Control

- Create vehicles via GUI on any edge
- Batch injection (stress test mode)
- Adjustable vehicle properties: speed, route, color

✔ Traffic Light Management

- Live display of states & duration
- Manual switching
- Adjustable timings

✔ Statistics & Analytics

- Average speed
- Congestion level per edge
- Hotspots & density metrics
- Travel time distribution
- Real-time charts

✔ Exportable Reports

- Export statistics as CSV
- Generate PDF summary reports

⭐ Additional Recommended Features

- Stress testing utilities
- Adaptive traffic control logic
- Optional JavaFX 3D visualizations

🧱 Architecture & Object-Oriented Design
##### Main Components

| Component              | Description                                   |
|------------------------|-----------------------------------------------|
| **SimulationController** | Manages SUMO connection & simulation loop     |
| **VehicleManager**       | OO wrapper for vehicles                       |
| **TrafficLightManager**  | Controls and monitors traffic lights          |
| **MapRenderer**          | Renders road network & UI map                 |
| **StatisticsEngine**     | Computes real-time metrics                    |
| **ReportExporter**       | CSV/PDF report generator                      |
| **Exception Layer**      | Custom exceptions for error handling          |

##### OOP Principles Used

1. Encapsulation
2. Inheritance
3. Interfaces for abstraction
4. Polymorphism (vehicle types, render strategies)
5. Custom exceptions
6. Multi-threading (simulation loop + GUI thread)

🛠️ Technologies Used

- Java 17+
- SUMO (Simulation of Urban Mobility)
- TraaS (Traffic Control API)
- Swing / JavaFX GUI
- java.util.logging
- Collections API
- Threads & Executors

📂 Project Structure
```
/src
 ├── controller/
 ├── model/
 │     ├── vehicles/
 │     ├── trafficlights/
 │     └── edges/
 ├── view/
 │     ├── gui/
 │     └── renderer/
 ├── utils/
 ├── exceptions/
 └── Main.java
/docs
 ├── UML_diagram.pdf
 ├── user_manual.pdf
 └── milestone_documents/
/export
 ├── csv/
 └── pdf/
```

🚀 How to Run the Project
1. Install Requirements
- Java 17+
- SUMO with TraCI enabled
- TraaS library added to classpath
- (Optional) JavaFX SDK if using JavaFX
2. Start SUMO
Example: sumo-gui -c yourSimulation.sumocfg --remote-port 9999

3. Run the Program java -jar TrafficSimulation.jar

📘 Documentation
- All project documentation is located in /docs, including:
- User manual with screenshots
- Technical documentation
- UML class diagrams
- Milestone reports
- Declaration of authorship

👥 Team
| Name                | Role               | Responsibilities                   |
| ------------------- | ------------------ | ---------------------------------- |
| Member 1            | Project Lead       | SUMO integration & simulation loop |
| Member 2            | GUI Lead           | Map rendering, user interactions   |
| Member 3            | Data/Analytics     | Statistics & reporting             |
| Member 4            | Systems            | Collections, exceptions, threading |
| Member 5 (optional) | QA & Documentation | Testing, logging, docs             |

📝 Declaration of AI Usage
- Some parts of this project may have been supported by AI tools (e.g., explanation, debugging assistance).
All AI-assisted content is reviewed, verified, and understood by the authors.
