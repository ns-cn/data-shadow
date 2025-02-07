## 分组下拉选择框组件原型设计

<div style="width: 100%; padding: 20px; background: #f5f5f5;">
<div style="border: 1px solid #ddd; padding: 10px; background: white;">
    <!-- 未展开状态 -->
    <div style="margin-bottom: 20px;">
        <div style="border: 1px solid #ccc; border-radius: 3px; background: linear-gradient(to bottom, #ffffff 0%, #f2f2f2 100%); height: 25px; display: flex; align-items: center;">
            <input type="text" readonly placeholder="请选择" style="flex: 1; border: none; background: transparent; padding: 0 5px; cursor: pointer;">
            <button style="width: 20px; border: none; background: transparent; color: #666; font-size: 12px; cursor: pointer;">×</button>
        </div>
    </div>


<!-- 展开状态 -->
<div style="margin-bottom: 20px;">
        <div style="border: 1px solid #ccc; border-radius: 3px; background: linear-gradient(to bottom, #ffffff 0%, #f2f2f2 100%); height: 25px; display: flex; align-items: center;">
            <input type="text" readonly value="数值比较器/整数比较器" style="flex: 1; border: none; background: transparent; padding: 0 5px; cursor: pointer;">
            <button style="width: 20px; border: none; background: transparent; color: #666; font-size: 12px; cursor: pointer;">×</button>
        </div>
        <!-- 下拉面板 -->
        <div style="position: relative;">
            <div style="position: absolute; top: 2px; left: 0; right: 0; border: 1px solid #c4c4c4; border-radius: 3px; background: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); display: flex;">
                <!-- 分组列表 -->
                <div style="width: 100px; max-height: 250px; overflow-y: auto; padding: 2px;">
                    <div style="padding: 4px 8px; height: 24px; display: flex; align-items: center; background: #0078d7; color: white;">数值比较器</div>
                    <div style="padding: 4px 8px; height: 24px; display: flex; align-items: center;">字符串比较器</div>
                    <div style="padding: 4px 8px; height: 24px; display: flex; align-items: center;">日期比较器</div>
                    <div style="padding: 4px 8px; height: 24px; display: flex; align-items: center;">布尔比较器</div>
                </div>
                <!-- 选项列表 -->
                <div style="width: 100px; max-height: 250px; overflow-y: auto; padding: 2px; border-left: 1px solid #e6e6e6;">
                    <div style="padding: 4px 8px; height: 24px; display: flex; align-items: center; background: #0078d7; color: white;">整数比较器</div>
                    <div style="padding: 4px 8px; height: 24px; display: flex; align-items: center;">小数比较器</div>
                    <div style="padding: 4px 8px; height: 24px; display: flex; align-items: center;">金额比较器</div>
                </div>
            </div>
        </div>
    </div>
</div>
</div>

## 组件说明

1. 组件结构
   - 显示区域：
     * 文本显示框：显示当前选中的值或提示文本
     * 清除按钮：清空当前选择
   - 下拉面板：
     * 分组列表：显示所有可选的分组
     * 选项列表：显示当前分组下的所有选项

2. 显示区域
   - 高度固定为25px
   - 背景使用渐变色
   - 边框为1px实线，圆角3px
   - 文本框：
     * 占据主要空间
     * 只读状态
     * 透明背景
     * 显示格式：{分组名称}/{选项名称}
   - 清除按钮：
     * 固定宽度20px
     * 显示"×"符号
     * 透明背景

3. 下拉面板
   - 位置：显示在组件正下方
   - 边框：1px实线，圆角3px
   - 背景：白色，带阴影效果
   - 分组列表：
     * 固定宽度（默认100px）
     * 最大高度250px，超出显示滚动条
     * 每个分组项高度24px
   - 选项列表：
     * 固定宽度（默认100px）
     * 最大高度250px，超出显示滚动条
     * 左侧有1px分隔线
     * 每个选项项高度24px

4. 交互说明
   - 点击显示区域：
     * 显示/隐藏下拉面板
     * 面板显示时自动获取焦点
   - 点击清除按钮：
     * 清空当前选择
     * 恢复提示文本显示
   - 鼠标悬停分组：
     * 背景色变为选中色
     * 显示该分组下的选项列表
   - 鼠标离开分组：
     * 恢复默认背景色
   - 鼠标悬停选项：
     * 背景色变为选中色
   - 点击选项：
     * 更新显示区域的文本
     * 触发选择事件
     * 关闭下拉面板
   - 点击面板外部：
     * 自动关闭下拉面板

5. 样式说明
   - 默认状态：
     * 背景：白色到浅灰色渐变
     * 边框：#cccccc
     * 文字：#333333
   - 悬停状态：
     * 背景：系统默认选中色
     * 文字：白色
   - 提示文本：
     * 颜色：#999999
   - 分隔线：
     * 颜色：#e6e6e6

6. 可配置项
   - 提示文本
   - 分组列表宽度
   - 选项列表宽度
   - 数据源（分组和选项的映射关系）
   - 选择事件监听器
   - 是否允许清除（默认允许）
