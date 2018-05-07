    package mortimer.l.moneymanager;

    import android.content.Intent;
    import android.support.annotation.NonNull;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.widget.Toolbar;
    import android.text.TextUtils;
    import android.util.Log;
    import android.view.View;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.Task;
    import com.google.firebase.auth.AuthResult;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;

    import org.w3c.dom.Text;

    public class CreateAccount extends AppCompatActivity implements View.OnClickListener
    {

        FirebaseAuth auth;

        private TextView nameInput;
        private TextView emailInput;
        private TextView passwordInput;
        private TextView confirmPasswordInput;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate( savedInstanceState );
            setContentView( R.layout.activity_create_account );

            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById( R.id.toolbar );
            setSupportActionBar( custToolBar );
            getSupportActionBar().setDisplayShowTitleEnabled( false );

            // Set title and hide bome button as not logged in yet
            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( getString( R.string.create_acc_title ) );
            findViewById( R.id.homeBtn ).setVisibility( View.GONE );

            // Get the views of user's login details
            nameInput = findViewById( R.id.nameInput );
            emailInput = findViewById( R.id.emailInput );
            passwordInput = findViewById( R.id.passwordInput );
            confirmPasswordInput = findViewById( R.id.confirmPasswordInput );

            // Setup listeners on the buttons
            findViewById( R.id.createAccountBtn ).setOnClickListener( this );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();
        }

        private void createAccount( String email, String password )
        { // Attempt to create the new account

            // Check the inputs are there and valid
            Log.d( "EmailPassword", "createAccount:" + email );

            // Valid inputs, create the account on Firebase database
            auth.createUserWithEmailAndPassword( email, password )
                    .addOnCompleteListener( this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete( @NonNull Task<AuthResult> task )
                        {
                            if ( task.isSuccessful() )
                            {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d( "EmailPassword", "createUserWithEmail:success" );
                                FirebaseUser user = auth.getCurrentUser();

                                // Authentication success, upload the inputted data to the real time
                                // Firebase database.
                                uploadData();

                                // Login successful, clear the create acc details.
                                emailInput.setText( "" );
                                passwordInput.setText( "" );
                                nameInput.setText( "" );

                                if( user != null )
                                { // Sing in successful, take to home screen
                                    Intent intent = new Intent( getApplicationContext(), Home.class );
                                    startActivity( intent );
                                }

                            }
                            else
                            {
                                // If sign in fails, display a message to the user.
                                Log.w( "EmailPassword", "createUserWithEmail:failure", task.getException() );
                                Toast.makeText( CreateAccount.this, "Failed To Create Account.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    } );
        }

        private void uploadData()
        { // Authentication & form validation success, upload data to the database

            // Get the new users name input
            String newName = nameInput.getText().toString();

            // Get the userID
            FirebaseUser user = auth.getCurrentUser();
            String uid = user.getUid();

            // Create the new user object
            User newUser = new User( uid, newName );

            // Get database reference and write the data of the new user to the database
            FirebaseDatabase database =  FirebaseDatabase.getInstance();
            DatabaseReference dbRef = database.getReference().child( "Users" ).child( uid );
            dbRef.setValue( newUser );
        }

        private boolean validateForm()
        { // Check all data inputted is valid before trying to create the new account

            // Go through each field and check that all fields have been filled.
            boolean valid = true;

            // Check there is an email input
            String email = emailInput.getText().toString();
            if ( TextUtils.isEmpty( email ) )
            {
                // No input, inform user and return false
                emailInput.setError( "Required" );
                valid = false;
            }
            else
            {
                emailInput.setError( null );
            }

            // Check there is a password input
            String password = passwordInput.getText().toString();
            if ( TextUtils.isEmpty( password ) )
            {
                // No input, inform the user and return false
                passwordInput.setError( "Required" );
                valid = false;
            }
            else
            {
                passwordInput.setError( null );
            }

            // Check there is a re-entered password input
            String confirmPassword = confirmPasswordInput.getText().toString();
            if ( TextUtils.isEmpty( confirmPassword ) )
            {
                // No input, inform the user and return false
                confirmPasswordInput.setError( "Required" );
                valid = false;
            }
            else
            {
                confirmPasswordInput.setError( null );
            }

            // Check the password, and re-entered pass word match for verification
            if( !password.equals( confirmPassword ) )
            { // Passwords do not match
                confirmPasswordInput.setError( "Password Does Not Match" );
                valid = false;
            }



            // Check there is a name input
            String name = nameInput.getText().toString();
            if( TextUtils.isEmpty( name ) )
            {
                // No input, inform the user and return false
                nameInput.setError( "Required" );
                valid = false;
            }

            // Return valid boolean, true if all fields present, false if at least one field is empty.
            return valid;
        }

        @Override
        public void onBackPressed()
        { // Return to login
            Intent intent = new Intent( getApplicationContext(), Login.class );
            startActivity( intent );
        }

        @Override
        public void onClick( View v )
        {
            if( v.getId() == R.id.createAccountBtn )
            {// Take the user to the create account screen
                if( validateForm() )
                { // Valid details entered, create the new account
                    createAccount( emailInput.getText().toString(), passwordInput.getText().toString() );

                }
            }
        }

    }
