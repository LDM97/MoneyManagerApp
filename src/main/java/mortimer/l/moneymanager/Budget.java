    package mortimer.l.moneymanager;

    import android.content.DialogInterface;
    import android.content.Intent;
    import android.graphics.Color;
    import android.support.design.widget.NavigationView;
    import android.support.v4.view.GravityCompat;
    import android.support.v4.widget.DrawerLayout;
    import android.support.v7.app.AlertDialog;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.widget.Toolbar;
    import android.text.InputType;
    import android.text.TextUtils;
    import android.view.LayoutInflater;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.EditText;
    import android.widget.ProgressBar;
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
    import java.math.BigInteger;
    import java.math.RoundingMode;
    import java.text.DateFormatSymbols;
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.Calendar;
    import java.util.Date;
    import java.util.LinkedList;
    import java.util.List;

    public class Budget extends AppCompatActivity implements View.OnClickListener
    {
        private FirebaseAuth auth;
        private final NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
        private DrawerLayout navDraw;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_budget);

            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById( R.id.toolbar );
            setSupportActionBar(custToolBar);
            getSupportActionBar().setDisplayShowTitleEnabled( false );

            // Set title
            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( getString( R.string.budget_title ) );

            getSupportActionBar().setDisplayHomeAsUpEnabled( true );
            getSupportActionBar().setHomeAsUpIndicator( R.drawable.menu_icon );

            // Setup button listeners
            findViewById( R.id.navLogout ).setOnClickListener( this );
            findViewById( R.id.homeBtn ).setOnClickListener( this );
            findViewById( R.id.editBudgetBtn ).setOnClickListener( this );

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

            // Get a reference to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseRef = database.getReference();

            // Display the transactions done this month
            databaseRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot snapshot)
                {
                    // Get user object from the database
                    FirebaseUser currentUserAuth = auth.getCurrentUser();
                    String userId = currentUserAuth.getUid();
                    User user = snapshot.child("Users").child(userId).getValue( User.class );

                    // Setup the linear layout for displaying views
                    ViewGroup linearLayout = (ViewGroup) findViewById( R.id.thisMonthTransactionsContainer );

                    BigDecimal monthlyExpenditure = new BigDecimal( 0 ).setScale( 2, RoundingMode.HALF_EVEN );

                    // Find transactions from this current month
                    for( Transaction t : user.getSortedTransactions() )
                    {
                        // Get transaction and months to compare
                        String transactionMonthStr = t.getTransactionDate().substring( 3, 5 );
                        int transactionMonth = Integer.parseInt( transactionMonthStr ) -1;

                        Calendar c = Calendar.getInstance();
                        int currentMonth = c.get( Calendar.MONTH );

                        if( currentMonth == transactionMonth )
                        { // Is from this month, display to the screen

                            // Add to total for monthly expenditure
                            BigDecimal transactionPriceBigDec = new BigDecimal( t.getTransactionPrice() ).setScale( 2, RoundingMode.HALF_EVEN );

                            if( transactionPriceBigDec.compareTo( new BigDecimal( 0 ) ) < 0 )
                            { // Is expenditure, add to expenditure list
                                transactionPriceBigDec = transactionPriceBigDec.subtract( transactionPriceBigDec.multiply( new BigDecimal( 2 ) ) );
                                monthlyExpenditure = monthlyExpenditure.add( transactionPriceBigDec );
                            }

                            // Inflate layout
                            View searchResultItem = LayoutInflater.from( getApplicationContext() ).inflate( R.layout.search_result, linearLayout, false );

                            // Display the name of the transaction
                            TextView title = searchResultItem.findViewById( R.id.transactionName );
                            title.setText( t.getTransactionName() );

                            // Display the date of the transaction
                            TextView date = searchResultItem.findViewById( R.id.transactionDate );
                            date.setText( t.getTransactionDate() );

                            // Display the price of the transaction
                            BigDecimal transactionPrice = new BigDecimal( t.getTransactionPrice() ).setScale( 2, RoundingMode.HALF_EVEN );;
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
                            linearLayout.addView( searchResultItem );

                        }

                    }

                    // Display in text the monthly budget spent
                    TextView monthlyExpenditureOutput = findViewById( R.id.spentMonthly );
                    String expenseOutputStr = "£" + monthlyExpenditure.toPlainString();
                    monthlyExpenditureOutput.setText( expenseOutputStr );

                    TextView monthlyBudgetOutput = findViewById( R.id.monthlyBudget );
                    String budgetOutputStr = "£" + user.getMonthlyBudget();
                    monthlyBudgetOutput.setText( budgetOutputStr );

                    // Visually display the monthly budget spent
                    BigDecimal monthlyBudgetBig = new BigDecimal( user.getMonthlyBudget() ).setScale( 2, RoundingMode.HALF_EVEN );
                    BigDecimal percentage = ( monthlyExpenditure.divide( monthlyBudgetBig ) ).multiply( new BigDecimal( 100 ) );
                    ProgressBar bar = findViewById( R.id.progressBar );
                    bar.setMax( 100 );
                    bar.setProgress( percentage.intValue() );

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });


        }

        private void updateBudget( String newBudget )
        {
            final BigDecimal convertedBudget = new BigDecimal( newBudget ).setScale( 2, RoundingMode.HALF_EVEN );

            // Get a reference to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseRef = database.getReference();

            databaseRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot snapshot)
                {

                    // Get user object from the database
                    FirebaseUser currentUserAuth = auth.getCurrentUser();
                    String userId = currentUserAuth.getUid();
                    User user = snapshot.child("Users").child(userId).getValue( User.class );

                    // Update the monthly budget based on the formatted date input
                    user.setMonthlyBudget( convertedBudget.toPlainString() );
                    DatabaseReference userRef = snapshot.child( "Users" ).child( userId ).getRef();
                    userRef.setValue( user );

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
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


        @Override
        public void onClick( View v ) {
            if (v.getId() == R.id.navLogout)
            { // Logout the user
                navDrawerHandler.signOut(getApplicationContext());
            }

            if (v.getId() == R.id.homeBtn)
            { // Return user to the home screen
                Intent intent = new Intent(this, Home.class);
                startActivity(intent);
            }

            if( v.getId() == R.id.editBudgetBtn )
            { // Giver user popup to edit the monthly budget

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                // Set up the input
                final EditText input = new EditText( this );

                // Specify the type of input expected
                input.setInputType( InputType.TYPE_TEXT_FLAG_MULTI_LINE );
                builder.setView( input );
                builder.setTitle( R.string.new_budget_popup_title );

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String newBudget = input.getText().toString();

                        if( TextUtils.isEmpty( newBudget ) )
                        {
                            // Notify user an input is required
                            Toast.makeText( Budget.this, "Please add a new input",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Update the budget
                            updateBudget( newBudget );
                            // Refresh the screen to display updated budget
                            finish();
                            startActivity( getIntent() );
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });

                builder.show();
            }

        }
    }
