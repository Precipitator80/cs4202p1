package com.rainlandsociety;

import com.alibaba.fastjson2.annotation.JSONField;

public class Cache {
    // enum Kind {
    //     DIRECT,
    //     FULL,
    //     TWO_WAY,
    //     FOUR_WAY,
    //     EIGHT_WAY
    // }

    // enum ReplacementPolicy {
    //     RR,
    //     LRU,
    //     LFU
    // }

    @JSONField(name = "name")
    public String name;

    @JSONField(name = "size")
    public int size;

    @JSONField(name = "line_size")
    public int line_size;

    @JSONField(name = "kind")
    public String kind;

    @JSONField(name = "replacement_policy")
    public String replacement_policy;

    @JSONField(name = "hits")
    public int hits;

    @JSONField(name = "misses")
    public int misses;
}