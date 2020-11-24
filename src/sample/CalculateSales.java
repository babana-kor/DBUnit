package sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import beans.Branch;

public class CalculateSales {

	public static void main(String[] args) {

		//コマンドライン引数のチェック
		if (args.length != 1) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		//DBUnit用追加機能①参照メソッド
		//支店定義テーブルからブランチ<支店コード,支店名>リストを取得する
		List<Branch> branchsList = null;
		try {
			branchsList = Sample.allBranch();
		} catch (Exception e1) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		//2:集計
		//2-1:売上げファイルを検索して読み込む(拡張子がrcd、且つファイル名が数字8桁)
		File[] files = new File(args[0]).listFiles();
		List<File> rcdFiles = new ArrayList<>();
		//ファイル内から売上げファイルを検索
		for (int i = 0; i < files.length; i++) {
			String fileName = files[i].getName();
			//ファイル、売上ファイル名のチェック
			if (files[i].isFile() && fileName.matches("^[0-9]{8}.rcd$")) {
				//rcdFilesにいれていく
				rcdFiles.add(files[i]);
			}
		}

		//昇順並び替え
		Collections.sort(rcdFiles);

		//連番チェック
		//後ファイル名の番号と前ファイル名の番号比較でチェック
		for (int i = 0; i < rcdFiles.size() - 1; i++) {
			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			if ((latter - former) != 1) {
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		//2-2:支店コード、売上額を抽出する。抽出した売上額を該当する支店の合計金額にそれぞれ加算
		//追加
		//売上げファイルを読み込み、支店コード、売上額を抽出する。抽出した売上額を該当する支店の合計金額、
		//にそれぞれ加算する。それを売上ファイルの数だけ繰り返す。
		//rcdファイルぶんだけ繰り返し
		BufferedReader br = null;
		for (int i = 0; i < rcdFiles.size(); i++) {
			try {
				br = new BufferedReader(new FileReader(rcdFiles.get(i)));
				ArrayList<String> fileContents = new ArrayList<>();

				String line;
				while ((line = br.readLine()) != null) {
					fileContents.add(line);
				}

				String fileName = rcdFiles.get(i).getName();
				//行数チェック
				if (fileContents.size() != 2) {
					System.out.println(fileName + "のフォーマットが不正です");
					return;
				}
				String branchCode = fileContents.get(0);

				//リストから対象ブランチを検索
				Branch tgtBranch = searchBranch(branchsList, branchCode);

				//支店コードの存在チェック
				if (tgtBranch == null) {
					System.out.println(fileName + "の支店コードが不正です");
					return;
				}
				//売上金額数字チェック
				if (fileContents.get(1).matches("^[0-9]$")) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				//売上金額の加算
				//String型の売上金額をlong型に変換
				long fileBranchSale = Long.parseLong(fileContents.get(1));

				//支店マップから支店コードで支店別売上を呼び出して加算
				long saleBranchAmount = tgtBranch.getBranchSale() + fileBranchSale;

				if (saleBranchAmount >= 10000000000L) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				//ブランチに売上金額を追加
				tgtBranch.setBranchSale(saleBranchAmount);

			} catch (Exception e) {

				System.out.println("予期せぬエラーが発生しました");
				return;

			} finally {

				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						System.out.println("予期せぬエラーが発生しました");
						return;
					}
				}
			}
		}

		//DBUnit用追加機能②更新メソッド
		try {
			if (!Sample.updataBranch(branchsList)) {
				System.out.println("予期せぬエラーが発生しました");
			}
		} catch (Exception e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

	}

	private static Branch searchBranch(List<Branch> branchsList, String branchCode) {
		Branch resultBranch = null;

		for (Branch branch : branchsList) {
			if (branch.getBranchCode().equals(branchCode)) {
				resultBranch = branch;
			}
		}

		return resultBranch;
	}

}
