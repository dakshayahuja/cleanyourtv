package tv.cleanyourtv.ps5;

import android.app.Activity;
import android.content.Intent;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Bundle;
import android.widget.Toast;

/** Switches the TV to the HDMI port the PlayStation is on (found by its CEC label), then finishes. */
public class Ps5Activity extends Activity {
    private static final String FALLBACK = "com.tcl.tvinput/.TvPassThroughService/HW15";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        String target = FALLBACK;
        TvInputManager tim = (TvInputManager) getSystemService(TV_INPUT_SERVICE);
        if (tim != null) {
            for (TvInputInfo info : tim.getTvInputList()) {
                CharSequence label = info.loadLabel(this);
                if (label != null && label.toString().toLowerCase().contains("playstation")) {
                    target = info.getParentId() != null ? info.getParentId() : info.getId();
                    break;
                }
            }
        }
        Intent view = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(TvContract.buildChannelUriForPassthroughInput(target), "vnd.android.cursor.item/channel")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(view);
        } catch (Exception e) {
            Toast.makeText(this, "PS5 input not available", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
