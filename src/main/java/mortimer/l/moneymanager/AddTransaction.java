    package mortimer.l.moneymanager;

    import android.content.Intent;
    import android.graphics.Color;
    import android.support.design.widget.NavigationView;
    import android.support.v4.view.GravityCompat;
    import android.support.v4.widget.DrawerLayout;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.widget.Toolbar;
    import android.text.TextUtils;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.RadioButton;
    import android.widget.RadioGroup;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.math.BigDecimal;
    import java.math.RoundingMode;

    public class AddTransaction extends AppCompatActivity implements View.OnClickListener
    {

        private FirebaseAuth auth;
        private final NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
        private DrawerLayout navDraw;

        private TextView transactionNameInput;
        private TextView transactionPriceInput;
        private TextView transactionDateInput;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_transaction);

            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(custToolBar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            // Set title
            TextView actionBarTitle = (TextView) findViewById(R.id.toolbarTitle);
            actionBarTitle.setText(getString( R.string.add_transaction_title ) );

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu_icon);

            // Get the views of the transaction's details
            transactionNameInput = findViewById( R.id.transactionName );
            transactionPriceInput = findViewById( R.id.transactionPrice );
            transactionDateInput = findViewById( R.id.transactionDate );

            // Setup button listeners
            findViewById( R.id.navLogout ).setOnClickListener( this );
            findViewById( R.id.homeBtn ).setOnClickListener( this );
            findViewById( R.id.addTransactionBtn ).setOnClickListener( this );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();

            // Nav drawer code
            navDraw = findViewById(R.id.drawer_layout);
            NavigationView navigationView = findViewById(R.id.nav_view);

            navigationView.setNavigationItemSelectedListener
                    (
                            new NavigationView.OnNavigationItemSelectedListener() {
                                @Override
                                public boolean onNavigationItemSelected(MenuItem menuItem) {

                                    // close drawer when item is tapped
                                    navDraw.closeDrawers();

                                    // Pass selected item and context to handle view
                                    View thisView = findViewById(android.R.id.content);
                                    navDrawerHandler.itemSelectHandler(menuItem, thisView.getContext());

                                    return true;
                                }
                            });
        }

        @Override
        public void onStart()
        {
            super.onStart();
            FirebaseUser currentUser = auth.getCurrentUser();
            if( currentUser == null )
            { // Not logged in, bad navigation attempt, return to login screen
                Intent intent = new Intent( getApplicationContext(), Login.class );
                startActivity( intent );
            }
        }

        @Override
        public void onBackPressed()
        { // Take the user back to the home screen
            Intent intent = new Intent(this, Home.class );
            startActivity( intent );
        }

        @Override
        public boolean onOptionsItemSelected( MenuItem item )
        { // Open the navigation drawer if the nav draw button is selected
            if ( item.getItemId() == android.R.id.home ) {
                navDraw.openDrawer( GravityCompat.START );
                return true;
            }
            return super.onOptionsItemSelected( item );
        }

        private void uploadData()
        { // Add the new transaction to the database

            String transactionName = transactionNameInput.getText().toString();
            String transactionPrice = transactionPriceInput.getText().toString();
            String transactionDate = transactionDateInput.getText().toString();

            transactionPrice = transactionPrice.trim();
            String roundedPrice = new BigDecimal( transactionPrice ).setScale( 2, RoundingMode.HALF_EVEN ).toPlainString();

            // Check cash flow has been set
            RadioButton expenditure = findViewById( R.id.expenditureRadioBtn );
            RadioButton income = findViewById( R.id.incomeRadioBtn );

            if( expenditure.isChecked() )
            { // Set to minus value if expenditure
                roundedPrice = "-" + roundedPrice;
            }

            final Transaction newTransaction = new Transaction( transactionName, roundedPrice, transactionDate);

            // Get a reference to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseRef = database.getReference();

            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    // Get user id, used to locate the team this user plays for
                    FirebaseUser currentUserAuth = auth.getCurrentUser();
                    final String userId = currentUserAuth.getUid();

                    // Get the user obj
                    User currentUser = snapshot.child("Users").child(userId).getValue(User.class);

                    // Update the user w the new transaction
                    currentUser.addTransaction( newTransaction );

                    // Update database w user's updated balance and transaction
                    DatabaseReference userRef = snapshot.child( "Users" ).child( userId ).getRef();
                    userRef.setValue( currentUser );

                    // Refresh the screen once transaction added
                    finish();
                    startActivity( getIntent() );

                    // Notify user of successful transaction
                    Toast.makeText( AddTransaction.this, "Transaction added",
                            Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }

        private boolean validateForm()
        { // Check all data inputted is valid before trying store the new transaction

            // Go through each field and check that all fields have been filled.
            boolean valid = true;

            // Check cash flow has been set
            RadioButton expenditure = findViewById( R.id.expenditureRadioBtn );
            RadioButton income = findViewById( R.id.incomeRadioBtn );

            if( !expenditure.isChecked() && !income.isChecked() )
            {
                expenditure.setError( "Choose One" );
                income.setError( "Choose One" );
                valid = false;
            } else {
                expenditure.setError( null );
                income.setError( null );
            }

            // Check there is a transaction name input
            String transactionName = transactionNameInput.getText().toString();
            if( TextUtils.isEmpty( transactionName ) )
            { // No input, inform user and return false
                transactionNameInput.setError( "Required" );
                valid = false;
            } else {
                transactionNameInput.setError( null );
            }

            // Check there is a transaction price input
            String transactionPrice = transactionPriceInput.getText().toString();
            if( TextUtils.isEmpty( transactionPrice ) )
            { // No input, inform user and return false
                transactionPriceInput.setError( "Required" );
                valid = false;
            } else {
                transactionPriceInput.setError( null );
            }

            // Check there is a transaction date input
            String transactionDate = transactionDateInput.getText().toString();
            if( TextUtils.isEmpty( transactionDate ) )
            { // No input, inform user and return false
                transactionDateInput.setError( "Required" );
                valid = false;
            } else {
                transactionDateInput.setError( null );
            }

            // Return valid boolean, true if all fields present, false if at least one field is empty.
            return valid;
        }

        @Override
        public void onClick( View v )
        {
            if( v.getId() == R.id.navLogout )
            { // Logout the user
                navDrawerHandler.signOut( getApplicationContext() );
            }

            if( v.getId() == R.id.homeBtn )
            { // Return user to the home screen
                Intent intent = new Intent(this, Home.class );
                startActivity( intent );
            }

            if( v.getId() == R.id.addTransactionBtn )
            {
                if( validateForm() )
                {
                    uploadData();
                }
            }

        }
    }
