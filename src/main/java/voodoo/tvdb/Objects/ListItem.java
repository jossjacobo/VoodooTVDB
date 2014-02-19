package voodoo.tvdb.objects;

public class ListItem {
	
	// KEYS
	public static final String KEY_TRUE = "t";
	public static final String KEY_FALSE = "f";
	public static final String KEY_EMPTY = "e";
	public static final String KEY_TYPE_SHOW = "show";
	
	public String ID;
	public String LIST_NAME;
	public String TYPE;
	public String SERIES_ID;
	public String TITLE;
	public String SYNCHED;
	public String DELETED;
	public String MISC1;
	public String MISC2;
	
	public ListItem(){
		
	}
	
	/**
	 * Default Object
	 * @param seriesId
	 * @param listName
	 * @param title
	 */
	public ListItem(String seriesId, String listName, String title){
		
		LIST_NAME = listName;
		TYPE = KEY_TYPE_SHOW;
		SERIES_ID = seriesId;
		TITLE = title;
		SYNCHED = KEY_FALSE;
		DELETED = KEY_FALSE;
		MISC1 = KEY_EMPTY;
		MISC2 = KEY_EMPTY;
		
	}
	
	public String toString(){
		return "ROWID=" + ID +
			" LIST_NAME=" + LIST_NAME + 
			" TYPE=" + TYPE + 
			" SERIES_ID=" + SERIES_ID + 
			" TITLE=" + TITLE + 
			" SYNCHED=" + SYNCHED + 
			" DELETED=" + DELETED + 
			" MYSC1=" + MISC1 + 
			" MYSC2=" + MISC2;
	}
	
}
