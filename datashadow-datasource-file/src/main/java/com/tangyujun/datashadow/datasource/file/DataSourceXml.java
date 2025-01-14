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

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

/**
 * XML数据源
 */
public class DataSourceXml extends DataSourceFile {

    /**
     * 验证XML文件路径是否正确
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
     * 
     * @return 查询结果列表,每行数据以Map形式存储,key为列名,value为列值
     * @throws DataAccessException 当XML文件读取失败时抛出
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
}
