package com.pillow.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author wujt
 * @ClassName ElasticSearchConfiguration
 * @Description 类的说明
 * @date 2023/3/15
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ElasticSearchConfiguration.ESConfigProperties.class)
public class ElasticSearchConfiguration {

    @Bean
    public RestHighLevelClient restHighLevelClient(ESConfigProperties properties) throws IOException {
        String host = properties.host;
        Integer port = properties.port;
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(host,port,"http")));
        initIndices(client,properties.indices);
        return client;
    }

    /**
     * @author: wujt
     * @date: 2023/3/15
     * @Title: initIndices
     * @Description : 初始化索引
     * @param client
     * @param indices
     * @param doc
     * @return void
     */
    private void initIndices(RestHighLevelClient client, String indices) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indices);
        boolean exists = client.indices().exists(getIndexRequest,RequestOptions.DEFAULT);
        if(exists){
            log.info("The indices:{} is exists!",indices);
        }else{
            log.warn("The indices:{} is not exists!Create indices:{}",indices,indices);
            CreateIndexRequest  createIndexRequest = new CreateIndexRequest(indices);
            //客户端执行请求
            client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        }
    }

    @Data
    @ConfigurationProperties(prefix = "es")
    static class ESConfigProperties{
        private String host;
        private Integer port;
        private String indices;
    }
}
