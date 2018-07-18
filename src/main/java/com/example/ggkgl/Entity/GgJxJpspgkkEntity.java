package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "GG_JX_JPSPGKK", schema = "basic_data", catalog = "")
public class GgJxJpspgkkEntity {
    private String id;
    private String modifyUserId;
    private String modifyTime;
    private String seqNo;
    private String kcmc;
    private String fzr;
    private String pdsj;
    private String pc;
    private String cydws;
    private String xxmc;
    private String xxdm;
    private String kcmcZz;

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
    @Column(name = "KCMC")
    public String getKcmc() {
        return kcmc;
    }

    public void setKcmc(String kcmc) {
        this.kcmc = kcmc;
    }

    @Basic
    @Column(name = "FZR")
    public String getFzr() {
        return fzr;
    }

    public void setFzr(String fzr) {
        this.fzr = fzr;
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
    @Column(name = "KCMC_ZZ")
    public String getKcmcZz() {
        return kcmcZz;
    }

    public void setKcmcZz(String kcmcZz) {
        this.kcmcZz = kcmcZz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GgJxJpspgkkEntity that = (GgJxJpspgkkEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(modifyUserId, that.modifyUserId) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(seqNo, that.seqNo) &&
                Objects.equals(kcmc, that.kcmc) &&
                Objects.equals(fzr, that.fzr) &&
                Objects.equals(pdsj, that.pdsj) &&
                Objects.equals(pc, that.pc) &&
                Objects.equals(cydws, that.cydws) &&
                Objects.equals(xxmc, that.xxmc) &&
                Objects.equals(xxdm, that.xxdm) &&
                Objects.equals(kcmcZz, that.kcmcZz);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, modifyUserId, modifyTime, seqNo, kcmc, fzr, pdsj, pc, cydws, xxmc, xxdm, kcmcZz);
    }
}
