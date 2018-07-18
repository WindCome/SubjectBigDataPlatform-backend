package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "GG_HJ_ZGZLJ", schema = "basic_data", catalog = "")
public class GgHjZgzljEntity {
    private String id;
    private String modifyUserId;
    private String modifyTime;
    private String seqNo;
    private String zlh;
    private String zlmc;
    private String fmr;
    private String tjdw;
    private String xklb;
    private String bz;
    private String hjdj;
    private String pdsj;
    private String pc;
    private String cydws;
    private String xxmc;
    private String xxdm;
    private String zlmcZz;

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
    @Column(name = "ZLH")
    public String getZlh() {
        return zlh;
    }

    public void setZlh(String zlh) {
        this.zlh = zlh;
    }

    @Basic
    @Column(name = "ZLMC")
    public String getZlmc() {
        return zlmc;
    }

    public void setZlmc(String zlmc) {
        this.zlmc = zlmc;
    }

    @Basic
    @Column(name = "FMR")
    public String getFmr() {
        return fmr;
    }

    public void setFmr(String fmr) {
        this.fmr = fmr;
    }

    @Basic
    @Column(name = "TJDW")
    public String getTjdw() {
        return tjdw;
    }

    public void setTjdw(String tjdw) {
        this.tjdw = tjdw;
    }

    @Basic
    @Column(name = "XKLB")
    public String getXklb() {
        return xklb;
    }

    public void setXklb(String xklb) {
        this.xklb = xklb;
    }

    @Basic
    @Column(name = "BZ")
    public String getBz() {
        return bz;
    }

    public void setBz(String bz) {
        this.bz = bz;
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
    @Column(name = "PDSJ")
    public String getPdsj() {
        return pdsj;
    }

    public void setPdsj(String pdsj) {
        this.pdsj = pdsj;
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
    @Column(name = "CYDWS")
    public String getCydws() {
        return cydws;
    }

    public void setCydws(String cydws) {
        this.cydws = cydws;
    }

    @Basic
    @Column(name = "XXMC")
    public String getXxmc() {
        return xxmc;
    }

    public void setXxmc(String xxmc) {
        this.xxmc = xxmc;
    }

    @Basic
    @Column(name = "XXDM")
    public String getXxdm() {
        return xxdm;
    }

    public void setXxdm(String xxdm) {
        this.xxdm = xxdm;
    }

    @Basic
    @Column(name = "ZLMC_ZZ")
    public String getZlmcZz() {
        return zlmcZz;
    }

    public void setZlmcZz(String zlmcZz) {
        this.zlmcZz = zlmcZz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GgHjZgzljEntity that = (GgHjZgzljEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(modifyUserId, that.modifyUserId) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(seqNo, that.seqNo) &&
                Objects.equals(zlh, that.zlh) &&
                Objects.equals(zlmc, that.zlmc) &&
                Objects.equals(fmr, that.fmr) &&
                Objects.equals(tjdw, that.tjdw) &&
                Objects.equals(xklb, that.xklb) &&
                Objects.equals(bz, that.bz) &&
                Objects.equals(hjdj, that.hjdj) &&
                Objects.equals(pdsj, that.pdsj) &&
                Objects.equals(pc, that.pc) &&
                Objects.equals(cydws, that.cydws) &&
                Objects.equals(xxmc, that.xxmc) &&
                Objects.equals(xxdm, that.xxdm) &&
                Objects.equals(zlmcZz, that.zlmcZz);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, modifyUserId, modifyTime, seqNo, zlh, zlmc, fmr, tjdw, xklb, bz, hjdj, pdsj, pc, cydws, xxmc, xxdm, zlmcZz);
    }
}
