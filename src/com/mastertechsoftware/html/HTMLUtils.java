package com.mastertechsoftware.html;

import com.mastertechsoftware.list.KeyValue;
import com.mastertechsoftware.list.ListMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Kevin
 * Date: Mar 26, 2010
 */
public class HTMLUtils {
    private static ListMap<String, String> htmlCodeMap;
    private static List<String> htmlMap;

    public static String stripHTML(String html) {

        if (html == null || html.length() == 0) {
            return html;
        }
        // 1st do a quick search to see if any html exists
        Pattern htmlPattern = Pattern.compile("<(\\?|/?)[a-zA-Z]{1,6}[^>]*/?>", Pattern.DOTALL);
        Matcher matcher = htmlPattern.matcher(html);
        if (html.length() == 0 || !matcher.find()) {
            return html;
        }
        if (htmlMap == null) {
            populateHTMLMap();
        }
        Iterator<String> iterator = htmlMap.iterator();
        while (iterator.hasNext()) {
            String htmlCode = iterator.next();
            // <([^>\s]+?)(?:\s+([^>]*))??>(.*?)(</\1>)
            htmlPattern = Pattern.compile("<(\\?|/?)" + htmlCode + "[^>]*/?>", Pattern.DOTALL);
            matcher = htmlPattern.matcher(html);
            if (html.length() > 0 && matcher.find()) {
                html = matcher.replaceAll("");
            }
        }
        return html;

    }

    public static String convertHTMLCodes(String html) {

        if (html == null || html.length() == 0) {
            return html;
        }

        // 1st do a quick search to see if any html exists
        Pattern htmlPattern = Pattern.compile("&#?[^;]{1,5};", Pattern.DOTALL);
        Matcher matcher = htmlPattern.matcher(html);
        if (html.length() == 0 || !matcher.find()) {
            return html;
        }
        if (htmlCodeMap == null) {
            populateCodeMap();
        }
        Iterator<KeyValue<String,String>> iterator = htmlCodeMap.iterator();
        while (iterator.hasNext()) {
            KeyValue<String, String> keyValue = iterator.next();
            String htmlEntity = keyValue.getKey();
            String value = keyValue.getValue();
            htmlPattern = Pattern.compile(htmlEntity, Pattern.DOTALL);
            matcher = htmlPattern.matcher(html);
            if (html.length() > 0 && matcher.find()) {
                html = matcher.replaceAll(value);
            }
        }
        return html;
    }

    private static void populateCodeMap() {
        htmlCodeMap = new ListMap<String, String>();
//        htmlCodeMap.add("&quot;", "\"");
        htmlCodeMap.add("&amp;", "&");
        htmlCodeMap.add("&amp;", "&"); // Need to do this 2x
        htmlCodeMap.add("&lt;", "<");
        htmlCodeMap.add("&gt;", ">");
//        htmlCodeMap.add("&lsquo;", "&quot;");
//        htmlCodeMap.add("&tilde;", "~");
//        htmlCodeMap.add("&nbsp;", " ");
        htmlCodeMap.add("%", "&#37;");
        htmlCodeMap.add("\\?", "&#63;");
//        htmlCodeMap.add("&#8211;", "-");
//        htmlCodeMap.add("&#8212;", "-");
//        htmlCodeMap.add("&#8216;", "&quot;");
//        htmlCodeMap.add("&#8217;", "&quot;");
//        htmlCodeMap.add("&#8218;", "&quot;");
//        htmlCodeMap.add("&#8220;", "\"");
//        htmlCodeMap.add("&#8221;", "\"");
//        htmlCodeMap.add("&#8222;", "\"");
//        htmlCodeMap.add("&#8230;", "...");
    }

    private static void populateHTMLMap() {
        htmlMap = new ArrayList<String>();
        htmlMap.add("html");
        htmlMap.add("body");
        htmlMap.add("a");
        htmlMap.add("div");
        htmlMap.add("p");
        htmlMap.add("head");
        htmlMap.add("b");
        htmlMap.add("br");
        htmlMap.add("button");
        htmlMap.add("caption");
        htmlMap.add("center");
        htmlMap.add("cite");
        htmlMap.add("code");
        htmlMap.add("col");
        htmlMap.add("colgroup");
        htmlMap.add("font");
        htmlMap.add("form");
        htmlMap.add("frame");
        htmlMap.add("frameset");
        htmlMap.add("h1");
        htmlMap.add("h2");
        htmlMap.add("h3");
        htmlMap.add("h3");
        htmlMap.add("h4");
        htmlMap.add("h5");
        htmlMap.add("h6");
        htmlMap.add("hr");
        htmlMap.add("img");
        htmlMap.add("input");
        htmlMap.add("label");
        htmlMap.add("li");
        htmlMap.add("link");
        htmlMap.add("map");
        htmlMap.add("meta");
        htmlMap.add("noframes");
        htmlMap.add("object");
        htmlMap.add("ol");
        htmlMap.add("option");
        htmlMap.add("pre");
        htmlMap.add("script");
        htmlMap.add("select");
        htmlMap.add("span");
        htmlMap.add("strong");
        htmlMap.add("style");
        htmlMap.add("table");
        htmlMap.add("tbody");
        htmlMap.add("td");
        htmlMap.add("textarea");
        htmlMap.add("tfoot");
        htmlMap.add("th");
        htmlMap.add("thead");
        htmlMap.add("title");
        htmlMap.add("tr");
        htmlMap.add("ul");
    }
}
