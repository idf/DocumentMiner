<%@page import="java.util.LinkedHashMap"%>
<%@page import="java.util.Map"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    Map<String, String> links = new LinkedHashMap<String, String>();
    links.put("Home", "/");
    links.put("Search", "/search/search.jsp");
    links.put("Statistics", "/search/stat.jsp");
    links.put("KM (v2)", "/km_v2/");
    
    String root = request.getContextPath();
    String filename = request.getRequestURI().toString();
    filename = filename.substring(root.length());
%>

<script type="text/javascript">
var _root = "<%=root%>";
</script>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Knowledge Miner</title>
        <link href="<%=root%>/css/main.css" rel="stylesheet" />
        <link href="<%=root%>/lib/jquery-ui-1.11.0/jquery-ui.css" rel="stylesheet">
    </head>
    <body>

        <table width="100%" class="top_container">
            <tr>
                <td width="400">
                    <div class="logo">Knowledge Miner</div>
                </td>
                <td valign="bottom" class="top_menu">
                    <%
                        String style;
                        for (Map.Entry<String, String> entry: links.entrySet()) {
                            String text = entry.getKey();
                            String url = entry.getValue();
                            style = "";
                            if (url.equals(filename)) {
                                style = " class='active'";
                            }
                            url = root + url;
                            out.println("<a href='" + url + "' " + style + ">" + text + "</a>");
                        }
                    %>
                </td>
            </tr>
        </table>

        <div class="container">