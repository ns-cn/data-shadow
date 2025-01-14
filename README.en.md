# DataShadow

#### Project Introduction
DataShadow is a data comparison tool that can read structured data from various data sources, perform data comparison, and generate comparison results.

#### Project Structure
- datashadow-launcher: Launch module, containing GUI interface and main program entry
- datashadow-datasource-sdk: Data source SDK module, defining data source interface specifications
- datashadow-datasource-db: Database data source module, supporting MySQL/Oracle and other databases
- datashadow-datasource-file: File data source module, supporting Excel/CSV/JSON/XML and other file formats

#### Technical Architecture
- Development Language: Java 23
- GUI Framework: JavaFX 21.0.2
- Project Management: Maven
- Unit Testing: JUnit 5
- Main Dependencies:
  - Apache POI: Excel file reading and writing
  - FastJSON: JSON data processing
  - Apache Commons CSV: CSV file processing
  - MySQL/Oracle JDBC: Database connectivity

#### Core Features
- Support for reading data from multiple data sources
- Data source configuration and availability validation
- Data field mapping and conversion
- Data comparison and result generation

#### Installation Guide
1. Ensure JDK 23 or higher is installed
2. Clone the project locally
3. Build the project using Maven:
