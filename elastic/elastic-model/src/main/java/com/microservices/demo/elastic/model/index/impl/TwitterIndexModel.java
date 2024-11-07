package com.microservices.demo.elastic.model.index.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microservices.demo.elastic.model.index.IndexModel;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/* To set indexName, we use spring expression language. spel template expression: indexName is parsed with SpelExpressionParser, so
we need to use expression language here. */
@Data
@Builder
@Document(indexName = "#{elasticConfigData.indexName}")
public class TwitterIndexModel implements IndexModel {
    @JsonProperty
    private String id;

    @JsonProperty
    private Long userId;

    @JsonProperty
    private String text;

    /* @JsonFormat: formats the field when converting object to json by using the given pattern.

    @Field: TemporalAccessor properties must have @Field or custom converters. This annotation is required to be able to convert
    createdAt from LocalDateTime type to elasticsearch date during indexing operation.

    Instructor used `DateFormat.custom`.

    Note: for year, we use u instead of y. This is the format for custom elasticsearch pattern for date.

    He used `custom` because we get date from twitterStatus obj in a custom format.*/
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ssZZ")
    @JsonProperty
    private LocalDateTime createdAt;

    // This is not needed, because @Data automatically adds getters and setters to the compiled class.
//    @Override
//    public String getId() {
//        return id;
//    }
}
