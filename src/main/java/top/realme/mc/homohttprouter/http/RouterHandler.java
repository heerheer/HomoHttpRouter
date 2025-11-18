package top.realme.mc.homohttprouter.http;

import okhttp3.Request;
import okhttp3.Response;

public interface RouterHandler {
    Response handle(Request request) throws Exception;
}