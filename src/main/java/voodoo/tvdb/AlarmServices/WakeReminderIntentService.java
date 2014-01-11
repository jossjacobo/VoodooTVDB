package voodoo.tvdb.AlarmServices;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public abstract class WakeReminderIntentService extends IntentService {
	// This abstract method is implemented in any children of this class
	abstract void doReminderWork(Intent intent);
	// This is the tag name of the lock that will use to acquire
	// the CPU lock.
	public static final String LOCK_NAME_STATIC = "voodoo.tvdb.static";
	private static PowerManager.WakeLock lockStatic = null;
	
	public static void acquireStaticLock(Context context){
		getLock(context).acquire();
	}
	
	synchronized private static PowerManager.WakeLock getLock(Context context){
		if(lockStatic == null){
			//Creates new WakeLock
			PowerManager powManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			
			lockStatic = powManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
		}
		return (lockStatic);
	}
	public WakeReminderIntentService(String name) {
		super(name);
	}
	
	/**
	 * This is the onHandlerIntent() call of the IntentService. As soon as the 
	 * service is started, this method is called to handle the intent
	 * that was passed to it.
	 */
	@Override
	final protected void onHandleIntent(Intent intent){
		// This service attempts to perform the necessary work by
		// calling doReminderWork
		try{
			doReminderWork(intent);
		}finally{
			//Regardless if the doReminderWork is successful or not i want to 
			//release the WakeLock. If i don't the device could be left ON
			//until the phone is REBOOTED.
			getLock(this).release();
		}
	}
	
}