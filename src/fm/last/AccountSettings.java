package fm.last;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.lang.Runnable;
import java.lang.Thread;

import android.app.*;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.os.Handler;
import android.os.Looper;

import android.content.*;
import android.app.ApplicationContext;

public class AccountSettings extends Activity {
   private EditText userField, passwordField;
   private final Handler m_handler = new Handler();
   ProgressDialog m_progress = null;

   /** Called when the activity is first created. */
   public void onCreate( Bundle icicle )
   {
      super.onCreate( icicle );
      setContentView( R.layout.account_settings );

      final Button loginButton = (Button)findViewById( R.id.login_button );
      userField = (EditText)findViewById( R.id.login_username );
      passwordField = (EditText)findViewById( R.id.login_password );

      
      Log.i( "Last.fm", "Handler object: " + m_handler.toString() );
      loginButton.setOnClickListener( onLoginClick );
   }

   private OnClickListener onLoginClick = new OnClickListener()
   {
   	public void onClick( View v )
   	{
   	   m_progress = ProgressDialog.show( AccountSettings.this,
                                                        getResources().getString( R.string.authProgressTitle ),
                                                        getResources().getString( R.string.authProgressMessage ),
                                                        true );
         new Thread(runAuthentication).start();
   	}
   };
 
   Runnable runAuthentication = new Runnable()
   {  
      private RadioHandshake m_loginTest;

      public void run()
      {
         
         final String userName = userField.getText().toString();
         final String userPassword = md5( passwordField.getText().toString() );
         m_loginTest = new RadioHandshake( userName, userPassword );

         m_loginTest.connect();

         m_progress.dismiss();
         if( !m_loginTest.isValid() )
         {
            	m_handler.post ( showAlert );
         }
         else
         {
             SharedPreferences prefs = 
                	AccountSettings.this.getSharedPreferences( "Last.fm", Context.MODE_PRIVATE );

             SharedPreferences.Editor prefEdit = prefs.edit();

             prefEdit.putString( "username", userName );
             prefEdit.putString( "md5Password", userPassword );

             prefEdit.commit();

             AccountSettings.this.finish();
         }
      }
    };

  	Runnable showAlert = new Runnable() {
        public void run() {
           showAlert( 
  	           getResources().getString( R.string.badAuthTitle ), 
  	           R.drawable.icon, 
  	           getResources().getString( R.string.badAuth ), 
  	           "OK", 
  	           true
            );
         }
      };    
    
    private String md5( String in )
    {
        try
        {
            MessageDigest m = MessageDigest.getInstance( "MD5" );
            m.update( in.getBytes(), 0, in.length() );
            return (new BigInteger( 1, m.digest())).toString( 16 );
        }
        catch( java.security.NoSuchAlgorithmException e )
        {
            Log.e( "Last.fm", e.toString() );
            return "";
        }
    }
}