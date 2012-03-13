
<div class="snippet grid_4 tablet" style="width:200px;padding:0;">
	<g:set var="mainImage" value="${observationInstance.mainImage()}" />
	<div class="figure"
		style="float: left; max-height: 220px; max-width: 200px">

		<%def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null%>
		<g:link action="show" controller="observation"
			id="${observationInstance.id}">

			<g:if
				test="${imagePath && (new File(grailsApplication.config.speciesPortal.observations.rootDir + imagePath)).exists()}">

				<span class="wrimg"> <span></span> <img
					src="${createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL,
											file: imagePath)}" />
				</span>

			</g:if>
			<g:else>
				<img class="galleryImage"
					src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
					title="You can contribute!!!" />
			</g:else>
		</g:link>
	</div>
	<div class="grid_4" style="width:200px; padding:0; margin:0;">
		<obv:showStoryTablet model="['observationInstance':observationInstance]"></obv:showStoryTablet>
	</div>
</div>
