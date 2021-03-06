Ext.define("BuddiLive.controller.budget.Editor", {
	"extend": "Ext.app.Controller",
	"stores": [
		"transaction.split.FromComboboxStore",
		"transaction.split.ToComboboxStore"
	],

	"init": function() {
		this.control({
			"budgeteditor combobox": {
				"blur": this.updateButtons,
				"select": this.updateButtons,
				"afterrender": this.updateButtons
			},
			"budgeteditor textfield": {
				"blur": this.updateButtons,
				"afterrender": this.updateButtons,
				"keyup": this.updateButtons
			},
			"budgeteditor button[itemId='ok']": {"click": this.ok},
			"budgeteditor button[itemId='cancel']": {"click": this.cancel}
		});
	},
	
	"cancel": function(component){
		component.up("budgeteditor").close();
	},
	
	"ok": function(component){
		var me = this;
		var window = component.up("budgeteditor");
		var panel = window.initialConfig.panel;
		var selected = window.initialConfig.selected;

		var request = {};
		request.action = (selected ? "update" : "insert");
		if (selected) request.id = selected.id;
		request.name = window.down("textfield[itemId='name']").getValue();
		request.periodType = window.down("textfield[itemId='periodType']").getValue();
		request.parent = window.down("parentcombobox[itemId='parent']").getValue();
		request.type = window.down("combobox[itemId='type']").getValue();

		var conn = new Ext.data.Connection();
		conn.request({
			"url": "data/categories",
			"headers": {
				"Accept": "application/json"
			},
			"method": "POST",
			"jsonData": request,
			"success": function(response){
				window.close();
				panel.fireEvent("reload", panel);
				me.getTransactionSplitFromComboboxStoreStore().load();
				me.getTransactionSplitToComboboxStoreStore().load();
			},
			"failure": function(response){
				BuddiLive.app.error(response);
			}
		});
	},
	
	"updateButtons": function(component, foo, bar, baz){
		var window = component.up("budgeteditor");
		var ok = window.down("button[itemId='ok']");
		var name = window.down("textfield[itemId='name']");
		var periodType = window.down("textfield[itemId='periodType']");
		var type = window.down("combobox[itemId='type']");
		
		ok.setDisabled(name.getValue().length == 0 || periodType.getValue().length == 0 || type.getValue().length == 0);
	}
});