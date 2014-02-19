package voodoo.tvdb.xmlHandlers;

import android.content.Context;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import voodoo.tvdb.objects.Episode;


public class XmlHandlerEpisode extends DefaultHandler{

	private Context context;
	
	/** Episode Fields */
	private boolean in_episodeTag = false;//
	private boolean in_episodeNameTag = false;//
	private boolean in_episodeNumberTag = false;//
	private boolean in_guestStarsTag = false;//
	private boolean in_seasonNumberTag = false;//
	private boolean in_filenameTag = false;//
	private boolean in_idTag = false;//
	private boolean in_overviewTag = false;//
	private boolean in_ratingTag = false;//
	private boolean in_ratingCountTag = false;//
	private boolean in_lastUpdatedTag = false;//
	private boolean in_seriesidTag = false;//
	private boolean in_firstairedTag = false;//
	private boolean in_airstimeTag = false;
	private boolean in_seriesnameTag = false;
	private boolean in_poster = false;
	
	/** Episode Object */
	private Episode episode;
	
	public XmlHandlerEpisode(Context context){
		
		this.context = context;
		
	}
	
	/**
	 * Getter Methods
	 */
	public Episode getEpisode(){
		return episode;
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
		if(localName.equalsIgnoreCase("episode")){//
			episode = new Episode();
			this.in_episodeTag = true;
		}else if(localName.equalsIgnoreCase("episodename")){//
			this.in_episodeNameTag = true;
		}else if(localName.equalsIgnoreCase("episodenumber")){//
			this.in_episodeNumberTag = true;
		}else if(localName.equalsIgnoreCase("gueststars")){//
			this.in_guestStarsTag = true;
		}else if(localName.equalsIgnoreCase("seasonnumber")){//
			this.in_seasonNumberTag = true;
		}else if(localName.equalsIgnoreCase("ratingcount")){//
			this.in_ratingCountTag = true;
		}else if(localName.equalsIgnoreCase("filename")){//
			this.in_filenameTag = true;
		}else if(localName.equalsIgnoreCase("lastupdated")){//
			this.in_lastUpdatedTag = true;
		}else if(localName.equalsIgnoreCase("id")){//
			this.in_idTag = true;
		}else if(localName.equalsIgnoreCase("overview")){//
			this.in_overviewTag = true;
		}else if(localName.equalsIgnoreCase("rating")){//
			this.in_ratingTag = true;
		}else if(localName.equalsIgnoreCase("seriesid")){//
			this.in_seriesidTag = true;
		}else if(localName.equalsIgnoreCase("FirstAired")){//
			this.in_firstairedTag = true;
		}else if(localName.equalsIgnoreCase("Airs_Time")){
			this.in_airstimeTag = true;
		}else if(localName.equalsIgnoreCase("SeriesName")){
			this.in_seriesnameTag = true;
		}else if(localName.equalsIgnoreCase("Poster")){
			this.in_poster = true;
		}
	}
	
	@Override
	public void endElement(String namespaceURI, String localName, String qNames) throws SAXException{
		
		if(localName.equalsIgnoreCase("episode")){//
			this.in_episodeTag = false;
		}else if(localName.equalsIgnoreCase("episodename")){//
			this.in_episodeNameTag = false;
		}else if(localName.equalsIgnoreCase("episodenumber")){//
			this.in_episodeNumberTag = false;
		}else if(localName.equalsIgnoreCase("gueststars")){//
			this.in_guestStarsTag = false;
		}else if(localName.equalsIgnoreCase("seasonnumber")){//
			this.in_seasonNumberTag = false;
		}else if(localName.equalsIgnoreCase("ratingcount")){//
			this.in_ratingCountTag = false;
		}else if(localName.equalsIgnoreCase("filename")){//
			this.in_filenameTag = false;
		}else if(localName.equalsIgnoreCase("lastupdated")){//
			this.in_lastUpdatedTag = false;
		}else if(localName.equalsIgnoreCase("id")){//
			this.in_idTag = false;
		}else if(localName.equalsIgnoreCase("overview")){//
			this.in_overviewTag = false;
		}else if(localName.equalsIgnoreCase("rating")){//
			this.in_ratingTag = false;
		}else if(localName.equalsIgnoreCase("seriesid")){//
			this.in_seriesidTag = false;
		}else if(localName.equalsIgnoreCase("FirstAired")){//
			this.in_firstairedTag = false;
		}else if(localName.equalsIgnoreCase("Airs_Time")){
			this.in_airstimeTag = false;
		}else if(localName.equalsIgnoreCase("SeriesName")){
			this.in_seriesnameTag = false;
		}else if(localName.equalsIgnoreCase("Poster")){
			this.in_poster = false;
		}
		
	}
	
	@Override
	public void characters(char ch[], int start, int length){
		String chars = new String(ch, start, length);
		
		if(this.in_episodeTag){
			//Get all the information for each episode
			if(this.in_idTag){
				episode.ID = episode.ID == null ? chars : episode.ID + chars;
			}else if(this.in_episodeNameTag){
				episode.TITLE = episode.TITLE == null ? chars : episode.TITLE + chars;
			}else if(this.in_overviewTag){
				episode.OVERVIEW = episode.OVERVIEW == null ? chars : episode.OVERVIEW + chars;
			}else if(this.in_filenameTag){
				episode.IMAGE_URL = episode.IMAGE_URL == null ? chars : episode.IMAGE_URL + chars;
			}else if(this.in_guestStarsTag){
				episode.GUEST_STARS = episode.GUEST_STARS == null ? chars : episode.GUEST_STARS + chars;
			}else if(this.in_ratingTag){
				//episode.RATING = new Float(chars).floatValue();
				episode.RATING = Float.valueOf(chars);
			}else if(this.in_ratingCountTag){
				//episode.RATING_COUNT = new Float(chars).floatValue();
				episode.RATING_COUNT = Float.valueOf(chars);
			}else if(this.in_episodeNumberTag){
				episode.EPISODE_NUMBER = Integer.parseInt(chars);
			}else if(this.in_seasonNumberTag){
				episode.SEASON_NUMBER = Integer.parseInt(chars);
			}else if(this.in_lastUpdatedTag){
				episode.LAST_UPDATED = episode.LAST_UPDATED == null ? chars : episode.LAST_UPDATED + chars;
			}else if(this.in_firstairedTag){
				episode.FIRST_AIRED = episode.FIRST_AIRED == null ? chars : episode.FIRST_AIRED + chars;
			}else if(this.in_seriesidTag){
				episode.SERIES_ID = episode.SERIES_ID == null ? chars : episode.SERIES_ID + chars;
			}else if(this.in_airstimeTag){
				episode.AIRS_TIME = episode.AIRS_TIME == null ? chars : episode.AIRS_TIME + chars;
			}else if(this.in_seriesnameTag){
				episode.SERIES_NAME = episode.SERIES_ID == null ? chars : episode.SERIES_NAME + chars;
			}else if(this.in_poster){
				episode.POSTER_URL = episode.POSTER_URL == null ? chars : episode.POSTER_URL + chars;
			}
			
		}
	}
}





















