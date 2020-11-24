package sample;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.FlatDtdDataSet;

import beans.Branch;

public class Sample {

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

	//	//DB参照メソッド
	//	public Branch load(String code, String name) throws Exception {
	//
	//		Branch resultBranch = new Branch();
	//
	//		Connection connection = null;
	//		try {
	//			connection = getConnection();
	//			String sql = "SELECT * FROM branch WHERE branchCode = ? AND branchName = ?";
	//
	//			PreparedStatement ps = connection.prepareStatement(sql);
	//			ps.setString(1, code);
	//			ps.setString(2, name);
	//			ResultSet result = ps.executeQuery();
	//
	//			if (result.next()) {
	//				resultBranch.setBranchCode(result.getString("branchCode"));
	//				resultBranch.setBranchName(result.getString("branchName"));
	//				resultBranch.setBranchSale(result.getLong("branchSale"));
	//			}
	//
	//		} finally {
	//			if (connection != null)
	//				connection.close();
	//
	//		}
	//		return resultBranch;
	//
	//	}

	//DB参照メソッド
	public static List<Branch> allBranch() throws Exception {

		Connection connection = null;
		List<Branch> branchesList = new ArrayList<Branch>();

		try {
			connection = getConnection();
			String sql = "SELECT * FROM branch";
			PreparedStatement ps = connection.prepareStatement(sql);

			ResultSet result = ps.executeQuery();

			while (result.next()) {
				Branch branch = new Branch();
				branch.setBranchCode(result.getString("branch_code"));
				branch.setBranchName(result.getString("branch_name"));

				branchesList.add(branch);
			}

		} finally {
			if (connection != null)
				connection.close();
		}
		return branchesList;

	}

	//DB更新メソッド
	public static boolean updataBranch(List<Branch> resultBranchList) throws Exception {

		Connection connection = null;

		for (int i = 0; i < resultBranchList.size(); i++) {

			Branch branch = resultBranchList.get(i);

			try {
				connection = getConnection();
				StringBuilder sql = new StringBuilder();
				sql.append("REPLACE branch_sale_out SET branch_code = ?, branch_name = ?, branch_sale = ?;");

				PreparedStatement ps = connection.prepareStatement(sql.toString());

				ps.setString(1, branch.getBranchCode());
				ps.setString(2, branch.getBranchName());
				ps.setLong(3, branch.getBranchSale());

				ps.executeUpdate();

				//Can't call commit when autocommit=true
				//connection.commit();

			} finally {
				try {
					if (connection != null) {
						connection.close();
					}
				} catch (SQLException e) {
					return false;
				}
			}
		}
		return true;

	}

}
