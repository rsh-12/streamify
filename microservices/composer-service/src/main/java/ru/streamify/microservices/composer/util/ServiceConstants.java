package ru.streamify.microservices.composer.util;

public final class ServiceConstants {

    private ServiceConstants() {}

    public static final String API_SONG_SERVICE = "http://song";
    public static final String API_COMMENT_SERVICE = "http://comment";
    public static final String API_STREAMING_SERVICE = "http://streaming";
    public static final String API_RECOMMENDATION_SERVICE = "http://recommendation";

    public static final String BINDING_SONGS = "songs-out-0";
    public static final String BINDING_COMMENTS = "comments-out-0";
    public static final String BINDING_RECOMMENDATIONS = "recommendations-out-0";
}
