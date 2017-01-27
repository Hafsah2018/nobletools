package edu.pitt.dbmi.nlp.noble.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.pitt.dbmi.nlp.noble.coder.NobleCoder;
import edu.pitt.dbmi.nlp.noble.coder.model.Document;
import edu.pitt.dbmi.nlp.noble.coder.model.Mention;
import edu.pitt.dbmi.nlp.noble.coder.model.Modifier;
import edu.pitt.dbmi.nlp.noble.coder.model.Sentence;
import edu.pitt.dbmi.nlp.noble.extract.InformationExtractor;
import edu.pitt.dbmi.nlp.noble.extract.model.ItemInstance;
import edu.pitt.dbmi.nlp.noble.extract.model.Template;
import edu.pitt.dbmi.nlp.noble.extract.model.TemplateDocument;
import edu.pitt.dbmi.nlp.noble.extract.model.TemplateItem;
import edu.pitt.dbmi.nlp.noble.mentions.model.AnnotationVariable;
import edu.pitt.dbmi.nlp.noble.mentions.model.Composition;
import edu.pitt.dbmi.nlp.noble.mentions.model.DomainOntology;
import edu.pitt.dbmi.nlp.noble.mentions.model.Instance;
import edu.pitt.dbmi.nlp.noble.terminology.Annotation;
import edu.pitt.dbmi.nlp.noble.terminology.Concept;
import edu.pitt.dbmi.nlp.noble.tools.ConText;
import edu.pitt.dbmi.nlp.noble.tools.TextTools;

/**
 * The Class HTMLExporter.
 */
public class HTMLExporter {
	public final String TERM_SERVLET = "http://slidetutor.upmc.edu/term/servlet/TerminologyServlet";
	public final String HTML_REPORT_LOCATION = "reports";
	private String title = "";
	private File outputDirectory;
	private String resultFileName;
	private BufferedWriter htmlIndexWriter;
	private boolean createIndex,showFooter = true,showReport = true,showConceptList = true;
	private String terminologySerlvet;
	
	
	
	/**
	 * create HTML Exporter that writes out a set of HTML files
	 * to a given output directory.
	 *
	 * @param outputDirectory the output directory
	 */
	public HTMLExporter(File outputDirectory){
		setOutputDirectory(outputDirectory);
		resultFileName = CSVExporter.DEFAULT_RESULT_FILE;
		createIndex = true;
		terminologySerlvet = TERM_SERVLET;
	}
	
	/**
	 * create HTML Exporter that writes out a set of HTML files
	 * to a given output directory.
	 */
	public HTMLExporter(){
		resultFileName = CSVExporter.DEFAULT_RESULT_FILE;
		createIndex = false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		flush();
	}

	/**
	 * Checks if is show footer.
	 *
	 * @return true, if is show footer
	 */
	public boolean isShowFooter() {
		return showFooter;
	}

	/**
	 * Sets the show footer.
	 *
	 * @param showFooter the new show footer
	 */
	public void setShowFooter(boolean showFooter) {
		this.showFooter = showFooter;
	}


	/**
	 * Checks if is show report.
	 *
	 * @return true, if is show report
	 */
	public boolean isShowReport() {
		return showReport;
	}

	/**
	 * Sets the show report.
	 *
	 * @param showReport the new show report
	 */
	public void setShowReport(boolean showReport) {
		this.showReport = showReport;
	}

	/**
	 * Checks if is show concept list.
	 *
	 * @return true, if is show concept list
	 */
	public boolean isShowConceptList() {
		return showConceptList;
	}

	/**
	 * Sets the show concept list.
	 *
	 * @param showConceptList the new show concept list
	 */
	public void setShowConceptList(boolean showConceptList) {
		this.showConceptList = showConceptList;
	}

	/**
	 * Gets the terminology serlvet.
	 *
	 * @return the terminology serlvet
	 */
	public String getTerminologySerlvet() {
		return terminologySerlvet;
	}

	/**
	 * Sets the terminology serlvet.
	 *
	 * @param terminologySerlvet the new terminology serlvet
	 */
	public void setTerminologySerlvet(String terminologySerlvet) {
		this.terminologySerlvet = terminologySerlvet;
	}

	/**
	 * get the result file name that is being used.
	 *
	 * @return the result file name
	 */
	
	public String getResultFileName() {
		return resultFileName;
	}


	/**
	 * sget the result file name that is being used.
	 *
	 * @param resultFileName the new result file name
	 */
	public void setResultFileName(String resultFileName) {
		this.resultFileName = resultFileName;
	}


	/**
	 * should HTML index file be created.
	 *
	 * @return true, if is creates the index
	 */
	public boolean isCreateIndex() {
		return createIndex;
	}


	/**
	 * cretae an HTML index file.
	 *
	 * @param createIndex the new creates the index
	 */
	public void setCreateIndex(boolean createIndex) {
		this.createIndex = createIndex;
	}


	/**
	 * get output directory.
	 *
	 * @return the output directory
	 */

	public File getOutputDirectory() {
		return outputDirectory;
	}

	/**
	 * set output directory.
	 *
	 * @param outputDirectory the new output directory
	 */
	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
		File reports = new File(this.outputDirectory,HTML_REPORT_LOCATION);
		if(!reports.isDirectory())
			reports.mkdirs();
	}


	/**
	 * create pretty CAP template.
	 *
	 * @param doc the doc
	 * @return the string
	 */
	private String createTemplate(TemplateDocument doc){
		StringBuffer cap = new StringBuffer();
		
		for(Template template: doc.getItemInstances().keySet()){
			int num = 1;
			cap.append("<h3>"+template.getName()+"</h3>");
			cap.append("<table border=0 cellspacing=0 cellpadding=2>");
			for(TemplateItem temp: template.getTemplateItems()){
				List<ItemInstance> items = doc.getItemInstances(temp);
				String name = (items.isEmpty())?temp.getName():codeTemplateItem(items.get(0));
				Map<String,Collection<ItemInstance>> names = new LinkedHashMap<String,Collection<ItemInstance>>();
				if(temp.getAttributeValues().isEmpty()){
					names.put(name,items);
				}else{
					for(TemplateItem attr : temp.getAttributes()){
						Set<ItemInstance> list = new LinkedHashSet<ItemInstance>();
						for(ItemInstance item: items){
							for(ItemInstance a: item.getAttributes()){
								if(a.getTemplateItem().equals(attr)){
									list.addAll(item.getAttributeValues(a));
								}
							}
						}
						names.put(name+" "+attr.getName(),list);
					}
				}	
				for(String nm: names.keySet()){
					cap.append("<tr><td> <font color=\"#E0E0E0 \">"+(num++)+"</font> </td><th align=left> "+nm+" </th><td align=left style=\"padding-left:20px;\">");
					String br = "";
					for(ItemInstance item: names.get(nm)){
						cap.append(br+codeConcept(item));
						br="<br>";
					}
				}
				cap.append("</td></tr>");
			}
			cap.append("</table>");
		}
		return cap.toString();
	}
	

	/**
	 * code label.
	 *
	 * @param l the l
	 * @param mentions the mentions
	 * @return the string
	 */
	private String codeLabel(Annotation l, List<Mention> mentions){
		String lid = ""+l.getOffset();
		String word = l.getText();
		List<String> codes = new ArrayList<String>();
		StringBuffer tip = new StringBuffer();
		String color = "green";
		for(Mention m: mentions){
			Concept c = m.getConcept();
			String p = (m.isNegated())?"N":(m.isHedged()?"U":"");
			codes.add("'"+p+m.getConcept().getCode()+"'");
			tip.append(c.getName()+" ("+c.getCode()+") "+Arrays.toString(c.getSemanticTypes())+"\n");
			
			// add modifiers
			tip.append(getModifiers(m));
			
			if(!isDefaultModifiers(m)){
				color = "#994d00";
			}
			
		}
		return "<label id=\""+lid+"\" style=\"color:"+color+";\" onmouseover=\"h("+codes+");\" onmouseout=\"u("+codes+
				");\" title=\""+TextTools.escapeHTML(tip.toString())+"\">"+word+"</label>";
	}
	
	/**
	 * Checks if is default modifiers.
	 *
	 * @param m the m
	 * @return true, if is default modifiers
	 */
	private boolean isDefaultModifiers(Mention m) {
		for(Modifier mod: m.getModifiers().values()){
			if(!mod.isDefaultValue()){
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the modifiers.
	 *
	 * @param m the m
	 * @return the modifiers
	 */
	private String getModifiers(Mention m){
		StringBuffer st = new StringBuffer();
		for(String type: Arrays.asList(
				ConText.MODIFIER_TYPE_CERTAINTY,	ConText.MODIFIER_TYPE_POLARITY,
				ConText.MODIFIER_TYPE_EXPERIENCER,ConText.MODIFIER_TYPE_TEMPORALITY)){
			if(m.getModifierValue(type) != null)
				st.append("\t"+type+" :\t"+m.getModifierValue(type)+"\n");
		}
		
		return st.toString();
	}
	
	
	/**
	 * code individual concept.
	 *
	 * @param c the c
	 * @param color the color
	 * @param aa the aa
	 * @return the string
	 */
	private String codeConcept(Concept c, String color,List<Annotation> aa){
		String p = "";
		List<String> ids = new ArrayList<String>();
		for(Annotation a: aa){
			ids.add("'"+a.getOffset()+"'");
		}
		String code = c.getCode();
		StringBuffer sy = new StringBuffer("\nterms:  ");
		for(String s: c.getSynonyms())
			sy.append(s+"; ");
		String tip = c.getCode()+" "+Arrays.toString(c.getSemanticTypes())+"\n"+c.getDefinition()+sy;
		String term = c.getTerminology().getName();
		StringBuffer out = new StringBuffer();
		out.append("<a style=\"color:"+color+";\" onmouseover=\"h("+ids+");t=setTimeout(function(){j("+ids+");},2000);\" ");
		out.append(	"onmouseout=\"u("+ids+"); clearTimeout(t);\" id=\""+p+code+"\"");
		out.append(" href=\""+terminologySerlvet+"?action=lookup_concept&term="+term+"&code="+code);
		out.append("\" target=\"_blank\" title=\""+TextTools.escapeHTML(tip)+"\">"+TextTools.escapeHTML(c.getName())+"</a> &nbsp; ");
		return out.toString();
	}
	
	
	/**
	 * code individual concept.
	 *
	 * @param c the c
	 * @param color the color
	 * @param aa the aa
	 * @return the string
	 */
	private String codeVariable(AnnotationVariable var){
		String color = "blue";
		String code = var.getConceptClass().getName();
		String tip = "";
		
		StringBuffer out = new StringBuffer();
		
		out.append("<table><tr><th colspan=3 align=left>");
		out.append(codeEntity(var.getLabel(),code, tip, color,var.getAnnotations()));
		out.append("</th></tr>");
		
		Map<String,Set<Instance>> modifiers = var.getModifierInstances();
		for(String prop: modifiers.keySet()){
			Set<Instance> instances = modifiers.get(prop);
			String val =  codeEntities(instances,!DomainOntology.HAS_ANCHOR.equals(prop));
			out.append("<tr><td>&nbsp;</td><td>"+prop);
			out.append("</td><td>"+val+"</td></tr>");
		}
		out.append("</table>");
		
		return out.toString();
	}
	
	/**
	 * code multiple entities
	 * @param instances
	 * @return
	 */
	private String codeEntities(Collection<Instance> instances){
		return codeEntities(instances,true);
	}
	
	/**
	 * code multiple entities
	 * @param instances
	 * @return
	 */
	private String codeEntities(Collection<Instance> instances,boolean includeComponents){
		StringBuffer out = new StringBuffer();
		for(Instance inst: instances){
			String color = inst.getAnnotations().isEmpty()?"black":"blue";
			out.append(codeEntity(inst.getLabel(),inst.getName(),"",color,inst.getAnnotations()));
			if(includeComponents){
				String props = codeEntities(inst.getModifierInstanceList());
				if(props.length() > 0)
					out.append(" ("+props+")");
			}
			out.append(", ");
		}
		// remove the last comma and space
		if(out.length() > 2)
			out.replace(out.length()-2, out.length(), "");
		return out.toString();
	}
	
	
	/**
	 * create a coded entity
	 * @param label
	 * @param code
	 * @param tip
	 * @param color
	 * @param annnotations
	 * @return
	 */
	private String codeEntity(String label, String code, String tip, String color, Collection<Annotation> annnotations){
		List<String> ids = new ArrayList<String>();
		for(Annotation a: annnotations){
			ids.add("'"+a.getOffset()+"'");
		}
		StringBuffer out = new StringBuffer();
		out.append("<span style=\"color:"+color+";\" onmouseover=\"h("+ids+");t=setTimeout(function(){j("+ids+");},2000);\" ");
		out.append(	"onmouseout=\"u("+ids+"); clearTimeout(t);\" id=\""+code+"\"");
		//out.append(" href=\""+terminologySerlvet+"?action=lookup_concept&term="+term+"&code="+code+"\" target=\"_blank\"");
		out.append(" title=\""+TextTools.escapeHTML(tip)+"\">"+TextTools.escapeHTML(label)+"</span> ");
		return out.toString();
	}
	
	
	
	/**
	 * group annotations in a sentence.
	 *
	 * @param s the s
	 * @return the map
	 */
	private Map<Annotation,List<Mention>> groupAnnotations(Sentence s) {
		Map<Annotation,List<Mention>> map = new TreeMap<Annotation, List<Mention>>();
		for(Mention m: s.getMentions()){
			// this takes care of main mentions
			for(Annotation a: m.getAnnotations()){
				if(s.contains(a) && !intersects(a,map.keySet())){
					List<Mention> mm = map.get(a);
					if(mm == null){
						mm = new ArrayList<Mention>();
						map.put(a,mm);
					}
					mm.add(m);
				}
			}
			// what about modifiers for each mention?
			for(Modifier mmm: m.getModifiers().values()){
				for(Annotation a: mmm.getAnnotations()){
					if(s.contains(a) && !intersects(a,map.keySet())){
						List<Mention> mm = map.get(a);
						if(mm == null){
							mm = new ArrayList<Mention>();
							map.put(a,mm);
						}
						mm.add(m);
					}
				}
			}
			
		}
		return map;
	}

	/**
	 * Intersects.
	 *
	 * @param an the an
	 * @param aa the aa
	 * @return true, if successful
	 */
	private boolean intersects(Annotation an, Set<Annotation> aa) {
		for(Annotation a: aa){
			if(!a.equals(an) && (a.contains(an) || an.contains(a)))
				return true;
		}
		return false;
	}

	/**
	 * create an HTML representation of Sentence.
	 *
	 * @param s the s
	 * @return the string
	 */
	private String codeSentence(Sentence s) {
		StringBuffer str = new StringBuffer();
		if(Sentence.TYPE_HEADER.equals(s.getSentenceType())){
			str.append("<b>"+s.getText()+"</b><br>");
		}else{
			int offs = 0;
			String content = s.getText();
			Map<Annotation,List<Mention>> annotations = groupAnnotations(s);
			for(Annotation l : annotations.keySet()){
				try{
					int o = l.getOffset()-s.getOffset();
					str.append(content.substring(offs,o).replaceAll("\n", "<br>"));
					str.append(codeLabel(l,annotations.get(l)));
					offs = o+l.getLength();
				}catch(StringIndexOutOfBoundsException ex){
					System.err.print("Error: "+ex.getMessage()+"\t");
					System.err.print("Sentence:\t"+content.trim()+"/"+s.getOffset()+"\t");
					System.err.println("Label:\t"+l);
					//ex.printStackTrace();
				}
			}
			str.append(content.substring(offs).replaceAll("\n", "<br>"));
		}
		return str.toString();
	}

	/**
	 * code a list of mentions of a given modality.
	 *
	 * @param mentions the mentions
	 * @return the string
	 */
	
	private String codeMentions(List<Mention> mentions){
		Map<Concept,List<Annotation>> map = new TreeMap<Concept, List<Annotation>>(new Comparator<Concept>() {
			public int compare(Concept o1, Concept o2) {
				int x = o1.compareTo(o2);
				return (x == 0)?o1.getCode().compareTo(o2.getCode()):x;
			}
		});
		StringBuffer str = new StringBuffer();
		// load and sort mentions
		for(Mention m: mentions){
			List<Annotation> list = map.get(m.getConcept());
			if(list == null){
				list = new ArrayList<Annotation>();
				map.put(m.getConcept(),list);
			}
			list.addAll(m.getAnnotations());
			
		}
		// now create a list of concepts
		boolean alt = true;
		for(Concept c: map.keySet()){
			String color = (alt)?"blue":"black";
			alt ^= true;
			str.append(codeConcept(c,color,map.get(c)));
		}
		return (str.length() > 0)?"<p><b>Concepts</b><br>"+str+"</p>":"";
	}
	
	
	/**
	 * code annotation variable
	 * @param variables
	 * @return
	 */
	
	private String codeVariables(List<AnnotationVariable> variables){
		StringBuffer str = new StringBuffer();
		str.append("<ol>");
		for(AnnotationVariable var: variables){
			str.append("<li>"+codeVariable(var)+"</li>");
		}
		str.append("</ol>");
		return str.toString();
	}
	
	
	

	/**
	 * get index buffer.
	 *
	 * @return the index
	 * @throws Exception the exception
	 */
	private BufferedWriter getIndex() throws Exception {
		if(htmlIndexWriter == null){
			// write header 
			htmlIndexWriter = new BufferedWriter(new FileWriter(new File(outputDirectory,"index.html")));
			htmlIndexWriter.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
			htmlIndexWriter.write("<head><title>"+title+"</title>\n");
			htmlIndexWriter.write("<script type=\"text/javascript\">function l(){var h=800;if(!window.innerWidth){\n");
			htmlIndexWriter.write("if(!(document.documentElement.clientWidth == 0)){\n h = document.documentElement.clientHeight;\n");
			htmlIndexWriter.write("}else{h = document.body.clientHeight;}}else{ h = window.innerHeight;} var hd = (h-100)+\"px\";\n");
			htmlIndexWriter.write("document.getElementById(\"d1\").style.maxHeight=hd;}</script>\n");
			htmlIndexWriter.write("</head><body style=\"overflow: hidden;\" bgcolor=\"#EEEEFF\" onload=\"l();\" onresize=\"l();\"><center><h3>"+title+" Output [");
			htmlIndexWriter.write("<a href=\""+resultFileName+"\" title=\"Download the entire result in Tab Seperated Values (.tsv) format \">TSV</a>]</h3></center>\n");
			htmlIndexWriter.write("<center><table bgcolor=\"#FFFFF\" width=\"100%\" height=\"95%\" border=0>\n");
			htmlIndexWriter.write("<tr><td align=\"left\" valign=\"top\" width=\"200px\" style=\"white-space: nowrap\">\n");
			htmlIndexWriter.write("<div id=\"d1\" style=\"overflow: auto; max-height: 800px;\"><div style=\"border-style:solid; border-color: #EEEEFF; padding:10px 10px;\">");
		}
		return htmlIndexWriter;
	}
	
	/**
	 * flush all writers.
	 *
	 * @throws Exception the exception
	 */
	public void flush() throws Exception {
		if(htmlIndexWriter != null){
			htmlIndexWriter.write("</div></div></td><td valign=top><iframe bgcolor=white frameborder=\"0\" scrolling=\"auto\" name=\"frame\" width=\"100%\" height=\"100%\"></iframe>\n");
			htmlIndexWriter.write("</td></tr></table></center></body></html>\n");
			htmlIndexWriter.flush();
			htmlIndexWriter.close();
		}
		htmlIndexWriter = null;
	}
	
	/**
	 * create a coded html report.
	 *
	 * @param doc the doc
	 * @throws Exception the exception
	 */
	public void export(Document doc) throws Exception {
		String name = doc.getTitle();
		if(name.endsWith(".txt"))
			name = name.substring(0,name.length()-".txt".length());
		File out = new File(outputDirectory.getAbsolutePath()+File.separator+HTML_REPORT_LOCATION+File.separator+name+".html");
		BufferedWriter htmlWriter = new BufferedWriter(new FileWriter(out));
		export(doc,htmlWriter);
	}

	/**
	 * create a coded html report.
	 *
	 * @param doc the doc
	 * @throws Exception the exception
	 */
	public void export(Composition doc) throws Exception {
		String name = doc.getTitle();
		if(name.endsWith(".txt"))
			name = name.substring(0,name.length()-".txt".length());
		File out = new File(outputDirectory.getAbsolutePath()+File.separator+HTML_REPORT_LOCATION+File.separator+name+".html");
		BufferedWriter htmlWriter = new BufferedWriter(new FileWriter(out));
		export(doc,htmlWriter);
	}
	
	/**
	 * create a coded html report.
	 *
	 * @param doc the doc
	 * @param htmlWriter the html writer
	 * @throws Exception the exception
	 */
	public void export(Document doc, Writer htmlWriter) throws Exception {
		title = "Noble Coder";
		// build report
		String content = doc.getText();
		StringBuffer text = new StringBuffer();
		int offs = 0;
		for(Sentence s: doc.getSentences()){
			int o = s.getOffset();
			text.append(content.substring(offs,o).replaceAll("\n","<br>"));
			text.append(codeSentence(s));
			offs = o+s.getLength();
		}
		if(offs < content.length())
			text.append(content.substring(offs).replaceAll("\n", "<br>"));
			
		StringBuffer result = new StringBuffer();
		result.append(codeMentions(doc.getMentions()));
			
		// get report representation and cap protocol
		String report = text.toString(); //convertToHTML(text.toString());
		
		StringBuffer info = new StringBuffer();
		Long time = doc.getProcessTime().get(NobleCoder.class.getSimpleName());
		info.append("report process time: <b>"+((time != null)?time.longValue():-1)+"</b> ms , ");
		info.append("found items: <b>"+doc.getMentions().size()+"</b>");
		
		// write out results
		String name = null; 
		if(doc.getTitle() != null){
			name = doc.getTitle();
			if(name.endsWith(".txt"))
				name = name.substring(0,name.length()-".txt".length());
		}
		//File out = new File(outputDirectory.getAbsolutePath()+File.separator+HTML_REPORT_LOCATION+File.separator+name+".html");
		//BufferedWriter htmlWriter = new BufferedWriter(new FileWriter(out));
		
		
		htmlWriter.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		htmlWriter.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		htmlWriter.write("<head><title>Report Processor Output</title><script type=\"text/javascript\">");
		htmlWriter.write("function h(id){for(i=0;i<id.length;i++){if(document.getElementById(id[i])!=null){document.getElementById(id[i]).style.backgroundColor=\"yellow\";}}}");
		htmlWriter.write("function u(id){for(i=0;i<id.length;i++){if(document.getElementById(id[i])!=null){document.getElementById(id[i]).style.backgroundColor=\"white\";}}}"); //</script>
		htmlWriter.write("function j(id){for(i=0;i<id.length;i++){location.href=\"#\";location.href=\"#\"+id[i];}}");
		htmlWriter.write("function l(){var h=800;if(!window.innerWidth){\n");
		htmlWriter.write("if(!(document.documentElement.clientWidth == 0)){\n h = document.documentElement.clientHeight;\n");
		htmlWriter.write("}else{h = document.body.clientHeight;}}else{ h = window.innerHeight;} var hd = (h-100)+\"px\";\n");
		htmlWriter.write("document.getElementById(\"d1\").style.maxHeight=hd;document.getElementById(\"d2\").style.maxHeight=hd;}</script>\n");
		
		htmlWriter.write("</head><body onload=\"l();\" onresize=\"l();\"><table width=\"100%\" style=\"table-layout:fixed; \" cellspacing=\"5\">\n"); //word-wrap:break-word;
		if(name != null)
			htmlWriter.write("<tr><td colspan=2 align=center><h3>"+name+"</h3></td></tr>\n");
		
		String sz = "50%";
		if(showReport ^ showConceptList)
			sz = "100%";
		
		if(showReport)
			htmlWriter.write("<tr><td width=\""+sz+"\" valign=middle><div id=\"d1\" style=\"overflow: auto; max-height: 800px; \">"+report+"</div></td>");
		if(showConceptList)
			htmlWriter.write("<td width=\""+sz+"\" valign=top><div id=\"d2\" style=\"overflow: auto; max-height: 800px;\">"+result+"</div></td></tr>\n");
		if(showFooter)
			htmlWriter.write("<tr><td colspan=2 align=center>"+info+"</td></tr>\n");
		htmlWriter.write("<tr><td colspan=2 align=center></td></tr>\n");
		htmlWriter.flush();
		
		// finish up
		htmlWriter.write("<tr><td colspan=2></td></tr>\n");
		htmlWriter.write("</table></body></html>\n");
		htmlWriter.flush();
		htmlWriter.close();

		// add link to index
		if(createIndex){
			getIndex().write("<span style=\"max-width: 190px; font-size: 90%; overflow: hidden; display:block;\">");
			getIndex().write("<a href=\""+HTML_REPORT_LOCATION+"/"+name+".html\" target=\"frame\">"+doc.getTitle()+"</a></span>\n");
			getIndex().flush();
		}
	}
	

	/**
	 * create a coded html report.
	 *
	 * @param doc the doc
	 * @throws Exception the exception
	 */
	public void export(TemplateDocument doc) throws Exception {
		title = "Information Extraction";
		
		// create cap protocol
		String cap =  createTemplate(doc);
		
		
		// build report
		String content = doc.getText();
		StringBuffer text = new StringBuffer();
		int offs = 0;
		for(Sentence s: doc.getSentences()){
			int o = s.getOffset();
			text.append(content.substring(offs,o).replaceAll("\n","<br>"));
			text.append(codeSentence(s));
			offs = o+s.getLength();
		}
		text.append(content.substring(offs).replaceAll("\n", "<br>"));
		
		int n = 0;
		for(Template t: doc.getItemInstances().keySet()){
			n += doc.getItemInstances().get(t).size();
		}
		
		// get report representation and cap protocol
		String report = text.toString(); //convertToHTML(text.toString());
		
		StringBuffer info = new StringBuffer();
		Long time = doc.getProcessTime().get(InformationExtractor.class.getSimpleName());
		info.append("report process time: <b>"+((time != null)?time.longValue():-1)+"</b> ms , ");
		info.append("found items: <b>"+n+"</b>");
		
		// write out results
		String name = doc.getTitle();
		if(name.endsWith(".txt"))
			name = name.substring(0,name.length()-".txt".length());
		File out = new File(outputDirectory.getAbsolutePath()+File.separator+HTML_REPORT_LOCATION+File.separator+name+".html");
		BufferedWriter htmlWriter = new BufferedWriter(new FileWriter(out));
		
		
		htmlWriter.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		htmlWriter.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		htmlWriter.write("<head><title>Report Processor Output</title><script type=\"text/javascript\">");
		htmlWriter.write("function h(id){for(i=0;i<id.length;i++){document.getElementById(id[i]).style.backgroundColor=\"yellow\";}}");
		htmlWriter.write("function u(id){for(i=0;i<id.length;i++){document.getElementById(id[i]).style.backgroundColor=\"white\";}}"); //</script>
		htmlWriter.write("function j(id){for(i=0;i<id.length;i++){location.href=\"#\";location.href=\"#\"+id[i];}}");
		htmlWriter.write("function l(){var h=800;if(!window.innerWidth){\n");
		htmlWriter.write("if(!(document.documentElement.clientWidth == 0)){\n h = document.documentElement.clientHeight;\n");
		htmlWriter.write("}else{h = document.body.clientHeight;}}else{ h = window.innerHeight;} var hd = (h-100)+\"px\";\n");
		htmlWriter.write("document.getElementById(\"d1\").style.maxHeight=hd;document.getElementById(\"d2\").style.maxHeight=hd;}</script>\n");
		
		htmlWriter.write("</head><body onload=\"l();\" onresize=\"l();\"><table width=\"100%\" style=\"table-layout:fixed; \" cellspacing=\"5\">\n"); //word-wrap:break-word;
		htmlWriter.write("<tr><td colspan=2 align=center><h3>"+name+"</h3></td></tr>\n");
		htmlWriter.write("<tr><td width=\"50%\" valign=middle><div id=\"d1\" style=\"overflow: auto; max-height: 800px; \">"+report+"</div></td>");
		htmlWriter.write("<td width=\"50%\" valign=top><div id=\"d2\" style=\"overflow: auto; max-height: 800px;\">"+cap+"</div></td></tr>\n");
		htmlWriter.write("<tr><td colspan=2 align=center>"+info+"</td></tr>\n");
		htmlWriter.write("<tr><td colspan=2 align=center></td></tr>\n");
		htmlWriter.flush();
		
		// finish up
		htmlWriter.write("<tr><td colspan=2></td></tr>\n");
		htmlWriter.write("</table></body></html>\n");
		htmlWriter.flush();
		htmlWriter.close();

		// add link to index
		if(createIndex){
			getIndex().write("<span style=\"max-width: 190px; font-size: 90%; overflow: hidden; display:block;\">");
			getIndex().write("<a href=\""+HTML_REPORT_LOCATION+"/"+name+".html\" target=\"frame\">"+doc.getTitle()+"</a></span>\n");
			getIndex().flush();
		}
	}
	
	
	/**
	 * create a coded html report.
	 *
	 * @param doc the doc
	 * @param htmlWriter - where to write to
	 * @throws Exception the exception
	 */
	public void export(Composition doc, Writer htmlWriter) throws Exception {
		title = "Noble Mentions";
		// build report
		String content = doc.getText();
		StringBuffer text = new StringBuffer();
		int offs = 0;
		for(Sentence s: doc.getSentences()){
			int o = s.getOffset();
			text.append(content.substring(offs,o).replaceAll("\n","<br>"));
			text.append(codeSentence(s));
			offs = o+s.getLength();
		}
		if(offs < content.length())
			text.append(content.substring(offs).replaceAll("\n", "<br>"));
			
		// build up results
		StringBuffer result = new StringBuffer();
		result.append("<p><b>Annotations</b><p>");
		result.append(codeVariables(doc.getAnnotationVariables()));
		result.append("</p>");
		
		// create list of rejected variables
		if(!doc.getRejectedAnnotationVariables().isEmpty()){
			result.append("<p><b><a href=\"\" onclick=\"showHide('failedVariables'); return false\" >Rejected Annotations</a> ..</b>");
			result.append("<div id=\"failedVariables\" style=\"visibility: hidden\">");
			result.append(codeVariables(doc.getRejectedAnnotationVariables()));
			result.append("</div></p>");
		}
		
		
		// get report representation and cap protocol
		String report = text.toString(); //convertToHTML(text.toString());
		
		StringBuffer info = new StringBuffer();
		Long time = doc.getProcessTime().get(NobleCoder.class.getSimpleName());
		info.append("report process time: <b>"+((time != null)?time.longValue():-1)+"</b> ms , ");
		info.append("found items: <b>"+doc.getMentions().size()+"</b>");
		
		// write out results
		String name = null; 
		if(doc.getTitle() != null){
			name = doc.getTitle();
			if(name.endsWith(".txt"))
				name = name.substring(0,name.length()-".txt".length());
		}
		//File out = new File(outputDirectory.getAbsolutePath()+File.separator+HTML_REPORT_LOCATION+File.separator+name+".html");
		//BufferedWriter htmlWriter = new BufferedWriter(new FileWriter(out));
		
		
		htmlWriter.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		htmlWriter.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		htmlWriter.write("<head><title>Report Processor Output</title><script type=\"text/javascript\">");
		htmlWriter.write("function h(id){for(i=0;i<id.length;i++){if(document.getElementById(id[i])!=null){document.getElementById(id[i]).style.backgroundColor=\"yellow\";}}}");
		htmlWriter.write("function u(id){for(i=0;i<id.length;i++){if(document.getElementById(id[i])!=null){document.getElementById(id[i]).style.backgroundColor=\"white\";}}}"); //</script>
		htmlWriter.write("function j(id){for(i=0;i<id.length;i++){location.href=\"#\";location.href=\"#\"+id[i];}}");
		htmlWriter.write("function l(){var h=800;if(!window.innerWidth){\n");
		htmlWriter.write("if(!(document.documentElement.clientWidth == 0)){\n h = document.documentElement.clientHeight;\n");
		htmlWriter.write("}else{h = document.body.clientHeight;}}else{ h = window.innerHeight;} var hd = (h-100)+\"px\";\n");
		htmlWriter.write("document.getElementById(\"d1\").style.maxHeight=hd;document.getElementById(\"d2\").style.maxHeight=hd;}");
		htmlWriter.write("function showHide(id){ var a = \"hidden\"; if(document.getElementById(id).style.visibility == \"hidden\"){ a = \"visible\";}");
		htmlWriter.write( "document.getElementById(id).style.visibility = a;}");
		htmlWriter.write("</script>\n");
		
		htmlWriter.write("</head><body onload=\"l();\" onresize=\"l();\"><table width=\"100%\" style=\"table-layout:fixed; \" cellspacing=\"5\">\n"); //word-wrap:break-word;
		if(name != null)
			htmlWriter.write("<tr><td colspan=2 align=center><h3>"+name+"</h3></td></tr>\n");
		
		String sz = "50%";
		if(showReport ^ showConceptList)
			sz = "100%";
		
		if(showReport)
			htmlWriter.write("<tr><td width=\""+sz+"\" valign=middle><div id=\"d1\" style=\"overflow: auto; max-height: 800px; \">"+report+"</div></td>");
		if(showConceptList)
			htmlWriter.write("<td width=\""+sz+"\" valign=top><div id=\"d2\" style=\"overflow: auto; max-height: 800px;\">"+result+"</div></td></tr>\n");
		if(showFooter)
			htmlWriter.write("<tr><td colspan=2 align=center>"+info+"</td></tr>\n");
		htmlWriter.write("<tr><td colspan=2 align=center></td></tr>\n");
		htmlWriter.flush();
		
		// finish up
		htmlWriter.write("<tr><td colspan=2></td></tr>\n");
		htmlWriter.write("</table></body></html>\n");
		htmlWriter.flush();
		htmlWriter.close();

		// add link to index
		if(createIndex){
			getIndex().write("<span style=\"max-width: 190px; font-size: 90%; overflow: hidden; display:block;\">");
			getIndex().write("<a href=\""+HTML_REPORT_LOCATION+"/"+name+".html\" target=\"frame\">"+doc.getTitle()+"</a></span>\n");
			getIndex().flush();
		}
	}
	
	
	
	/**
	 * is this a valid annotation belonging to found items.
	 *
	 * @param previous the previous
	 * @param l the l
	 * @param map the map
	 * @return true, if successful
	 */
	private boolean checkAnnotation(List<Annotation> previous, Annotation l, Map<Template, List<ItemInstance>> map) {
		// if span is with previous one, don't include
		int fromIndex = (previous.size()>5)?previous.size()-5:0; 
		for(Annotation last: previous.subList(fromIndex, previous.size())){
			if(((last.getStartPosition() <= l.getStartPosition() && l.getEndPosition() <= last.getEndPosition()) ||
				(l.getStartPosition() <= last.getStartPosition() && last.getEndPosition() <= l.getEndPosition())))
				return false;
		}
		// check if it was mentioned
		boolean include = false;
		for(Template t: map.keySet()){
			for(ItemInstance i: map.get(t)){
				if(i.getAnnotations().contains(l)){
					include = true;
					break;
				}
			}
		}
		
		return include;
	}
	
	/**
	 * code label.
	 *
	 * @param e the e
	 * @return the string
	 */
	private String codeConcept(ItemInstance e){
		String lid = e.getName();
		String text = e.getAnswer();
		String tip = e.getDescription();
		List<String> codes = new ArrayList<String>();
		try{
			for(Annotation l: e.getAnnotations()){
				codes.add("'"+l.getOffset()+"'");
			}
		}catch(Exception ex){}
		return "<label id=\""+lid+"\" style=\"color:blue;\" onmouseover=\"h("+codes+");\" onmouseout=\"u("+codes+");\" onclick=\"j("+codes+")\" title=\""+tip+"\">"+text+"</label>";
	}

	/**
	 * code label.
	 *
	 * @param e the e
	 * @return the string
	 */
	private String codeTemplateItem(ItemInstance e){
		String lid = e.getName();
		String text = e.getQuestion();
		String tip = e.getTemplateItem().getDescription();
		List<String> codes = new ArrayList<String>();
		try{
			for(Annotation l: e.getAnnotations()){
				codes.add("'"+l.getOffset()+"'");
			}
		}catch(Exception ex){}
		return "<label id=\""+lid+"\" style=\"color:blue;\" onmouseover=\"h("+codes+");\" onmouseout=\"u("+codes+");\" onclick=\"j("+codes+")\" title=\""+tip+"\">"+text+"</label>";
	}

	/**
	 * convert regular text report to HTML
	 * 
	 * @param txt
	 * @return
	 *
	public static String convertToHTML(String txt) {
		return (txt + "\n").replaceAll("\n", "<br>");
		//.replaceAll("(^|<br>)([A-Z ]+:)<br>", "$1<b>$2</b><br>")
		//.replaceAll("(^|<br>)(\\[[A-Za-z ]+\\])<br>", "$1<b>$2</b><br>");
	}
	*/
}
