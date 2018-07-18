package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "GG_HJ_GXYXCGJSK", schema = "basic_data", catalog = "")
public class GgHjGxyxcgjskEntity {
    private String id;
    private String modifyUserId;
    private String modifyTime;
    private String seqNo;
    private String cgmc;
    private String hjlb;
    private String hjdj;
    private String cbs;
    private String cbny;
    private String xk;
    private String pc;
    private String hjny;
    private String hjr1;
    private String hjr2;
    private String hjr3;
    private String hjr4;
    private String hjr5;
    private String hjr6;
    private String cgmcZz;

    @Id
    @Column(name = "ID")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(name = "MODIFY_USER_ID")
    public String getModifyUserId() {
        return modifyUserId;
    }

    public void setModifyUserId(String modifyUserId) {
        this.modifyUserId = modifyUserId;
    }

    @Basic
    @Column(name = "MODIFY_TIME")
    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Basic
    @Column(name = "SEQ_NO")
    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    @Basic
    @Column(name = "CGMC")
    public String getCgmc() {
        return cgmc;
    }

    public void setCgmc(String cgmc) {
        this.cgmc = cgmc;
    }

    @Basic
    @Column(name = "HJLB")
    public String getHjlb() {
        return hjlb;
    }

    public void setHjlb(String hjlb) {
        this.hjlb = hjlb;
    }

    @Basic
    @Column(name = "HJDJ")
    public String getHjdj() {
        return hjdj;
    }

    public void setHjdj(String hjdj) {
        this.hjdj = hjdj;
    }

    @Basic
    @Column(name = "CBS")
    public String getCbs() {
        return cbs;
    }

    public void setCbs(String cbs) {
        this.cbs = cbs;
    }

    @Basic
    @Column(name = "CBNY")
    public String getCbny() {
        return cbny;
    }

    public void setCbny(String cbny) {
        this.cbny = cbny;
    }

    @Basic
    @Column(name = "XK")
    public String getXk() {
        return xk;
    }

    public void setXk(String xk) {
        this.xk = xk;
    }

    @Basic
    @Column(name = "PC")
    public String getPc() {
        return pc;
    }

    public void setPc(String pc) {
        this.pc = pc;
    }

    @Basic
    @Column(name = "HJNY")
    public String getHjny() {
        return hjny;
    }

    public void setHjny(String hjny) {
        this.hjny = hjny;
    }

    @Basic
    @Column(name = "HJR1")
    public String getHjr1() {
        return hjr1;
    }

    public void setHjr1(String hjr1) {
        this.hjr1 = hjr1;
    }

    @Basic
    @Column(name = "HJR2")
    public String getHjr2() {
        return hjr2;
    }

    public void setHjr2(String hjr2) {
        this.hjr2 = hjr2;
    }

    @Basic
    @Column(name = "HJR3")
    public String getHjr3() {
        return hjr3;
    }

    public void setHjr3(String hjr3) {
        this.hjr3 = hjr3;
    }

    @Basic
    @Column(name = "HJR4")
    public String getHjr4() {
        return hjr4;
    }

    public void setHjr4(String hjr4) {
        this.hjr4 = hjr4;
    }

    @Basic
    @Column(name = "HJR5")
    public String getHjr5() {
        return hjr5;
    }

    public void setHjr5(String hjr5) {
        this.hjr5 = hjr5;
    }

    @Basic
    @Column(name = "HJR6")
    public String getHjr6() {
        return hjr6;
    }

    public void setHjr6(String hjr6) {
        this.hjr6 = hjr6;
    }

    @Basic
    @Column(name = "CGMC_ZZ")
    public String getCgmcZz() {
        return cgmcZz;
    }

    public void setCgmcZz(String cgmcZz) {
        this.cgmcZz = cgmcZz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GgHjGxyxcgjskEntity that = (GgHjGxyxcgjskEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(modifyUserId, that.modifyUserId) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(seqNo, that.seqNo) &&
                Objects.equals(cgmc, that.cgmc) &&
                Objects.equals(hjlb, that.hjlb) &&
                Objects.equals(hjdj, that.hjdj) &&
                Objects.equals(cbs, that.cbs) &&
                Objects.equals(cbny, that.cbny) &&
                Objects.equals(xk, that.xk) &&
                Objects.equals(pc, that.pc) &&
                Objects.equals(hjny, that.hjny) &&
                Objects.equals(hjr1, that.hjr1) &&
                Objects.equals(hjr2, that.hjr2) &&
                Objects.equals(hjr3, that.hjr3) &&
                Objects.equals(hjr4, that.hjr4) &&
                Objects.equals(hjr5, that.hjr5) &&
                Objects.equals(hjr6, that.hjr6) &&
                Objects.equals(cgmcZz, that.cgmcZz);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, modifyUserId, modifyTime, seqNo, cgmc, hjlb, hjdj, cbs, cbny, xk, pc, hjny, hjr1, hjr2, hjr3, hjr4, hjr5, hjr6, cgmcZz);
    }
}
