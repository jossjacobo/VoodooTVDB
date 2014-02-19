package voodoo.tvdb.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class Reminder implements Parcelable{
	
	public static final int TYPE_ITEM = 0;
	public static final int TYPE_SEPARATOR = 1;

	public String EPISODE_ID;
	public String DATE;
	public String TIME;
	public String SERIES_NAME;
	public String EPISODE_NAME;
	public int SEASON_NUMBER;
	public int EPISODE_NUMBER;
	public String SERIES_ID;
	public float RATING;
	public String OVERVIEW;
	public String GUESTSTARS;
	public String IMAGE_URL;
	public int TYPE;
	public String DIVIDER_TEXT;
	
	/** Default Constructor */
	public Reminder(){
		this.EPISODE_ID = null;
		this.DATE = null;
		this.TIME = null;
		this.SERIES_NAME = null;
		this.EPISODE_NAME = null;
		this.SEASON_NUMBER = -1;
		this.EPISODE_NUMBER = 0;
		this.SERIES_ID = null;
		this.RATING = 0;
		this.OVERVIEW = null;
		this.GUESTSTARS = null;
		this.IMAGE_URL = null;
		this.TYPE = TYPE_ITEM;
		this.DIVIDER_TEXT = null;
	}
	
	/**
	 * Constructor used by the Creator to populate
	 * the object from the Parcel data @param source
	 */
	public Reminder(Parcel source){
		EPISODE_ID = source.readString();
		DATE = source.readString();
		TIME = source.readString();
		SERIES_NAME = source.readString();
		EPISODE_NAME = source.readString();
		SEASON_NUMBER = source.readInt();
		EPISODE_NUMBER = source.readInt();
		SERIES_ID = source.readString();
		RATING = source.readFloat();
		OVERVIEW = source.readString();
		GUESTSTARS = source.readString();
		IMAGE_URL = source.readString();
		TYPE = source.readInt();
		DIVIDER_TEXT = source.readString();
	}
	@Override
	public int describeContents() {
		return 0;
	}

	/** Write your Object's data to the passed-in Parcel */
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(EPISODE_ID);
		out.writeString(DATE);
		out.writeString(TIME);
		out.writeString(SERIES_NAME);
		out.writeString(EPISODE_NAME);
		out.writeInt(SEASON_NUMBER);
		out.writeInt(EPISODE_NUMBER);
		out.writeString(SERIES_ID);
		out.writeFloat(RATING);
		out.writeString(OVERVIEW);
		out.writeString(GUESTSTARS);
		out.writeString(IMAGE_URL);
		out.writeInt(TYPE);
		out.writeString(DIVIDER_TEXT);
	}
	
	/**
	 * This is used to regenerate your objects. All Parcelables
	 * must have a CREATOR that implements these two methods
	 */
	public static final Creator<Reminder> CREATOR = new Creator<Reminder>() {

		@Override
		public Reminder createFromParcel(Parcel source) {
			return new Reminder(source);
		}

		@Override
		public Reminder[] newArray(int size) {
			return new Reminder[size];
		}
		
	};
	
	public int compareTo(Reminder r){
		
		if(this.TYPE != r.TYPE){
			return 0;
		}
			
		
		Integer thisId = Integer.getInteger(this.EPISODE_ID);
		Integer otherId = Integer.getInteger(r.EPISODE_ID);
		
		if(thisId != otherId){
			return 0;
		}
		
		return 1;
	}
	
	
	@Override
	public String toString(){
		
		return "EPISODE_ID:" + EPISODE_ID
				+ " DATE:" + DATE
				+ " TIME:" + TIME
				+ " SERIES_NAME:" + SERIES_NAME
				+ " EPISODE_NAME:" + EPISODE_NAME
				+ " SEASON_NUMBER:" + SEASON_NUMBER
				+ " EPISODE_NUMBER:" + EPISODE_NUMBER
				+ " SERIES_ID:" + SERIES_ID
				+ " RATING:" + RATING
				+ " OVERVIEW:" + "NAH LATER HAHA"
				+ " GUESTSTARS:" + GUESTSTARS
				+ " IMAGE_URL:" + IMAGE_URL
				+ " TYPE:" + TYPE
				+ " DIVIDER_TEXT:" + DIVIDER_TEXT;
	}
}

























