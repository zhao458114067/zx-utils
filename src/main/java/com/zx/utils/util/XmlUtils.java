package com.zx.utils.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author KuiChi
 * @date 2023/4/29 22:29
 */
public class XmlUtils {
    /**
     * 将xml字符串转化为javaBean对象
     * @param xmlString
     * @param tClass
     * @return
     * @param <T>
     */
    public static <T> T xmlToJavaBean(String xmlString, Class<T> tClass) {
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(tClass).createUnmarshaller();
            StringReader stringReader = new StringReader(xmlString);
            return (T) unmarshaller.unmarshal(stringReader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将javaBean对象转化为xml字符串
     * @param t
     * @return
     * @param <T>
     */
    public static <T> String javaBeanToXmlString(T t) {
        try {
            Marshaller marshaller = JAXBContext.newInstance(t.getClass()).createMarshaller();
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(t, stringWriter);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            return stringWriter.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
