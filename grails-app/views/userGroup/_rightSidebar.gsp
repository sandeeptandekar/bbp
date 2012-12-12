
<div class="sidebar right-sidebar span3">
	<div class="sidebar_section" style="left: 0px">
		<h5><i class=" icon-bookmark"></i>Bookmarks</h5>
		
		<ul class="nav block-tagadelic">
			<g:each in="${pages}" var="newsletterInstance">

				<li><g:if test="${userGroupInstance}">
						<a
							href="${uGroup.createLink('mapping':'userGroup', 'action':'page', 'id':newsletterInstance.id, 'userGroup':userGroupInstance) }">
							${fieldValue(bean: newsletterInstance, field: "title")} </a>
					</g:if>
					<g:else>
						<a
							href="${uGroup.createLink(controller:'userGroup', action:'page', id:newsletterInstance.id) }">
							${fieldValue(bean: newsletterInstance, field: "title")} </a>
					</g:else></li>
			</g:each>
			<li><g:if test="${userGroupInstance}">
						<a
							href="${uGroup.createLink('mapping':'userGroup', action:"pages", 'userGroup':userGroupInstance)}">Pages </a>
				</g:if> <g:else>
					<g:link url="${uGroup.createLink(controller:'userGroup', action:'pages')}">Pages</g:link>
				</g:else>
			</li>
		</ul>
	</div>
	<div class="tags_section sidebar_section">
<%--		<g:if test="${params.action == 'search' }">--%>
<%--			<obv:showAllTags--%>
<%--				model="['tags':tags , 'count':tags?tags.size():0, 'isAjaxLoad':true, 'context':userGroupInstance]" />--%>
<%--		</g:if>--%>
<%--		<g:else>--%>
			<obv:showAllTags
				model="['tagFilterByProperty':'All' , 'params':params, 'isAjaxLoad':true, 'context':userGroupInstance]" />
<%--		</g:else>--%>
	</div>
</div>
