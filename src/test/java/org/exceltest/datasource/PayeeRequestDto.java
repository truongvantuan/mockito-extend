package org.exceltest.datasource;
/*
 * ==============================================================
 * Date			Author		Remarks
 * --------------------------------------------------------------
 * 20210304		Calvis	Created class
 * 20210611     Calvis  Updated attributes
 * ==============================================================
 */
public class PayeeRequestDto {
    private String orgId;
    private String orgBranchCode;
    private String codeType;
    private String fccd;

    public String getFccd() {
        return fccd;
    }

    public void setFccd(String fccd) {
        this.fccd = fccd;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgBranchCode() {
        return orgBranchCode;
    }

    public void setOrgBranchCode(String orgBranchCode) {
        this.orgBranchCode = orgBranchCode;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

}
