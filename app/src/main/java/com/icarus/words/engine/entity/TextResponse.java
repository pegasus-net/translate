package com.icarus.words.engine.entity;

import java.util.List;

public class TextResponse {
    private int error_code;
    private String error_msg;
    private String from;
    private String to;
    private List<Data> trans_result;

    public boolean isSuccess() {
        return error_code == 0;
    }

    public String getSrc() {
        if (null != trans_result) {
            StringBuilder builder = new StringBuilder();
            for (Data d : trans_result) {
                builder.append(d.src);
            }
            return builder.toString();
        }
        return "";
    }

    public int getError() {
        return error_code;
    }

    public String getErrorMsg() {
        return error_msg;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getDst() {
        if (null != trans_result) {
            StringBuilder builder = new StringBuilder();
            for (Data d : trans_result) {
                builder.append(d.dst);
            }
            return builder.toString();
        }
        return "";
    }

    private static class Data {
        public String src;
        public String dst;
    }

}
