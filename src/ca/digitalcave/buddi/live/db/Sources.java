package ca.digitalcave.buddi.live.db;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.AccountType;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.CategoryPeriod;
import ca.digitalcave.buddi.live.model.Source;
import ca.digitalcave.buddi.live.model.User;


public interface Sources {
	public Account selectAccount(@Param("user") User user, @Param("uuid") String uuid);
	
	public List<Account> selectAccountBalances(@Param("user") User user);

	public List<Account> selectAccounts(@Param("user") User user);
	public List<Account> selectAccounts(@Param("user") User user, @Param("accountType") String accountType);
	
	public List<AccountType> selectAccountTypes(@Param("user") User user);
	
	public Category selectCategory(@Param("user") User user, @Param("id") Integer id);
	public Category selectCategory(@Param("user") User user, @Param("categoryPeriod") CategoryPeriod categoryPeriod, @Param("id") Integer id);
	public Category selectCategory(@Param("user") User user, @Param("uuid") String uuid);
	
	public List<Category> selectCategories(@Param("user") User user);
	public List<Category> selectCategories(@Param("user") User user, @Param("income") Boolean income);
	public List<Category> selectCategories(@Param("user") User user, @Param("periodType") String periodType);
	public List<Category> selectCategories(@Param("user") User user, @Param("categoryPeriod") CategoryPeriod categoryPeriod);
	
	@MapKey("id")
	public Map<Integer, Category> selectCategoriesMap(@Param("user") User user);
	
	public List<String> selectCategoryPeriods(@Param("user") User user);
	
	public Source selectSource(@Param("user") User user, @Param("id") int id);
	public Integer selectSourceAssociatedCount(@Param("user") User user, @Param("source") Source source);
	
	public Integer insertAccount(@Param("user") User user, @Param("account") Account account);
	public Integer insertCategory(@Param("user") User user, @Param("category") Category category);
	
	public Integer updateAccount(@Param("user") User user, @Param("account") Account account);
	public Integer updateAccountBalance(@Param("user") User user, @Param("accountId") int accountId, @Param("balance") String balance);
	public Integer updateCategory(@Param("user") User user, @Param("category") Category category);
	public Integer updateSourceDeleted(@Param("user") User user, @Param("source") Source source);
	
	public Integer deleteSource(@Param("user") User user, @Param("source") Source source);
	public Integer deleteAllSources(@Param("user") User user);
}
