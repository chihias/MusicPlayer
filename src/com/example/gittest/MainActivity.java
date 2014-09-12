
package com.example.gittest;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //addPlayerFragment();
        addListFragment();
    }

    private void addPlayerFragment() {
        Fragment newFrag = new playerFragment();
        FragmentManager fragMng = getFragmentManager();
        FragmentTransaction fragTran = fragMng.beginTransaction();
        fragTran.add(R.id.MainActivityUI, newFrag, "player");
        fragTran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragTran.commit();
    }

    private void addListFragment() {
        Fragment newFrag = new ListFragment();
        FragmentManager fragMng = getFragmentManager();
        FragmentTransaction fragTran = fragMng.beginTransaction();
        fragTran.add(R.id.MainActivityUI, newFrag, "list");
        fragTran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragTran.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
