package fr.fouss.drawy;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class DrawActivity extends AppCompatActivity {

    private MenuItem toolbarColor;
    private MenuItem toolbarThickness;
    private MenuItem toolbarShape;
    private MenuItem toolbarBrush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * When creating the toolbar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.draw_menu, menu);
        toolbarColor = menu.findItem(R.id.colorButton);
        toolbarThickness = menu.findItem(R.id.thicknessButton);
        toolbarShape = menu.findItem(R.id.shapeButton);
        toolbarBrush = menu.findItem(R.id.brushButton);
        return true;
    }

    /**
     * When a menu item in the toolbar is clicked
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.colorButton :
                return true;
            case R.id.thicknessButton :
                return true;
            case R.id.shapeButton :
                return true;
            case R.id.brushButton :
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }
}
