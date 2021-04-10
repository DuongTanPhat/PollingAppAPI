package com.example.demo.util;

import com.example.demo.model.Poll;
import com.example.demo.model.User;
import com.example.demo.model.Vote;
import com.example.demo.payload.ChoiceResponse;
import com.example.demo.payload.PollResponse;
import com.example.demo.payload.UserSummary;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelMapper {

    public static PollResponse mapPollToPollResponse(Poll poll, Map<Long, Long> choiceVotesMap, User creator, List<Vote> userVote) {
        PollResponse pollResponse = new PollResponse();
        pollResponse.setId(poll.getId());
        pollResponse.setQuestion(poll.getQuestion());
        pollResponse.setCreationDateTime(poll.getCreatedAt());
        pollResponse.setExpirationDateTime(poll.getExpirationDateTime());
        pollResponse.setAnonymousUser(poll.isAnonymousUser());
        pollResponse.setAddChoice(poll.isAddChoice());
        pollResponse.setCanFix(poll.isCanFix());
        pollResponse.setMaxVote(poll.getMaxVote());
        pollResponse.setMaxVotePerChoice(poll.getMaxVotePerChoice());
        pollResponse.setTimeLoad(poll.getTimeLoad());
        Instant now = Instant.now();
        pollResponse.setExpired(poll.getExpirationDateTime().isBefore(now));
        List<ChoiceResponse> choiceResponses = poll.getChoices().stream().map(choice -> {
            ChoiceResponse choiceResponse = new ChoiceResponse();
            choiceResponse.setId(choice.getId());
            choiceResponse.setText(choice.getText());

            if(choiceVotesMap.containsKey(choice.getId())) {
                choiceResponse.setVoteCount(choiceVotesMap.get(choice.getId()));
            } else {
                choiceResponse.setVoteCount(0);
            }
            return choiceResponse;
        }).collect(Collectors.toList());

        pollResponse.setChoices(choiceResponses);
        UserSummary creatorSummary = new UserSummary(creator.getId(), creator.getUsername(), creator.getName(),creator.getPhoto());
        pollResponse.setCreatedBy(creatorSummary);

        if(userVote != null) {
        	List<Long> sle = userVote.stream().map(idc ->{
        		return idc.getChoice().getId();
        	}).collect(Collectors.toList());
            pollResponse.setSelectedChoice(sle);
        }

        long totalVotes = pollResponse.getChoices().stream().mapToLong(ChoiceResponse::getVoteCount).sum();
        pollResponse.setTotalVotes(totalVotes);

        return pollResponse;
    }
}
