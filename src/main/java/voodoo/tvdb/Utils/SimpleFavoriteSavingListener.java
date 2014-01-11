package voodoo.tvdb.Utils;

/**
 * A convenient class to extends when you only want to listen
 * for the process of saving a Series to the Database.
 * 
 * This implements all methods in the view {@link FavoriteSavingListener} 
 * but does nothing.
 * 
 * @author Team2
 *
 */
public class SimpleFavoriteSavingListener implements FavoriteSavingListener{

	@Override
	public void onSavingCompleted(String series_id) {
		// empty implementation
		
	}

    @Override
    public void onDeleteCompleted(String series_id){
        // empty implemenation
    }

}
