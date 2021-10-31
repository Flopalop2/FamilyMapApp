package com.mbrow233.familymap;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mbrow233.familymap.data.DataCache;
import com.mbrow233.familymap.net.ServerProxy;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Model.Person;
import Request.LoginRequest;
import Request.RegisterRequest;
import Result.LoginResult;
import Result.RegisterResult;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String SUCCEED_KEY = "succeed_key";
    private String TAG = "Login Fragment";

    private EditText serverHostText;
    private EditText serverPortText;
    private EditText usernameText;
    private EditText passwordText;
    private EditText firstNameText;
    private EditText lastNameText;
    private EditText emailText;

    private ProgressBar spinner;

    private TextView hiddenTV; //todo: ask how better to check for radio button pressed.
    private RadioGroup genderButton;

    private Button loginButton;
    private Button registerButton;

    Boolean restoreButton = false;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        serverHostText = (EditText) v.findViewById(R.id.server_host_input);
        serverPortText = (EditText) v.findViewById(R.id.port_number_input);
        usernameText = (EditText) v.findViewById(R.id.username_input);
        passwordText = (EditText) v.findViewById(R.id.password_input);
        firstNameText = (EditText) v.findViewById(R.id.first_name_input);
        lastNameText = (EditText) v.findViewById(R.id.last_name_input);
        emailText = (EditText) v.findViewById(R.id.email_input);

        genderButton = (RadioGroup) v.findViewById(R.id.genderGroup);
        hiddenTV = (TextView) v.findViewById(R.id.gender_hidden);

        loginButton = (Button)v.findViewById(R.id.login_button);
        registerButton = (Button)v.findViewById(R.id.register_button);

        spinner=(ProgressBar) v.findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            serverHostText.setText(savedInstanceState.getString("serverHost"));
        }

        serverHostText.addTextChangedListener(loginTextWatcher);
        serverPortText.addTextChangedListener(loginTextWatcher);
        usernameText.addTextChangedListener(loginTextWatcher);
        passwordText.addTextChangedListener(loginTextWatcher);

        serverHostText.addTextChangedListener(registerTextWatcher);
        serverPortText.addTextChangedListener(registerTextWatcher);
        usernameText.addTextChangedListener(registerTextWatcher);
        passwordText.addTextChangedListener(registerTextWatcher);
        firstNameText.addTextChangedListener(registerTextWatcher);
        lastNameText.addTextChangedListener(registerTextWatcher);
        emailText.addTextChangedListener(registerTextWatcher);

        genderButton.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked) {
                    hiddenTV.setText("Checked:" + checkedRadioButton.getText());
                    //to not use this really sick hidden text method, make a global boolean for radio group and text fields being checked/filled in. check here if text is good, then enable button. check in textlistener if radiogroup is checked to enable button
                }
            }
        });

        hiddenTV.addTextChangedListener(registerTextWatcher);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity().getApplicationContext(), "Login button!", Toast.LENGTH_SHORT).show();

                //make loginrequest and pass it to async task
                LoginRequest request = new LoginRequest(usernameText.getText().toString(), passwordText.getText().toString());

                Log.i(TAG, "Before login handler");
                @SuppressLint("HandlerLeak") Handler uiThreadMessageHandler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        Log.i(TAG, "In handleMessage");
                        spinner.setVisibility(View.GONE);
                        Bundle bundle = message.getData();
                        //LoginResult result = bundle.getClass(KEY, null);
                        boolean succeeded = bundle.getBoolean(SUCCEED_KEY);
                        LoginResult result = (LoginResult) message.obj;
                        if (succeeded) {
                            Toast.makeText(getActivity().getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                            //send to data task (implement data functions in server proxy
                            Handler dataThreadMessageHandler = new Handler() {
                                @Override
                                public void handleMessage(Message message1) {
                                    Log.i(TAG, "In handlemessage2");
                                    Bundle bundle1 = message1.getData();
                                    boolean succeeded1 = bundle1.getBoolean(SUCCEED_KEY);
                                    if (succeeded1) {
                                        Map<String, Person> persons = DataCache.getInstance().getPeople();
                                        Person tempPerson = persons.get(result.getPersonID());
                                        Toast.makeText(getActivity().getApplicationContext(), tempPerson.getFirstName()+" "+tempPerson.getLastName(), Toast.LENGTH_SHORT).show();


                                        ((MainActivity)getActivity()).changeToMapFragment();

                                    }
                                    else {
                                        Toast.makeText(getActivity().getApplicationContext(), "Error retrieving data", Toast.LENGTH_LONG).show(); //todo:let this message handler pass a result back and print the error message
                                        Log.e(TAG, "Error retrieving data");
                                    }
                                }
                            };

                            DataTask dataTask = new DataTask(dataThreadMessageHandler);
                            ExecutorService executor2 = Executors.newSingleThreadExecutor();
                            executor2.submit(dataTask);
                            Log.i(TAG, "submitted to executor2");
                        }
                        else {
                            Toast.makeText(getActivity().getApplicationContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error logging in");

                            loginButton.setEnabled(true);
                            /**todo: maybe re-enable even on success for logout or back button. we will see. ALSO check if you should re-enable somehow (if fields have been changed) or disable text fields?
                             * ACTUALLY make a white screen with loading bar and words like connecting(to server)/logging in/registering/loading
                             */
                            if (restoreButton) {
                                registerButton.setEnabled(true);
                            }
                        }
                    }
                };


                LoginTask loginTask = new LoginTask(uiThreadMessageHandler, request, serverHostText.getText().toString(), serverPortText.getText().toString());
                ExecutorService executor = Executors.newSingleThreadExecutor();
                loginButton.setEnabled(false);
                if (registerButton.isEnabled()) {
                    registerButton.setEnabled(false);
                    restoreButton = true;
                }
                spinner.setVisibility(View.VISIBLE);
                executor.submit(loginTask);


                Log.i(TAG, "submitted to executor");

                //LoginResult result = ServerProxy.login(request);

                //notify main activity of result
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity().getApplicationContext(), "Register button!", Toast.LENGTH_SHORT).show();
                String gender;
                if(genderButton.getCheckedRadioButtonId() == R.id.male_button) {
                    gender = "m";
                }
                else {
                    gender = "f";
                }
                RegisterRequest request = new RegisterRequest(usernameText.getText().toString(),
                        passwordText.getText().toString(),
                        emailText.getText().toString(),
                        firstNameText.getText().toString(),
                        lastNameText.getText().toString(),
                        gender
                        );

                final RegisterResult[] result = new RegisterResult[1]; //why in the heck did i use an array here //todo: remove result array
                Log.i(TAG, "Before Register handler");
                Handler uiThreadMessageHandler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        Log.i(TAG, "In handleMessage");
                        spinner.setVisibility(View.GONE);
                        Bundle bundle = message.getData();
                        //LoginResult result = bundle.getClass(KEY, null);
                        boolean succeeded = bundle.getBoolean(SUCCEED_KEY);
                        result[0] = (RegisterResult) message.obj;
                        if (succeeded) {
                            Toast.makeText(getActivity().getApplicationContext(), "Register successful", Toast.LENGTH_SHORT).show();
                            //send to data task (implement data functions in server proxy
                            Handler dataThreadMessageHandler = new Handler() {
                                @Override
                                public void handleMessage(Message message1) {
                                    Log.i(TAG, "In handlemessage2");
                                    Bundle bundle1 = message1.getData();
                                    boolean succeeded1 = bundle1.getBoolean(SUCCEED_KEY);
                                    if (succeeded1) {
                                        Map<String, Person> persons = DataCache.getInstance().getPeople();
                                        Person tempPerson = persons.get(result[0].getPersonID());
                                        Toast.makeText(getActivity().getApplicationContext(), tempPerson.getFirstName()+" "+tempPerson.getLastName(), Toast.LENGTH_SHORT).show();
                                        ((MainActivity)getActivity()).changeToMapFragment();
                                    }
                                    else {
                                        Toast.makeText(getActivity().getApplicationContext(), "Error retrieving data", Toast.LENGTH_LONG).show(); //todo:let this message handler pass a result back and print the error message
                                        Log.e(TAG, "Error retrieving data");
                                    }
                                }
                            };

                            DataTask dataTask = new DataTask(dataThreadMessageHandler);
                            ExecutorService executor2 = Executors.newSingleThreadExecutor();
                            executor2.submit(dataTask);
                            Log.i(TAG, "submitted to executor2");
                        }
                        else {
                            Toast.makeText(getActivity().getApplicationContext(), result[0].getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error Registering");
                            registerButton.setEnabled(true);
                            if (restoreButton) {
                                loginButton.setEnabled(true);
                            }
                        }
                    }
                };




                RegisterTask registerTask = new RegisterTask(uiThreadMessageHandler, request, serverHostText.getText().toString(), serverPortText.getText().toString());
                ExecutorService executor = Executors.newSingleThreadExecutor();
                spinner.setVisibility(View.VISIBLE);
                registerButton.setEnabled(false);
                if (loginButton.isEnabled()) { //this isn't needed for register button as loginbutton will always be enabled
                    loginButton.setEnabled(false);
                    restoreButton = true;
                }
                executor.submit(registerTask);

                Log.i(TAG, "submitted to executor");


                //notify main of result
            }
        });

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("serverHost", serverHostText.getText().toString());
    }

    private TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String usernameInput = usernameText.getText().toString().trim();
            String passwordInput = passwordText.getText().toString().trim();
            String serverHostInput = serverHostText.getText().toString().trim();
            String serverPortInput = serverPortText.getText().toString().trim();
            loginButton.setEnabled(!usernameInput.isEmpty() && !passwordInput.isEmpty() &&
                    !serverHostInput.isEmpty() && !serverPortInput.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private TextWatcher registerTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String serverHostInput = serverHostText.getText().toString().trim();
            String serverPortInput = serverPortText.getText().toString().trim();
            String usernameInput = usernameText.getText().toString().trim();
            String passwordInput = passwordText.getText().toString().trim();
            String firstNameInput = firstNameText.getText().toString().trim();
            String lastNameInput = lastNameText.getText().toString().trim();
            String genderInput = hiddenTV.getText().toString().trim();
            String emailInput = emailText.getText().toString().trim();

// TODO: implement this for all fields on button press (not here lol)
            //Toast.makeText(getActivity().getApplicationContext(), "Please select Gender", Toast.LENGTH_SHORT).show();

            registerButton.setEnabled(!usernameInput.isEmpty() && !passwordInput.isEmpty() &&
                    !serverHostInput.isEmpty() && !serverPortInput.isEmpty() && !firstNameInput.isEmpty()
                    && !lastNameInput.isEmpty() && !genderInput.isEmpty() && !emailInput.isEmpty());

        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };


}