package beans;

import java.io.Serializable;

public class Branch implements Serializable {
    private static final long serialVersionUID = 1L;

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

}