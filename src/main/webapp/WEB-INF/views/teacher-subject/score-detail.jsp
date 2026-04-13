<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="scoreSidebarPath" scope="request" value="/WEB-INF/views/teacher-subject/_sidebar.jsp"/>
<c:set var="scoreListUrl" scope="request" value="/teacher-subject/score"/>
<c:set var="scoreDetailUrl" scope="request" value="/teacher-subject/score/detail"/>

<jsp:include page="/WEB-INF/views/teacher/score-detail.jsp"/>
