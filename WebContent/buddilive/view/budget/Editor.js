Ext.define('BuddiLive.view.budget.Editor', {
	"extend": "Ext.window.Window",
	"alias": "widget.budgeteditor",
	"requires": [
		"BuddiLive.view.component.CurrencyField",
		"BuddiLive.view.budget.ParentCombobox"
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected
		var editor = this;
		
		this.title = (s ? "${translation("EDIT_BUDGET_CATEGORY")?json_string}" : "${translation("ADD_BUDGET_CATEGORY")?json_string}")
		this.layout = "fit";
		this.modal = true;
		this.width = 400;
		this.items = [
			{
				"xtype": "form",
				"layout": "anchor",
				"bodyPadding": 5,
				"items": [
					{
						"xtype": "hidden",
						"itemId": "id",
						"value": (s ? s.id : null)
					},
					{
						"xtype": "selfdocumentingfield",
						"anchor": "100%",
						"messageBody": "${translation("HELP_BUDGET_CATEGORY_NAME")?json_string}",
						"type": "textfield",
						"itemId": "name",
						"value": (s ? s.name : null),
						"fieldLabel": "${translation("BUDGET_CATEGORY_NAME")?json_string}",
						"allowBlank": false,
						"enableKeyEvents": true,
						"emptyText": "${translation("BUDGET_CATEGORY_EXAMPLES")?json_string}",
						"listeners": {
							"afterrender": function(field) {
								field.focus(false, 500);
							}
						}
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_BUDGET_CATEGORY_PARENT")?json_string}",
						"type": "parentcombobox",
						"itemId": "parent",
						"fieldLabel": "${translation("BUDGET_CATEGORY_PARENT")?json_string}",
						"emptyText": "Parent",
						"value": (s ? s.parent : null),
						"url": "data/categories/parents.json" + (s ? "?exclude=" + s.id : ""),
						"listeners": {
							"change": function(){
								var parent = editor.down("parentcombobox[itemId='parent']");
								if (parent.getValue() != null && (parent.getValue() + "").length > 0){
									editor.down("combobox[itemId='periodType']").setValue(parent.getStore().findRecord("value", parent.getValue()).data.periodType);
									editor.down("combobox[itemId='type']").setValue(parent.getStore().findRecord("value", parent.getValue()).data.type);
								}
								editor.down("combobox[itemId='periodType']").setDisabled(parent.getValue() != null && parent.getValue() != "");
								editor.down("combobox[itemId='type']").setDisabled(parent.getValue() != null && parent.getValue() != "");
							}
						}
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_BUDGET_CATEGORY_PERIOD_TYPE")?json_string}",
						"type": "combobox",
						"itemId": "periodType",
						"value": (s ? s.type : "MONTH"),
						"hidden": s != null,
						"fieldLabel": "${translation("BUDGET_CATEGORY_PERIOD_TYPE")?json_string}",
						"editable": false,
						"allowBlank": false,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "${translation("BUDGET_CATEGORY_TYPE_WEEK")?json_string}", "value": "WEEK"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_SEMI_MONTH")?json_string}", "value": "SEMI_MONTH"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_MONTH")?json_string}", "value": "MONTH"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_QUARTER")?json_string}", "value": "QUARTER"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_SEMI_YEAR")?json_string}", "value": "SEMI_YEAR"},
								{"text": "${translation("BUDGET_CATEGORY_TYPE_YEAR")?json_string}", "value": "YEAR"}
							]
						}),
						"queryMode": "local",
						"valueField": "value"
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_BUDGET_CATEGORY_TYPE")?json_string}",
						"type": "combobox",
						"itemId": "type",
						"value": (s ? s.categoryType : "E"),
						"hidden": s != null,
						"fieldLabel": "${translation("BUDGET_CATEGORY_TYPE")?json_string}",
						"editable": false,
						"allowBlank": false,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "${translation("INCOME")?json_string}", "value": "I"},
								{"text": "${translation("EXPENSE")?json_string}", "value": "E"}
							]
						}),
						"queryMode": "local",
						"valueField": "value"
					}
				]
			}
		];
		this.buttons = [
			{
				"text": "${translation("OK")?json_string}",
				"itemId": "ok",
				"disabled": true
			},
			{
				"text": "${translation("CANCEL")?json_string}",
				"itemId": "cancel"
			}
		]
	
		this.callParent(arguments);
	}
});