
<%@ page import="content.Project"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'project.label', default: 'Project')}" />
<title><g:message code="default.list.label" args="[entityName]" /></title>
<r:require modules="content_view" />

<style>
<!--
.project-list .odd {
	background-color: ghostwhite;
}

-->
.item {
	border-top: 5px solid #c2c2c2;
	border-bottom: 2px solid #c2c2c2;
	background-color: #ffffff;
	width: 590px;
}

.project-list-item {
	margin: 20px;
}
</style>

</head>
<body>

	<div class="span12">
		<g:render template="/project/projectSubMenuTemplate"
			model="['entityName':'Western Ghats CEPF Projects']" />
		<uGroup:rightSidebar />
		<div class="span8 right-shadow-box"
			style="margin: 0px; padding-right: 5px;">
			<g:render template="/project/search" model="['params':params]" />
			
			<obv:showObservationFilterMessage />
			
			<g:render template="/project/projectListTemplate" />

		</div>
		<g:render template="/project/projectSidebar" />
	</div>
</body>
</html>