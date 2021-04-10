package com.example.demo.payload;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.List;

public class PollRequest {
    @NotBlank
    @Size(max = 140)
    private String question;

    @NotNull
    @Size(min = 2, max = 6)
    @Valid
    private List<ChoiceRequest> choices;

    @NotNull
    @Valid
    private PollLength pollLength;
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

	public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<ChoiceRequest> getChoices() {
        return choices;
    }

    public void setChoices(List<ChoiceRequest> choices) {
        this.choices = choices;
    }

    public PollLength getPollLength() {
        return pollLength;
    }

    public void setPollLength(PollLength pollLength) {
        this.pollLength = pollLength;
    }
}
