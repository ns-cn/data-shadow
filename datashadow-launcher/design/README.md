针对数据影子应用的UI设计，通过markdown文件进行描述，在markdown文件内部使用html进行界面原型说明。

## 核心界面

![主界面](./PAGE_MAIN.md)
![数据项维护界面](./PAGE_DATA_ITEM.md)
![数据源维护界面](./PAGE_DATA_SOURCE.md)
![数据对比结果界面](./PAGE_DATA_COMPARE_RESULT.md)


### 主界面布局说明

完善主窗口的原型设计（包含界面和原型设计说明），整体布局为【数据项维护】、【数据源维护】和【对比功能区】和【对比结果展示区】
整体采用从上到下布局，
其中数据项维护包含数据项展示（数据项是否id、名称、别名、备注）、数据项增删改、数据项id设置、数据项排序；
数据源维护包含数据源的类型选择、数据源配置以及数据源字段映射三个功能，另外展示已经配置的数据源；
对比功能区包含执行对比、对比字段范围、是否仅显示差异数据项、导入和导出对比结果；
对比结果展示区用表格展示双方的结果，将两个数据源在某个数据项上的数据不一致展示两个数据项，并标红显示；