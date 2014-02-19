package voodoo.tvdb.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class Episode implements Parcelable{

	// KEYS
	public static final String KEY_TRUE = "t";
	public static final String KEY_FALSE = "f";
	public static final int NOT_WATCHED = 0;
	public static final int WATCHED = 1;
	
	public String ID;
	public String SERIES_ID;
	public String TITLE;
	public String OVERVIEW;
	public String IMAGE_URL;
	public String GUEST_STARS;
	public float RATING;
	public float RATING_COUNT;
	public int EPISODE_NUMBER;
	public int SEASON_NUMBER;
	public String LAST_UPDATED;
	public String FIRST_AIRED;
	public int REMINDER;
	public int TYPE;
	public String DIVIDER_TEXT;
	public String AIRS_TIME;
	public String SERIES_NAME;
	public String POSTER_URL;
	public String SYNCHED;
	public String DELETED;
	
	/** Added on version 0.9.13 */
	public String PREVIOUS_EPISODE_ID;
	
	/** Default Constructor */
	public Episode(){
		this.ID = null;
		this.SERIES_ID = null;
		this.TITLE = null;
		this.OVERVIEW = null;
		this.IMAGE_URL = null;
		this.GUEST_STARS = null;
		this.RATING = 0;
		this.RATING_COUNT = 0;
		this.EPISODE_NUMBER = 0;
		this.SEASON_NUMBER = 0;
		this.LAST_UPDATED = null;
		this.FIRST_AIRED = null;
		this.REMINDER = 0;
		this.TYPE = 0;	
		this.DIVIDER_TEXT = null;
		this.AIRS_TIME = null;
		this.SERIES_NAME = null;
		this.POSTER_URL = null;
		this.SYNCHED = KEY_FALSE;
		this.DELETED = KEY_FALSE;
		this.PREVIOUS_EPISODE_ID = null;
	}
	
	/** 
	 * Constructor used by the Creator to populate the object from 
	 * the Parcel data @param source
	 */
	public Episode(Parcel source){
		ID = source.readString();
		SERIES_ID = source.readString();
		TITLE = source.readString();
		OVERVIEW = source.readString();
		IMAGE_URL = source.readString();
		GUEST_STARS = source.readString();
		RATING = source.readFloat();
		RATING_COUNT = source.readFloat();
		EPISODE_NUMBER = source.readInt();
		SEASON_NUMBER = source.readInt();
		LAST_UPDATED = source.readString();
		FIRST_AIRED = source.readString();
		REMINDER = source.readInt();
		TYPE = source.readInt();
		DIVIDER_TEXT = source.readString();
		AIRS_TIME = source.readString();
		SERIES_NAME = source.readString();
		POSTER_URL = source.readString();
		SYNCHED = source.readString();
		DELETED = source.readString();
		PREVIOUS_EPISODE_ID = source.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	/** Write your object's data to the passed-in Parcel */
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(ID);
		out.writeString(SERIES_ID);
		out.writeString(TITLE);
		out.writeString(OVERVIEW);
		out.writeString(IMAGE_URL);
		out.writeString(GUEST_STARS);
		out.writeFloat(RATING);
		out.writeFloat(RATING_COUNT);
		out.writeInt(EPISODE_NUMBER);
		out.writeInt(SEASON_NUMBER);
		out.writeString(LAST_UPDATED);
		out.writeString(FIRST_AIRED);
		out.writeInt(REMINDER);
		out.writeInt(TYPE);
		out.writeString(DIVIDER_TEXT);
		out.writeString(AIRS_TIME);
		out.writeString(SERIES_NAME);
		out.writeString(POSTER_URL);
		out.writeString(SYNCHED);
		out.writeString(DELETED);
		out.writeString(PREVIOUS_EPISODE_ID);
	}
	
	/** 
	 * This is used to regenerate your object. All Parcelables must 
	 * have a CREATOR that implements these two methods
	 */
	public static final Creator<Episode> CREATOR = new Creator<Episode>() {

		@Override
		public Episode createFromParcel(Parcel source) {
			return new Episode(source);
		}

		@Override
		public Episode[] newArray(int size) {
			return new Episode[size];
		}
	};
	
	public int compareTo(Episode e){
		
		if(this.TYPE != e.TYPE){
			return 0;
		}
			
		if(!e.ID.equals(ID)){
			return 0;
		}
		
		return 1;
	}
	
	public String toString(){
		return "ID = " + ID + " || " + " SERIES_ID = " + SERIES_ID + " || " 
		+ " TITLE = " + TITLE + " || " + " IMAGE_URL = " + IMAGE_URL + " || "
		+ " RATING = " + RATING + " || " + " RATING COUNT = " + RATING_COUNT + " || "
		+ " EPISODE_NUMBER = " + EPISODE_NUMBER + " || " + " SEASON_NUMBR = " + SEASON_NUMBER + " || "
		+ " LAST_UPDATED = " + LAST_UPDATED + " || " + " FIRST_AIRED = " + FIRST_AIRED + " || " 
		+ " REMINDER = " + REMINDER + " || " + " GUEST_STARS = " + GUEST_STARS + " || "
		;
	}
	
}


















