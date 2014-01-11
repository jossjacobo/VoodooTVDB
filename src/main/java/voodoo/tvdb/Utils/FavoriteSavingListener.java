package voodoo.tvdb.Utils;

public interface FavoriteSavingListener {

	/**
	 * Is called when the Show saving has started
	 * 
	 * @param series_id		ID for the series
	 
	void onSavingStarted(String series_id);*/
	
	/**
	 * Is called when an error occurred during the saving process.
	 * 
	 * @param series_id		ID for the series 
	 * @param errorMessage	String error message
	 
	void onSavingFailed(String series_id, String errorMessage);*/
	
	/**
	 * Is called when the series was successfully saved.
	 * 
	 * @param series_id		ID for the series
	 */
	void onSavingCompleted(String series_id);

    void onDeleteCompleted(String series_id);
	
}
