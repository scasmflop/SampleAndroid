package com.sample.helloandroiddb;



import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Camera;
import android.os.Bundle;
import android.provider.Browser;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class HelloAndroidDB extends Activity {
	final static int HELLOANDROIDDBVERSION = 1;
	
	private ListView listSrc;
	private ListView listDb;
	private ViewGroup linlay;
	private SQLiteDatabase db;
	
	private static final String[] sources = {
		"Bookmarks",
		"Contacts",
		"SQLite"
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        listSrc = (ListView) findViewById(R.id.listsrc);
        listDb = (ListView) findViewById(R.id.listdb);
        linlay = (ViewGroup) findViewById(R.id.llaymain);
        
        // Prepare the ListView
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, this.sources);
        
        listSrc.setAdapter(adapter);
        
        listSrc.setOnItemClickListener(new OnItemClickListener(){
        	public void onItemClick(AdapterView parent, View v, int position, long id){
        		//Toast.makeText(HelloAndroidDB.this, "Parent:" + parent.getId() + "|View:" + v.getId() + "|pos:" + position + "|id:" + id,Toast.LENGTH_LONG).show();
        		switch (position){
        			case 0:
        				testBookmarkDB();
        				HelloAndroidDB.this.setTitle("Android Bookmarks");
        				break;
        			case 1:
        				getContactName();
        				HelloAndroidDB.this.setTitle("Android Contacts");
        				break;
        			case 2:
        				testDB();
        				HelloAndroidDB.this.setTitle("SQLite DB");
        				break;
        		}
                applyRotation(position, 0, 90);
        	}
        });
        
        listDb.setOnItemClickListener(new OnItemClickListener(){
        	public void onItemClick(AdapterView parent, View v, int position, long id){
        		Cursor ci = (Cursor) listDb.getItemAtPosition(position);
        		String val = ci.getString(1);
        		Toast.makeText(HelloAndroidDB.this, val, Toast.LENGTH_LONG).show();
				HelloAndroidDB.this.setTitle(R.string.app_name);
                //applyRotation(-1, 180, 90);
        		applyRotation(-1, 0, 90);
        	}
        });
        
        linlay.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
        
    }
    
    /**
     * Setup a new 3D rotation on the container view.
     *
     * @param position the item that was clicked to show a picture, or -1 to show the list
     * @param start the start angle at which the rotation must begin
     * @param end the end angle of the rotation
     */
    private void applyRotation(int position, float start, float end) {
        // Find the center of the container
        final float centerX = linlay.getWidth() / 2.0f;
        final float centerY = linlay.getHeight() / 2.0f;

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        //final Rotate3dAnimation rotation = new Rotate3dAnimation(start, end, centerX, centerY, 310.0f, true);
        final Rotate3dAnimation rotation = new Rotate3dAnimation(start, end, centerX, centerY, 310.0f, true);
        rotation.setDuration(500);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(position));

        linlay.startAnimation(rotation);
    }

    /**
     * This class listens for the end of the first half of the animation.
     * It then posts a new action that effectively swaps the views when the container
     * is rotated 90 degrees and thus invisible.
     */
    private final class DisplayNextView implements Animation.AnimationListener {
        private final int mPosition;

        private DisplayNextView(int position) {
            mPosition = position;
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
        	linlay.post(new SwapViews(mPosition));
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    /**
     * This class is responsible for swapping the views and start the second
     * half of the animation.
     */
    private final class SwapViews implements Runnable {
        private final int mPosition;

        public SwapViews(int position) {
            mPosition = position;
        }

        public void run() {
            final float centerX = linlay.getWidth() / 2.0f;
            final float centerY = linlay.getHeight() / 2.0f;
            Rotate3dAnimation rotation;
            Camera camera = new Camera();
            
            if (mPosition > -1) {
            	listSrc.setVisibility(View.GONE);
            	//listDb
            	listDb.setVisibility(View.VISIBLE);
            	listDb.requestFocus();
            	
                //rotation = new Rotate3dAnimation(90, 180, centerX, centerY, 310.0f, false);
            	rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 310.0f, false);
            } else {
            	listDb.setVisibility(View.GONE);
            	listSrc.setVisibility(View.VISIBLE);
            	listSrc.requestFocus();

                rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 310.0f, false);
            }

            rotation.setDuration(500);
            rotation.setFillAfter(true);
            rotation.setInterpolator(new DecelerateInterpolator());

            linlay.startAnimation(rotation);
        }
    }
    
    
    @SuppressWarnings("unused")
	private void getContactName() {
        String dname = "Broken";
        String dphone = "(111) 222-3333";
    	String[] projection = new String[] {Contacts.DISPLAY_NAME};

        ContentResolver cr = this.getContentResolver();
        Cursor rs = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        listDb.setAdapter(new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,rs,projection,new int[] {android.R.id.text1}));
    }
    
    private void testBookmarkDB(){
    	try {
        	String[] projection = new String[] {Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL};
        	Cursor rs = managedQuery(Browser.BOOKMARKS_URI,projection,null,null,null);
        	this.startManagingCursor(rs);
        	
        	listDb.setAdapter(new SimpleCursorAdapter(this,R.layout.listbookmark,rs,projection,new int[] {R.id.btitle, R.id.burl}));

    	} catch (Exception e) {
    		Toast.makeText(this, e.getMessage(),Toast.LENGTH_LONG).show();
    	}
    }
    
	@SuppressWarnings("unused")
	private String testDB(){
    	String result = "fail";
    	//SQLiteDatabase db = null;
    	
    	createDB();
    	populateDB();
    	
    	try {
        	String[] projection = new String[] {"state", "country"};

        	result = "fail1";
        	//Cursor rs = db.query("tblstates", null, null, null, null, null, null);
        	Cursor rs = db.rawQuery("select s._id, s.state, c.country from tblstates s inner join tblcountries c on s.countryid = c._id", null);
        	result = "fail2";
        	listDb.setAdapter(new SimpleCursorAdapter(this,R.layout.listbookmark,rs,projection,new int[] {R.id.btitle, R.id.burl}));
    		
    	} catch(Exception e) {
    		Toast.makeText(this, "TestDB (" + result + "):" + e.getMessage(),Toast.LENGTH_LONG).show();
    	}
    	return result; 
    }
    
    private void createDB(){
    	//SQLiteDatabase db;
    	try {
        	db = this.openOrCreateDatabase("test.db3", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        	db.setVersion(HelloAndroidDB.HELLOANDROIDDBVERSION);
        	db.setLocale(java.util.Locale.getDefault());
        	db.setLockingEnabled(true);
        	
        	
        	db.execSQL("drop table if exists tblcountries;");
        	db.execSQL("drop table if exists tblstates;");
        	
        	final String CREATE_TABLE_COUNTRIES =
        		"create table tblcountries (_id integer primary key, country text);";
        	final String CREATE_TABLE_STATES =
        		"create table tblstates (_id integer primary key, state text, countryid integer not null);";

        	db.execSQL(CREATE_TABLE_COUNTRIES);
        	db.execSQL(CREATE_TABLE_STATES);
    		
    	} catch (Exception e){
    		Toast.makeText(this, e.getMessage(),Toast.LENGTH_LONG).show();
    	}
    	
    }
    
    private void populateDB(){
    	try {
        	ContentValues valcountry = new ContentValues();
        	valcountry.put("country","USA");
        	long countryId = db.insert("tblcountries", null, valcountry);
        	ContentValues valstate = new ContentValues();
        	valstate.put("state", "California");
        	valstate.put("countryid", countryId);
      		db.insertOrThrow("tblstates", null, valstate);

      		valstate.clear();
        	valstate.put("state", "Texas");
        	valstate.put("countryid", countryId);
      		db.insertOrThrow("tblstates", null, valstate);
    	} catch (Exception e){
    		Toast.makeText(this, e.getMessage(),Toast.LENGTH_LONG).show();
    	}
    }
}