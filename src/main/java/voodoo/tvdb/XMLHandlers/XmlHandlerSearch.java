package voodoo.tvdb.XMLHandlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import voodoo.tvdb.Objects.Series;


//TODO Move the sorting to the Search.Class Activity instead of here ><
public class XmlHandlerSearch extends DefaultHandler{
	
	/**
	 * Fields
	 */
	@SuppressWarnings("unused")
	private boolean in_datatag = false;
	
	@SuppressWarnings("unused")
	private boolean in_seriestag = false;
	
	private boolean in_seriesidtag = false;
	private boolean in_overviewtag = false;
	
	private ArrayList<Series> seriesIdList;
	private Series series;
	
	/**
	 * Getter & Setter
	 */
	public ArrayList<Series> getIdList() {
		return this.seriesIdList;
	}
	/**
	 * Methods
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
		
		if(localName.equalsIgnoreCase("data")){
			this.in_datatag = true;
			seriesIdList = new ArrayList<Series>();
		}else if(localName.equalsIgnoreCase("series")){
			this.in_seriestag = true;
			series = new Series();
		}else if(localName.equalsIgnoreCase("seriesid")){
			this.in_seriesidtag = true;
		}else if(localName.equalsIgnoreCase("overview")){
			this.in_overviewtag = true;
		}
		
	}
	
	@Override
	public void endElement(String namespaceURI, String localName, String qNames) throws SAXException{

		if(localName.equalsIgnoreCase("data")){
			this.in_datatag = false;
			seriesIdList = sortSeriesList();
		}else if(localName.equalsIgnoreCase("series")){
			this.in_seriestag = false;	
			seriesIdList.add(series);
		}else if(localName.equalsIgnoreCase("seriesid")){
			this.in_seriesidtag = false;
		}else if(localName.equalsIgnoreCase("overview")){
			this.in_overviewtag = false;
		}
		
	}
	@Override
	public void characters(char ch[], int start, int length){

		String chars = new String(ch, start, length);
		
		if(this.in_seriesidtag){
			series.ID = series.ID == null ? chars : series.ID + chars;
		}else if(this.in_overviewtag){
			series.OVERVIEW = series.OVERVIEW == null ? chars : series.OVERVIEW + chars;
		}
	}
	
	
	private ArrayList<Series> sortSeriesList() {
		
		//for now just puts series without overview to the end.
		ArrayList<Series> list = new ArrayList<Series>();
		
		//Sort into Two ArrayList; one with Overview and the other Without Overview
		int count = 0;
		for(int i = 0; i < seriesIdList.size(); i++){
			if(seriesIdList.get(i).OVERVIEW == "" || seriesIdList.get(i).OVERVIEW == null){
				list.add(seriesIdList.get(i));
			}else{
				list.add(count, seriesIdList.get(i));
				count++;
			}
		}
		
		return list;
	}
}
















