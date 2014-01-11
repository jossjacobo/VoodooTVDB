package voodoo.tvdb.XMLHandlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import voodoo.tvdb.Objects.Series;


public class XmlHandlerBackup extends DefaultHandler{

	private boolean in_idTag = false;
	private boolean in_watchedTag = false;
	
	private Series series;
	
	private ArrayList<Series> seriesList;
	
	public ArrayList<Series> getSeriesList(){
		return seriesList;
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
		
		if(localName.equalsIgnoreCase("id")){
			series = new Series();
			this.in_idTag = true;
		}else if(localName.equalsIgnoreCase("watched")){
			//TODO watched = new Watched();
			this.in_watchedTag = true;
		}
		
	}
	
	@Override
	public void endElement(String namespaceURI, String localName, String qNames) throws SAXException{
		if(localName.equalsIgnoreCase("id")){
			this.in_idTag = false;
			seriesList.add(series);
		}else if(localName.equalsIgnoreCase("watched")){
			this.in_watchedTag = true;
			//TODO watchedList.add(watched);
		}
	}
	
	@Override
	public void characters(char ch[], int start, int length){
		String chars = new String(ch, start, length);
		
		if(in_idTag){
			series.ID = series.ID == null ? chars : series.ID + chars;
			series.LAST_UPDATED = "0";
		}else if(in_watchedTag){
			//TODO do somethign with the back up, like set the series ID
		}
	}
	
}
