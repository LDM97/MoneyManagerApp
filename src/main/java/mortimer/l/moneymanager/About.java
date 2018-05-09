    package mortimer.l.moneymanager;

    import android.content.Intent;
    import android.support.design.widget.NavigationView;
    import android.support.v4.view.GravityCompat;
    import android.support.v4.widget.DrawerLayout;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.widget.Toolbar;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.TextView;

    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;

    public class About extends AppCompatActivity implements View.OnClickListener
    {

        private FirebaseAuth auth;
        private final NavDrawerHandler navDrawerHandler= new NavDrawerHandler();
        private DrawerLayout navDraw;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_about);
            // Custom toolbar setup
            Toolbar custToolBar = (Toolbar) findViewById( R.id.toolbar );
            setSupportActionBar( custToolBar );
            getSupportActionBar().setDisplayShowTitleEnabled( false );

            // Set title
            TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
            actionBarTitle.setText( getString( R.string.about_title ) );

            getSupportActionBar().setDisplayHomeAsUpEnabled( true );
            getSupportActionBar().setHomeAsUpIndicator( R.drawable.menu_icon );

            // Get Firebase authenticator
            auth = FirebaseAuth.getInstance();

            // Nav drawer code
            navDraw = findViewById( R.id.drawer_layout );
            NavigationView navigationView = findViewById( R.id.nav_view );

            navigationView.setNavigationItemSelectedListener
                    (
                            new NavigationView.OnNavigationItemSelectedListener()
                            {
                                @Override
                                public boolean onNavigationItemSelected( MenuItem menuItem ) {

                                    // close drawer when item is tapped
                                    navDraw.closeDrawers();

                                    // Pass selected item and context to handle view
                                    View thisView = findViewById(android.R.id.content);
                                    navDrawerHandler.itemSelectHandler( menuItem, thisView.getContext() );

                                    return true;
                                }
                            });

            // Setup button listeners
            findViewById( R.id.navLogout ).setOnClickListener( this );
            findViewById( R.id.homeBtn ).setOnClickListener( this );
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

        }
    }
