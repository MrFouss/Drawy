package fr.fouss.drawy;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

public class DrawActivity extends AppCompatActivity {

    private MenuItem toolbarColor;
    private MenuItem toolbarThickness;
    private MenuItem toolbarShape;
    private MenuItem toolbarBrush;

    private int drawingColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawingColor = 0xffffffff;
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
                ColorPickerDialogBuilder
                        .with(this)
                        .setTitle("Choose a color")
                        .initialColor(drawingColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(10)
                        .showAlphaSlider(false)
                        .showLightnessSlider(true)
                        .showColorEdit(false)
                        .showColorPreview(false)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                            }
                        })
                        .setPositiveButton("Ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                Toast.makeText(DrawActivity.this, "setDrawingColor: 0x" + Integer.toHexString(selectedColor), Toast.LENGTH_SHORT).show();
                                drawingColor = selectedColor;
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        })
                        .build()
                        .show();
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
