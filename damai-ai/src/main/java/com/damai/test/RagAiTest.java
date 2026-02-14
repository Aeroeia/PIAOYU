package com.damai.test;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RagAiTest implements CommandLineRunner {
    
    @Autowired
    private VectorStore vectorStore;


    @Override
    public void run(String... args) throws Exception {
        //搜索条件
        SearchRequest request = SearchRequest.builder()
                .query("退票政策")
                .topK(1)
                .similarityThreshold(0.6)
                .similarityThresholdAll()
                .build();
        //查询
        List<Document> docs = vectorStore.similaritySearch(request);
        if (CollectionUtil.isEmpty(docs)) {
            log.info("====没有搜索到任何内容===");
            return;
        }
        log.info("====搜索到内容了===");
        for (Document doc : docs) {
            log.info(doc.getId());
            log.info(String.valueOf(doc.getScore()));
            log.info(doc.getText());
        }
    }
}