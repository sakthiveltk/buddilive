Ext.define('BuddiLive.view.component.CurrencyField', {
	"extend": "Ext.form.NumberField",
	"alias": "widget.currencyfield",

	"decimalSeparator": Ext.util.Format.decimalSeparator,
	"thousandSeparator": Ext.util.Format.thousandSeparator,
	"forcePrecision" : false,
	"hideTrigger": true,
	"keyNavEnabled": false,
	"mouseWheelEnabled": false,
	"emptyText": "0${decimalSeparator!"."?json_string}00",
	
	"initComponent": function(){
		Ext.util.Format.decimalSeparator = "${decimalSeparator!"."?json_string}";
		Ext.util.Format.thousandSeparator = "${thousandSeparator!"."?json_string}";

		this.callParent(arguments);
	},

	//Convert a string to a number
	"parseValue": function(value){
		var me = this;
		if (!isNaN(value)) return value;	//If this is already a number, then just return it.
		var parsedValue = parseFloat(String(value).replace("${currencySymbol!?json_string}", "").replace(me.thousandSeparator, "").replace(me.decimalSeparator, "."));
		return isNaN(parsedValue) ? null : parsedValue;
	},
	
	//Convert a number or number-ish string to a formatted string for display
	"valueToRaw": function(value) {
		var me = this;
		value = me.parseValue(value);
		if (isNaN(value)){
			return "";
		}
		else {
			return Ext.util.Format.number(value, "0,0.00");
		}
	},
	
	"validate": function(){
		return !isNaN(this.rawToValue(this.getRawValue()));	
	}
});