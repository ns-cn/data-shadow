## 数据项维护区域原型设计

针对数据项维护的增改弹出对话框做如下原型设计

<div style="width: 400px; padding: 20px; background: #f5f5f5;">
    <div style="border: 1px solid #ddd; padding: 15px; background: white;">
        <!-- 标题 -->
        <div style="font-weight: bold; margin-bottom: 15px; border-bottom: 1px solid #eee; padding-bottom: 10px;">
            数据项编辑
        </div>
        <!-- 表单内容 -->
        <div style="display: flex; flex-direction: column; gap: 10px;">
            <div style="display: flex; align-items: center;">
                <label style="width: 100px;">是否唯一:</label>
                <input type="checkbox" />
            </div>
            <div style="display: flex; align-items: center;">
                <label style="width: 100px;">名称: <span style="color: red;">*</span></label>
                <input type="text" style="flex: 1; padding: 5px;" placeholder="请输入数据项名称，例如id"/>
            </div>
            <div style="display: flex; align-items: center;">
                <label style="width: 100px;">别名:</label>
                <input type="text" style="flex: 1; padding: 5px;" placeholder="请输入数据项别名，例如主键"/>
            </div>
            <div style="display: flex; align-items: start;">
                <label style="width: 100px;">比较器:</label>
                <div style="flex: 1;">
                    <!-- 比较器选择区域 -->
                    <div style="display: flex; flex-direction: column; gap: 10px;">
                        <!-- 比较器类型选择 -->
                        <div style="display: flex; gap: 10px;">
                            <select style="flex: 1; padding: 5px;" id="comparatorSelect">
                                <option value="">请选择比较器</option>
                            </select>
                            <button style="padding: 5px 10px;" id="comparatorConfig" title="配置比较器">
                                <span style="font-size: 16px;">⚙</span>
                            </button>
                        </div>
                        <!-- 当前配置预览 -->
                        <div style="font-size: 12px; color: #666; padding: 5px; background: #f9f9f9; border: 1px solid #eee; border-radius: 3px;">
                            <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                                <span>当前配置:</span>
                                <span id="currentConfig">未配置</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div style="display: flex; align-items: start;">
                <label style="width: 100px;">备注:</label>
                <textarea style="flex: 1; height: 60px; padding: 5px;" placeholder="请输入备注信息"></textarea>
            </div>
        </div>
        <!-- 按钮区域 -->
        <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
            <div style="flex: 1; color: red; font-size: 12px; align-self: center;">* 表示必填项</div>
            <button style="padding: 5px 20px;">取消</button>
            <button style="padding: 5px 20px; background: #1890ff; color: white; border: none;">确定</button>
        </div>
    </div>
</div>

## 数据项维护增改功能说明

### 1. 功能概述
数据项维护增改功能用于添加新的数据项或修改现有数据项的信息。通过弹出对话框的方式进行操作，确保用户输入的数据完整性和正确性。

### 2. 界面元素说明
- **是否唯一**: 复选框控件，用于标识该数据项是否作为唯一标识。在数据比对时，唯一标识字段将用于确定记录的对应关系。
- **名称**: 文本输入框，用于输入数据项的标识名称。要求：
  - 不能为空
  - 不能与已有数据项重名
- **别名**: 文本输入框，用于输入数据项的显示名称。要求：
  - 可以为空
  - 支持中文、字母、数字等字符
  - 长度不超过50个字符
- **比较器**: 分组下拉选框和配置按钮，用于选择或编写数据比对规则。要求：
  - 必须选择一个比较器
  - 可以不选择比较器，此时数据比对时将跳过该字段
  - 比较器按类型分组展示：
    * 数值组
      - 整数比较器（支持符号、类型转换配置）
      - 浮点数比较器（支持精度、符号、类型转换配置）
    * 字符串组
      - 普通文本比较器（支持空格、null处理配置）
      - 忽略大小写比较器（支持空格、null处理配置）
    * 布尔值组
      - 标准布尔比较器（支持类型转换配置）
      - 扩展布尔比较器（支持自定义true/false值配置）
  - 每种比较器都有其特定的配置项：
    * 数值-整数：
      - 是否忽略符号（1与-1是否相等）
      - 是否允许类型转换（字符串"1"与数字1是否相等）
    * 数值-浮点数：
      - 精度设置（保留几位小数）
      - 是否忽略符号
      - 是否允许类型转换
    * 字符串-普通文本：
      - 是否忽略前后空格
      - 是否允许null等于空字符串
    * 字符串-忽略大小写文本：
      - 是否忽略前后空格
      - 是否允许null等于空字符串
    * 布尔值-标准：
      - 是否允许类型转换（"true"字符串是否等于true）
    * 布尔值-扩展：
      - true的其他等价值（例如1、"yes"、"on"等）
      - false的其他等价值（例如0、"no"、"off"等）
- **备注**: 多行文本输入框，用于输入数据项的补充说明信息。要求：
  - 可以为空
  - 长度不超过200个字符

### 3. 操作流程
1. **新增数据项**:
   - 点击"新增数据项"按钮打开空白表单
   - 填写各项信息
   - 点击确定进行保存，取消则关闭对话框

2. **修改数据项**:
   - 选中需要修改的数据项
   - 点击"编辑选中项"按钮
   - 在弹出的对话框中修改信息
   - 点击确定保存修改，取消则放弃修改

3. **比较器选择与配置**:
   - 从分组下拉框中选择合适的比较器
   - 点击配置按钮（⚙）：
     * 弹出对应比较器的配置对话框
     * 配置对话框内容由比较器自行定义
     * 配置完成后点击确定保存配置

### 4. 数据验证
- 所有必填字段不能为空
- 名称必须符合命名规范
- 比较器代码如果填写则必须能够正确执行
- 如果设置为唯一标识，需要确认是否已存在其他唯一标识字段

### 5. 错误处理
- 输入验证失败时，在相应字段旁显示错误提示
- 保存时如发生错误，弹出错误提示对话框
- 取消编辑时，如有未保存的修改，提示用户确认

### 6. 注意事项
- 修改现有数据项时需谨慎，可能影响已有的比对方案
- 建议在修改前备份当前配置
- 比较器代码将在运行时动态执行，需注意代码安全性
