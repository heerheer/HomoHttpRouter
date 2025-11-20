package top.realme.mc.homohttprouter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathTemplateParser {

    // 把 <name> 或 [name] 提取成组名
    private static final Pattern TOKEN_PATTERN = Pattern.compile("<([a-zA-Z_][a-zA-Z0-9_]*)>|\\[([a-zA-Z_][a-zA-Z0-9_]*)]");

    public static Map<String, String> parse(String template, String path) {
        // 1. 把模板转换成 Regex
        StringBuilder regexBuilder = new StringBuilder();
        List<String> fieldNames = new ArrayList<>();

        Matcher matcher = TOKEN_PATTERN.matcher(template);
        int last = 0;

        while (matcher.find()) {
            regexBuilder.append(Pattern.quote(template.substring(last, matcher.start())));

            String name = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            fieldNames.add(name);

            regexBuilder.append("([^/]+)");  // 捕获一个 path 段
            last = matcher.end();
        }

        // 添加剩余普通部分
        regexBuilder.append(Pattern.quote(template.substring(last)));

        Pattern pattern = Pattern.compile("^" + regexBuilder + "$");
        Matcher m = pattern.matcher(path);

        Map<String, String> result = new HashMap<>();
        if (m.matches()) {
            for (int i = 0; i < fieldNames.size(); i++) {
                result.put(fieldNames.get(i), m.group(i + 1));
            }
        }

        return result;
    }
}