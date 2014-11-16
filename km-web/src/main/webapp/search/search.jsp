<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="../include/header.jsp" %>
<link href="facet.css" rel="stylesheet" />
<link href="post.css" rel="stylesheet" />

<div align="center">
    <select id="sltQueryType">
        <option value="content" selected>Content</option>
        <option value="title">Title</option>
        <option value="poster">Poster</option>
    </select>
    <input type="text" id="txtKeyword" size="50" value="" />
    <label><input type="checkbox" id="chkGroupByThread" checked />Group result by thread</label>
</div>
<div align="center">
    Post Date: <input type="text" id="txtDateFrom" value="" size="8" /> -
    <input type="text" id="txtDateTo" value="" size="8" />
    <button type="button" id="btnSearch">Search</button>
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
        <td>
            <div>
                <span id="spSearchResult"></span>
                <select id="sltSort">
                    <option value="1">Date Newest</option>
                    <option value="2" selected="true">Relevance</option>
                    <option value="3">Date Oldest</option>
                </select>
            </div>
            <div id="divPageUp"></div>
            <div id="divPosts"></div>
            <div id="divPageDown"></div>
        </td>
    </tr>
</table>

<script type="text/javascript" defer="true" src="./search.js"></script>
<script type="text/javascript" defer="true" src="./page_widget.js"></script>
<script type="text/javascript" defer="true" src="./post_widget.js"></script>
<script type="text/javascript" defer="true" src="./facet_widget.js"></script>
<script type="text/javascript" defer="true" src="./facet_dialog.js"></script>
<script type="text/javascript" defer="true" src="./facet_summary.js"></script>
<%@include file="../include/footer.jsp" %>
