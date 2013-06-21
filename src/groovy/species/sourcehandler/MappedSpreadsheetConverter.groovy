package species.sourcehandler

import groovy.util.Node;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;

import species.Species;
import species.formatReader.SpreadsheetReader;
import org.apache.log4j.Logger; 
import org.apache.log4j.FileAppender;

class MappedSpreadsheetConverter extends SourceConverter {

	//protected static SourceConverter _instance;
	private static def log = LogFactory.getLog(this);
	def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
	def fieldsConfig = config.speciesPortal.fields
	
	public List<Map> imagesMetaData;
	public List<Map> mappingConfig;
	
	public MappedSpreadsheetConverter() {
		imagesMetaData = [];		
	}

	public List<Species> convertSpecies(String file, String mappingFile, int mappingSheetNo, int mappingHeaderRowNo, int contentSheetNo, int contentHeaderRowNo, int imageMetaDataSheetNo) {
		List<Map> content = SpreadsheetReader.readSpreadSheet(file, contentSheetNo, contentHeaderRowNo);
		mappingConfig = SpreadsheetReader.readSpreadSheet(mappingFile, mappingSheetNo, mappingHeaderRowNo);				
		if(imageMetaDataSheetNo && imageMetaDataSheetNo  >= 0) {
			imagesMetaData = SpreadsheetReader.readSpreadSheet(file, imageMetaDataSheetNo, 0);
		}
		return convertSpecies(content, mappingConfig, imagesMetaData);
	}

//	public List<Species> convertSpecies(List<Map> content, List<Map> mappingConfig, List<Map> imagesMetaData) {
//		List<Species> species = new ArrayList<Species>();
//		
//		XMLConverter converter = new XMLConverter();
//		
//		for(Map speciesContent : content) {
//			Node speciesElement = createSpeciesXML(content, mappingConfig);
//			//log.debug "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
//			//log.debug speciesElement;
//			//log.debug "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
//			Species s = converter.convertSpecies(speciesElement)
//			if(s)
//				species.add(s);
//		}
//		return species;
//	}
	
	public Node createSpeciesXML(Map speciesContent) {
		if(!mappingConfig) {
			log.error "No mapping config";
			return;
		}
		
		NodeBuilder builder = NodeBuilder.newInstance();
		int i=0;
		
			//log.debug speciesContent;
			Node speciesElement = builder.createNode("species");
			for(Map mappedField : mappingConfig) {
				String fieldName = mappedField.get("field name(s)")
				String delimiter = mappedField.get("content delimiter");
				String customFormat = mappedField.get("content format");
println fieldName;
				if(fieldName && (customFormat || speciesContent.get(fieldName.toLowerCase()))) {
println "----"+fieldName;
					fieldName = fieldName.toLowerCase();
					Node field = new Node(speciesElement, "field");
					Node concept = new Node(field, "concept", mappedField.get("concept"));
					Node category = new Node(field, "category", mappedField.get("category"));
					Node subcategory = new Node(field, "subcategory", mappedField.get("subcategory"));
                    println category.text()
                    println field.category.text()
					if (customFormat && mappedField.get("category")?.equalsIgnoreCase("images")) {
						Node images = getImages(imagesMetaData, fieldName, 'images', customFormat, delimiter, speciesContent, speciesElement);
					} else if (customFormat && category.text().equalsIgnoreCase("icons")) {
						Node icons = getImages(imagesMetaData, fieldName, 'icons', customFormat, delimiter, speciesContent, speciesElement);
					} else if (customFormat && category.text().equalsIgnoreCase("audio")) {
						//						Node images = getAudio(fieldName, customFormat, speciesContent);
						//						new Node(speciesElement, audio);
					} else if (customFormat && category.text().equalsIgnoreCase("video")) {
						//						Node images = getVideo(fieldName, customFormat, speciesContent);
						//						new Node(speciesElement, video);
					} else if (concept.text().equalsIgnoreCase((String)fieldsConfig.INFORMATION_LISTING) && field.category.text().equalsIgnoreCase((String)fieldsConfig.REFERENCES)) {
println "----"+fieldName;
                        Node data = createDataNode(field, speciesContent.get(fieldName), speciesContent, mappedField);
						createReferences(data, speciesContent, mappedField);
					} else if(customFormat) {
						String text = getCustomFormattedText(mappedField.get("field name(s)"), customFormat, speciesContent);
						createDataNode(field, text, speciesContent, mappedField);
					} else if(delimiter) {
						String text = speciesContent.get(fieldName);
						if(text) {
							for(String part : text.split(delimiter)) {
								if(part) {
									part = part.trim();
									if(category.text().equalsIgnoreCase("common name")) {
										String[] commonNames = part.split(":");
										if(commonNames.length == 2) {
											commonNames[1].split(",|;").each {
												Node data = createDataNode(field, it, speciesContent, mappedField);
												Node language = new Node(data, "language");
												Node name = new Node(language, "name", commonNames[0]);
											}
										} else {
											commonNames[0].split(",|;").each {
												createDataNode(field, it, speciesContent, mappedField);
											}
										}
									} else {
										createDataNode(field, part, speciesContent, mappedField);
									}
								}
							}
						}
					} else {
						createDataNode(field, speciesContent.get(fieldName), speciesContent, mappedField);
					}
				}
			}
			return speciesElement
	}

	private Node createDataNode(Node field, String text, Map speciesContent, Map mappedField) {
		if(!text) return;

		Node data = new Node(field, "data", text);
		attachMetadata(data, speciesContent, mappedField);
		return data;
	}

	private void attachMetadata(Node data, Map speciesContent, Map mappedField) {

		String contributorFields = mappedField.get("contributor");
		if(contributorFields) {
			contributorFields.split(",").each { contributorField ->
				String contributors = speciesContent.get(contributorField.toLowerCase())
				String delimiter = mappedField.get("content delimiter") ?: "\n";
				contributors?.split(delimiter).each {
					new Node(data, "contributor", it);
				}
			}
		}

		String attributionFields = mappedField.get("attributions");
		if(attributionFields) {
			attributionFields.split(",").each { attributionField ->
				String attribution = speciesContent.get(attributionField.toLowerCase())
				String delimiter = mappedField.get("content delimiter") ?: "\n";
				attribution?.split(delimiter).each {
					new Node(data, "attribution", it);
				}
			}
		}

		String licenseFields = mappedField.get("license");
		if(licenseFields) {
			licenseFields.split(",").each { licenseField ->
				String licenses = speciesContent.get(licenseField.toLowerCase());
				String delimiter = mappedField.get("content delimiter") ?: ",|;|\n";
				licenses?.split(delimiter).each {
					new Node(data, "license", it);
				}
			}
		}

		String audienceTypeFields = mappedField.get("audience");
		if(audienceTypeFields) {
			audienceTypeFields.split(",").each { audienceTypeField ->
				String audience = speciesContent.get(audienceTypeField.toLowerCase());
				String delimiter = mappedField.get("content delimiter") ?: ",|;|\n";
				audience?.split(delimiter).each {
					new Node(data, "audienceType", it);
				}
			}
		}

		String referenceFields = mappedField.get("references");
		if(referenceFields) {
			referenceFields.split(",").each { referenceField ->
				String references = speciesContent.get(referenceField.toLowerCase());
				String delimiter = mappedField.get("content delimiter") ?: "\n";
				references?.split(delimiter).each {
					Node refNode = new Node(data, "reference");
					getReferenceNode(refNode, it);
				}
			}
		}

		String imagesField = mappedField.get("images");
		if(imagesField) {
			imagesField.split(",").each { imageField ->
				String images = speciesContent.get(imageField.toLowerCase());
				if(images) {
					def imagesNode = data;
					imagesNode = new Node(data, "images");
					String delimiter = mappedField.get("content delimiter") ?: "\n|\\s{3,}|,|;";
					images.split(delimiter).each {
						String loc = cleanLoc(it)
						new Node(imagesNode, "image", loc);
					}
				}
			}
		}
		
		String iconsField = mappedField.get("icons");
		if(iconsField) {
			iconsField.split(",").each { iconField ->
				String icons = speciesContent.get(iconField.toLowerCase());
				if(icons) {
					def iconsNode = data;
					iconsNode = new Node(data, "icons");
					String delimiter = mappedField.get("content delimiter") ?: "\n|\\s{3,}|,|;";
					icons.split(delimiter).each {
						String loc = cleanLoc(it)
						new Node(iconsNode, "icon", loc);
					}
				}
			}
		}
		
		String customFormat = mappedField.get("content format");
		if(customFormat) {
		def format = getCustomFormat(customFormat);
		String action = format.get("action") ?:null;
		if(action) {
			new Node(data, "action", action);
		}
		}
	}

	private Map getCustomFormat(String customFormat) {
		return customFormat.split(';').inject([:]) { map, token ->
			token = token.toLowerCase();
			token.split('=').with {
				map[it[0]] = it[1];
			}
			map
		}
	}

	private getCustomFormattedText(String fieldName, String customFormat, Map speciesContent) {
		def result = getCustomFormat(customFormat);
		int group = result.get("group") ? Integer.parseInt(result.get("group")?.toString()) : -1;
		boolean includeHeadings = result.get("includeheadings") ? Boolean.parseBoolean(result.get("includeheadings")?.toString()).booleanValue() : false;
		String con = "";
		fieldName.split(",").eachWithIndex { t, index ->
			String txt = speciesContent?.get(t.toLowerCase().trim());
			if(txt) {
				if (index%group == 0) {

					if(group > 1)
						txt = "<h6>"+txt+"</h6>";
					else {
						txt = "<p>"+txt+"</p>";
						if(includeHeadings) txt = "<h6>"+t.trim()+"</h6>"+txt;
					}
					if(con)
						con += txt;
					else con = txt;

				} else {
					txt = "<p>"+txt+"</p>";
					if(includeHeadings) txt = "<h6>"+t.trim()+"</h6>"+txt;
					con += txt;
				}
			}
		}
		return con;
	}

	private Node getImages(List<Map> imagesMetaData, String fieldName, String fieldType, String customFormat, String delimiter, Map speciesContent, Node speciesElement) {
		Node images = new Node(speciesElement, fieldType);
println customFormat
		def result = getCustomFormat(customFormat);
        println "%%%%%%"+result
		int group = result.get("group") ? Integer.parseInt(result.get("group")?.toString()):-1
		int location = result.get("location") ? Integer.parseInt(result.get("location")?.toString())-1:-1
		int source = result.get("source") ? Integer.parseInt(result.get("source")?.toString())-1:-1
		int caption = result.get("caption") ? Integer.parseInt(result.get("caption")?.toString())-1:-1
		int attribution = result.get("attribution") ? Integer.parseInt(result.get("attribution")?.toString())-1:-1
		int contributor = result.get("contributor") ? Integer.parseInt(result.get("contributor")?.toString())-1:-1
		int license = result.get("license") ? Integer.parseInt(result.get("license")?.toString())-1:-1
		int name = result.get("name") ? Integer.parseInt(result.get("name")?.toString())-1:-1
		boolean incremental = result.get("incremental") ? new Boolean(result.get("incremental")) : false
		String imagesmetadatasheet = result.get("imagesmetadatasheet") ?: null
        
        println "&&&&&imagesmetadatasheet"+imagesmetadatasheet
		if(imagesmetadatasheet && imagesMetaData) {
			//TODO:This is getting repeated for every row in spreadsheet costly
            println fieldName
			fieldName.split(",").eachWithIndex { t, index ->
				String txt = speciesContent.get(t);
                println txt
                println "####"+delimiter
                if(delimiter) {
                    txt.split(delimiter).each { loc ->
                        println 'loc:'+loc
                        if(loc) {
                            createImages(images, loc, imagesMetaData);
                        }
                    }
                } else {
						createImages(images, txt, imagesMetaData);
                }
			}
		} else {
			List<String> groupValues = new ArrayList<String>();
			fieldName.split(",").eachWithIndex { t, index ->
				try{
				String txt = speciesContent.get(t.trim());
				if (index != 0 && index % group == 0) {
					populateImageNode(images, groupValues, delimiter, location, source, caption, attribution, contributor, license, name, incremental);
					groupValues = new ArrayList<String>();
				}
				groupValues.add(txt);
				}catch(e) {
					e.printStackTrace()
				}
			}
			populateImageNode(images, groupValues, delimiter, location, source, caption, attribution, contributor, license, name, incremental);
		}
		return images;
	}

	private void populateImageNode(Node images, List<String> groupValues, String delimiter, int location, int source, int caption, int attribution, int contributor, int license, int name, boolean incremental) {
		if(location != -1 && groupValues.get(location)) {
			String locationStr = groupValues.get(location);
			def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
			String uploadDir = config.speciesPortal.images.uploadDir;
			if(locationStr) {
				if(delimiter) {
					locationStr.split(delimiter).each { loc ->
						createImageNode(images, groupValues, loc, uploadDir, source, caption, attribution, contributor, license, name, incremental);
					}
				} else {
					createImageNode(images, groupValues, locationStr, uploadDir, source, caption, attribution, contributor, license, name, incremental);
				}
			}
		}
	}

	private void createImageNode(Node images, List<String> groupValues, String loc, String uploadDir, int source, int caption, int attribution, int contributor, int license, int name, boolean incremental) {
		String refKey = loc;
		loc = cleanLoc(loc);
		File imagesLocation = new File(uploadDir, loc);
		if(imagesLocation.isDirectory()) {
			imagesLocation.listFiles().eachWithIndex { file, index ->
				Node image = new Node(images, "image");
				new Node(image, "refKey", loc);
				new Node(image, "fileName", file.getAbsolutePath());
				if(source != -1 && groupValues.get(source)) new Node(image, "source", groupValues.get(source));
				if(caption != -1 && groupValues.get(caption)) new Node(image, "caption", groupValues.get(caption));
				if(attribution != -1 && groupValues.get(attribution)) new Node(image, "attribution", groupValues.get(attribution));
				if(contributor != -1 && groupValues.get(contributor)) new Node(image, "contributor", groupValues.get(contributor));
				if(license != -1 && groupValues.get(license)) new Node(image, "license", groupValues.get(license));
			}
		} else if(imagesLocation.exists()){
			Node image = new Node(images, "image");
			new Node(image, "refKey", loc);
			new Node(image, "fileName", imagesLocation.getAbsolutePath());
			if(source != -1 && groupValues.get(source)) new Node(image, "source", groupValues.get(source));
			if(caption != -1 && groupValues.get(caption)) new Node(image, "caption", groupValues.get(caption));
			if(attribution != -1 && groupValues.get(attribution)) new Node(image, "attribution", groupValues.get(attribution));
			if(contributor != -1 && groupValues.get(contributor)) new Node(image, "contributor", groupValues.get(contributor));
			if(license != -1 && groupValues.get(license)) new Node(image, "license", groupValues.get(license));
		}
	}

	private void createImages(Node images, String imageId, List<Map> imageMetaData) {
        println '======='+imageId
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
		String uploadDir = config.speciesPortal.images.uploadDir;
		imageMetaData.each { imageData ->
			String refKey = imageData.get("id");
			if(refKey.trim().equals(imageId.trim())) {
				Node image = new Node(images, "image");
				String loc = imageData.get("imageno.")?:imageData.get("image")?:imageData.get("id");
				File file = new File(uploadDir, cleanLoc(loc));
                println ')))))'+loc
				new Node(image, "refKey", refKey);
				new Node(image, "fileName", file.getAbsolutePath());
				new Node(image, "source", imageData.get("source")?:imageData.get("url"));
				new Node(image, "caption", imageData.get("possiblecaption")?:imageData.get("caption"));
				new Node(image, "attribution", imageData.get("attribution"));
				new Node(image, "contributor", imageData.get("contributor"));
				new Node(image, "license", imageData.get("license"));
			}
		}
	}

	private String cleanLoc(String loc) {
		return loc.replaceAll("\\\\", File.separator);
	}

	private getReferenceNode(Node refNode, String text) {
		if(text.startsWith("http://")) {
			new Node(refNode, "url", text);
		} else {
			new Node(refNode, "title", text);
		}
	}
	
	private void createReferences(Node dataNode, speciesContent, mappedField) {
        log.debug "Creating References"
		def referenceFields = mappedField.get("field name(s)");		
		if(referenceFields) {
			referenceFields.split(",").each { referenceField ->
				String references = speciesContent.get(referenceField.toLowerCase());
                println references
				String delimiter = mappedField.get("content delimiter") ?: "\n";
                println "delimiter "+delimiter
				references?.split(delimiter).each {
					Node refNode = new Node(dataNode, "reference");
                    println it;
					getReferenceNode(refNode, it);
				}
			}
		}
        println "8888888"
	}

    void setLogAppender(FileAppender fa) {
        if(fa) {
            Logger LOG = Logger.getLogger(this.class);
            LOG.addAppender(fa);
        }
    }

}
