/**
 *
 */
package sample;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dbunit.Assertion;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import beans.Branch;
import junit.framework.TestCase;

/**
 * @author baba.kaoru
 *
 */
public class SampleTest extends TestCase {

	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String URL = "jdbc:mysql://localhost/test";
	private static final String USER = "root";
	private static final String PASSWORD = "root";

	public SampleTest(String name) {
		super(name);
	}

	private File file;

	//DB接続部分(DBUtil)
	static {
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static Connection getConnection() throws Exception {
		Class.forName(DRIVER);
		Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
		return connection;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		IDatabaseConnection connection = null;
		try {
			super.setUp();
			Connection conn = getConnection();
			connection = new DatabaseConnection(conn);

			//現状のバックアップを取得
			QueryDataSet partialDataSet = new QueryDataSet(connection);
			partialDataSet.addTable("branch");
			partialDataSet.addTable("branch_sale_out");
			file = File.createTempFile("branch", ".xml");
			FlatXmlDataSet.write(partialDataSet,
					new FileOutputStream(file));

			//現状のバックアップをエクセルで取得
			XlsDataSet.write(partialDataSet, new FileOutputStream("data.xls"));

			//テストデータを投入する
			IDataSet dataSetBranch = new FlatXmlDataSet(new File("branch_test_data.xml"));
			DatabaseOperation.CLEAN_INSERT.execute(connection, dataSetBranch);

			//テストデータを投入する
			IDataSet dataSetBranchSale = new FlatXmlDataSet(new File("branch_test_data2.xml"));
			DatabaseOperation.CLEAN_INSERT.execute(connection, dataSetBranchSale);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
			}
		}
	}

	/**
	 * テスト後の片付け
	 */
	@After
	public void tearDown() throws Exception {

		IDatabaseConnection connection = null;
		try {
			super.tearDown();
			Connection conn = getConnection();
			connection = new DatabaseConnection(conn);

			IDataSet dataSet = new FlatXmlDataSet(file);
			DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
			}
		}

	}

	//	/**
	//	 * 参照メソッドのテスト
	//	 */
	//	@Test
	//	public void testLoad() throws Exception {
	//
	//		//参照メソッドの実行
	//		Sample order = new Sample();
	//		Branch result = order.load("001", "札幌支店");
	//
	//		//値の検証
	//		assertEquals("branchCode=001", "branchCode=" + result.getBranchCode());
	//		assertEquals("branchName=札幌支店", "branchName=" + result.getBranchName());
	//		assertEquals("branchSale=3000", "branchSale=" + result.getBranchSale());
	//
	//	}

	/**
	 * 参照メソッドのテスト
	 */
	@Test
	public void testAllBranch() throws Exception {

		//参照メソッドの実行
		List<Branch> resultList = Sample.allBranch();

		//値の検証

		//件数
		assertEquals(2, resultList.size());

		//データ
		Branch result001 = resultList.get(1);
		assertEquals("branchCode=001", "branchCode=" + result001.getBranchCode());
		assertEquals("branchName=札幌支店", "branchName=" + result001.getBranchName());

		Branch result002 = resultList.get(0);
		assertEquals("branchCode=002", "branchCode=" + result002.getBranchCode());
		assertEquals("branchName=北極支店", "branchName=" + result002.getBranchName());
	}

	/**
	 * 更新メソッドのテスト
	 */
	@Test
	public void testUpdataBranch() throws Exception {

		//テスト対象となる、storeメソッドを実行
		//テストのインスタンスを生成
		Branch branch001 = new Branch();
		branch001.setBranchCode("001");
		branch001.setBranchName("札幌支店");
		branch001.setBranchSale(13000);

		Branch branch002 = new Branch();
		branch002.setBranchCode("002");
		branch002.setBranchName("北極支店");
		branch002.setBranchSale(7000);

		Branch branch003 = new Branch();
		branch003.setBranchCode("003");
		branch003.setBranchName("南極支店");
		branch003.setBranchSale(13000);

		List<Branch> branchSetList = new ArrayList<Branch>();
		branchSetList.add(branch001);
		branchSetList.add(branch002);
		branchSetList.add(branch003);

		Sample.updataBranch(branchSetList);

		//テスト結果として期待されるべきテーブルデータを表すITableインスタンスを取得
		IDatabaseConnection connection = null;
		try {

			Connection conn = getConnection();
			connection = new DatabaseConnection(conn);

			//メソッド実行した実際のテーブル
			IDataSet databaseDataSet = connection.createDataSet();
			ITable actualTable = databaseDataSet.getTable("branch_sale_out");

			// テスト結果として期待されるべきテーブルデータを表すITableインスタンスを取得
			IDataSet expectedDataSet = new FlatXmlDataSet(new File("branch_test_data3.xml"));
			ITable expectedTable = expectedDataSet.getTable("branch_sale_out");

			//期待されるITableと実際のITableの比較
			Assertion.assertEquals(expectedTable, actualTable);
		} finally {
			if (connection != null)
				connection.close();
		}

	}

}
