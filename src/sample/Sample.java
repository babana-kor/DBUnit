package sample;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.FlatDtdDataSet;

public class Sample {

	//beans部分
	private String branchCode;
	private String branchName;
	private long branchSale;

	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public long getBranchSale() {
		return branchSale;
	}

	public void setBranchSale(long branchSale) {
		this.branchSale = branchSale;
	}

	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String URL = "jdbc:mysql://localhost/test";
	private static final String USER = "root";
	private static final String PASSWORD = "root";

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

    public static void main(String[] args) throws Exception {

        // データベースに接続する。
        Class.forName("com.mysql.jdbc.Driver");
        Connection jdbcConnection = DriverManager.getConnection(URL, USER, PASSWORD);
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

        // Dtdファイルを作成する
        FlatDtdDataSet.write(connection.createDataSet(),
                             new FileOutputStream("test.dtd"));
     }

	//DB参照メソッド
	public void load(String code, String name) throws Exception {

		Connection connection = null;
		try {
			connection = getConnection();
			String sql = "SELECT * FROM branch WHERE branchCode = ? AND branchName = ?";

			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, code);
			ps.setString(2, name);
			ResultSet result = ps.executeQuery();

			if (result.next()) {
				this.branchCode = result.getString("branchCode");
				this.branchName = result.getString("branchName");
				this.branchSale = result.getLong("branchSale");
			}

		} finally {
			if (connection != null)
				connection.close();
		}

	}

	//DB更新メソッド
	public void store() throws Exception {

		Connection connection = null;
		try {
			connection = getConnection();
			String sql = "INSERT INTO branch values(?,?,?)";

			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, this.branchCode);
			ps.setString(2, this.branchName);
			ps.setLong(3, this.branchSale);

			ps.executeUpdate();

			//Can't call commit when autocommit=true
			//connection.commit();

		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
			}
		}

	}

}
