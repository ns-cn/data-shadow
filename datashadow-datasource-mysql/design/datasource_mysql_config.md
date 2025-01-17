## MYSQL数据源配置界面原型设计

### 数据源配置界面

<div style="width: 100%; padding: 20px; background: #f5f5f5;">
<div style="border: 1px solid #ddd; padding: 20px; background: white;">
    <!-- 数据库连接配置 -->
    <div style="margin-bottom: 20px;">
        <div style="font-weight: bold; margin-bottom: 10px;">数据库连接配置</div>
        <div style="display: flex; flex-direction: column; gap: 10px;">
            <div style="display: flex; align-items: center; gap: 10px;">
                <label style="width: 100px;">主机地址:</label>
                <input type="text" style="flex: 1;" placeholder="localhost"/>
                <label style="width: 80px;">端口:</label>
                <input type="text" style="width: 100px;" placeholder="3306"/>
            </div>
            <div style="display: flex; align-items: center; gap: 10px;">
                <label style="width: 100px;">数据库名:</label>
                <input type="text" style="flex: 1;" placeholder="database"/>
            </div>
            <div style="display: flex; align-items: center; gap: 10px;">
                <label style="width: 100px;">用户名:</label>
                <input type="text" style="flex: 1;" placeholder="root"/>
            </div>
            <div style="display: flex; align-items: center; gap: 10px;">
                <label style="width: 100px;">密码:</label>
                <input type="password" style="flex: 1;" placeholder="******"/>
            </div>
        </div>
    </div>
    <!-- SQL查询配置 -->
    <div style="margin-bottom: 20px;">
        <div style="font-weight: bold; margin-bottom: 10px;">SQL查询配置</div>
        <div style="display: flex; flex-direction: column; gap: 10px;">
            <textarea style="width: 100%; height: 100px; resize: vertical;" placeholder="SELECT * FROM table"></textarea>
        </div>
    </div>
    <!-- 操作按钮 -->
    <div style="display: flex; justify-content: flex-end; gap: 10px;">
        <button style="padding: 5px 15px;">测试连接</button>
        <button style="padding: 5px 15px;">确定</button>
        <button style="padding: 5px 15px;">取消</button>
    </div>
</div>
</div>

### 数据源配置功能说明

1. 数据库连接配置
   - 主机地址：MySQL数据库服务器的IP地址或域名
   - 端口：MySQL数据库服务器的端口号，默认3306
   - 数据库名：要连接的数据库名称
   - 用户名：数据库连接用户名
   - 密码：数据库连接密码

2. SQL查询配置
   - 可输入自定义SQL查询语句
   - SQL语句必须是查询语句（SELECT）
   - 查询结果将作为数据源的数据来源

3. 功能按钮
   - 测试连接：验证当前配置的数据库连接是否可用
   - 确定：保存当前配置并关闭窗口
   - 取消：放弃当前配置并关闭窗口

4. 注意事项
   - 所有字段都为必填项
   - SQL语句应当符合MySQL语法规范
   - 建议在保存配置前先测试连接
