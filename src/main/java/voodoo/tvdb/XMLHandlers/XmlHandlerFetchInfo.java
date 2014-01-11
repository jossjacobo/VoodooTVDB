package voodoo.tvdb.XMLHandlers;

import android.content.Context;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import voodoo.tvdb.Objects.Series;


public class XmlHandlerFetchInfo extends DefaultHandler{
	
	/** Fields */
	@SuppressWarnings("unused")
	private boolean in_dataTag = false;
	private boolean in_seriesTag = false;
	private boolean in_idTag = false;
	private boolean in_actorsTag = false;
	private boolean in_airsDayOfWeekTag = false;
	private boolean in_airTimeTag = false;
	private boolean in_genreTag = false;
	private boolean in_IMDBTag = false;
	private boolean in_networkTag = false;
	private boolean in_overviewTag = false;
	private boolean in_ratingTag = false;
	private boolean in_runtimeTag = false;
	private boolean in_seriesNameTag = false;
	private boolean in_statusTag = false;
	private boolean in_posterTag = false;
	
	private Series series;
	private Context context;
	
	public XmlHandlerFetchInfo(Context context){

		this.context = context;
		
	}
	
	public Series getSeries(){
		return series;
	}
	
	/** Methods */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
		
		if(localName.equalsIgnoreCase("data")){
			this.in_dataTag = true;
		}else if(localName.equalsIgnoreCase("series")){
			this.in_seriesTag = true;
			series = new Series();
		}else if(localName.equalsIgnoreCase("id")){
			this.in_idTag = true;
		}else if(localName.equalsIgnoreCase("actors")){
			this.in_actorsTag = true;
		}else if(localName.equalsIgnoreCase("airs_dayofweek")){
			this.in_airsDayOfWeekTag = true;
		}else if(localName.equalsIgnoreCase("airs_time")){
			this.in_airTimeTag = true;
		}else if(localName.equalsIgnoreCase("genre")){
			this.in_genreTag = true;
		}else if(localName.equalsIgnoreCase("imdb_id")){
			this.in_IMDBTag = true;
		}else if(localName.equalsIgnoreCase("network")){
			this.in_networkTag = true;
		}else if(localName.equalsIgnoreCase("overview")){
			this.in_overviewTag = true;
		}else if(localName.equalsIgnoreCase("rating")){
			this.in_ratingTag = true;
		}else if(localName.equalsIgnoreCase("runtime")){
			this.in_runtimeTag = true;
		}else if(localName.equalsIgnoreCase("seriesname")){
			this.in_seriesNameTag = true;
		}else if(localName.equalsIgnoreCase("status")){
			this.in_statusTag = true;
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
		}else if(localName.equalsIgnoreCase("id")){
			this.in_idTag = false;
		}else if(localName.equalsIgnoreCase("actors")){
			this.in_actorsTag = false;
		}else if(localName.equalsIgnoreCase("airs_dayofweek")){
			this.in_airsDayOfWeekTag = false;
		}else if(localName.equalsIgnoreCase("airs_time")){
			this.in_airTimeTag = false;
		}else if(localName.equalsIgnoreCase("genre")){
			this.in_genreTag = false;
		}else if(localName.equalsIgnoreCase("imdb_id")){
			this.in_IMDBTag = false;
		}else if(localName.equalsIgnoreCase("network")){
			this.in_networkTag = false;
		}else if(localName.equalsIgnoreCase("overview")){
			this.in_overviewTag = false;
		}else if(localName.equalsIgnoreCase("rating")){
			this.in_ratingTag = false;
		}else if(localName.equalsIgnoreCase("runtime")){
			this.in_runtimeTag = false;
		}else if(localName.equalsIgnoreCase("seriesname")){
			this.in_seriesNameTag = false;
		}else if(localName.equalsIgnoreCase("status")){
			this.in_statusTag = false;
		}else if(localName.equalsIgnoreCase("poster")){
			this.in_posterTag = false;
		}
	}
	
	@Override
	public void characters(char ch[], int start, int length){
		String chars = new String(ch, start, length);
		if(this.in_seriesTag){
			if(this.in_posterTag){
				series.POSTER_URL = series.POSTER_URL == null ? chars : series.POSTER_URL + chars;
			}else if(this.in_statusTag){
				series.STATUS = series.STATUS == null ? chars : series.STATUS + chars;
			}else if(this.in_seriesNameTag){
				series.TITLE = series.TITLE == null ? chars : series.TITLE + chars;
			}else if(this.in_runtimeTag){
				series.RUNTIME = series.RUNTIME == null ? chars : series.RUNTIME + chars;
			}else if(this.in_ratingTag){
				//series.RATING = new Float(chars).floatValue();
				series.RATING = Float.valueOf(chars);
			}else if(this.in_overviewTag){
				series.OVERVIEW = series.OVERVIEW == null ? chars : series.OVERVIEW + chars;
			}else if(this.in_networkTag){
				series.NETWORK = series.NETWORK == null ? chars : series.OVERVIEW + chars;
			}else if(this.in_IMDBTag){
				series.IMDB_ID = series.IMDB_ID == null ? chars : series.IMDB_ID + chars;
			}else if(this.in_genreTag){
				series.GENRE = series.GENRE == null ? chars : series.GENRE + chars;
			}else if(this.in_airTimeTag){
				series.AIRS_TIME = series.AIRS_TIME == null ? chars : series.AIRS_TIME + chars;
			}else if(this.in_airsDayOfWeekTag){
				series.AIRS_DAYOFWEEK = series.AIRS_DAYOFWEEK == null ? chars : series.AIRS_DAYOFWEEK + chars;
			}else if(this.in_actorsTag){
				series.ACTORS = series.ACTORS == null ? chars : series.ACTORS + chars;
			}else if(this.in_idTag){
				series.ID = series.ID == null ? chars : series.ID + chars;
			}
		}
	}
}
















