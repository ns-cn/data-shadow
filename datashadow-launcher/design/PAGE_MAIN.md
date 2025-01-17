## 主界面原型设计

<div style="width: 100%; padding: 20px; background: #f5f5f5;">
<div style="border: 1px solid #ddd; padding: 10px; background: white;">
    <!-- 菜单栏 -->
    <div style="border-bottom: 1px solid #eee; padding: 5px; margin-bottom: 10px;">
        <div style="display: flex; gap: 20px;">
            <div style="cursor: pointer;">文件
                <div style="display: none; position: absolute; background: white; border: 1px solid #ddd; padding: 5px;">
                    <div style="padding: 5px;">退出</div>
                </div>
            </div>
            <div style="cursor: pointer;">帮助
                <div style="display: none; position: absolute; background: white; border: 1px solid #ddd; padding: 5px;">
                    <div style="padding: 5px;">Q&A</div>
                    <div style="padding: 5px;">关于</div>
                </div>
            </div>
        </div>
    </div>
    <!-- 数据项维护区域 -->
    <div style="border-bottom: 1px solid #eee; padding: 10px; margin-bottom: 10px;">
        <div style="font-weight: bold; margin-bottom: 5px;">数据项维护</div>
        <div style="display: flex; gap: 10px;">
            <div style="flex: 1; border: 1px solid #ddd;">
                <!-- 数据项列表 -->
                <table style="width: 100%; border-collapse: collapse; text-align: center;">
                    <tr style="background: #f8f8f8;">
                        <th style="text-align: center;">是否唯一</th>
                        <th style="text-align: center;">名称</th>
                        <th style="text-align: center;">别名</th>
                        <th style="text-align: center;">自定义比较器</th>
                        <th style="text-align: center;">备注</th>
                    </tr>
                    <tr style="background: #e6f3ff;">
                        <td><span style="color: green;">✓</span></td>
                        <td>id</td>
                        <td>用户ID</td>
                        <td>已设置</td>
                        <td>用户的唯一标识符</td>
                    </tr>
                    <tr>
                        <td></td>
                        <td>userName</td>
                        <td>用户名称</td>
                        <td>未设置</td>
                        <td>用户的显示名称</td>
                    </tr>
                </table>
            </div>
            <div style="width: 120px;">
                <button style="width: 100%; margin-bottom: 5px;">新增数据项</button>
                <button style="width: 100%; margin-bottom: 5px;">编辑选中项</button>
                <button style="width: 100%; margin-bottom: 5px;">删除选中项</button>
                <button style="width: 100%; margin-bottom: 5px;">上移</button>
                <button style="width: 100%;">下移</button>
            </div>
        </div>
    </div>
    <!-- 数据源维护区域 -->
    <div style="border-bottom: 1px solid #eee; padding: 10px; margin-bottom: 10px;">
        <div style="font-weight: bold; margin-bottom: 5px;">数据源维护</div>
        <div style="display: flex; gap: 20px;">
            <!-- 主数据源 -->
            <div style="flex: 1; border: 1px solid #ddd; padding: 10px;">
                <div style="margin-bottom: 5px;">主数据源</div>
                <div style="display: flex; gap: 5px;">
                    <select style="flex: 1;">
                        <option>MySQL</option>
                        <option>Oracle</option>
                        <option>Excel</option>
                        <option>CSV</option>
                    </select>
                    <button>配置数据源</button>
                    <button>字段映射</button>
                </div>
                <div style="margin-bottom: 5px;">已配置数据源：mysql:127.0.0.1:3306/test</div>
            </div>
            <!-- 影子数据源 -->
            <div style="flex: 1; border: 1px solid #ddd; padding: 10px;">
                <div style="margin-bottom: 5px;">影子数据源</div>
                <div style="display: flex; gap: 5px;">
                    <select style="flex: 1;">
                        <option>MySQL</option>
                        <option>Oracle</option>
                        <option>Excel</option>
                        <option>CSV</option>
                    </select>
                    <button>配置数据源</button>
                    <button>字段映射</button>
                </div>
                <div style="margin-bottom: 5px;">已配置数据源：D:/test.xlsx</div>
            </div>
        </div>
    </div>
    <!-- 对比功能和结果展示区 -->
    <div style="padding: 10px;">
        <div style="font-weight: bold; margin-bottom: 5px;">对比功能与结果</div>
        <div style="display: flex; gap: 10px; align-items: center; margin-bottom: 10px;">
            <button style="padding: 5px 20px;">执行对比</button>
            <label><input type="checkbox"> 仅显示差异项</label>
            <label><input type="checkbox"> 优先显示数据项别名</label>
        </div>
        <table style="width: 100%; border-collapse: collapse; border: 1px solid #ddd; text-align: center;">
            <tr style="background: #f8f8f8;">
                <th style="padding: 5px; text-align: center;">用户ID</th>
                <th style="text-align: center;">用户名称</th>
                <th style="text-align: center;">年龄</th>
                <th style="text-align: center;">性别</th>
                <th style="text-align: center;">手机号</th>
            </tr>
            <tr>
                <td>10001</td>
                <td style="color: red;">张三 ❌ 李四</td>
                <td>25</td>
                <td>男</td>
                <td style="color: red;">13800138000 ❌ 13900139000</td>
            </tr>
            <tr>
                <td>10002</td>
                <td>王五</td>
                <td style="color: red;">30 ❌ 31</td>
                <td>女</td>
                <td>13700137000</td>
            </tr>
        </table>
    </div>
</div>
</div>

## 界面说明

1. 菜单栏
   - 文件菜单
     * 退出：退出应用程序
   - 帮助菜单
     * Q&A：显示常见问题解答
     * 关于：显示应用程序版本信息和开发者信息

2. 数据项维护区域
   - 左侧表格展示已配置的数据项列表，包含以下字段：
     * ID：数据项的唯一标识符（code）
     * 名称：数据项的显示名称
     * 别名：数据项的别名（nick）
     * 是否唯一：标识该数据项是否具有唯一性
     * 备注：数据项的详细说明
     * 排序：显示顺序
   - 支持单选和多选操作
   - 右侧提供操作按钮：
     * 新增数据项：添加新的数据项
     * 编辑选中项：修改选中数据项的信息
     * 删除选中项：移除选中的数据项
     * 上移/下移：调整选中项的排序位置

3. 数据源维护区域
   - 分为主数据源和影子数据源两部分
   - 每个数据源可以选择类型（MySQL/Oracle/Excel/CSV等）
   - 提供数据源配置和字段映射功能
   - 配置完成后显示数据源连接状态

4. 对比功能与结果展示区
   - 顶部提供对比功能按钮和选项：
     * 执行对比按钮
     * 仅显示差异项选项
     * 优先显示数据项别名选项
   - 下方表格实时展示对比结果：
     * 每行展示一条数据的所有数据项
     * 相同值直接显示
     * 不同值用红色高亮并用❌分隔显示
     * 支持按列排序和结果过滤
     * 可导出对比结果

## 交互说明

1. 菜单栏
   - 点击"文件"显示下拉菜单
     * 点击"退出"关闭应用程序
   - 点击"帮助"显示下拉菜单
     * 点击"Q&A"显示常见问题解答对话框
     * 点击"关于"显示关于对话框

2. 数据项维护
   - 可以通过复选框选择一个或多个数据项
   - 选中数据项后右侧对应按钮才可用
   - 编辑和排序操作仅支持单选
   - 删除操作支持多选
   - 新增/编辑数据项时弹出表单，包含：
     * ID输入框（必填，唯一）
     * 名称输入框（必填）
     * 别名输入框
     * 是否唯一复选框
     * 比较器选择（可选择内置比较器或自定义比较规则）
     * 备注多行文本框
   - 移动操作时：
     * 选中单条数据时，上移/下移按钮根据位置自动启用/禁用
     * 选中多条数据时，移动按钮禁用

3. 数据源配置
   - 选择数据源类型后点击"配置数据源"进行详细配置
   - 配置完成后自动验证连接
   - 点击"字段映射"可以设置数据源字段与数据项的对应关系

4. 对比功能与结果
   - 两个数据源配置完成后才能执行对比
   - 对比过程中显示进度条
   - 可以随时暂停/继续对比过程
   - 支持导入导出对比方案复用
   - 结果表格支持：
     * 按列排序
     * 差异项筛选
     * 差异值红色标记
     * 导出到Excel
     * 可切换显示数据项名称或别名（通过"优先显示数据项别名"选项控制）
