package com.festena.bot;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class TextManager {
    private Yaml yaml = new Yaml();
    private InputStream inputStream;
    private Map<String, Object> texts;

    public TextManager() {
        inputStream = TextManager.class.getClassLoader().getResourceAsStream("texts.yaml");
        texts = yaml.load(inputStream);
    }

    public String getText(String name) {
        return texts.get(name).toString();
    }
}
