package com.rainlandsociety;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;

public class JsonHandler {
    public class CacheConfiguration {
        @JSONField(name = "caches")
        List<Cache> caches;

        public List<Cache> getCaches() {
            return caches;
        }

        public void setCaches(List<Cache> caches) {
            this.caches = caches;
        }
    }

    public static List<Cache> readConfiguration(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String jsonString = readAllLinesWithStream(reader);
            System.out.println(jsonString);
            CacheConfiguration cacheConfiguration = JSON.parseObject(jsonString, CacheConfiguration.class);
            return cacheConfiguration.getCaches();
        }
    }

    static String readAllLinesWithStream(BufferedReader reader) {
        return reader.lines()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public static void printResults(CacheSimulator simulator) {
        String jsonObject = JSON.toJSONString(simulator);
        System.out.println(jsonObject);
    }
}