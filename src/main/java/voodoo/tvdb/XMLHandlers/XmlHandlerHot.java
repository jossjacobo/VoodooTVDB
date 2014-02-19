package voodoo.tvdb.xmlHandlers;

import android.content.Context;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import voodoo.tvdb.objects.Series;


public class XmlHandlerHot extends DefaultHandler {
	
	/** Fields */
	private boolean in_dataTag = false;
	private boolean in_seriesTag = false;
	private boolean in_idTag = false;
	private boolean in_seriesNameTag = false;
	private boolean in_posterTag = false;
	
	/** List */
	private ArrayList<Series> seriesList;
	//private ArrayList<String> idList;
	
	/** Series */
	private Series series;
	
	/** Getter Function */
	public ArrayList<Series> getSeries(){
		return seriesList;
	}
	//public ArrayList<String> getIdList(){
	//	return idList;
	//}
	
	/** Context */
	private Context context;
	
	/** Constructor */
	public XmlHandlerHot(Context context){
		
		this.context = context;

	}
	
	/** Methods */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
		
		if(localName.equalsIgnoreCase("data")){
			this.in_dataTag = true;
			seriesList = new ArrayList<Series>();
		}else if(localName.equalsIgnoreCase("series")){
			this.in_seriesTag = true;
			series = new Series();
		}else if(localName.equalsIgnoreCase("id")){
			this.in_idTag = true;
		}else if(localName.equalsIgnoreCase("SeriesName")){
			this.in_seriesNameTag = true;
		}else if(localName.equalsIgnoreCase("poster")){
			this.in_posterTag = true;
		}
		
	}
	
	@Override
	public void endElement(String namespaceURI, String localName, String qNames) throws SAXException{
		if(localName.equalsIgnoreCase("data")){
			this.in_dataTag = false;
		}else if(localName.equalsIgnoreCase("series")){
			this.in_seriesTag = false;
			
			seriesList.add(series);
			//idList.add(series.ID);
			
		}else if(localName.equalsIgnoreCase("id")){
			this.in_idTag = false;
		}else if(localName.equalsIgnoreCase("SeriesName")){
			this.in_seriesNameTag = false;
		}else if(localName.equalsIgnoreCase("poster")){
			this.in_posterTag = false;
		}
	}
	
	@Override
	public void characters(char ch[], int start, int length){
		String chars = new String(ch, start, length);

		if(this.in_dataTag){
			
			if(this.in_seriesTag){
				
				if(this.in_idTag){
					series.ID = series.ID == null ? chars : series.ID + chars;
				}else if(this.in_seriesNameTag){
					series.TITLE = series.TITLE == null ? chars : series.TITLE + chars;
				} else if(this.in_posterTag){
					series.POSTER_URL = series.POSTER_URL == null ? chars : series.POSTER_URL + chars;
				}
			}
		}
	}
}
