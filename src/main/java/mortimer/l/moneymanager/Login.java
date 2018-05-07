package mortimer.l.moneymanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference();

        // Custom toolbar setup
        Toolbar custToolBar = (Toolbar) findViewById( R.id.my_toolbar );
        setSupportActionBar( custToolBar );
        getSupportActionBar().setDisplayShowTitleEnabled( false );

        TextView actionBarTitle = (TextView) findViewById( R.id.toolbarTitle );
        actionBarTitle.setText( getString( R.string.login_title ) );
    }
}
