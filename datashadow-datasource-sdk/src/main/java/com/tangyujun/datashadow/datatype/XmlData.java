package com.tangyujun.datashadow.datatype;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.tangyujun.datashadow.exception.DataAccessException;

/**
 * XML数据处理工具类
 * 用于解析和处理XML格式的数据
 */
public class XmlData extends ShadowData {
    /**
     * 从XML文件路径读取并解析数据
     * 首先打开文件输入流,然后调用流解析方法
     * 
     * @param path XML文件的路径
     * @return 解析后的数据列表,每个元素为一个Map表示一行数据
     * @throws DataAccessException 当文件读取失败时抛出此异常
     */
    public static List<Map<String, Object>> getValues(String path) {
        try (InputStream stream = new FileInputStream(path)) {
            return getValues(stream);
        } catch (IOException e) {
            throw new DataAccessException("读取XML文件失败: " + path + ", 原因: " + e.getMessage(), e);
        }
    }

    /**
     * 从输入流解析XML数据
     * 使用DOM解析器将XML输入流解析为Document对象
     * 然后调用Document解析方法获取数据
     * 
     * @param stream XML格式的输入流
     * @return 解析后的数据列表,每个元素为一个Map表示一行数据
     * @throws DataAccessException 当XML解析出错时抛出此异常
     */
    public static List<Map<String, Object>> getValues(InputStream stream) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(stream);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new DataAccessException("XML解析错误: " + e.getMessage(), e);
        }
        return getValues(document);
    }

    /**
     * 解析XML文档数据
     * 将XML文档解析为数据列表,每个子元素作为一行数据
     * 子元素的各个子节点作为该行数据的键值对
     *
     * @param document XML文档对象
     * @return 解析后的数据列表,每个元素为一个Map表示一行数据
     * @throws DataAccessException 当XML解析出错时抛出此异常
     */
    public static List<Map<String, Object>> getValues(Document document) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            // 解析XML文件,标准化文档结构
            document.getDocumentElement().normalize();
            // 获取根元素下的所有子节点
            NodeList nodeList = document.getDocumentElement().getChildNodes();
            // 遍历每个子节点,每个子节点作为一行数据
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    Map<String, Object> rowData = new HashMap<>();

                    // 获取元素的所有子节点,将每个子节点的名称和内容作为键值对
                    NodeList childNodes = element.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node childNode = childNodes.item(j);
                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                            String key = childNode.getNodeName();
                            String value = childNode.getTextContent();
                            rowData.put(key, value);
                        }
                    }
                    // 只添加非空的数据行
                    if (!rowData.isEmpty()) {
                        result.add(rowData);
                    }
                }
            }
            return result;
        } catch (DOMException e) {
            throw new DataAccessException("XML解析错误: " + e.getMessage(), e);
        }
    }

    /**
     * 判断输入字符串是否为XML格式
     * 通过尝试解析输入字符串来判断其是否为合法的XML格式
     * 如果解析成功则说明是XML格式,返回true
     * 如果解析失败则说明不是XML格式,返回false
     * 
     * @param content 需要判断格式的输入字符串
     * @return 如果是合法的XML格式返回true,否则返回false
     */
    public static boolean isXml(String content) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.parse(new InputSource(new StringReader(content)));
            return true;
        } catch (IOException | ParserConfigurationException | SAXException ignored) {
            return false;
        }
    }
}
