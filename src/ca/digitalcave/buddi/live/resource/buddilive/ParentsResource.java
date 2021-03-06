package ca.digitalcave.buddi.live.resource.buddilive;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class ParentsResource extends ServerResource {

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
			final List<Category> categories = Category.getHierarchy(sqlSession.getMapper(Sources.class).selectCategories(user));
			Integer exclude = null;
			try { exclude = Integer.parseInt(getQuery().getFirstValue("exclude")); }
			catch (Throwable e){}
			final Category category = (exclude == null ? null : sqlSession.getMapper(Sources.class).selectCategory(user, exclude));
			
			final JSONObject result = new JSONObject();
			final JSONArray data = new JSONArray();
			
			final JSONObject item = new JSONObject();
			item.put("value", "");
			item.put("text", LocaleUtil.getTranslation(getRequest()).getString("TOP_LEVEL"));
			data.put(item);

			
			getJsonArray(data, categories, category, user, 0);
			result.put("data", data);
			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}
	
	private void getJsonArray(JSONArray array, List<Category> categories, Category exclude, User user, int depth) throws JSONException, CryptoException {
		final StringBuilder sb = new StringBuilder();
		for (Category category : categories) {
			if (exclude != null && (category.getId().equals(exclude.getId()) || !category.getType().equals(exclude.getType()) || !category.getPeriodType().equals(exclude.getPeriodType()))) continue;
			final JSONObject item = new JSONObject();
			item.put("value", category.getId());
			if (category.isDeleted()) sb.append(" text-decoration: line-through;");
			if (!category.isIncome()) sb.append(" color: " + FormatUtil.HTML_RED + ";");
			item.put("style", sb.toString());
			sb.setLength(0);
			item.put("text", StringUtils.repeat("\u00a0", depth * 2) + CryptoUtil.decryptWrapper(category.getName(), user));
			item.put("income", category.isIncome());
			item.put("type", category.getType());
			item.put("periodType", category.getPeriodType());
			array.put(item);
			if (category.getChildren() != null) getJsonArray(array, category.getChildren(), exclude, user, depth + 1);
		}
	}
}
