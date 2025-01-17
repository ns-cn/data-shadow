# DataShadow

DataShadow is a JavaFX-based data comparison tool that supports structured data reading, comparison and result display from multiple data sources. It is mainly used for data migration, data validation, data consistency checking and other scenarios.

## Main Features

- 🔌 Plugin Architecture
  - Develop data source plugins based on SDK 
  - Support dynamic loading of data sources
  - Provide standard interface specifications

- 📊 Multiple Data Source Support
  - Database: MySQL, Oracle etc.
  - Files: Excel, CSV, JSON, XML
  - Memory Data: Support direct input comparison

- 🔧 Flexible Configuration
  - Field Mapping: Support mapping between different data sources
  - Data Conversion: Built-in type conversion and formatting
  - Custom Comparators: Customizable comparison rules
  - JavaScript Scripts: Support using JS to write custom data item comparison logic

- 📈 Intuitive Result Display
  - Difference highlighting
  - Multiple export formats
  - Detailed comparison reports

## Project Progress

✅ Completed:
- Basic framework and JavaFX interface
- Plugin loading mechanism
- Data source SDK interface design
- Basic UI layout and navigation

⏳ In Development:
- Data item management (CRUD, sorting, comparators)
- Memory data source (JSON/XML/CSV import, preview)
- Database data source (MySQL/Oracle)
- File data source (Excel/CSV/JSON/XML)
- Data comparison functionality
- Result export functionality
- Field mapping configuration
- Comparison result visualization

## Screenshots

![Homepage](screenshots/homepage.png)
![Plugin Management](screenshots/plugins_demo.png)

## Tech Stack

- Java 21
- JavaFX 21.0.2
- Maven
- JUnit 5
- Main Dependencies:
  - Apache POI
  - FastJSON
  - Apache Commons CSV
  - MySQL/Oracle JDBC
