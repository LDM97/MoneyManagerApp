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
    import android.view.LayoutInflater;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.RadioButton;
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
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.LinkedList;
    import java.util.List;

    public class History extends AppCompatActivity implements  View.OnClickListener
    {

        private FirebaseAuth auth;
        private final NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
        private DrawerLayout navDraw;

        private TextView fromDateInput;
        private TextView toDateInput;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_history);
            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById( R.id.toolbar );
            setSupportActionBar(custToolBar);
            getSupportActionBar().setDisplayShowTitleEnabled( false );

            // Set title
            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( getString( R.string.history_title ) );

            getSupportActionBar().setDisplayHomeAsUpEnabled( true );
            getSupportActionBar().setHomeAsUpIndicator( R.drawable.menu_icon );

            // Get the views of the seach details
            fromDateInput = findViewById( R.id.fromDate );
            toDateInput = findViewById( R.id.toDate );

            // Setup button listeners
            findViewById( R.id.navLogout ).setOnClickListener( this );
            findViewById( R.id.homeBtn ).setOnClickListener( this );
            findViewById( R.id.searchBtn ).setOnClickListener( this );

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

        private boolean validateForm()
        { // Check all data inputted is valid before trying store the new transaction

            // Go through each field and check that all fields have been filled.
            boolean valid = true;

            // Check the date inputs, must be one or both present
            String fromDate = fromDateInput.getText().toString();
            String toDate = toDateInput.getText().toString();

            if( TextUtils.isEmpty( fromDate ) && TextUtils.isEmpty( toDate )  )
            { // must be one or both present, notify error if neither is present

                Toast.makeText( History.this, "Please set one or both date fields",
                        Toast.LENGTH_SHORT).show();
                valid = false;
            }

            // Return valid boolean, true if all fields present, false if at least one field is empty.
            return valid;
        }

        private void search()
        { // Find and display the results

            // Get a reference to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseRef = database.getReference();

            databaseRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot snapshot)
                {

                    // Check if searching from x to y, from x alone, or to y alone.
                    boolean fromPresent = !TextUtils.isEmpty( fromDateInput.getText().toString() );
                    boolean toPresent = !TextUtils.isEmpty( toDateInput.getText().toString() );

                    // Get user id, used to locate the team this user plays for
                    FirebaseUser currentUserAuth = auth.getCurrentUser();
                    final String userId = currentUserAuth.getUid();

                    List<Transaction> searchResults = new LinkedList<>();
                    SimpleDateFormat dateFormat = new SimpleDateFormat( "dd.MM.yyyy" );

                    User user = snapshot.child("Users").child(userId).getValue( User.class );

                    for( Transaction currentTransaction : user.getSortedTransactions() )
                    { // Iterate and find transactions which meet the search criteria

                        // Parse the date for comparison
                        Date transactionDate = null;
                        try
                        {
                            transactionDate = dateFormat.parse( currentTransaction.getTransactionDate() );
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        // Attempt parse from date
                        Date fromDate = null;
                        try
                        {
                            fromDate = dateFormat.parse( fromDateInput.getText().toString() );
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        // Attempt parse to date
                        Date toDate = null;
                        try
                        {
                            toDate = dateFormat.parse( toDateInput.getText().toString() );
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        // Compare transaction to search accordingly
                        if( fromPresent && toPresent )
                        { // Transaction must be between dates to be valid search result

                            if( transactionDate.compareTo( fromDate ) > 0 && transactionDate.compareTo( toDate ) < 0 )
                            { // Transaction date is within search bounds, add to list for display
                                searchResults.add( currentTransaction );
                            }
                        }
                        else if ( fromPresent )
                        { // Transaction must be after this date to be valid search result
                            if( transactionDate.compareTo( fromDate ) > 0 )
                            { // Transaction is after this date, add to list for display
                                searchResults.add( currentTransaction );
                            }

                        }
                        else if( toPresent )
                        { // Transaction must be before this date to be valid search result
                            if( transactionDate.compareTo( toDate ) < 0 )
                            { // Transaction is before to date, add to list for display
                                searchResults.add( currentTransaction );
                            }

                        }

                    }

                    if( searchResults.isEmpty() )
                    { // Notify user no search results found
                        // Notify user of successful transaction
                        Toast.makeText( History.this, "No search results found",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Display the search results
                        displaySearchResults(searchResults);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });

        }

        private void displaySearchResults( List<Transaction> searchResults )
        { // Given the list of searach results, dispaly the results

            // Setup the linear layout for displaying views
            ViewGroup linearLayout = (ViewGroup) findViewById( R.id.searchResultsContainer );
            linearLayout.removeAllViews(); // Clear linear layout for new search results


            for( Transaction result : searchResults )
            { // Dynamically display each search result

                View searchResultItem = LayoutInflater.from( getApplicationContext() ).inflate( R.layout.search_result, linearLayout, false );

                // Display the name of the transaction
                TextView title = searchResultItem.findViewById( R.id.transactionName );
                title.setText( result.getTransactionName() );

                // Display the date of the transaction
                TextView date = searchResultItem.findViewById( R.id.transactionDate );
                date.setText( result.getTransactionDate() );

                // Display the price of the transaction
                BigDecimal transactionPrice = new BigDecimal( result.getTransactionPrice() );
                BigDecimal zeroPounds = new BigDecimal( 0 );

                TextView priceOutput = searchResultItem.findViewById( R.id.transactionPrice );

                // Color the balance based on positive or negative balance
                if( transactionPrice.compareTo( zeroPounds ) < 0 )
                {
                    priceOutput.setTextColor( Color.RED );
                } else {
                    priceOutput.setTextColor( Color.GREEN );
                }

                // Display the transaction price
                String priceOutputString = "£" + transactionPrice.setScale( 2, RoundingMode.HALF_EVEN ).toPlainString();
                priceOutput.setText( priceOutputString );

                // Add the view to the screen w all the event data
                linearLayout.removeView( searchResultItem );
                linearLayout.addView( searchResultItem );
            }

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

            if( v.getId() == R.id.searchBtn )
            { // Attempt search
                if( validateForm() )
                {
                    search();
                }

            }

        }
    }
