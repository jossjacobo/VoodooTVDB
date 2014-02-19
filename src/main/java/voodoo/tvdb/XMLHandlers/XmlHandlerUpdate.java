package voodoo.tvdb.xmlHandlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

public class XmlHandlerUpdate extends DefaultHandler{
	
	@SuppressWarnings("unused")
	private boolean in_data = false;
	private boolean in_episode = false;
	private boolean in_series = false;
	
	private ArrayList<String> series;
	private ArrayList<String> episode;
	
	private String series_id;
	private String episode_id;
	
	/**
	 * Getter
	 */
	public ArrayList<String> getSeries(){
		return series;
	}
	public ArrayList<String> getEpisode(){
		return episode;
	}

	/**
	 * Methods
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
		
		if(localName.equalsIgnoreCase("Data")){
			this.in_data = true;
			
			//Create the Episode ArrayList
			episode = new ArrayList<String>();
			
			//Create the Series ArrayList
			series = new ArrayList<String>();
			
		}else if(localName.equalsIgnoreCase("Episode")){
			this.in_episode = true;
			episode_id = null;
		}else if(localName.equalsIgnoreCase("Series")){
			this.in_series = true;
			series_id = null;
		}
		
	}
	
	@Override
	public void endElement(String namespaceURI, String localName, String qNames) throws SAXException{

		if(localName.equalsIgnoreCase("Data")){
			this.in_data = false;
		}else if(localName.equalsIgnoreCase("Episode")){
			this.in_episode = false;
			episode.add(episode_id);
		}else if(localName.equalsIgnoreCase("Series")){
			this.in_series = false;
			series.add(series_id);
		}
		
	}
	
	@Override
	public void characters(char ch[], int start, int length){
		String chars = new String(ch, start, length);
		
		if(this.in_episode){
			episode_id = episode_id == null ? chars : episode_id + chars;
		}else if(this.in_series){
			series_id = series_id == null ? chars : series_id + chars;
		}
	}
}
