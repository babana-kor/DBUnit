/**
 *
 */
package sample;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.dbunit.Assertion;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
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
			file = File.createTempFile("branch", ".xml");
			FlatXmlDataSet.write(partialDataSet,
					new FileOutputStream(file));

			//テストデータを投入する
			IDataSet dataSet = new FlatXmlDataSet(new File("branch_test_data.xml"));
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

	/**
	 * 参照メソッドのテスト
	 */
	@Test
	public void testLoad() throws Exception {

		//参照メソッドの実行
		Sample order = new Sample();
		order.load("001", "札幌支店");

		//値の検証
		assertEquals("branchCode=001", "branchCode=" + order.getBranchCode());
		assertEquals("branchName=札幌支店", "branchName=" + order.getBranchName());
		assertEquals("branchSale=3000", "branchSale=" + order.getBranchSale());

	}

	/**
	 * 更新メソッドのテスト
	 */
	@Test
	public void testStore() throws Exception {

		//テスト対象となる、storeメソッドを実行
		//テストのインスタンスを生成
		Sample order = new Sample();
		order.setBranchCode("003");
		order.setBranchName("南極支店");
		order.setBranchSale(13000);

		order.store();

		//テスト結果として期待されるべきテーブルデータを表すITableインスタンスを取得
		IDatabaseConnection connection = null;
		try {

			Connection conn = getConnection();
			connection = new DatabaseConnection(conn);

			//メソッド実行した実際のテーブル
			IDataSet databaseDataSet = connection.createDataSet();
			ITable actualTable = databaseDataSet.getTable("branch");

			// テスト結果として期待されるべきテーブルデータを表すITableインスタンスを取得
			IDataSet expectedDataSet = new FlatXmlDataSet(new File("branch_test_data2.xml"));
			ITable expectedTable = expectedDataSet.getTable("branch");

			//期待されるITableと実際のITableの比較
			Assertion.assertEquals(expectedTable, actualTable);
		} finally {
			if (connection != null)
				connection.close();
		}

	}

}
