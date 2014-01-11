package voodoo.tvdb.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import voodoo.tvdb.Objects.ListObject;

public class ListManager {

	// TAG
	//private static final String TAG = "ListManager";
	
	// List Helper
	ListHelper listHelper;
	
	// List Names
	ArrayList<CharSequence> listNames;
	
	// Alert Items Selected
	ArrayList<Integer> mSelectedItems;
	
	// Alert
	AlertDialog.Builder builder;
	
	// Context
	Context context;
	
	public ListManager(Context c){
		
		// Context
		context = c;
		
		// Create List Helper
		listHelper = new ListHelper(context);
		
		// Get List Names
		listNames = listHelper.getAllListNames();
	
		// Alert Builder
		builder = new AlertDialog.Builder(context);
		
		// Alert Items Selected
		mSelectedItems = new ArrayList<Integer>();
		
		// Create the Alert
		createAlert();
	}
	
	public AlertDialog.Builder getAlertBuilder(){
		return builder;
	}
	
	private void createAlert() {
		
		// Default Settings
		builder.setCancelable(true)
		.setTitle("Manage your lists")
		.setPositiveButton("New List", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				// Close Current Dialog and Open another
				dialog.dismiss();
				
				AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
				builder1.setCancelable(true);
				builder1.setMessage("List Name:");
				
				//Set and EditText view to get user input
				//have the default name be a time stamp
				final EditText input = new EditText(context);
				input.setSingleLine();
				builder1.setView(input);
				
				//Set Positive button
				builder1.setPositiveButton("Create", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog1, int which) {
						
						String name = input.getText().toString();
						
						if(name.compareTo("") == 0){
							
							Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show();
							
						}else{
							
							if(listHelper.insertList(name, ListObject.KEY_EMPTY)){
								Toast.makeText(context, "List " + name + " Created!", Toast.LENGTH_SHORT).show();
							}else{
								Toast.makeText(context, "Error occured", Toast.LENGTH_SHORT).show();
							}
							dialog1.dismiss();
							
						}
					}
				});
				
				//Set Negative button
				builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog1, int which) {
						dialog1.dismiss();
					}
				});
				builder1.create();
				builder1.show();
			}
		})
		.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				// Close
				dialog.dismiss();
				
			}
		});
		
		if(listNames != null){
			builder.setMultiChoiceItems(listNames.toArray(new CharSequence[listNames.size()]), null, new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					if (isChecked) {
	                   // If the user checked the item, add it to the selected items
	                   mSelectedItems.add(which);
	               } else if (mSelectedItems.contains(which)) {
	                   // Else, if the item is already in the array, remove it 
	                   mSelectedItems.remove(Integer.valueOf(which));
	               }
				}
			})
			.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					// List Selected (If any)
					if(mSelectedItems.size() > 0){
					
						ArrayList<String> names = new ArrayList<String>();
						
						for(int i = 0; i < mSelectedItems.size(); i++){
							names.add(listNames.get(mSelectedItems.get(i)).toString());
						}
						
						listHelper.flagListsAsDeleted(names);
						
					}else{
						
						Toast.makeText(context, "No List Selected", Toast.LENGTH_SHORT).show();
					
					}
					
				}
			});
			
		}else{
			
			builder.setMessage("No lists found. Create some lists to get started!");
			
		}
	}
}































