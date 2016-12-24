<%@ page contentType="text/html;charset=UTF-8" language="java" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!-- nav sidebar -->
<aside class="main-sidebar">
  <!-- sidebar: style can be found in sidebar.less -->
  <section class="sidebar">
    <!-- sidebar user panel -->
    <div class="user-panel">
      <div class="pull-left image">
        <img src="${app.cdn}${admin.photo}" class="img-circle" alt="User Image">
      </div>
      <div class="pull-left info">
        <p>${admin.name}</p>
        <a href="#"><i class="fa fa-circle text-success"></i> 在线</a>
      </div>
    </div>
    <!-- search form -->
    <form action="#" method="get" class="sidebar-form">
      <div class="input-group">
        <input type="text" name="q" class="form-control" placeholder="搜索...">
          <span class="input-group-btn">
            <button type="submit" name="search" id="search-btn" class="btn btn-flat"><i class="fa fa-search"></i>
            </button>
          </span>
      </div>
    </form>
    <!-- /.search form -->
    <!-- sidebar menu: : style can be found in sidebar.less -->
    <ul class="sidebar-menu">
      <c:forEach items="${admin.moduleList}" var="grp">
        <c:if test="${grp.pid==1}">
          <c:if test="${grp.type==3}">
            <c:set var="mdl" value="${grp}"/>
            <li <c:if test="${module.href == mdl.href}"> class="active" </c:if> >
              <a  <c:if test="${mdl.href == ''}"> href="javascript:;" </c:if>
                  <c:if test="${mdl.href != ''}"> href="${page_context}${mdl.href}" </c:if>
                  <c:if test="${mdl.dataToggle != ''}"> data-toggle="${mdl.dataToggle}" </c:if> >
                <i class="fa ${mdl.icon}"></i> <span>${mdl.name}</span>

                <c:choose>
                  <c:when test="${grp.name == '总览'}">
                    <small class="label pull-right bg-green">new</small>
                  </c:when>
                  <c:when test="${grp.name == '通知'}">
                    <small class="label pull-right bg-red">3</small>
                  </c:when>
                </c:choose>
              </a>
            </li>
          </c:if>
          <c:if test="${grp.type==2}">
            <li class="treeview <c:forEach items="${admin.moduleList}" var="mdl">
                                  <c:if test="${mdl.pid==grp.id and mdl.type==3}">
                                    <c:if test="${module.href == mdl.href}"> active </c:if>
                                  </c:if>
                                </c:forEach>">
              <a href="javascript:;">
                <i class="fa ${grp.icon}"></i>
                <span>${grp.name}</span>

                <c:choose>
                  <c:when test="${grp.name == '交易管理'}">
                    <span class="label label-primary pull-right">4</span>
                  </c:when>
                  <c:otherwise>
                    <i class="fa fa-angle-left pull-right"></i>
                  </c:otherwise>
                </c:choose>
              </a>
              <ul class="treeview-menu">
                <c:forEach items="${admin.moduleList}" var="mdl">
                  <c:if test="${mdl.pid==grp.id and mdl.type==3}">
                    <li <c:if test="${module.href == mdl.href}"> class="active" </c:if> >
                      <a  <c:if test="${mdl.href == ''}"> href="javascript:;" </c:if>
                          <c:if test="${mdl.href != ''}"> href="${page_context}${mdl.href}" </c:if>
                          <c:if test="${mdl.dataToggle != ''}"> data-toggle="${mdl.dataToggle}" </c:if> >
                        <i class="fa ${mdl.icon}"></i> <span>${mdl.name}</span>
                      </a>
                    </li>
                  </c:if>
                </c:forEach>
              </ul>
            </li>
          </c:if>
        </c:if>
      </c:forEach>
    </ul>
  </section>
  <!-- /.sidebar -->
</aside>
