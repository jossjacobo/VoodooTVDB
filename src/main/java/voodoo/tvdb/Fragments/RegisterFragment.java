package voodoo.tvdb.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
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

import voodoo.tvdb.R;
import voodoo.tvdb.alarmServices.ReminderManager;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;
import voodoo.tvdb.utils.UserFunctions;

/**
 * Created by Voodoo Home on 11/2/13.
 */
public class RegisterFragment extends BaseFragment{

    Button registerButton;
    Button loginLinkButton;
    EditText inputUsername;
    EditText inputEmail;
    EditText inputPassword;
    TextView registerErrorMessage;
    CheckBox traktCheckbox;

    private static String KEY_SUCCESS = "success";
    //private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private static String KEY_UID = "uid";
    private static String KEY_NAME = "name";
    private static String KEY_EMAIL = "email";

    private RegisterListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState){
        View view = inflater.inflate(R.layout.register, container, false);

        setupActionBar();

        // Click events
        registerButton = (Button) view.findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                builder.setTitle("Trakt.tv");
                builder.setMessage("Registering for Voodoo TVDB will also create a Trakt.tv account for you. This will allow us to give you an even greater and more personalized experience!");
                builder.setPositiveButton("Cool!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String username = inputUsername.getText().toString().toLowerCase(Locale.ENGLISH).trim();
                        String email = inputEmail.getText().toString().toLowerCase(Locale.ENGLISH).trim();
                        String password = inputPassword.getText().toString();

                        new registerAsync(context, username, email, password, true, traktCheckbox.isChecked()).execute("");
                    }
                });
                builder.setNegativeButton("Nah", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }

        });

        // Link to LoginActivity Screen
        loginLinkButton = (Button) view.findViewById(R.id.register_login_link);
        loginLinkButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                listener.onLoginClicked();
            }

        });

        inputUsername = (EditText) view.findViewById(R.id.register_username);
        inputEmail = (EditText) view.findViewById(R.id.register_email);
        inputPassword = (EditText) view.findViewById(R.id.register_password);
        registerErrorMessage = (TextView) view.findViewById(R.id.register_error);
        traktCheckbox = (CheckBox) view.findViewById(R.id.register_trakt_checkbox);

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

        setActionBarTitle(getResources().getString(R.string.register_title));
    }

    private class registerAsync extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private ProgressDialog dialog;

        String username;
        String email;
        String password;
        boolean sync;
        boolean trakt;

        private AsyncTask<String, Void, JSONObject> myRegisterAsync = null;

        //Constructor
        public registerAsync(Activity activity, String username, String email, String password, boolean b, boolean c){

            context = activity;
            dialog = new ProgressDialog(context);

            this.username = username;
            this.email = email;
            this.password = password;
            this.sync = b;
            this.trakt = c;
        }

        @Override
        protected void onPreExecute(){
            dialog.setMessage("Please wait...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    myRegisterAsync.cancel(true);
                }
            });
            dialog.show();
            myRegisterAsync = this;


        }

        @Override
        protected JSONObject doInBackground(String... params) {

            UserFunctions userFunctions = new UserFunctions(context);

            JSONObject json = userFunctions.registerUser(username, email, password,trakt);

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json){

            dialog.dismiss();

            UserFunctions userFunctions = new UserFunctions(context);

            if(json != null){
                // Check for register response
                try{

                    if(json.getString(KEY_SUCCESS) != null){

                        registerErrorMessage.setText("");
                        String response = json.getString(KEY_SUCCESS);

                        DatabaseAdapter db = new DatabaseAdapter(context);
                        db.open();

                        if(Integer.parseInt(response) == 1){

                            // User successfully registered
                            // Store user details in SQLite Database

                            JSONObject json_voodootvdb_user = json.getJSONObject("voodootvdb");
                            JSONObject json_user = json_voodootvdb_user.getJSONObject("user");

                            // Clear all previous data in database
                            userFunctions.logOffUserStatic();

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

                            db.close();

                            // Toast
                            Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show();

                            /**
                             * Determine if you need to setup the Sync Service
                             */
                            if(userFunctions.getSyncStatus()){

                                // Start Sync Service
                                ReminderManager rm = new ReminderManager(context);
                                rm.setSyncUpdateService();

                            }

                            // Close RegisterActivity Screen
                            listener.onRegister();
                        }else{
                            // Error in registration
                            registerErrorMessage.setText(json.getString(KEY_ERROR_MSG));
                            db.close();
                        }
                    }

                } catch(JSONException e){
                    e.printStackTrace();
                    registerErrorMessage.setText("Error Occured during Registration");
                }
            }else{
                registerErrorMessage.setText("Error Occured during Registration");
            }


        }

    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        if(activity instanceof RegisterListener){
            listener = (RegisterListener) activity;
        }else{
            throw new ClassCastException(activity.toString()
                    + " must implement RegisterFragment.RegisterListener");
        }

    }


    public interface RegisterListener{
        public void onRegister();
        public void onLoginClicked();
    }
}





















