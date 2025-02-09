# 系统设置的原型设计

## 1. 系统设置界面

<div style="width: 100%; padding: 20px; background: #f5f5f5;">
<div style="border: 1px solid #ddd; border-radius: 4px; padding: 25px; background: white; min-width: 600px;">
    <!-- 表单容器 -->
    <div style="display: flex; flex-direction: column; gap: 20px;">
        <!-- 基础配置表单 -->
        <fieldset style="border: 1px solid #ddd; border-radius: 4px; padding: 15px;">
            <legend style="font-weight: bold; padding: 0 10px; color: #333;">基础配置</legend>
            <div style="padding: 10px;">
                <!-- 插件目录配置 -->
                <div style="display: grid; grid-template-columns: 120px 1fr; gap: 10px; align-items: center;">
                    <div style="color: #333;">插件目录路径：</div>
                    <input type="text" style="padding: 8px; border: 1px solid #ddd; border-radius: 4px; background: #fafafa; width: 400px;" value="C:/Users/username/.datashadow/plugins" readonly/>
                </div>
                <!-- 按钮组 -->
                <div style="margin-top: 10px; margin-left: 130px;">
                    <button style="padding: 8px 15px; margin-right: 10px; border: 1px solid #ddd; border-radius: 4px;">选择目录</button>
                    <button style="padding: 8px 15px; margin-right: 10px; border: 1px solid #ddd; border-radius: 4px;">重置为默认</button>
                    <button style="padding: 8px 15px; border: 1px solid #ddd; border-radius: 4px;">打开目录</button>
                </div>
            </div>
        </fieldset>
        <!-- AI配置表单 -->
        <fieldset style="border: 1px solid #ddd; border-radius: 4px; padding: 15px;">
            <legend style="font-weight: bold; padding: 0 10px; color: #333;">AI配置</legend>
            <div style="padding: 10px;">
                <!-- AI模型选择 -->
                <div style="display: grid; grid-template-columns: 120px 1fr; gap: 10px; align-items: center; margin-bottom: 15px;">
                    <div style="color: #333;">AI模型：</div>
                    <select style="padding: 8px; border: 1px solid #ddd; border-radius: 4px; width: 400px;">
                        <option value="Qwen/Qwen2.5-7B-Instruct">Qwen2.5-7B-Instruct</option>
                    </select>
                </div>
                <!-- API Key配置 -->
                <div style="display: grid; grid-template-columns: 120px 1fr; gap: 10px; align-items: center;">
                    <div style="color: #333;">API Key：</div>
                    <input type="password" style="padding: 8px; border: 1px solid #ddd; border-radius: 4px; width: 400px;" value="sk-xxxxxxxxxxxxxxxxxxxxxxxx"/>
                </div>
                <!-- 验证按钮 -->
                <div style="margin-top: 10px; margin-left: 130px;">
                    <button style="padding: 8px 15px; margin-right: 10px; border: 1px solid #ddd; border-radius: 4px;">验证</button>
                    <button style="padding: 8px 15px; margin-right: 10px; border: 1px solid #ddd; border-radius: 4px;">测试对话</button>
                    <button style="padding: 8px 15px; border: 1px solid #ddd; border-radius: 4px;" title="需要注册并申请APIkey，不必担心，使用的ai模型均为免费模型，不会产生费用">获取APIKEY</button>
                </div>
                <!-- AI说明文字 -->
                <div style="margin-top: 15px; margin-left: 130px; font-size: 12px; color: #666;">
                    注：本程序基于硅基流动的免费模型，主要用于数据项自动映射等场景，不会产生任何费用。
                </div>
            </div>
        </fieldset>
    </div>
    <!-- 底部按钮 -->
    <div style="margin-top: 30px; padding-top: 20px; text-align: right; gap: 10px; display: flex; justify-content: flex-end; border-top: 1px solid #eee;">
        <button style="padding: 8px 25px; background: #1976d2; border: none; color: white; border-radius: 4px; cursor: pointer; transition: all 0.3s;">保存设置</button>
        <button style="padding: 8px 25px; background: #fff; border: 1px solid #ddd; color: #666; border-radius: 4px; cursor: pointer; transition: all 0.3s;">取消</button>
    </div>
</div>
</div>

## 2. 功能说明

### 2.1 基础配置

1. 插件目录配置
   - 显示当前配置的插件加载目录路径
   - 【选择目录】按钮：打开目录选择对话框，选择新的插件目录
   - 【重置为默认】按钮：将插件目录重置为默认路径（用户目录/.datashadow/plugins）
   - 【打开目录】按钮：在系统资源管理器中打开当前插件目录

### 2.2 AI配置

1. AI模型选择
   - 下拉框选择要使用的AI模型
   - 支持主流AI模型如GPT系列、Claude系列等
   - 切换模型时自动保存配置

2. API Key管理
   - 输入框采用密码形式显示API Key
   - 【验证】按钮：测试API Key的有效性
   - 【测试对话】按钮：打开对话测试窗口，可进行简单的问答测试
   - 【获取APIKEY】按钮：
     * 使用默认浏览器打开硅基流动官网
     * 悬浮提示：需要注册并申请APIkey，不必担心，使用的ai模型均为免费模型，不会产生费用

### 2.3 通用功能

1. 保存机制
   - 【保存设置】按钮：
     * 保存所有配置项
     * 配置信息使用Base64编码后存储到配置文件
     * 保存成功后提示"设置已保存，重启程序后生效"
   - 【取消】按钮：
     * 放弃当前修改
     * 关闭设置窗口

2. 配置存储
   - 配置文件位置：用户目录/.datashadow/ds.conf
   - 使用Base64编码存储配置内容
   - 包含以下配置项：
     * pluginDir：插件目录路径
     * aiModel：AI模型选择
     * aiApiKey：AI API密钥

3. 错误处理
   - 目录选择失败时显示错误提示
   - API Key验证失败时显示具体错误信息
   - 保存配置失败时显示错误原因

4. 界面交互
   - 所有输入框和按钮都支持键盘操作
   - Tab键可以在各个控件间切换
   - Esc键可以关闭设置窗口
   - 保存时检查必填项

