package voodoo.tvdb.Objects;

public class ListObject {
	
	// KEYS
	public static final String KEY_TRUE = "t";
	public static final String KEY_FALSE = "f";
	public static final String KEY_EMPTY = "e";
		
	public String ID;
	public String NAME;
	public String SLUG;
	public String PRIVACY;
	public String DESCRIPTION;
	public String SHOW_NUMBERS;
	public String ALLOW_COMMENTS;
	public String SYNCHED;
	public String CREATED;
	public String DELETED;
	public String MISC1;
	public String MISC2;
	
	/**
	 * Empty Object
	 */
	public ListObject(){
		
	}
	
	/**
	 * Default Object
	 * @param name
	 * @param privacy set to "public"
	 * @param description
	 * @param show_numbers set to "true"
	 * @param allow_comments set to "true"
	 * @param synched set to "false"
	 * @param created set to "false"
	 * @param deleted set to "false"
	 * @param misc1 set to ""
	 * @param misc2 set to ""
	 */
	public ListObject(String name, String description){
		this.NAME = name;
		this.PRIVACY = "public";
		this.DESCRIPTION = description;
		this.SHOW_NUMBERS = KEY_FALSE;
		this.ALLOW_COMMENTS = KEY_TRUE;
		this.SYNCHED = KEY_FALSE;
		this.CREATED = KEY_FALSE;
		this.DELETED = KEY_FALSE;
		this.MISC1 = KEY_EMPTY;
		this.MISC2 = KEY_EMPTY;
	}
	
	public String toString(){
		return "[ID=" + ID + ", NAME=" + NAME + ", " + 
				" SLUG=" + SLUG + ", PRIVACY=" + PRIVACY + 
				" DESCRIPTION=" + DESCRIPTION + ", SHOW_NUMBERS=" + SHOW_NUMBERS +
				" ALLOW_COMMENTS=" + ALLOW_COMMENTS + ", SYNCHED=" + SYNCHED + 
				" CREATED=" + CREATED + ", DELETED=" + DELETED;
	}
}
