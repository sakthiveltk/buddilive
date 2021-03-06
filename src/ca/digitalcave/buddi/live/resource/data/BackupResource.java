package ca.digitalcave.buddi.live.resource.data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Entries;
import ca.digitalcave.buddi.live.db.ScheduledTransactions;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.Entry;
import ca.digitalcave.buddi.live.model.ScheduledTransaction;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class BackupResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession(true);
		final User user = (User) getRequest().getClientInfo().getUser();
		try {
			final JSONObject result = new JSONObject();
			final List<Account> accounts = sqlSession.getMapper(Sources.class).selectAccounts(user);
			final List<Category> categories = Category.getHierarchy(sqlSession.getMapper(Sources.class).selectCategories(user));
			final List<Transaction> transactions = sqlSession.getMapper(Transactions.class).selectTransactions(user);
			final List<ScheduledTransaction> scheduledTransactions = sqlSession.getMapper(ScheduledTransactions.class).selectScheduledTransactions(user);
			final List<Entry> entries = sqlSession.getMapper(Entries.class).selectEntries(user);
			final Map<Integer, String> sourceUUIDsById = new HashMap<Integer, String>();
			
			for (Account account : accounts) {
				addAccount(result, user, account, sourceUUIDsById);
			}
			for (Category category : categories) {
				addCategory(result, user, category, sourceUUIDsById);
			}
			for (Entry entry : entries) {
				addEntry(result, user, entry, sourceUUIDsById);
			}
			for (Transaction transaction : transactions) {
				addTransaction(result, user, transaction, sourceUUIDsById);
			}
			
			for (ScheduledTransaction scheduledTransaction : scheduledTransactions){
				addScheduledTransaction(result, user, scheduledTransaction, sourceUUIDsById);
			}
			
			final JsonRepresentation json = new JsonRepresentation(result);
			final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
			disposition.setFilename("Backup (" + FormatUtil.formatDate(new Date(), user) + ").json");
			json.setDisposition(disposition);
			return json;
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}
	
	private void addAccount(JSONObject result, User user, Account account, Map<Integer, String> sourceUUIDsById) throws JSONException, CryptoException {
		sourceUUIDsById.put(account.getId(), account.getUuid());
		final JSONObject a = new JSONObject();
		a.put("uuid", account.getUuid());
		a.put("name", CryptoUtil.decryptWrapper(account.getName(), user));
		a.put("startDate", FormatUtil.formatDateInternal((Date) account.getStartDate()));
		if (account.isDeleted()) a.put("deleted", account.isDeleted());
		a.put("type", account.getType());
		a.put("startBalance", CryptoUtil.decryptWrapperBigDecimal(account.getStartBalance(), user, true).toPlainString());
		a.put("accountType", CryptoUtil.decryptWrapper(account.getAccountType(), user));
		result.append("accounts", a);
	}
	
	private void addCategory(JSONObject result, User user, Category category, Map<Integer, String> sourceUUIDsById) throws JSONException, CryptoException {
		sourceUUIDsById.put(category.getId(), category.getUuid());
		final JSONObject c = new JSONObject();
		c.put("uuid", category.getUuid());
		c.put("name", CryptoUtil.decryptWrapper(category.getName(), user));
		if (category.isDeleted()) c.put("deleted", category.isDeleted());
		c.put("type", category.getType());
		c.put("parent", sourceUUIDsById.get(category.getParent()));
		c.put("periodType", category.getPeriodType());
		if (category.getChildren() != null){
			for (Category child : category.getChildren()) {
				addCategory(c, user, child, sourceUUIDsById);
			}
		}
		result.append("categories", c);
	}
	
	private void addEntry(JSONObject result, User user, Entry entry, Map<Integer, String> sourceUUIDsById) throws JSONException, CryptoException {
		final JSONObject e = new JSONObject();
		e.put("date", FormatUtil.formatDateInternal((Date) entry.getDate()));
		e.put("category", sourceUUIDsById.get(entry.getCategoryId()));
		e.put("amount", CryptoUtil.decryptWrapperBigDecimal(entry.getAmount(), user, true).toPlainString());
		result.append("entries", e);
	}
	
	private void addTransaction(JSONObject result, User user, Transaction transaction, Map<Integer, String> sourceUUIDsById) throws JSONException, CryptoException {
		final JSONObject t = new JSONObject();
		t.put("uuid", transaction.getUuid());
		t.put("description", CryptoUtil.decryptWrapper(transaction.getDescription(), user));
		t.put("number", CryptoUtil.decryptWrapper(transaction.getNumber(), user));
		t.put("date", FormatUtil.formatDateInternal((Date) transaction.getDate()));
		if (transaction.isDeleted()) t.put("deleted", transaction.isDeleted());
		if (transaction.getSplits() != null){
			for (Split split : transaction.getSplits()) {
				final JSONObject s = new JSONObject();
				s.put("amount", CryptoUtil.decryptWrapperBigDecimal(split.getAmount(), user, true).toPlainString());
				s.put("from", sourceUUIDsById.get(split.getFromSource()));
				s.put("to", sourceUUIDsById.get(split.getToSource()));
				s.put("memo", CryptoUtil.decryptWrapper(split.getMemo(), user));
				t.append("splits", s);
			}
		}
		result.append("transactions", t);
	}

	private void addScheduledTransaction(JSONObject result, User user, ScheduledTransaction scheduledTransaction, Map<Integer, String> sourceUUIDsById) throws JSONException, CryptoException {
		final JSONObject t = new JSONObject();
		t.put("uuid", scheduledTransaction.getUuid());
		t.put("description", CryptoUtil.decryptWrapper(scheduledTransaction.getDescription(), user));
		t.put("number", CryptoUtil.decryptWrapper(scheduledTransaction.getNumber(), user));
		t.put("scheduleName", CryptoUtil.decryptWrapper(scheduledTransaction.getScheduleName(), user));
		t.put("scheduleDay", scheduledTransaction.getScheduleDay());
		t.put("scheduleWeek", scheduledTransaction.getScheduleWeek());
		t.put("scheduleMonth", scheduledTransaction.getScheduleMonth());
		t.put("frequencyType", scheduledTransaction.getFrequencyType());
		t.put("startDate", FormatUtil.formatDateInternal((Date) scheduledTransaction.getStartDate()));
		t.put("endDate", FormatUtil.formatDateInternal((Date) scheduledTransaction.getEndDate()));
		t.put("lastCreatedDate", FormatUtil.formatDateInternal((Date) scheduledTransaction.getLastCreatedDate()));
		t.put("message", CryptoUtil.decryptWrapper(scheduledTransaction.getMessage(), user));
		if (scheduledTransaction.getSplits() != null){
			for (Split split : scheduledTransaction.getSplits()) {
				final JSONObject s = new JSONObject();
				s.put("amount", CryptoUtil.decryptWrapperBigDecimal(split.getAmount(), user, true).toPlainString());
				s.put("from", sourceUUIDsById.get(split.getFromSource()));
				s.put("to", sourceUUIDsById.get(split.getToSource()));
				s.put("memo", CryptoUtil.decryptWrapper(split.getMemo(), user));
				t.append("splits", s);
			}
		}
		result.append("scheduledTransactions", t);
	}

}
