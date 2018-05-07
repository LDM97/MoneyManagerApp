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

    public class Login extends AppCompatActivity implements View.OnClickListener
    {

        private FirebaseAuth auth;

        private TextView emailInput;
        private TextView passwordInput;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById( R.id.toolbar );
            setSupportActionBar( custToolBar );
            getSupportActionBar().setDisplayShowTitleEnabled( false );

            // Set title and hide bome button as not logged in yet
            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( getString( R.string.login_title ) );
            findViewById( R.id.homeBtn ).setVisibility( View.GONE );

            // Get the views of user's login details
            emailInput = findViewById( R.id.emailInput );
            passwordInput = findViewById( R.id.passwordInput );

            // Setup listeners on the buttons
            findViewById( R.id.loginBtn ).setOnClickListener( this );
            findViewById( R.id.createAccountBtn ).setOnClickListener( this );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();
        }

        @Override
        public void onStart()
        {
            super.onStart();
            FirebaseUser currentUser = auth.getCurrentUser();
            if( currentUser != null )
            { // Has account already assigned on this phone, take to home screen.
                Intent intent = new Intent( getApplicationContext(), Home.class );
                startActivity( intent );
            }
        }

        private void signIn( String email, String password )
        { // Attempt to login the user

            // Only attempt sign in if login credentials are present
            Log.d( "EmailPassword", "signIn:" + email );

            auth.signInWithEmailAndPassword( email, password )
                    .addOnCompleteListener( this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete( @NonNull Task<AuthResult> task )
                        {
                            if ( task.isSuccessful() )
                            {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d( "EmailPassword", "signInWithEmail:success" );
                                FirebaseUser user = auth.getCurrentUser();
                                if( user != null )
                                { // Sing in successful, take to home screen
                                    Intent intent = new Intent( getApplicationContext(), Home.class );
                                    startActivity( intent );
                                }

                                // Sign in successful, clear the login details
                                emailInput.setText( "" );
                                passwordInput.setText( "" );
                            }
                            else
                            {
                                // If sign in fails, display a message to the user.
                                Log.w( "EmailPassword", "signInWithEmail:failure", task.getException() );
                                Toast.makeText( Login.this, "Login Failed.",
                                        Toast.LENGTH_SHORT ).show();
                            }

                        }
                    });
        }

        private boolean validateForm()
        { // Check the login credentials have been entered

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

            // False if at least one input is not present
            return valid;
        }

        @Override
        public void onClick( View v )
        {
            if( v.getId() == R.id.createAccountBtn )
            {// Take the user to the create account screen
                Intent intent = new Intent( getApplicationContext(), CreateAccount.class );
                startActivity( intent );
            }

            if( v.getId() == R.id.loginBtn  )
            { // Attempt to sign in the user with the given credentials

                if ( validateForm() )
                { // Only attempt sign in if details entered.
                    signIn( emailInput.getText().toString(), passwordInput.getText().toString() );
                }
            }
        }

    }
