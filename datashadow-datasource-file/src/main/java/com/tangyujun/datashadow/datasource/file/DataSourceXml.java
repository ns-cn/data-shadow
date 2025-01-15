package com.tangyujun.datashadow.datasource.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.tangyujun.datashadow.datasource.DataSourceConfigurationCallback;
import com.tangyujun.datashadow.datasource.DataSourceGenerator;
import com.tangyujun.datashadow.datasource.DataSourceRegistry;
import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import javafx.stage.Window;

/**
 * XML数据源
 * 
 * 支持读取XML格式的结构化数据,将XML文档中的数据转换为标准的行列格式。
 * 
 * XML文件格式要求:
 * 1. 根元素下包含多个相同结构的子元素,每个子元素代表一条数据记录
 * 2. 每条记录的子元素名称作为列名,元素内容作为数据值
 * 
 * 示例XML格式:
 * ┌─────────────────────────┐
 * │ <root> │
 * │ <record> │
 * │ <name>张三</name> │
 * │ <age>20</age> │
 * │ </record> │
 * │ <record> │
 * │ <name>李四</name> │
 * │ <age>30</age> │
 * │ </record> │
 * │ </root> │
 * └─────────────────────────┘
 * 
 * HTML转义格式:
 * &lt;root&gt;
 * &lt;record&gt;
 * &lt;name&gt;张三&lt;/name&gt;
 * &lt;age&gt;20&lt;/age&gt;
 * &lt;/record&gt;
 * &lt;record&gt;
 * &lt;name&gt;李四&lt;/name&gt;
 * &lt;age&gt;30&lt;/age&gt;
 * &lt;/record&gt;
 * &lt;/root&gt;
 */
public class DataSourceXml extends DataSourceFile {

    /**
     * 注册XML数据源生成器
     * 
     * @return 数据源生成器
     */
    @DataSourceRegistry(friendlyName = "XML")
    public static DataSourceGenerator generator() {
        return () -> new DataSourceXml();
    }

    /**
     * 验证XML文件路径是否正确
     * 检查以下内容:
     * 1. 文件路径不能为空
     * 2. 文件必须以.xml结尾
     * 3. 文件必须存在且可读
     * 
     * @throws DataSourceValidException 当XML文件路径格式错误时抛出
     */
    @Override
    public void valid() throws DataSourceValidException {
        if (path == null || path.isBlank()) {
            throw new DataSourceValidException("XML文件路径不能为空", null);
        }

        String lowercasePath = path.toLowerCase();
        if (!lowercasePath.endsWith(".xml")) {
            throw new DataSourceValidException("文件不是XML格式", null);
        }
        // 检查文件是否存在且可读
        File file = new File(path);
        if (!file.exists()) {
            throw new DataSourceValidException("XML文件不存在", null);
        }
        if (!file.canRead()) {
            throw new DataSourceValidException("XML文件无法读取", null);
        }
    }

    /**
     * 从XML文件中获取数据
     * 读取步骤:
     * 1. 创建XML解析器
     * 2. 解析XML文档获取DOM树
     * 3. 遍历根元素下的每个子元素(数据记录)
     * 4. 将每个记录的子元素名称和内容转换为Map
     * 5. 将所有记录添加到结果列表中
     * 
     * @return 查询结果列表,每行数据以Map形式存储,key为列名,value为列值
     * @throws DataAccessException 当XML文件读取失败时抛出,可能的原因包括:
     *                             - XML解析器配置错误
     *                             - XML文件格式错误
     *                             - 文件读取IO异常
     */
    @Override
    public List<Map<String, Object>> getValues() throws DataAccessException {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            // 创建DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // 解析XML文件
            Document document = builder.parse(new File(path));
            document.getDocumentElement().normalize();

            // 获取根元素下的所有子节点
            NodeList nodeList = document.getDocumentElement().getChildNodes();

            // 遍历每个子节点
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    Map<String, Object> rowData = new HashMap<>();

                    // 获取元素的所有子节点
                    NodeList childNodes = element.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node childNode = childNodes.item(j);
                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                            String key = childNode.getNodeName();
                            String value = childNode.getTextContent();
                            rowData.put(key, value);
                        }
                    }

                    if (!rowData.isEmpty()) {
                        result.add(rowData);
                    }
                }
            }

            return result;
        } catch (ParserConfigurationException e) {
            throw new DataAccessException("XML解析器配置错误: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new DataAccessException("XML文件格式错误: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DataAccessException("读取XML文件失败: " + path + ", 原因: " + e.getMessage(), e);
        }
    }

    /**
     * 获取XML文件的列名
     * 通过以下步骤获取列名:
     * 1. 解析XML文档
     * 2. 获取根元素下的第一个子元素(第一条记录)
     * 3. 获取该记录的所有子元素名称作为列名
     * 
     * @return 列名列表,如果读取失败或XML为空则返回空列表
     */
    @Override
    public List<String> getColumns() {
        try {
            // 创建DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // 解析XML文件
            Document document = builder.parse(new File(path));
            document.getDocumentElement().normalize();

            // 获取根元素下的第一个子元素
            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    // 获取第一个元素的所有子节点名称作为列名
                    NodeList childNodes = element.getChildNodes();
                    List<String> columns = new ArrayList<>();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node childNode = childNodes.item(j);
                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                            columns.add(childNode.getNodeName());
                        }
                    }
                    return columns;
                }
            }
            return new ArrayList<>();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            return new ArrayList<>();
        }
    }

    /**
     * 获取数据源的描述信息
     * 用于在界面上显示数据源的基本信息
     * 例如: XML文件: D:/test.xml
     * 
     * @return 数据源的描述信息字符串,如果路径为空则返回空字符串
     */
    @Override
    public String getDescription() {
        if (path == null || path.isBlank()) {
            return "";
        }
        return "XML文件: " + path;
    }

    /**
     * 配置XML文件数据源
     * 
     * @param primaryStage 主窗口
     * @param callback     配置完成后的回调函数
     */
    @Override
    public void configure(Window primaryStage, DataSourceConfigurationCallback callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'configure'");
    }
}
