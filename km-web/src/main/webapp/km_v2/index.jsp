<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Knowledge Miner</title>
        <link href="css/main.css" rel="stylesheet">
        <link href="css/top_bar.css" rel="stylesheet">
        <link href="css/menu.css" rel="stylesheet">
        <link href="css/dropdown.css" rel="stylesheet">
        <link href="css/popup.css" rel="stylesheet">
        <link href="css/posts.css" rel="stylesheet">
        <link href="css/chart.css" rel="stylesheet">
        <link href="css/pagination.css" rel="stylesheet">
        <link href="css/bootstrap.css" rel="stylesheet">
    </head>
    <body>
        <div class="top_bar_panel">
            <div class="top_bar_logo">Knowledge Miner</div>
            <div class="top_bar_keyword_panel">
                <input type="text" class="top_bar_input_keyword" id="txtQuery" />
            </div>
            <div class="top_bar_search_panel">
                <button class="top_bar_button_search" id="btnSearch">Search</button>
            </div>
        </div>

        <div class="menu_panel" id="topMenu"></div>
        <div class="dd_panel" id="topMenuDropdown"></div>
        <div class="pp_panel" id="topMenuPopup"></div>

        <div class="hr"></div>

        <div class="result"></div>

        <table>
            <tr valign="top">
                <td>
                    <div id="posts"></div>
                    <div id="pagination"></div>
                </td>
                <td>
                    <div class="chart"><div id="divChartMonth"></div></div>
                    <div class="chart"><div id="divChartYear"></div></div>
                    <div class="chart"><div id="divChartTopic"></div></div>
                    <div class="chart"><div id="divChartForum"></div></div>
                    <div class="chart"><div id="divChartThread"></div></div>
                    <div class="chart"><div id="divChartPoster"></div></div>
                </td>
            </tr>
        </table>

        <div class="hr"></div>
        <div class="footer">&nbsp;</div>

        <script type="text/javascript" src="../js/lib/jquery-2.1.0.js"></script>
        <script type="text/javascript" src="../js/lib/moment.js"></script>
        <script type="text/javascript" src="../js/lib/notify.js"></script>
        <script type="text/javascript" src="../js/lib/canvasjs-1.5.0-beta/source/canvasjs.js"></script>
        <script type="text/javascript" src="../js/jquery-ext.js"></script>
        <script type="text/javascript" src="../js/commons-util-js/Number.js"></script>
        <script type="text/javascript" src="../js/commons-util-js/String.js"></script>
        <script type="text/javascript" src="../js/commons-util-js/DomUtils.js"></script>
        <script type="text/javascript" src="../js/km_v2/main.js"></script>
        <script type="text/javascript" src="../js/km_v2/menu.js"></script>
        <script type="text/javascript" src="../js/km_v2/dropdown.js"></script>
        <script type="text/javascript" src="../js/km_v2/popup.js"></script>
        <script type="text/javascript" src="../js/km_v2/post.js"></script>
        <script type="text/javascript" src="../js/km_v2/pagination.js"></script>
        <script type="text/javascript" src="../js/km_v2/chart.js"></script>
    </body>
</html>
