package com.example.common.util;

import android.text.TextUtils;
import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class XMLParser {
    public final static String TAG = "XMLParser";
    public Map<String,Boolean> xmlMap = new ConcurrentHashMap<>();
    public static String mKey;

    public String parser(String file) {
        Log.i(TAG, "parser file:" + file);
        InputStream inputStream;
        File f = new File(file);
        try {
            inputStream = new FileInputStream(f);//与根据File类对象的所代表的实际文件建立链接创建fileInputStream对象
            parser(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String parser(InputStream inputStream) {
        Log.i(TAG, "parser");
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            if (inputStream != null) {
                saxParser.parse(inputStream, new XMLHandler());
            } else {
                Log.i(TAG, "inputStream = null");
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //把key对应的元素下，所有的子元素保存到一个map里（xmlMap）
    public String parser(InputStream inputStream, String key) {
        Log.i(TAG, "parser file:" + inputStream + ", key:" + key);
        mKey = key;
        String s = parser(inputStream);
        return "";
    }

    public String getXMLString(){
        for (Map.Entry<String, Boolean> entry:xmlMap.entrySet()){
            Log.i(TAG,"key:"+entry.getKey());
            Log.i(TAG,"value:"+entry.getValue());
        }
        return "";
    }

    class XMLHandler extends DefaultHandler {
        StringBuilder stringBuilder = new StringBuilder();
        boolean canParse = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
//            Log.i(TAG, "startElement uri" + uri + ", localName:" + localName + ", qName:" + qName + ", attributes:" + attributes);
            //找到对应key，开始读里面的元素
            if (localName.equals(mKey)) {
                canParse = true;
                stringBuilder.setLength(0);
            }
            if (canParse) {
                //没过一个子项，就清空一下
                stringBuilder.setLength(0);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (localName.equals(mKey)) {
                canParse = false;
            }
            if (canParse) {
                String elementValue = stringBuilder.toString();
//                Log.i(TAG, "endElement localName:" + localName + " elementValue:" + elementValue);
                //没一个子项结束符，把该子项及对应值保存到map里
                if (!TextUtils.isEmpty(elementValue)) {
                    xmlMap.put(localName,Boolean.valueOf(elementValue));
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            if (canParse) {
                //只有在对应key下，才把元素值取出来
                stringBuilder.append(ch, start, length);
            }

        }
    }
}
