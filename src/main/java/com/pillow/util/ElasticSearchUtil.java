package com.pillow.util;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.ws.Action;
import java.io.IOException;

/**
 * @author wujt
 * @ClassName ElasticSearchUtil
 * @Description 类的说明
 * @date 2023/3/15
 */
@Component
public class ElasticSearchUtil {

    private static RestHighLevelClient client;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @PostConstruct
    public void init(){
        ElasticSearchUtil.client = restHighLevelClient;
    }

    public static void createIndices(String index) throws IOException {
        //创建索引请求
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
        //客户端执行请求
        CreateIndexResponse response = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);

    }


}
