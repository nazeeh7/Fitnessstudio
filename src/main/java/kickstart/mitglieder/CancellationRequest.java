package kickstart.mitglieder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
public class CancellationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "contract_id", nullable = false)
    private MembershipContract contract;

    @Column(nullable = false)
    private String reason;

    @Column(name = "STATUS")
    private String status = "Pending"; // Set a default status of "Pending"

    @Column(nullable = false)
    private String approval;

    @Column(name = "NOTICE_PERIOD")
    private int noticePeriod = 30; // Set a default notice period of 30 days

    private boolean processed = false;
    
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // Getter and Setter for all attributes

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MembershipContract getContract() {
        return contract;
    }

    public void setContract(MembershipContract contract) {
        this.contract = contract;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApproval() {
        return approval;
    }

    public void setApproval(String approval) {
        this.approval = approval;
    }

    public int getNoticePeriod() {
        return noticePeriod;
    }

    public void setNoticePeriod(int noticePeriod) {
        this.noticePeriod = noticePeriod;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
    // Getter für 'processed'
    public boolean isProcessed() {
        return processed;
    }

    // Setter für 'processed'
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
