# DataShadow

#### 项目介绍
DataShadow是一个数据比对工具，可以读取多种数据源的结构化数据，进行数据比对，并生成比对结果。

#### 项目结构
- datashadow-launcher: 启动模块,包含GUI界面和主程序入口
- datashadow-datasource-sdk: 数据源SDK模块,定义数据源接口规范
- datashadow-datasource-db: 数据库数据源模块,支持MySQL/Oracle等数据库
- datashadow-datasource-file: 文件数据源模块,支持Excel/CSV/JSON/XML等文件格式

#### 技术架构
- 开发语言: Java 23
- GUI框架: JavaFX 21.0.2
- 项目管理: Maven
- 单元测试: JUnit 5
- 主要依赖:
  - Apache POI: Excel文件读写
  - FastJSON: JSON数据处理
  - Apache Commons CSV: CSV文件处理
  - MySQL/Oracle JDBC: 数据库连接

#### 核心功能
- 支持多种数据源的数据读取
- 数据源配置和可用性验证
- 数据字段映射和转换
- 数据比对和结果生成
 
#### 安装说明
1. 确保已安装JDK 23或更高版本
2. 克隆项目到本地
3. 使用Maven构建项目:
