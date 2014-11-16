<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="../include/header.jsp" %>
<link href="facet.css" rel="stylesheet" />
<link href="chart.css" rel="stylesheet" />

<div align="center">
    <select id="sltQueryType">
        <option value="content" selected>Content</option>
        <option value="title">Title</option>
        <option value="poster">Poster</option>
    </select>
    <input type="text" id="txtKeyword" size="50" value="" />
</div>
<div align="center">
    Post Date: <input type="text" id="txtDateFrom" value="" size="8" /> -
    <input type="text" id="txtDateTo" value="" size="8" />
    <button type="button" id="btnSearch">Search</button>
    <button type="button" id="btnClearAll">Clear All</button>
</div>

<form id="frmSearch">
    <input type="hidden" name="queryType" value="content" />
    <input type="hidden" name="keyword" value="" />
    <input type="hidden" name="dateFrom" value="" />
    <input type="hidden" name="dateTo" value="" />
    <input type="hidden" name="pageSize" value="25" />
    <input type="hidden" name="page" value="1" />
    <input type="hidden" name="sortType" value="2" />
    <input type="hidden" name="forumId" value="" />
    <input type="hidden" name="threadId" value="" />
    <input type="hidden" name="postYear" value="" />
    <input type="hidden" name="postMonth" value="" />
    <input type="hidden" name="poster" value="" />
    <input type="hidden" name="topicId" value="" />
</form>

<table cellpadding="2" cellspacing="2">
    <tr valign="top">
        <td width="240">
            <div><b>Current Filters</b> <button id="btnReset">Reset</button></div>
            <div id="divFilterSummary" class="facets"></div>
            <div id="divPostMonths" class="facets"></div>
            <div id="divPostYears" class="facets"></div>
            <div id="divTopics" class="facets"></div>
            <div id="divForums" class="facets"></div>
            <div id="divThreads" class="facets"></div>
            <div id="divPosters" class="facets"></div>
            <div id="divFacetDialog" class="facets"></div>
        </td>
        <td class="canvas">
            <div class="chart"><div id="divChartMonth"></div></div>
            <div class="chart"><div id="divChartYear"></div></div>
            <div class="chart"><div id="divChartTopic"></div></div>
            <div class="chart"><div id="divChartForum"></div></div>
            <div class="chart"><div id="divChartThread"></div></div>
            <div class="chart"><div id="divChartPoster"></div></div>
        </td>
    </tr>
</table>

<script type="text/javascript" defer="true" src="./stat.js"></script>
<script type="text/javascript" defer="true" src="./page_widget.js"></script>
<script type="text/javascript" defer="true" src="./post_widget.js"></script>
<script type="text/javascript" defer="true" src="./facet_widget.js"></script>
<script type="text/javascript" defer="true" src="./facet_dialog.js"></script>
<script type="text/javascript" defer="true" src="./facet_summary.js"></script>
<script type="text/javascript" defer="true" src="../lib/canvasjs-1.5.0-beta/source/canvasjs.js"></script>
<script type="text/javascript" defer="true" src="./chart.js"></script>
<%@include file="../include/footer.jsp" %>
