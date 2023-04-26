package com.kisro.cloud.controller;

import com.alibaba.fastjson.JSONObject;
import com.kisro.cloud.pojo.Vehicle;
import com.kisro.cloud.pojo.vo.SearchTimeRequest;
import com.kisro.cloud.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.ParsedCardinality;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.bucketsort.BucketSortPipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kisro
 * @since 2022/11/4
 **/
@RestController
@RequestMapping("/biz")
@Slf4j
public class BizController {
    @Resource
    private RestHighLevelClient client;
    @Resource
    private ElasticsearchRestTemplate template;

    @Resource
    private VehicleService vehicleService;
    private static final List<Vehicle> list = new ArrayList<>(10000);


    @PostMapping("/findByBetweenDate")
    public void findByBetweenDate(@RequestBody SearchTimeRequest request) throws IOException {
        // index name = trace_db_v2
        // start-> 1666602720000
        // end -> 1666604400000
        // total : 5939431条数据
        SearchRequest searchRequest = new SearchRequest("trace_db_v2");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.rangeQuery("receiveTime").gte(1666602720000L).lte(1666604400000L))
                .from(0)
                .size(500);
        searchRequest.source(sourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        System.out.println("Total = " + response.getHits().getTotalHits());
        System.out.println("hits.length = " + hits.length);
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }


    }

    @GetMapping("/findDistinct")
    public void findDistinct() throws IOException {
        // todo 1. 查询结果 底盘号去重？ 2. 数据导出问题？ 500+w 数据 3. 只搜索底盘号字段
        SearchRequest request = new SearchRequest("trace_db_v2");
        // 构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //指定去重字段
        CollapseBuilder collapseBuilder = new CollapseBuilder("chassisNumber");
//        CollapseBuilder collapseBuilder = new CollapseBuilder("name.keyword");
        // 查询去重后的结果数量
        CardinalityAggregationBuilder aggregationBuilder = AggregationBuilders.cardinality("chassisNumber").field("chassisNumber");
        // 去重后的内容
        TopHitsAggregationBuilder topHitsBuilder = AggregationBuilders.topHits("chassis_top")
                .sort("receiveTime", SortOrder.DESC);
//        CardinalityAggregationBuilder aggregationBuilder = AggregationBuilders.cardinality("name").field("name.keyword");
        sourceBuilder.query(QueryBuilders
                .rangeQuery("receiveTime")
                .gte(1666602720000L)
//                .lte(1666604400000L))
                .lte(1666603200000L))
                .collapse(collapseBuilder)
                .aggregation(aggregationBuilder)
                .aggregation(topHitsBuilder)
                .from(0)
                .size(500);
        request.source(sourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
//        System.out.println(JSON.toJSONString(response.getAggregations()));
        SearchHits hits = response.getHits();
        System.out.println("TotalHits = " + hits.getTotalHits());
        System.out.println(hits.getHits().length);
//        System.out.println(JSON.toJSONString(hits));
        for (SearchHit hit : hits) {
//            System.out.println(JSON.toJSONString(hit.getSource()));
            System.out.println(hit.getSourceAsString());
        }
    }

    @GetMapping("/findFieldDistinct/{from}")
    public void findFieldDistinct(@PathVariable("from") int from) throws IOException {
        SearchRequest request = new SearchRequest("trace_db_v2");
        // 构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //指定去重字段
        CollapseBuilder collapseBuilder = new CollapseBuilder("chassisNumber");
        // 查询去重后的结果数量
        CardinalityAggregationBuilder cardinalityAggregation = AggregationBuilders.
                cardinality("chassisNumber_count") // tag
                .field("chassisNumber"); // 去重字段
        sourceBuilder.collapse(collapseBuilder);
        sourceBuilder.aggregation(cardinalityAggregation);
        // 去重后的内容
        TopHitsAggregationBuilder topHitsBuilder = AggregationBuilders.topHits("chassis_top")
                .fetchSource(new String[]{"chassisNumber"}, new String[]{})
                .sort("receiveTime", SortOrder.DESC)
                .size(1); // 重复数据只返回一条
        // 聚合去重分页
        TermsAggregationBuilder termAggregationBuilder = AggregationBuilders
                .terms("distinct_agg") // tag
                .field("chassisNumber")
                .subAggregation(topHitsBuilder)
                .size(26000);// 最大返回条数
        termAggregationBuilder.subAggregation(
                new BucketSortPipelineAggregationBuilder("bucket_field", null)
                        .from(from) // 分页
                        .size(10000));

        sourceBuilder.aggregation(termAggregationBuilder);
        // 范围查询
        sourceBuilder.query(QueryBuilders
                .rangeQuery("receiveTime")
                .gte(1666602720000L)
                .lte(1666604400000L))
//                .lte(1666603200000L))
//                .sort("receiveTime", SortOrder.DESC)
                .size(0); // 不返回数据
//                .from(0)
//                .size(1000);
        request.source(sourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        System.out.println("TotalHits = " + hits.getTotalHits());
        ParsedCardinality chassisNumberCount = response.getAggregations().get("chassisNumber_count");
        System.out.println("chassisNumber.getValueAsString() = " + chassisNumberCount.getValueAsString());
        Terms terms = response.getAggregations().get("distinct_agg");
//        terms.getBuckets().get(0).getAggregations().get("chassis_top")
        List<? extends Terms.Bucket> buckets = terms.getBuckets();

        for (Terms.Bucket bucket : buckets) {
            TopHits topHits = bucket.getAggregations().get("chassis_top");
            SearchHit[] searchHits = topHits.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                String source = searchHit.getSourceAsString();
                Vehicle vehicle = JSONObject.parseObject(source, Vehicle.class);
                if (vehicle != null) {
                    list.add(vehicle);
                }
            }
        }
        vehicleService.batchInsert(list);
        list.clear();
    }


}
