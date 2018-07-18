package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "GG_ZJ_CJ", schema = "basic_data", catalog = "")
public class GgZjCjEntity {
    private String id;
    private String modifyUserId;
    private Timestamp modifyTime;
    private String seqNo;
    private String xklb;
    private String xm;
    private String lb;
    private String pc;
    private String pdsj;
    private String cydws;
    private String xxmc;
    private String xxdm;

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
    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
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
    @Column(name = "XKLB")
    public String getXklb() {
        return xklb;
    }

    public void setXklb(String xklb) {
        this.xklb = xklb;
    }

    @Basic
    @Column(name = "XM")
    public String getXm() {
        return xm;
    }

    public void setXm(String xm) {
        this.xm = xm;
    }

    @Basic
    @Column(name = "LB")
    public String getLb() {
        return lb;
    }

    public void setLb(String lb) {
        this.lb = lb;
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
    @Column(name = "PDSJ")
    public String getPdsj() {
        return pdsj;
    }

    public void setPdsj(String pdsj) {
        this.pdsj = pdsj;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GgZjCjEntity that = (GgZjCjEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(modifyUserId, that.modifyUserId) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(seqNo, that.seqNo) &&
                Objects.equals(xklb, that.xklb) &&
                Objects.equals(xm, that.xm) &&
                Objects.equals(lb, that.lb) &&
                Objects.equals(pc, that.pc) &&
                Objects.equals(pdsj, that.pdsj) &&
                Objects.equals(cydws, that.cydws) &&
                Objects.equals(xxmc, that.xxmc) &&
                Objects.equals(xxdm, that.xxdm);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, modifyUserId, modifyTime, seqNo, xklb, xm, lb, pc, pdsj, cydws, xxmc, xxdm);
    }
}
