package com.eventmaster.model;

public class RsvpSummaryResponse {
    private long going;
    private long interested;
    private long notGoing;

    public RsvpSummaryResponse(long going, long interested, long notGoing) {
        this.going = going;
        this.interested = interested;
        this.notGoing = notGoing;
    }

    public long getGoing() { return going; }
    public long getInterested() { return interested; }
    public long getNotGoing() { return notGoing; }
}
