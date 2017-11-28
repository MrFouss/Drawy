package fr.fouss.drawy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DrawActivity extends AppCompatActivity {

    private static final int FILE_SELECTION_CODE = 1337;

    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_RETURN_CODE = 7357;

    private DrawView drawView;

    private MenuItem toolbarColorButton;
    private MenuItem toolbarThicknessButton;
    private MenuItem toolbarInsertImageButton;
    private MenuItem toolbarSaveImageButton;
    private MenuItem toolbarCancelImageButton;
    private MenuItem toolbarConfirmImageButton;
    private LinearLayout thicknessContainer;

    private Boolean writeExternalStoragePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        writeExternalStoragePermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        drawView = findViewById(R.id.drawView);

        thicknessContainer = findViewById(R.id.thicknessContainer);

        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            try {
                Uri imageUri = Uri.parse(imageUriString);
                drawView.resetCanvas(MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri));
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "I/O error", Toast.LENGTH_SHORT).show();
            }
        } else {
            drawView.resetCanvas(getResources().getColor(R.color.canvasDefaultColor));
        }

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

        toolbarColorButton = menu.findItem(R.id.colorButton);
        toolbarThicknessButton = menu.findItem(R.id.thicknessButton);
        toolbarInsertImageButton = menu.findItem(R.id.insertImageButton);
        toolbarSaveImageButton = menu.findItem(R.id.saveImageButton);
        toolbarCancelImageButton = menu.findItem(R.id.cancelImageButton);
        toolbarConfirmImageButton = menu.findItem(R.id.confirmImageButton);

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
            case R.id.thicknessButton:
                thicknessContainer.setVisibility(thicknessContainer.getVisibility() == View.VISIBLE
                        ? View.INVISIBLE : View.VISIBLE);
                return true;
            case R.id.insertImageButton:
                showFileChooser();
                return true;
            case R.id.saveImageButton:
                if (!writeExternalStoragePermission) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_EXTERNAL_STORAGE_PERMISSION_RETURN_CODE);
                    Toast.makeText(this, "Please try to save your image again", Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (!saveImage()) {
                    Toast.makeText(this, "Something bad happened while saving the image...", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.dialog_content_quit_on_save))
                            .setTitle(getString(R.string.dialog_title_quit_on_save));
                    builder.setPositiveButton("Yes", (dialog, id) -> finish());
                    builder.setNegativeButton("No", (dialog, id) -> {
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                return true;
            case R.id.cancelImageButton:
                drawView.setMode(DrawView.Mode.BRUSH);

                toolbarColorButton.setVisible(true);
                toolbarThicknessButton.setVisible(true);
                toolbarInsertImageButton.setVisible(true);
                toolbarSaveImageButton.setVisible(true);

                toolbarCancelImageButton.setVisible(false);
                toolbarConfirmImageButton.setVisible(false);

                return true;
            case R.id.confirmImageButton:
                drawView.anchorImage();
                drawView.setMode(DrawView.Mode.BRUSH);

                toolbarColorButton.setVisible(true);
                toolbarThicknessButton.setVisible(true);
                toolbarInsertImageButton.setVisible(true);
                toolbarSaveImageButton.setVisible(true);

                toolbarCancelImageButton.setVisible(false);
                toolbarConfirmImageButton.setVisible(false);

                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_PERMISSION_RETURN_CODE:
                writeExternalStoragePermission = true;
                break;
        }
    }

    private Boolean saveImage() {
        File storageDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Drawy");

        if (!storageDirectory.exists()) {
            if (!storageDirectory.mkdirs())
                return false;
        }

        @SuppressLint("SimpleDateFormat")
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());

        File mediaFile;
        String imageName = "drawy_" + timestamp + ".png";
        mediaFile = new File(storageDirectory.getPath() + File.separator + imageName);

        try (FileOutputStream fos = new FileOutputStream(mediaFile)) {
            drawView.getDrawing().compress(Bitmap.CompressFormat.PNG, 90, fos);
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        Toast.makeText(this, "Drawing saved at " + mediaFile.getPath(), Toast.LENGTH_LONG).show();

        return true;
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select an image to open"),
                    FILE_SELECTION_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a file manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case FILE_SELECTION_CODE:
                if (resultCode == RESULT_OK && data.getData() != null) {
                    try {
                        Bitmap image = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                        drawView.setImage(image);
                    } catch (IOException e) {
                        return;
                    }

                    drawView.setMode(DrawView.Mode.IMAGE);

                    toolbarColorButton.setVisible(false);
                    toolbarThicknessButton.setVisible(false);
                    toolbarInsertImageButton.setVisible(false);
                    toolbarSaveImageButton.setVisible(false);

                    toolbarCancelImageButton.setVisible(true);
                    toolbarConfirmImageButton.setVisible(true);
                }
                break;
        }
    }
}
