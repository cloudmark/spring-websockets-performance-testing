package org.cloudmark.samples.performance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Ping {

    @JsonProperty("time")
    private long timeInMillis;
    @JsonProperty("data")
    private String dummyData;

    public Ping(long timeInMillis, final String dummyData){
        this.timeInMillis = timeInMillis;
        this.dummyData = dummyData;

    }
}
