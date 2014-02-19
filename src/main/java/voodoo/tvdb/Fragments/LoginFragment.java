package voodoo.tvdb.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import voodoo.tvdb.alarmServices.ReminderManager;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.UserFunctions;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

/**
 * Created by Voodoo Home on 11/2/13.
 */
public class LoginFragment extends BaseFragment{

    private static final String TAG = "LoginFragment";

    // Buttons & Inputs
    Button loginButton;
    Button registerLinkButton;
    EditText inputEmail;
    EditText inputPassword;
    TextView loginErrorMessage;
    CheckBox syncCheckbox;

    /** JSON Response node names */
    private static String KEY_SUCCESS = "success";
    //private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private static String KEY_UID = "uid";
    private static String KEY_NAME = "name";
    private static String KEY_EMAIL = "email";
    //private static String KEY_CREATED_AT = "created_at";

    private LoginListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState){
        View view = inflater.inflate(R.layout.login, container, false);

        // ActionBar
        setupActionBar();

        // Click Events
        loginButton = (Button) view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().toLowerCase(Locale.ENGLISH).trim();
                String password = inputPassword.getText().toString();

                new loginAsync(context, email, password, syncCheckbox.isChecked() ).execute("");
            }

        });

        // Link to RegisterActivity Screen
        registerLinkButton = (Button) view.findViewById(R.id.login_register_link);
        registerLinkButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                listener.onRegisterClicked();
            }

        });

        inputEmail = (EditText) view.findViewById(R.id.login_email);
        inputPassword = (EditText) view.findViewById(R.id.login_password);
        loginErrorMessage = (TextView) view.findViewById(R.id.login_error);
        syncCheckbox = (CheckBox) view.findViewById(R.id.login_sync_checkbox);

        return view;
    }

    private void setupActionBar() {
        ActionBar actionBar = context.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setIcon(R.drawable.icon);

        setActionBarTitle(getResources().getString(R.string.login_title));
    }

    private class loginAsync extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private ProgressDialog dialog;

        String email;
        String password;
        boolean sync;

        private AsyncTask<String, Void, JSONObject> myLoginAsync = null;

        //Constructor
        public loginAsync(Activity activity, String email, String password, boolean sync){
            context = activity;
            dialog = new ProgressDialog(context);

            this.email = email;
            this.password = password;
            this.sync = sync;
        }

        @Override
        protected void onPreExecute(){
            dialog.setMessage("Please wait...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    myLoginAsync.cancel(true);
                }
            });
            dialog.show();
            myLoginAsync = this;


        }

        @Override
        protected JSONObject doInBackground(String... params) {

            UserFunctions userFunctions = new UserFunctions(context);

            JSONObject json = userFunctions.loginUser(email, password);

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json){

            /** Dissmiss the Dialog box */
            dialog.dismiss();

            /** User Functions */
            UserFunctions userFunctions = new UserFunctions(context);

            if(json != null){

                /** Check for login response */
                try{

                    if(json.getString(KEY_SUCCESS) != null){

                        loginErrorMessage.setText("");
                        String res = json.getString(KEY_SUCCESS);

                        DatabaseAdapter db = new DatabaseAdapter(context);
                        db.open();

                        if(Integer.parseInt(res) == 1){

                            // User successfully logged in
                            // Store user details in SQLite Database

                            JSONObject json_user = json.getJSONObject("user");

                            Log.d("LOGIN", json_user.toString());

                            /**
                             *  Clear all previous data in database
                             */
                            //userFunctions.logoutUser();

                            /**
                             * Add user to database
                             */
                            db.addUser(json_user.getString(KEY_NAME),
                                    json_user.getString(KEY_EMAIL),
                                    json.getString(KEY_UID),
                                    null);

                            /**
                             * Set Sync Preference Flag
                             */
                            userFunctions.setSync(sync);


                            // Toast
                            Toast.makeText(context, "LoginActivity successful", Toast.LENGTH_SHORT).show();

                            /**
                             * Determine if you need to setup the Sync Service
                             */
                            if(userFunctions.getSyncStatus()){

                                // Start Sync Service
                                ReminderManager rm = new ReminderManager(context);
                                rm.setSyncUpdateService();

                            }

                            // Close database
                            db.close();

                            // Close LoginActivity Screen
                            listener.onLogin();
                        }else{
                            // Error in LoginActivity
                            loginErrorMessage.setText(json.getString(KEY_ERROR_MSG));
                            db.close();
                        }

                    }

                } catch (JSONException e){
                    e.printStackTrace();
                    loginErrorMessage.setText("Error Occured during Registration");
                }
            }else{
                loginErrorMessage.setText("Error, no network connection");
            }

        }
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        if(activity instanceof LoginListener){
            listener = (LoginListener) activity;
        }else{
            throw new ClassCastException(activity.toString()
                    + " must implement LoginFragment.LoginListener");
        }

    }

    public interface LoginListener{
        public void onLogin();
        public void onRegisterClicked();
    }
}


























