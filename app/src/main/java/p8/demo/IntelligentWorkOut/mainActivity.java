package p8.demo.IntelligentWorkOut;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

// declaration de notre activity h�rit�e de Activity
public class mainActivity extends Activity {

    private mainView mView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // initialise notre activity avec le constructeur parent    	
        super.onCreate(savedInstanceState);
        // charge le fichier main.xml comme vue de l'activité
        setContentView(R.layout.main);
        // recuperation de la vue une voie cree à partir de son id
        mView = (mainView) findViewById(R.id.mainView);
        // rend visible la vue
        mView.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mView.pause();
        //mView.stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mView.resume();
    }
}