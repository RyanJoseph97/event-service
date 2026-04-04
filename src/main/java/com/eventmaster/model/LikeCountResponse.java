package com.eventmaster.model;

public class LikeCountResponse {
    private long count;

    public LikeCountResponse(long count) {
        this.count = count;
    }

    public long getCount() { return count; }
}
