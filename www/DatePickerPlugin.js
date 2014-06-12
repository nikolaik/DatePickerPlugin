/**
 * Cordova DatePicker plugin.js
 *
 * @author Cristobal Dabed
 */
var exec = require('cordova/exec');


var pad       = function (val) { return (String(val).length == 1 ? "0" : "") + String(val); };
var parseDate = function (val) { return new Date(parseFloat(val)); };

/**
 * Datepicker
 */
function DatePicker() {
    var self = this;

    this.options   = null;
    this.callbacks =  {
        onSuccess: function (options) {
            if (options.changed !== false) {
                self.onChange(options.date);
            }
            else {
                self.onDismiss(options.date);
            }
        },
        onError: function () {
            self.onError();
        }
    };
}

/**
 * Show
 *
 * @param options
 */
DatePicker.prototype.show = function (options) {
    var date = options.date ? options.date : '';
    if (date) {
        options.date = [
            pad(date.getMonth() + 1), '/', pad(date.getDate()), '/', date.getFullYear(), '/',
            pad(date.getHours()), '/', pad(date.getMinutes())
        ].join("");

        // options.date = (options.date.getMonth() + 1) + "/" + (options.date.getDate()) + "/" + (options.date.getFullYear()) + "/"
        //		+ (options.date.getHours()) + "/" + (options.date.getMinutes());
    }

    var defaults = {
        mode: 'datetime', // date or time or blank for both
        date: '',
        allowOldDates:    true,
        allowFutureDates: true
    };

    for (var key in defaults) {
        if (key in options) {
            defaults[key] = options[key];
        }
    }

    // defaults.onChange = ("onChange" in options);

    this.options = options;
    exec(this.callbacks.onSuccess, this.callbacks.onError, "DatePicker", "show", [defaults]);
};

/**
 * Hide
 */
DatePicker.prototype.hide = function () {
    exec(null, null, "DatePicker", "hide", []);
};

/**
 * Dismiss
 *
 * @param val
 */
DatePicker.prototype.onDismiss = function (val) {
    if (this.options.onDismiss) {
        this.options.onDismiss(parseDate(val));
    }
};

/**
 * On error
 *
 * @param val
 */
DatePicker.prototype.onError = function (val) {
    if (this.options.onError) {
        this.options.onError(val);
    }
};

/**
 * On change
 *
 * @param val
 */
DatePicker.prototype.onChange = function (val) {
    if (this.options.onChange) {
        this.options.onChange(parseDate(val));
    }
};

module.exports = new DatePicker();
