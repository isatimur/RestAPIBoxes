package ru.soft.top.restapi;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import ru.soft.top.restapi.loader.RESTLoader;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/**
 * A simple ListActivity that display Boxes that contain the word
 * Android in them.
 * 
 * @author Timur Isachenko
 *
 */
public class RESTLoaderActivity extends FragmentActivity implements LoaderCallbacks<RESTLoader.RESTResponse> {
    private static final String TAG = RESTLoaderActivity.class.getName();
    
    private static final int LOADER_BOXES_SEARCH = 0x1;
    
    private static final String ARGS_URI    = "ru.soft.top.restapi.ARGS_URI";
    private static final String ARGS_PARAMS = "ru.soft.top.restapi.ARGS_PARAMS";
    
    private ArrayAdapter<String> mAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_loader);
        
        // Since we are using the Android Compatibility library
        // we have to use FragmentActivity. So, we use ListFragment
        // to get the same functionality as ListActivity.
        FragmentManager fm = getSupportFragmentManager();
        
        
        ListFragment list =(ListFragment) fm.findFragmentById(R.id.fragment_content); 
        if (list == null){
        	list = new ListFragment();
        	FragmentTransaction ft = fm.beginTransaction();
        	ft.add(R.id.fragment_content, list);
        	ft.commit();
        }
        
        mAdapter = new ArrayAdapter<String>(this, R.layout.item_label_list);
        
        // Let's set our list adapter to a simple ArrayAdapter.
        list.setListAdapter(mAdapter);
        
        // This is our REST action.
        Uri boxesSearchUri = Uri.parse("http://wall.wall.cloudbees.net/api/v1/boxes/");
        
        // Here we are going to place our REST call parameters. Note that
        // we could have just used Uri.Builder and appendQueryParameter()
        // here, but I wanted to illustrate how to use the Bundle params.
        Intent intent = getIntent();
        String accessToken = intent.getStringExtra(LoginActivity.ACCESS_TOKEN);
        Bundle params = new Bundle();
        params.putString("access_token",accessToken);
        
        // These are the loader arguments. They are stored in a Bundle because
        // LoaderManager will maintain the state of our Loaders for us and
        // reload the Loader if necessary. This is the whole reason why
        // we have even bothered to implement RESTLoader.
        Bundle args = new Bundle();
        args.putParcelable(ARGS_URI, boxesSearchUri);
        args.putParcelable(ARGS_PARAMS, params);
        
        // Initialize the Loader.
        getLoaderManager().initLoader(LOADER_BOXES_SEARCH, args, this);
    }

    @Override
    public Loader<RESTLoader.RESTResponse> onCreateLoader(int id, Bundle args) {
        if (args != null && args.containsKey(ARGS_URI) && args.containsKey(ARGS_PARAMS)) {
            Uri    action = args.getParcelable(ARGS_URI);
            Bundle params = args.getParcelable(ARGS_PARAMS);
            
            return new RESTLoader(this, RESTLoader.HTTPVerb.GET, action, params);
        }
        
        return null;
    }

    @Override
    public void onLoadFinished(Loader<RESTLoader.RESTResponse> loader, RESTLoader.RESTResponse data) {
        int    code = data.getCode();
        String json = data.getData();
        
        // Check to see if we got an HTTP 200 code and have some data.
        if (code == 200 && !json.equals("")) {
            
            // For really complicated JSON decoding I usually do my heavy lifting
            // Gson and proper model classes, but for now let's keep it simple
            // and use a utility method that relies on some of the built in
            // JSON utilities on Android.
            List<String> boxes = getBoxesFromJson(json);
            
            // Load our list adapter with our Tweets.
            mAdapter.clear();
            for (String box : boxes) {
                mAdapter.add(box);
            }
        }
        else {
            Toast.makeText(this, "Failed to load data. Check your internet connection.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<RESTLoader.RESTResponse> loader) {
    }
    
    private static List<String> getBoxesFromJson(String json) {
        ArrayList<String> boxList = new ArrayList<String>();
        
        try {
        	JSONArray boxesWrapper = (JSONArray) new JSONTokener(json).nextValue();
            JSONObject  boxes        = boxesWrapper.getJSONObject(0);
            
                boxList.add(boxes.getString("filename"));
            
        }
        catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON.", e);
        }
        
        return boxList;
    }
}