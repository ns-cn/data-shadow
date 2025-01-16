## 数据源映射区域原型设计

数据源映射是将数据源的列名与数据项的列名进行映射，以便于数据项的列名与数据源的列名一致。

<div style="border: 1px solid #ccc; padding: 20px; width: 600px;">
    <div style="text-align: center; font-size: 16px; font-weight: bold; margin-bottom: 20px;">
        数据源字段映射
    </div>
    <div style="border: 1px solid #eee; padding: 10px; margin-bottom: 20px;">
        <div style="margin-bottom: 10px;">
            <button style="margin-right: 10px;">自动映射</button>
            <button style="margin-right: 10px;">重建映射</button>
            <button>清空映射</button>
        </div>
        <div style="color: #666; font-size: 12px;">
            注：自动映射将根据字段名称相似度追加映射关系，重建映射将清空现有数据项并根据数据源字段创建新的数据项，清空映射将清除所有已建立的映射关系
        </div>
    </div>
    <div style="margin-bottom: 20px;">
        <table style="width: 100%; border-collapse: collapse;">
            <thead>
                <tr style="background-color: #f5f5f5;">
                    <th style="padding: 8px; border: 1px solid #ddd;">数据项名称</th>
                    <th style="padding: 8px; border: 1px solid #ddd;">数据源字段</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td style="padding: 8px; border: 1px solid #ddd;">用户ID</td>
                    <td style="padding: 8px; border: 1px solid #ddd;">
                        <select style="width: 100%;">
                            <option value="">请选择</option>
                            <option value="id">id</option>
                            <option value="user_id">user_id</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td style="padding: 8px; border: 1px solid #ddd;">用户名称</td>
                    <td style="padding: 8px; border: 1px solid #ddd;">
                        <select style="width: 100%;">
                            <option value="">请选择</option>
                            <option value="name">name</option>
                            <option value="username">username</option>
                        </select>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
    <div style="text-align: right;">
        <button style="margin-right: 10px;">取消</button>
        <button style="background-color: #1890ff; color: white; border: none; padding: 5px 15px;">确定</button>
    </div>
</div>

### 功能说明

1. 自动映射功能
   - 根据字段名称的相似度自动匹配数据源字段和数据项
   - 仅对未映射的数据项进行追加映射，不会覆盖已有映射
   - 相似度计算考虑以下因素：
     * 字段名称完全匹配
     * 字段名称忽略大小写匹配
     * 字段名称去除下划线后匹配
     * 字段名称中包含关键字匹配

2. 重建映射功能
   - 清空现有的所有数据项
   - 根据数据源的字段自动创建新的数据项
   - 自动设置数据项的名称（不设置id、别名等信息）
   - 操作前需要用户确认，因为会清空现有数据项

3. 清空映射功能
   - 清除所有已建立的字段映射关系
   - 保留现有的数据项定义
   - 操作前需要用户确认
   - 清空后所有下拉框恢复到未选择状态

4. 映射结果维护
   - 以表格形式展示所有数据项和对应的数据源字段
   - 每个数据项可以从下拉列表中选择对应的数据源字段
   - 下拉列表显示数据源的所有可用字段
   - 支持清除已选择的映射关系

5. 操作按钮
   - 取消按钮：关闭对话框，不保存修改
   - 确定按钮：保存映射关系并关闭对话框
   - 确定按钮仅在所有必需的数据项都已映射时才能点击

### 注意事项

1. 界面设计采用对话框形式，模态显示
2. 自动映射和重建映射的操作都需要二次确认
3. 必需的数据项在未映射时需要用红色标记
4. 下拉列表中已经被其他数据项使用的字段需要特殊标记
5. 界面需要适配不同数量的数据项，支持滚动显示


