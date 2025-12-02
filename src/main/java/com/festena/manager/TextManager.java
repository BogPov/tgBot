package com.festena.manager;

import org.yaml.snakeyaml.Yaml;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
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