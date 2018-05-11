    package mortimer.l.moneymanager;

    import android.content.Intent;
    import android.graphics.Color;
    import android.graphics.PorterDuff;
    import android.graphics.drawable.Drawable;
    import android.support.design.widget.NavigationView;
    import android.support.v4.view.GravityCompat;
    import android.support.v4.widget.DrawerLayout;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.widget.Toolbar;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.LinearLayout;
    import android.widget.PopupWindow;
    import android.widget.ProgressBar;
    import android.widget.RelativeLayout;
    import android.widget.TextView;

    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.math.BigDecimal;
    import java.math.RoundingMode;
    import java.util.Calendar;
    import java.util.List;

    public class Home extends AppCompatActivity implements View.OnClickListener
    {
        private FirebaseAuth auth;
        private final NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
        private DrawerLayout navDraw;
        private User currentUser;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView( R.layout.activity_home );

            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(custToolBar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            // Set title
            TextView actionBarTitle = (TextView) findViewById(R.id.toolbarTitle);
            actionBarTitle.setText(getString(R.string.home_title));

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu_icon);

            // Setup button listeners
            findViewById( R.id.navLogout ).setOnClickListener( this );

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

            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    // Get user id, used to locate the team this user plays for
                    FirebaseUser currentUserAuth = auth.getCurrentUser();
                    final String userId = currentUserAuth.getUid();

                    // Get the user obj
                    currentUser = snapshot.child("Users").child(userId).getValue(User.class);

                    // Get the account balance output
                    TextView accountBalanceOutput = (TextView) findViewById( R.id.accountBalance );

                    BigDecimal balance = new BigDecimal( currentUser.getAccountBalance() );
                    BigDecimal zeroPounds = new BigDecimal( 0 );

                    // Color the balance based on positive or negative balance
                    if( balance.compareTo( zeroPounds ) < 0 )
                    {
                        accountBalanceOutput.setTextColor( Color.RED );
                    } else {
                        accountBalanceOutput.setTextColor( Color.GREEN );
                    }

                    // Display the account balance
                    String balanceString = "£" + balance.setScale( 2, RoundingMode.HALF_EVEN ).toPlainString();
                    accountBalanceOutput.setText( balanceString );


                    // Display some of the recent transactions
                    List<Transaction> transactions = currentUser.getSortedTransactions();

                    // Setup the linear layout for displaying views
                    ViewGroup linearLayout = (ViewGroup) findViewById( R.id.recentTransactionsContainer );

                    for( int i = transactions.size() - 1; i > transactions.size() - 4 && i > -1; i-- )
                    { // Loop backward through sorted transactions and display the 3 most recent transactions
                        Transaction t = transactions.get( i );

                        View searchResultItem = LayoutInflater.from( getApplicationContext() ).inflate( R.layout.search_result, linearLayout, false );

                        // Display the name of the transaction
                        TextView title = searchResultItem.findViewById( R.id.transactionName );
                        title.setText( t.getTransactionName() );

                        // Display the date of the transaction
                        TextView date = searchResultItem.findViewById( R.id.transactionDate );
                        date.setText( t.getTransactionDate() );

                        // Display the price of the transaction
                        BigDecimal transactionPrice = new BigDecimal( t.getTransactionPrice() );

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

                    BigDecimal monthlyExpenditure = new BigDecimal( 0 ).setScale( 2, RoundingMode.HALF_EVEN );

                    // Find transactions from this current month
                    for( DataSnapshot i : snapshot.child( "Users" ).child( userId ).child( "transactions" ).getChildren() )
                    {
                        // Get transaction and months to compare
                        Transaction t = i.getValue( Transaction.class );
                        String transactionMonthStr = t.getTransactionDate().substring( 3, 5 );
                        int transactionMonth = Integer.parseInt( transactionMonthStr ) -1;

                        Calendar c = Calendar.getInstance();
                        int currentMonth = c.get( Calendar.MONTH );

                        if( currentMonth == transactionMonth )
                        { // Is from this month, take into account when calculating

                            // Add to total for monthly expenditure
                            BigDecimal transactionPriceBigDec = new BigDecimal( t.getTransactionPrice() ).setScale( 2, RoundingMode.HALF_EVEN );

                            if( transactionPriceBigDec.compareTo( new BigDecimal( 0 ) ) < 0 )
                            { // Is expenditure, add to expenditure list
                                transactionPriceBigDec = transactionPriceBigDec.subtract( transactionPriceBigDec.multiply( new BigDecimal( 2 ) ) );
                                monthlyExpenditure = monthlyExpenditure.add( transactionPriceBigDec );
                            }
                        }
                    }

                    // Display in text the monthly budget spent
                    TextView monthlyExpenditureOutput = findViewById( R.id.spentMonthly );
                    String expenseOutputStr = "£" + monthlyExpenditure.toPlainString();
                    monthlyExpenditureOutput.setText( expenseOutputStr );

                    TextView monthlyBudgetOutput = findViewById( R.id.monthlyBudget );
                    String budgetOutputStr = "£" + currentUser.getMonthlyBudget();
                    monthlyBudgetOutput.setText( budgetOutputStr );

                    // Visually display the monthly budget spent
                    BigDecimal monthlyBudgetBig = new BigDecimal( currentUser.getMonthlyBudget() ).setScale( 2, RoundingMode.HALF_EVEN );
                    BigDecimal percentage = ( monthlyExpenditure.divide( monthlyBudgetBig, RoundingMode.HALF_EVEN ) ).multiply( new BigDecimal( 100 ) );
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
        { // Already at home screen, do nothing
            return;
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
        public void onClick( View v )
        {
            if( v.getId() == R.id.navLogout )
            { // Logout the user
                navDrawerHandler.signOut( getApplicationContext() );
            }

        }
    }
