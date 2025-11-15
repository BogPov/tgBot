package com.festena.bot;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class TextManager {
    private static Yaml yaml = new Yaml();
    private static InputStream inputStream;
    private static Map<String, Object> texts;

    // Статический блок для инициализации
    static {
        inputStream = TextManager.class.getClassLoader().getResourceAsStream("texts.yaml");
        texts = yaml.load(inputStream);
    }

    public static String getText(String name) {
        return texts.get(name).toString();
    }
}
