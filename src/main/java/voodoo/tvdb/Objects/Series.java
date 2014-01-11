package voodoo.tvdb.Objects;

import android.os.Parcel;
import android.os.Parcelable;

public class Series implements Parcelable{
	
	public final static int TYPE_ITEM = 0;
	public final static int TYPE_SEPARATOR = 1;
	
	/** Added on version 0.9.13 */
	public final static int SERIES_IS_NOT_IN_FAVES = 0;
	public final static int SERIES_IS_IN_FAVES = 1;
	
	public String ID;
	public String TITLE;
	public String OVERVIEW;
	public String POSTER_URL;
	public String ACTORS;
	public String AIRS_DAYOFWEEK;
	public String AIRS_TIME;
	public String GENRE;
	public String IMDB_ID;
	public String NETWORK;
	public float RATING;
	public String RUNTIME;
	public String STATUS;
	public String LAST_UPDATED;
	public float RATING_COUNT;
	public String CONTENT_RATING;
	public String FIRST_AIRED;
	
	public int TYPE;
	public String DIVIDER_TEXT;
	
	/** Added on version 0.9.13 */
	public int IS_FAVORITE;

	/** Default Constructor */
	public Series(){
		this.ID = null;
		this.TITLE = null;
		this.OVERVIEW = null;
		this.POSTER_URL = null;
		this.ACTORS = null;
		this.AIRS_DAYOFWEEK = null;
		this.AIRS_TIME = null;
		this.GENRE = null;
		this.IMDB_ID = null;
		this.NETWORK = null;
		this.RATING = 0;
		this.RUNTIME = null;
		this.STATUS = null;
		this.LAST_UPDATED = null;
		this.RATING_COUNT = 0;
		this.CONTENT_RATING = null;
		this.FIRST_AIRED = null;
		this.TYPE = TYPE_ITEM;
		this.DIVIDER_TEXT = null;
		this.IS_FAVORITE = SERIES_IS_NOT_IN_FAVES;
	}

	/**
	 * Constructor used by the Creator to populate the object
	 * from the Parcel data @param source
	 */
	public Series(Parcel source){
		ID = source.readString();
		TITLE = source.readString();
		OVERVIEW = source.readString();
		POSTER_URL = source.readString();
		ACTORS = source.readString();
		AIRS_DAYOFWEEK = source.readString();
		AIRS_TIME = source.readString();
		GENRE = source.readString();
		IMDB_ID = source.readString();
		NETWORK = source.readString();
		RATING = source.readFloat();
		RUNTIME = source.readString();
		STATUS = source.readString();
		LAST_UPDATED = source.readString();
		RATING_COUNT = source.readFloat();
		CONTENT_RATING = source.readString();
		FIRST_AIRED = source.readString();
		TYPE = source.readInt();
		DIVIDER_TEXT = source.readString();
		IS_FAVORITE = source.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}
	/** Write your object's data to the passed-in Parcel */
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(ID);
		out.writeString(TITLE);
		out.writeString(OVERVIEW);
		out.writeString(POSTER_URL);
		out.writeString(ACTORS);
		out.writeString(AIRS_DAYOFWEEK);
		out.writeString(AIRS_TIME);
		out.writeString(GENRE);
		out.writeString(IMDB_ID);
		out.writeString(NETWORK);
		out.writeFloat(RATING);
		out.writeString(RUNTIME);
		out.writeString(STATUS);
		out.writeString(LAST_UPDATED);		
		out.writeFloat(RATING_COUNT);
		out.writeString(CONTENT_RATING);
		out.writeString(FIRST_AIRED);
		out.writeInt(TYPE);
		out.writeString(DIVIDER_TEXT);
		out.writeInt(IS_FAVORITE);
	}
	
	/**
	 * This is used to regenerate your object. All Parcelables must
	 * have a CREATOR that implements these two methods.
	 */
	public static final Creator<Series> CREATOR = new Creator<Series>() {

		@Override
		public Series createFromParcel(Parcel source) {
			return new Series(source);
		}

		@Override
		public Series[] newArray(int size) {
			return new Series[size];
		}
	};
	
	public int compareTo(Series s){
		
		if(this.TYPE != s.TYPE){
			return 0;
		}
			
		if(!s.ID.equals(ID)){
			return 0;
		}
		
		if(this.IS_FAVORITE != s.IS_FAVORITE){
			return 0;
		}
		
		return 1;
	}
	
	/**
	 * 
	public String toString(){

		return "[ID=" + ID + ", TITLE=" + TITLE + ", OVERVIEW=" + OVERVIEW + ", POSTER_URL=" + POSTER_URL + ", ACTORS=" + ", AIRS_DAYOFWEEK=" + AIRS_DAYOFWEEK + ", AIRS_TIME=" + AIRS_TIME
				+ ", GENRE=" + GENRE + ", IMDB_ID=" + IMDB_ID + ", NETWORK=" + NETWORK + ", RATING=" + RATING + ", RUNTIME=" + RUNTIME + ", STATUS=" + STATUS 
				+ ", LAST_UPDATED=" + LAST_UPDATED + ", CONTENT_RATING=" + CONTENT_RATING + ", FIRST_AIRED=" + FIRST_AIRED + ", TYPE=" + TYPE + ", DIVIDER_TEXT=" + DIVIDER_TEXT + "]";
	
	}
	*/
}