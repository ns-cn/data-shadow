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
            <div style="cursor: pointer;">对比方案
                <div style="display: none; position: absolute; background: white; border: 1px solid #ddd; padding: 5px;">
                    <div style="padding: 5px;">导入方案</div>
                    <div style="padding: 5px;">导出方案</div>
                </div>
            </div>
            <div style="cursor: pointer;">设置
                <div style="display: none; position: absolute; background: white; border: 1px solid #ddd; padding: 5px;">
                    <div style="padding: 5px;">系统设置</div>
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
                        <th style="text-align: center;">比较器</th>
                        <th style="text-align: center;">备注</th>
                    </tr>
                    <tr style="background: #e6f3ff;">
                        <td><span style="color: green;">✓</span></td>
                        <td>id</td>
                        <td>用户ID</td>
                        <td>整数比较器</td>
                        <td>用户的唯一标识符</td>
                    </tr>
                    <tr>
                        <td></td>
                        <td>userName</td>
                        <td>用户名称</td>
                        <td>字符串比较器(忽略大小写)</td>
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
        <div style="display: flex; gap: 10px; align-items: center; margin-bottom: 10px; justify-content: space-between;">
            <div style="display: flex; gap: 10px; align-items: center;">
                <button style="padding: 5px 20px;">执行对比</button>
                <select style="padding: 2px 10px;">
                    <option>全部数据</option>
                    <option>所有差异项</option>
                    <option>仅主数据源</option>
                    <option>仅主数据源差异项</option>
                    <option>仅影子数据源</option>
                    <option>仅影子数据源差异项</option>
                </select>
                <select style="padding: 2px 10px;">
                    <option>数据项名称</option>
                    <option>数据项别名优先</option>
                </select>
            </div>
            <div style="display: flex; gap: 5px;">
                <span>导出：</span>
                <select style="padding: 2px 10px;">
                    <optgroup label="导出到文件">
                        <option>Excel</option>
                        <option>CSV</option>
                        <option>JSON</option>
                    </optgroup>
                    <optgroup label="自定义导出">
                        <!-- 这里会动态加载用户自定义的导出选项 -->
                    </optgroup>
                </select>
                <button>导出</button>
            </div>
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
   - 对比方案菜单
     * 导入方案：从.shadow文件导入已保存的对比方案（包含数据项配置、数据源配置和映射关系）
     * 导出方案：将当前对比方案导出为.shadow文件，以便后续复用
   - 系统设置菜单
     * 系统设置：跳转到系统设置对话框
   - 帮助菜单
     * Q&A：显示常见问题解答
     * 关于：显示应用程序版本信息和开发者信息

2. 数据项维护区域
   - 左侧表格展示已配置的数据项列表，包含以下字段：
     * ID：数据项的唯一标识符（code）
     * 名称：数据项的显示名称
     * 别名：数据项的别名（nick）
     * 比较器：数据项比较器的具体实现
     * 备注：数据项的详细说明
     * 排序：显示顺序
   - 支持单选和多选操作
   - 双击数据项记录可直接进入编辑状态
   - 选中数据项后按Delete键可删除选中项（会弹出确认对话框）
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
   - 顶部工具栏分为左右两部分：
     * 左侧功能区：
       - 执行对比按钮：开始数据对比
       - 过滤模式下拉框：
         + 全部数据：显示所有数据项
         + 所有差异项：显示所有有差异的数据项
         + 仅主数据源：仅显示主数据源数据
         + 仅主数据源差异项：仅显示主数据源中有差异的数据项
         + 仅影子数据源：仅显示影子数据源数据
         + 仅影子数据源差异项：仅显示影子数据源中有差异的数据项
       - 表头显示模式下拉框：
         + 数据项名称：使用数据项代码作为列标题
         + 数据项别名优先：优先使用数据项别名作为列标题
     * 右侧导出功能区：
       - 导出格式选择下拉框：
         + 内置导出格式组：
           - Excel：导出为Excel文件
           - CSV：导出为CSV文件
           - JSON：导出为JSON文件
         + 自定义导出组：
           - 通过插件机制动态加载的自定义导出选项
       - 导出按钮：执行选中格式的导出操作
   - 下方表格实时展示对比结果：
     * 每列对应一个数据项
     * 列标题显示规则：
       - 根据表头显示模式选择显示数据项代码或别名
       - 如果数据项未设置比较器，在标题前添加❗警告标记
     * 单元格显示规则：
       - 相同值直接显示单个值
       - 不同值用红色显示，并用❌分隔主数据源和影子数据源的值
       - 所有单元格居中对齐
     * 表格支持：
       - 按列排序
       - 根据过滤模式筛选结果
       - 自适应布局，可随窗口调整大小
       - 最小高度限制，确保可用性

## 交互说明

1. 菜单栏
   - 点击"文件"显示下拉菜单
     * 点击"退出"关闭应用程序
   - 点击"对比方案"显示下拉菜单
     * 点击"导入方案"：
       - 打开文件选择对话框，仅显示.shadow文件
       - 选择文件后，解码Base64字符串得到JSON字符串
       - JSON包含以下内容：
         + dataItems：数据项列表，每个数据项包含：
           - id：数据项ID
           - name：数据项名称
           - nick：数据项别名
           - remark：数据项备注
           - comparator：比较器配置，包含：
             + group：比较器分组
             + friendlyName：比较器友好名称
             + config：比较器导出的配置字符串
         + primaryDataSourceName：主数据源类型名称
         + primaryDataSource：主数据源导出的配置字符串
         + shadowDataSourceName：影子数据源类型名称
         + shadowDataSource：影子数据源导出的配置字符串
       - 导入流程：
         + 清空当前所有配置
         + 导入数据项列表：
           - 创建数据项
           - 根据group和friendlyName找到对应的DataComparatorGenerator
           - 使用importComparator方法导入比较器配置
         + 根据数据源名称生成对应的数据源实例
         + 使用importSource方法导入数据源配置
     * 点击"导出方案"：
       - 打开文件保存对话框，默认扩展名为.shadow
       - 收集当前配置信息：
         + 从DataFactory获取数据项列表，每个数据项包含：
           - 基本信息（id、name、nick、remark）
           - 比较器信息：
             + 从DataComparatorRegistry获取group和friendlyName
             + 调用exportComparator获取比较器配置
         + 获取主数据源名称和实例
         + 获取影子数据源名称和实例
         + 调用数据源的exportSource方法获取序列化后的配置
       - 将配置信息组装为JSON
       - 将JSON转换为Base64编码
       - 保存为.shadow文件
   - 点击"系统设置"显示系统设置对话框
   - 点击"帮助"显示下拉菜单
     * 点击"Q&A"显示常见问题解筑对话框
     * 点击"关于"显示关于对话桑

2. 数据项维护
   - 可以通过复选框选择一个或多个数据项
   - 选中数据项后右侧对应按钮才可用
   - 编辑和排序操作仅支持单选
   - 删除操作支持多选
   - 双击数据项记录时：
     * 直接进入编辑状态
     * 弹出与"编辑选中项"相同的编辑表单
   - 选中数据项后按Delete键：
     * 弹出确认对话桑"确定要删除选中的数据项吗？"
     * 点击确定后执行删除操作
     * 点击取消则不执行任何操作
   - 新增/编辑数据项时弹出表单，包含：
     * ID输入框（必填，唯一）
     * 名称输入框（必填）
     * 别名输入框
     * 比较器类型选择
     * 比较器选择
     * 比较器配置
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
   - 对比方案管理：
     * 通过菜单栏的"对比方案"进行导入导出
     * 导出的方案为.shadow格式文件，内容为Base64编码的JSON字符串
     * JSON包含：
       - 数据项配置列表（包含比较器配置）
       - 主数据源类型名称和配置
       - 影子数据源类型名称和配置
     * 导入方案时会替换当前所有配置，包括：
       - 清空并重新导入数据项（包括重新生成比较器）
       - 重新生成并配置主数据源
       - 重新生成并配置影子数据源
   - 结果表格支持：
     * 按列排序
     * 差异项筛选
     * 差异值红色标记
     * 导出功能：
       - 从导出格式下拉框选择要导出的格式
       - 点击导出按钮执行导出操作
       - 弹出文件保存对话框，选择保存位置
       - 导出完成后显示成功提示，并提供打开文件目录的链接
       - 导出失败时显示错误提示，包含具体的错误信息
       - 支持通过插件机制扩展自定义导出格式
       - 导出时保持当前显示状态（包括筛选和排序）
     * 可切换显示数据项名称或别名
