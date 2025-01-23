# 数据源HTTP原型设计

## 数据源HTTP界面设计

<div style="width: 600px; height: 500px; border: 1px solid #ccc; padding: 20px; font-family: Arial;">
    <div style="margin-bottom: 20px;">
        <label style="display: block; margin-bottom: 5px;">请求URL：</label>
        <input type="text" style="width: 100%; padding: 5px;" placeholder="请输入HTTP请求URL，例如：https://api.example.com/data"/>
    </div>
    <div style="margin-bottom: 20px;">
        <div style="display: flex; gap: 20px;">
            <div style="flex: 1;">
                <div style="display: flex; align-items: center;">
                    <label style="margin-right: 10px;">请求方法：</label>
                    <select style="flex: 1; padding: 5px;">
                        <option value="GET">GET</option>
                        <option value="POST">POST</option>
                        <option value="PUT">PUT</option>
                        <option value="DELETE">DELETE</option>
                        <option value="HEAD">HEAD</option>
                        <option value="OPTIONS">OPTIONS</option>
                        <option value="PATCH">PATCH</option>
                    </select>
                </div>
            </div>
            <div style="flex: 1;">
                <div style="display: flex; align-items: center;">
                    <label style="margin-right: 10px;">响应数据类型：</label>
                    <select style="flex: 1; padding: 5px;">
                        <option value="json">JSON</option>
                        <option value="xml">XML</option>
                        <option value="csv">CSV</option>
                    </select>
                </div>
            </div>
        </div>
    </div>
    <div style="margin-bottom: 20px;">
        <label style="display: block; margin-bottom: 5px;">请求头：</label>
        <div style="border: 1px solid #ddd; padding: 10px; max-height: 150px; overflow-y: auto;">
            <div style="display: flex; gap: 10px; margin-bottom: 10px;">
                <input type="text" style="flex: 1; padding: 5px;" placeholder="Header名称"/>
                <input type="text" style="flex: 1; padding: 5px;" placeholder="Header值"/>
                <button style="padding: 5px 10px;">删除</button>
            </div>
            <button style="padding: 5px 10px;">添加请求头</button>
        </div>
    </div>
    <div style="margin-bottom: 20px;">
        <label style="display: block; margin-bottom: 5px;">请求体：</label>
        <textarea style="width: 100%; height: 100px; padding: 5px;" placeholder="请输入请求体内容（仅POST、PUT、PATCH、DELETE方法可用）"></textarea>
    </div>
    <div style="display: flex; justify-content: space-between; gap: 10px;">
        <button style="padding: 8px 15px;">从CURL导入</button>
        <div style="display: flex; gap: 10px;">
            <button style="padding: 8px 15px;">测试连接</button>
            <button style="padding: 8px 15px;">确定</button>
            <button style="padding: 8px 15px;">取消</button>
        </div>
    </div>
</div>

## 数据源HTTP功能说明

1. 界面布局说明
   - 整体采用垂直布局，各配置项依次排列
   - 使用固定宽度（600px）确保界面美观
   - 适当的内边距和间距确保视觉舒适

2. 配置项说明
   - 请求URL：必填项，用于输入完整的HTTP请求地址
   - 请求方法：下拉选择，支持常用的HTTP方法（GET、POST等）
   - 响应数据类型：下拉选择，支持JSON、XML、CSV三种格式
   - 请求头：可动态添加多个请求头键值对
   - 请求体：文本区域，仅在特定请求方法下可用

3. 操作按钮说明
   - 从CURL导入：点击后弹出输入对话框，可输入curl命令字符串并解析为对应配置
   - 测试连接：点击后会验证当前配置的有效性
   - 确定：保存当前配置并关闭窗口
   - 取消：放弃更改并关闭窗口

4. 交互说明
   - 请求方法切换时，如选择GET方法，请求体输入框将被禁用
   - 请求头支持动态添加和删除
   - 所有输入框都应该支持复制粘贴功能
   - 测试连接时应该显示加载动画和结果提示
   - CURL导入时会自动解析并填充URL、请求方法、请求头和请求体等信息

5. 数据验证
   - URL必须是有效的HTTP/HTTPS地址
   - 请求头的键不能为空
   - 响应数据必须符合所选格式（JSON/XML/CSV）
   - CURL字符串必须是有效的curl命令格式

6. 界面响应
   - 输入错误时应该显示醒目的错误提示
   - 测试连接成功/失败应该有明确的反馈
   - 保存配置时应该验证所有必填项
   - CURL导入失败时应该显示具体的错误原因
