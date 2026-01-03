package org.spark.ai.gourmetagent.config;


import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class RAGLoader {
    private static final Logger log = LoggerFactory.getLogger(RAGLoader.class);

    @Value("classpath:menu-knowledge.txt")
    private Resource resource;

    private final VectorStore vectorStore;
    private final JdbcClient jdbcClient; // count ?
    public RAGLoader(VectorStore vectorStore, JdbcClient jdbcClient) {
        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
    }

    @PostConstruct
    public void init(){
        try{
            String SQL="select count(*) from gourmet_vector";
            Integer count=jdbcClient.sql(SQL).query(Integer.class).single();
            if(count==0){
                log.info("데이터 로딩을 시작합니다...");
                try(BufferedReader br=new BufferedReader(new InputStreamReader(
                        resource.getInputStream(), StandardCharsets.UTF_8))){
                    List<Document> documents =br.lines().map(Document::new).toList();
                    // 텍스트 분할기(Splitter) 설정(TokenTextSplitter)
                    TokenTextSplitter splitter=new TokenTextSplitter(
                            800, 200, 10, 5000, true);
                    for(Document doc : documents){
                        List<Document> chunks =splitter.split(doc);
                        vectorStore.accept(chunks); // 임베딩->저장
                        log.info(chunks.size() + "개의 청크가 저장되었습니다.");
                        Thread.sleep(200);
                    }
                    log.info("데이터 로딩 완료.");
                }
            }else{
                log.info("데이터가 이미 로드되어 있습니다. (총 " + count + "개)");
            }
        }catch(Exception e){
            log.error("데이터 로딩 중 오류 발생", e);
        }
    }
}
