package ca.digitalcave.buddi.web.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.web.util.FormatUtil;

public class Entry {
	private Long id;
	private int categoryId;
	private Long amount;
	private Date date;
	private Date created;
	private Date modified;
	
	public Entry(){}
	public Entry(JSONObject json) throws JSONException {
		this.setId(json.has("id") ? json.getLong("id") : null);
		this.setCategoryId(json.has("categoryId") && json.getString("categoryId").length() > 0 ? json.getInt("categoryId") : null);
		this.setAmount(json.has("amount") && json.getString("amount").length() > 0 ? json.getLong("amount") : null);
		this.setDate(json.has("date") ? FormatUtil.parseDate(json.getString("date")) : null);
	}
	
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", this.getId());
		result.put("categoryId", this.getCategoryId());
		result.put("amount", this.getAmount());
		result.put("date", FormatUtil.formatDate((Date) this.getDate()));
		result.put("created", FormatUtil.formatDateTime((Date) this.getCreated()));
		result.put("modified", FormatUtil.formatDateTime((Date) this.getModified()));
		return result;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public Long getAmount() {
		return amount;
	}
	public void setAmount(Long amount) {
		this.amount = amount;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
}
