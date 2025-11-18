package top.realme.mc.homohttprouter;

import com.alibaba.fastjson2.JSON;

public class Util {
    /**
     * 将任意对象序列化为 JSON 字符串
     * @param obj 任意对象
     * @return JSON 字符串，如果序列化失败则返回 null
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            // 这里可按需要改成日志记录
            return null;
        }
    }
}
