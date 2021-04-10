package com.example.demo.payload;

import com.example.demo.model.Vote;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

public class PollResponse {
    private Long id;
    private String question;
    private List<ChoiceResponse> choices;
    private UserSummary createdBy;
    private Instant creationDateTime;
    private Instant expirationDateTime;
    private Boolean isExpired;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Long> selectedChoice;
    private Long totalVotes;
    private boolean isAnonymousUser;
    private boolean isAddChoice;
    private boolean isCanFix;
    private int maxVote;
    private int maxVotePerChoice;
    private int timeLoad;
    
    public Boolean getIsExpired() {
		return isExpired;
	}

	public void setIsExpired(Boolean isExpired) {
		this.isExpired = isExpired;
	}

	public boolean getIsAnonymousUser() {
		return isAnonymousUser;
	}

	public void setAnonymousUser(boolean isAnonymousUser) {
		this.isAnonymousUser = isAnonymousUser;
	}

	public boolean getIsAddChoice() {
		return isAddChoice;
	}

	public void setAddChoice(boolean isAddChoice) {
		this.isAddChoice = isAddChoice;
	}

	public boolean getIsCanFix() {
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

    public List<ChoiceResponse> getChoices() {
        return choices;
    }

    public void setChoices(List<ChoiceResponse> choices) {
        this.choices = choices;
    }

    public UserSummary getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserSummary createdBy) {
        this.createdBy = createdBy;
    }


    public Instant getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Instant creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Instant getExpirationDateTime() {
        return expirationDateTime;
    }

    public void setExpirationDateTime(Instant expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
    }

    public Boolean getExpired() {
        return isExpired;
    }

    public void setExpired(Boolean expired) {
        isExpired = expired;
    }

    public List<Long> getSelectedChoice() {
		return selectedChoice;
	}

	public void setSelectedChoice(List<Long> selectedChoice) {
		this.selectedChoice = selectedChoice;
	}

	public Long getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(Long totalVotes) {
        this.totalVotes = totalVotes;
    }
}