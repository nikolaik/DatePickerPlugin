/**
 * @author ng4e
 * @author Daniel van 't Oever
 * @author Cristobal Dabed
 * 
 * Rewritten plugin so it is similar to the iOS datepicker plugin and it
 * accepts prefilled dates and time
 */
package no.zeppelin.plugins;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

public class DatePickerPlugin extends CordovaPlugin {

    private static final String ACTION_DATE = "date";
    private static final String ACTION_TIME = "time";
    private final String pluginName = "DatePickerPlugin";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        Log.d(pluginName, "DatePicker called with options: " + args);

        return this.show(args, callbackContext);
    }

    public synchronized boolean show(final JSONArray data, final CallbackContext callbackContext) {
        final Calendar c = Calendar.getInstance();
        final Runnable runnable;
        final Context currentCtx = cordova.getActivity();
        final DatePickerPlugin datePickerPlugin = this;

        String action = "date";

        /*
         * Parse information from data parameter and where possible, override
         * above date fields
         */
        int month = -1, day = -1, year = -1, hour = -1, min = -1;
        try {
            JSONObject obj = data.getJSONObject(0);
            action = obj.getString("mode");

            String optionDate = obj.getString("date");

            String[] datePart = optionDate.split("/");
            month = Integer.parseInt(datePart[0]);
            day = Integer.parseInt(datePart[1]);
            year = Integer.parseInt(datePart[2]);
            hour = Integer.parseInt(datePart[3]);
            min = Integer.parseInt(datePart[4]);

            /* currently not handled in Android */
            // boolean optionAllowOldDates = obj.getBoolean("allowOldDates");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // By default initalize these fields to 'now'
        final int mYear = year == -1 ? c.get(Calendar.YEAR) : year;
        final int mMonth = month == -1 ? c.get(Calendar.MONTH) : month - 1;
        final int mDay = day == -1 ? c.get(Calendar.DAY_OF_MONTH) : day;
        final int mHour = hour == -1 ? c.get(Calendar.HOUR_OF_DAY) : hour;
        final int mMinutes = min == -1 ? c.get(Calendar.MINUTE) : min;

        if (ACTION_TIME.equalsIgnoreCase(action)) {
            runnable = new Runnable() {
                public void run() {
                    final TimeSetListener timeSetListener = new TimeSetListener(datePickerPlugin, callbackContext);
                    final TimePickerDialog timeDialog = new TimePickerDialog(currentCtx, timeSetListener, mHour,
                            mMinutes, true);
                    timeDialog.show();
                }
            };

        } else if (ACTION_DATE.equalsIgnoreCase(action)) {
            runnable = new Runnable() {
                public void run() {
                    final DateSetListener dateSetListener = new DateSetListener(datePickerPlugin, callbackContext);
                    final DatePickerDialog dateDialog = new DatePickerDialog(currentCtx, dateSetListener, mYear,
                            mMonth, mDay);
                    dateDialog.show();
                }
            };

        } else {
            Log.d(pluginName, "Unknown action. Only 'date' or 'time' are valid actions");
            return false;
        }

        cordova.getActivity().runOnUiThread(runnable);
        return true;
    }

    private final class DateSetListener implements OnDateSetListener {
        private final DatePickerPlugin datePickerPlugin;
        private final CallbackContext callbackContext;

        private DateSetListener(DatePickerPlugin datePickerPlugin, CallbackContext callbackContext) {
            this.datePickerPlugin = datePickerPlugin;
            this.callbackContext = callbackContext;
        }

        /**
         * Return a string containing the date in the format YYYY/MM/DD
         */
        public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
            String dateValue = Integer.toString(year) + "-" + pad(Integer.toString(monthOfYear + 1)) + "-" + pad(Integer.toString(dayOfMonth)) + "T00:00:00";

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date;
            JSONObject message;
            try {
                date    = dateFormat.parse(dateValue);
                message = new JSONObject();
                try {
                    message.put("date", date.getTime());
                    message.put("changed", false);
                    callbackContext.success(message);
                }
                catch (org.json.JSONException e) {
                    e.printStackTrace();
                }
            }
            catch (ParseException e) {
                e.printStackTrace();
            }

        }

        private String pad(String value) {
            return (value.length() == 1 ? "0" : "") + value;
        }
    }

    private final class TimeSetListener implements OnTimeSetListener {
        private final DatePickerPlugin datePickerPlugin;
        private final CallbackContext callbackContext;

        private TimeSetListener(DatePickerPlugin datePickerPlugin, CallbackContext callbackContext) {
            this.datePickerPlugin = datePickerPlugin;
            this.callbackContext = callbackContext;
        }

        /**
         * Return the current date with the time modified as it was set in the
         * time picker.
         */
        public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
            Date date = new Date();
            date.setHours(hourOfDay);
            date.setMinutes(minute);

            callbackContext.success(date.toLocaleString());
        }
    }

}
