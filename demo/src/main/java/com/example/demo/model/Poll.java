package com.example.demo.model;

import com.example.demo.model.audit.UserDateAudit;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "polls")
public class Poll extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 140)
    private String question;

    @OneToMany(
            mappedBy = "poll",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @Size(min = 2, max = 6)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 30)
    private List<Choice> choices = new ArrayList<>();

    @NotNull
    private Instant expirationDateTime;
    @NotNull
    private boolean isAnonymousUser;
    @NotNull
    private boolean isAddChoice;
    @NotNull
    private boolean isCanFix;
    @NotNull
    @Positive
    private int maxVote;
    @NotNull
    @Positive
    private int maxVotePerChoice;
    @NotNull
    @PositiveOrZero
    private int timeLoad;
    
    public boolean isAnonymousUser() {
		return isAnonymousUser;
	}

	public void setAnonymousUser(boolean isAnonymousUser) {
		this.isAnonymousUser = isAnonymousUser;
	}

	public boolean isAddChoice() {
		return isAddChoice;
	}

	public void setAddChoice(boolean isAddChoice) {
		this.isAddChoice = isAddChoice;
	}

	public boolean isCanFix() {
		return isCanFix;
	}

	public void setCanFix(boolean isCanFix) {
		this.isCanFix = isCanFix;
	}

	public int getMaxVote() {
		return maxVote;
	}

	public void setMaxVote(int maxVote) {
		this.maxVote = maxVote;
	}

	public int getMaxVotePerChoice() {
		return maxVotePerChoice;
	}

	public void setMaxVotePerChoice(int maxVotePerChoice) {
		this.maxVotePerChoice = maxVotePerChoice;
	}

	public int getTimeLoad() {
		return timeLoad;
	}

	public void setTimeLoad(int timeLoad) {
		this.timeLoad = timeLoad;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public Instant getExpirationDateTime() {
        return expirationDateTime;
    }

    public void setExpirationDateTime(Instant expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
    }

    public void addChoice(Choice choice) {
        choices.add(choice);
        choice.setPoll(this);
    }

    public void removeChoice(Choice choice) {
        choices.remove(choice);
        choice.setPoll(null);
    }
}
