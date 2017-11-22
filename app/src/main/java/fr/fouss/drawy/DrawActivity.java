package fr.fouss.drawy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

public class DrawActivity extends AppCompatActivity {

    private DrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawView = findViewById(R.id.drawView);
        drawView.resetCanvas(getResources().getColor(R.color.canvasDefaultColor));
        drawView.setBrushColor(getResources().getColor(R.color.paintDefaultColor));
        TypedValue value = new TypedValue();
        getResources().getValue(R.dimen.paintDefaultThickness, value, false);
        drawView.setBrushThickness((int) value.getFloat());

        SeekBar thicknessSeekbar = findViewById(R.id.thicknessSeekbar);
        thicknessSeekbar.setProgress(drawView.getBrushThickness());
        thicknessSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawView.setBrushThickness(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.draw_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.colorButton :
                ColorPickerDialogBuilder
                        .with(this)
                        .setTitle("Choose a color")
                        .initialColor(drawView.getBrushColor())
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(10)
                        .showAlphaSlider(true)
                        .showLightnessSlider(true)
                        .showColorEdit(false)
                        .showColorPreview(false)
                        .setOnColorSelectedListener(selectedColor -> {})
                        .setPositiveButton("Ok", (dialog, selectedColor, allColors) -> drawView.setBrushColor(selectedColor))
                        .setNegativeButton("Cancel", (dialog, which) -> {})
                        .build()
                        .show();
                return true;
            case R.id.thicknessButton :
                LinearLayout thicknessContainer = findViewById(R.id.thicknessContainer);
                thicknessContainer.setVisibility(thicknessContainer.getVisibility() == View.VISIBLE
                        ? View.INVISIBLE : View.VISIBLE);
                return true;
            case R.id.shapeButton :
                drawView.setMode(DrawView.Mode.SHAPE);
                return true;
            case R.id.brushButton :
                drawView.setMode(DrawView.Mode.BRUSH);
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }
}
