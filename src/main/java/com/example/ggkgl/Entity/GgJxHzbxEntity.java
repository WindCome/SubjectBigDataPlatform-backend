package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "GG_JX_HZBX", schema = "basic_data", catalog = "")
public class GgJxHzbxEntity {
    private String id;
    private String modifyUserId;
    private String modifyTime;
    private String seqNo;
    private String xmjgmc;
    private String pzsh;
    private String pzsyxq;
    private String wfxx;
    private String cc;
    private String sywfxx;
    private String pgjg;
    private String pzshZz;

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
    @Column(name = "XMJGMC")
    public String getXmjgmc() {
        return xmjgmc;
    }

    public void setXmjgmc(String xmjgmc) {
        this.xmjgmc = xmjgmc;
    }

    @Basic
    @Column(name = "PZSH")
    public String getPzsh() {
        return pzsh;
    }

    public void setPzsh(String pzsh) {
        this.pzsh = pzsh;
    }

    @Basic
    @Column(name = "PZSYXQ")
    public String getPzsyxq() {
        return pzsyxq;
    }

    public void setPzsyxq(String pzsyxq) {
        this.pzsyxq = pzsyxq;
    }

    @Basic
    @Column(name = "WFXX")
    public String getWfxx() {
        return wfxx;
    }

    public void setWfxx(String wfxx) {
        this.wfxx = wfxx;
    }

    @Basic
    @Column(name = "CC")
    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    @Basic
    @Column(name = "SYWFXX")
    public String getSywfxx() {
        return sywfxx;
    }

    public void setSywfxx(String sywfxx) {
        this.sywfxx = sywfxx;
    }

    @Basic
    @Column(name = "PGJG")
    public String getPgjg() {
        return pgjg;
    }

    public void setPgjg(String pgjg) {
        this.pgjg = pgjg;
    }

    @Basic
    @Column(name = "PZSH_ZZ")
    public String getPzshZz() {
        return pzshZz;
    }

    public void setPzshZz(String pzshZz) {
        this.pzshZz = pzshZz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GgJxHzbxEntity that = (GgJxHzbxEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(modifyUserId, that.modifyUserId) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(seqNo, that.seqNo) &&
                Objects.equals(xmjgmc, that.xmjgmc) &&
                Objects.equals(pzsh, that.pzsh) &&
                Objects.equals(pzsyxq, that.pzsyxq) &&
                Objects.equals(wfxx, that.wfxx) &&
                Objects.equals(cc, that.cc) &&
                Objects.equals(sywfxx, that.sywfxx) &&
                Objects.equals(pgjg, that.pgjg) &&
                Objects.equals(pzshZz, that.pzshZz);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, modifyUserId, modifyTime, seqNo, xmjgmc, pzsh, pzsyxq, wfxx, cc, sywfxx, pgjg, pzshZz);
    }
}
