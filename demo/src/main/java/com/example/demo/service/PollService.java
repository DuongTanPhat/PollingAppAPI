package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.payload.ChoiceRequest;
import com.example.demo.payload.PagedResponse;
import com.example.demo.payload.PollRequest;
import com.example.demo.payload.PollResponse;
import com.example.demo.payload.VoteRequest;
import com.example.demo.repository.ChoiceRepository;
import com.example.demo.repository.PollRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VoteRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.util.AppConstants;
import com.example.demo.util.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PollService {

	@Autowired
	private PollRepository pollRepository;

	@Autowired
	private VoteRepository voteRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ChoiceRepository choiceRepository;
	private static final Logger logger = LoggerFactory.getLogger(PollService.class);

	public PagedResponse<PollResponse> getAllPolls(UserPrincipal currentUser, int page, int size) {
		validatePageNumberAndSize(page, size);

		// Retrieve Polls
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Poll> polls = pollRepository.findAll(pageable);

		if (polls.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), polls.getNumber(), polls.getSize(),
					polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
		}

		// Map Polls to PollResponses containing vote counts and poll creator details
		List<Long> pollIds = polls.map(Poll::getId).getContent();
		Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
		Map<Long, List<Vote>> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
		Map<Long, User> creatorMap = getPollCreatorMap(polls.getContent());

		List<PollResponse> pollResponses = polls.map(poll -> {
			return ModelMapper.mapPollToPollResponse(poll, choiceVoteCountMap, creatorMap.get(poll.getCreatedBy()),
					pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
		}).getContent();

		return new PagedResponse<>(pollResponses, polls.getNumber(), polls.getSize(), polls.getTotalElements(),
				polls.getTotalPages(), polls.isLast());
	}

	public PagedResponse<PollResponse> getPollsCreatedBy(String username, UserPrincipal currentUser, int page,
			int size) {
		validatePageNumberAndSize(page, size);

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

		// Retrieve all polls created by the given username
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Poll> polls = pollRepository.findByCreatedBy(user.getId(), pageable);

		if (polls.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), polls.getNumber(), polls.getSize(),
					polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
		}

		// Map Polls to PollResponses containing vote counts and poll creator details
		List<Long> pollIds = polls.map(Poll::getId).getContent();
		Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
		Map<Long, List<Vote>> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);

		List<PollResponse> pollResponses = polls.map(poll -> {
			return ModelMapper.mapPollToPollResponse(poll, choiceVoteCountMap, user,
					pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
		}).getContent();

		return new PagedResponse<>(pollResponses, polls.getNumber(), polls.getSize(), polls.getTotalElements(),
				polls.getTotalPages(), polls.isLast());
	}

	public PagedResponse<PollResponse> getPollsVotedBy(String username, UserPrincipal currentUser, int page, int size) {
		validatePageNumberAndSize(page, size);

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

		// Retrieve all pollIds in which the given username has voted
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Long> userVotedPollIds = voteRepository.findVotedPollIdsByUserId(user.getId(), pageable);
		if (userVotedPollIds.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), userVotedPollIds.getNumber(),
					userVotedPollIds.getSize(), userVotedPollIds.getTotalElements(), userVotedPollIds.getTotalPages(),
					userVotedPollIds.isLast());
		}
		// Retrieve all poll details from the voted pollIds.
		List<Long> pollIds = userVotedPollIds.getContent();
		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
		List<Poll> polls = pollRepository.findByIdIn(pollIds, sort);
		// Map Polls to PollResponses containing vote counts and poll creator details
		Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
		Map<Long, List<Vote>> pollUserVoteMap = getPollUserVoteMap2(currentUser, pollIds);
		Map<Long, User> creatorMap = getPollCreatorMap(polls);
		List<PollResponse> pollResponses = polls.stream().map(poll -> {
			return ModelMapper.mapPollToPollResponse(poll, choiceVoteCountMap, creatorMap.get(poll.getCreatedBy()),
					pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
		}).collect(Collectors.toList());
		return new PagedResponse<>(pollResponses, userVotedPollIds.getNumber(), userVotedPollIds.getSize(),
				userVotedPollIds.getTotalElements(), userVotedPollIds.getTotalPages(), userVotedPollIds.isLast());
	}

	public Poll createPoll(PollRequest pollRequest) {
		Poll poll = new Poll();
		poll.setQuestion(pollRequest.getQuestion());

		pollRequest.getChoices().forEach(choiceRequest -> {
			poll.addChoice(new Choice(choiceRequest.getText()));
		});

		Instant now = Instant.now();
		Instant expirationDateTime = now.plus(Duration.ofDays(pollRequest.getPollLength().getDays()))
				.plus(Duration.ofHours(pollRequest.getPollLength().getHours()));

		poll.setExpirationDateTime(expirationDateTime);
		poll.setAnonymousUser(pollRequest.getIsAnonymousUser());
		poll.setAddChoice(pollRequest.getIsAddChoice());
		poll.setCanFix(pollRequest.getIsCanFix());
		poll.setMaxVote(pollRequest.getMaxVote());
		if(pollRequest.getMaxVote()<pollRequest.getMaxVotePerChoice()&&pollRequest.getTimeLoad()==0)poll.setMaxVotePerChoice(pollRequest.getMaxVote());
		else
		poll.setMaxVotePerChoice(pollRequest.getMaxVotePerChoice());
		poll.setTimeLoad(pollRequest.getTimeLoad());
		return pollRepository.save(poll);
	}
	public PollResponse addChoice(Long pollId, ChoiceRequest choiceRequest, UserPrincipal currentUser) {
		Poll poll = pollRepository.findById(pollId)
				.orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

		if (poll.getExpirationDateTime().isBefore(Instant.now())) {
			throw new BadRequestException("Sorry! This Poll has already expired");
		}
		User creator = userRepository.findById(poll.getCreatedBy())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy()));
//		User user = userRepository.getOne(currentUser.getId());
		if(creator.getId()!=currentUser.getId()&&!poll.isAddChoice()) throw new BadRequestException("Sorry! You can't add this choice!");

		Choice choice = new Choice();
		choice.setPoll(poll);
		choice.setText(choiceRequest.getText());
		try {
			choiceRepository.save(choice);
			poll.addChoice(choice);
			
		} catch (DataIntegrityViolationException ex) {
			logger.info("User {} has add choice error in Poll {}", currentUser.getId(), pollId);
			throw new BadRequestException("Sorry! Something went wrong!");
		}
		List<Vote> voteSelected = voteRepository.findByUserIdAndPollId(currentUser.getId(), pollId);
		// -- Vote Saved, Return the updated Poll Response now --
		// Retrieve Vote Counts of every choice belonging to the current poll
		List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);

		Map<Long, Long> choiceVotesMap = votes.stream()
				.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));
		
		// Retrieve poll creator details
		return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator, voteSelected);
	}
	public PollResponse deleteChoice(Long pollId, VoteRequest voteRequest, UserPrincipal currentUser) {
		Poll poll = pollRepository.findById(pollId)
				.orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

		if (poll.getExpirationDateTime().isBefore(Instant.now())) {
			throw new BadRequestException("Sorry! This Poll has already expired");
		}
		User creator = userRepository.findById(poll.getCreatedBy())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy()));
//		User user = userRepository.getOne(currentUser.getId());
		if(creator.getId()!=currentUser.getId()) throw new BadRequestException("Sorry! You can't delete this choice!");
		Choice selectedChoice = poll.getChoices().stream()
				.filter(choice -> choice.getId().equals(voteRequest.getChoiceId())).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Choice", "id", voteRequest.getChoiceId()));
		List<Vote> vote = voteRepository.findByChoiceId(selectedChoice.getId());
		for (Vote vote2 : vote) {
			try {
				voteRepository.delete(vote2);
			} catch (DataIntegrityViolationException ex) {
				logger.info("User {} has delete choice voted error in Poll {}", currentUser.getId(), pollId);
				throw new BadRequestException("Sorry! Something went wrong!");
			}
		}
		try {
			poll.removeChoice(selectedChoice);
			choiceRepository.delete(selectedChoice);
		} catch (DataIntegrityViolationException ex) {
			logger.info("User {} has delete choice error in Poll {}", currentUser.getId(), pollId);
			throw new BadRequestException("Sorry! Something went wrong!");
		}
			
		// -- Vote Saved, Return the updated Poll Response now --

		// Retrieve Vote Counts of every choice belonging to the current poll
		List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);

		Map<Long, Long> choiceVotesMap = votes.stream()
				.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

		// Retrieve poll creator details
		List<Vote> voteSeleted = voteRepository.findByUserIdAndPollId(currentUser.getId(),pollId);
		return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator, voteSeleted);
	}
	public PollResponse deleteVoted(Long pollId, VoteRequest voteRequest, UserPrincipal currentUser) {
		Poll poll = pollRepository.findById(pollId)
				.orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

		if (poll.getExpirationDateTime().isBefore(Instant.now())) {
			throw new BadRequestException("Sorry! This Poll has already expired");
		}

//		User user = userRepository.getOne(currentUser.getId());

		Choice selectedChoice = poll.getChoices().stream()
				.filter(choice -> choice.getId().equals(voteRequest.getChoiceId())).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Choice", "id", voteRequest.getChoiceId()));
		List<Vote> vote = voteRepository.findByUserIdAndChoiceId(currentUser.getId(), selectedChoice.getId());
		if (vote.isEmpty()) {
				logger.info("User {} has delete vote error in Poll {}", currentUser.getId(), pollId);
				throw new BadRequestException("Sorry! Something went wrong!");
		} else {
			if(poll.isCanFix()) {
//				vote.get(0).setChoice(selectedChoice);
				try {
					voteRepository.delete(vote.get(0));
//					voteRepository.save(vote.get(0));
				} catch (DataIntegrityViolationException ex) {

					logger.info("User {} has delete vote error in Poll {}", currentUser.getId(), pollId);
					throw new BadRequestException("Sorry! Something went wrong!");

				}
			}
			else {
				logger.info("User {} has change vote error in Poll {}", currentUser.getId(), pollId);
				throw new BadRequestException("Sorry! This poll can't change your voted!");
			}	
		}
			
		// -- Vote Saved, Return the updated Poll Response now --

		// Retrieve Vote Counts of every choice belonging to the current poll
		List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);

		Map<Long, Long> choiceVotesMap = votes.stream()
				.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

		// Retrieve poll creator details
		User creator = userRepository.findById(poll.getCreatedBy())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy()));
		List<Vote> voteSelected = voteRepository.findByUserIdAndPollId(currentUser.getId(), pollId);
		return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator, voteSelected);
	}
	
	public PollResponse getPollById(Long pollId, UserPrincipal currentUser) {
		Poll poll = pollRepository.findById(pollId)
				.orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

		// Retrieve Vote Counts of every choice belonging to the current poll
		List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);

		Map<Long, Long> choiceVotesMap = votes.stream()
				.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

		// Retrieve poll creator details
		User creator = userRepository.findById(poll.getCreatedBy())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy()));

		// Retrieve vote done by logged in user
		List<Vote> userVote = null;
		if (currentUser != null) {
			userVote = voteRepository.findByUserIdAndPollId(currentUser.getId(), pollId);
		}

		return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator,
				userVote != null ? userVote : null);
	}

	public PollResponse castVoteAndGetUpdatedPoll(Long pollId, VoteRequest voteRequest, UserPrincipal currentUser) {
		Poll poll = pollRepository.findById(pollId)
				.orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

		if (poll.getExpirationDateTime().isBefore(Instant.now())) {
			throw new BadRequestException("Sorry! This Poll has already expired");
		}

		User user = userRepository.getOne(currentUser.getId());

		Choice selectedChoice = poll.getChoices().stream()
				.filter(choice -> choice.getId().equals(voteRequest.getChoiceId())).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Choice", "id", voteRequest.getChoiceId()));
		List<Vote> vote = voteRepository.findByUserIdAndPollId(currentUser.getId(), pollId);
		if (vote.isEmpty()) {
			Vote vote2 = new Vote();
			vote2.setPoll(poll);
			vote2.setUser(user);
			vote2.setChoice(selectedChoice);
			try {
				vote2 = voteRepository.save(vote2);
			} catch (DataIntegrityViolationException ex) {

				logger.info("User {} has new vote error in Poll {}", currentUser.getId(), pollId);
				throw new BadRequestException("Sorry! Something went wrong!");

			}
		} else {
			if(poll.isCanFix()&&poll.getMaxVote()==1&&poll.getMaxVotePerChoice()==1) {
				vote.get(0).setChoice(selectedChoice);
				try {
					voteRepository.save(vote.get(0));
				} catch (DataIntegrityViolationException ex) {

					logger.info("User {} has change vote error in Poll {}", currentUser.getId(), pollId);
					throw new BadRequestException("Sorry! Something went wrong!");

				}
			}
			else if(!poll.isCanFix()&&poll.getMaxVote()==1&&poll.getMaxVotePerChoice()==1) {
				logger.info("User {} has change vote error in Poll {}", currentUser.getId(), pollId);
				throw new BadRequestException("Sorry! This poll can't change your voted!");
			}
			//vote.setChoice(selectedChoice);
			else if(poll.getMaxVote()>vote.size()){
				List<Vote> choiceVote = voteRepository.findByUserIdAndChoiceId(currentUser.getId(), selectedChoice.getId());
				if(choiceVote == null) {
					Vote vote2 = new Vote();
					vote2.setPoll(poll);
					vote2.setUser(user);
					vote2.setChoice(selectedChoice);
					try {
						vote2 = voteRepository.save(vote2);
					} catch (DataIntegrityViolationException ex) {

						logger.info("User {} has already voted in Poll {}", currentUser.getId(), pollId);
						throw new BadRequestException("Sorry! You have already cast your vote in this poll");

					}
				}
				else if(choiceVote.size()<poll.getMaxVotePerChoice()) {
					Vote vote2 = new Vote();
					vote2.setPoll(poll);
					vote2.setUser(user);
					vote2.setChoice(selectedChoice);
					try {
						vote2 = voteRepository.save(vote2);
					} catch (DataIntegrityViolationException ex) {

						logger.info("User {} has already voted in Poll {}", currentUser.getId(), pollId);
						throw new BadRequestException("Sorry! You have already cast your vote in this poll");

					}
				}
			}
			
		}
			

//		try {
//			vote = voteRepository.save(vote);
//		} catch (DataIntegrityViolationException ex) {
//
//			logger.info("User {} has already voted in Poll {}", currentUser.getId(), pollId);
//			throw new BadRequestException("Sorry! You have already cast your vote in this poll");
//
//		}
		// -- Vote Saved, Return the updated Poll Response now --

		// Retrieve Vote Counts of every choice belonging to the current poll
		List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);

		Map<Long, Long> choiceVotesMap = votes.stream()
				.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

		// Retrieve poll creator details
		User creator = userRepository.findById(poll.getCreatedBy())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy()));
		vote = voteRepository.findByUserIdAndPollId(currentUser.getId(), pollId);
		return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator, vote);
	}

	private void validatePageNumberAndSize(int page, int size) {
		if (page < 0) {
			throw new BadRequestException("Page number cannot be less than zero.");
		}

		if (size > AppConstants.MAX_PAGE_SIZE) {
			throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
		}
	}

	private Map<Long, Long> getChoiceVoteCountMap(List<Long> pollIds) {
		// Retrieve Vote Counts of every Choice belonging to the given pollIds
		List<ChoiceVoteCount> votes = voteRepository.countByPollIdInGroupByChoiceId(pollIds);

		Map<Long, Long> choiceVotesMap = votes.stream()
				.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

		return choiceVotesMap;
	}

	private Map<Long, List<Vote>> getPollUserVoteMap(UserPrincipal currentUser, List<Long> pollIds) {
		// Retrieve Votes done by the logged in user to the given pollIds
		Map<Long, List<Vote>> pollUserVoteMap = null;
		if (currentUser != null) {
//			List<Vote> userVotes = voteRepository.findByUserIdAndPollIdIn(currentUser.getId(), pollIds);
			pollUserVoteMap = pollIds.stream().collect(Collectors.toMap(vote->vote, vote->{
				return voteRepository.findByUserIdAndPollId(currentUser.getId(), vote);
			}));
		}
		return pollUserVoteMap;
	}
	private Map<Long, List<Vote>> getPollUserVoteMap2(UserPrincipal currentUser, List<Long> pollIds) {
		// Retrieve Votes done by the logged in user to the given pollIds
		Map<Long, List<Vote>> pollUserVoteMap = null;
		if (currentUser != null) {
//			List<Vote> userVotes = voteRepository.findByUserIdAndPollIdIn(currentUser.getId(), pollIds);
			pollUserVoteMap = pollIds.stream().collect(Collectors.toMap(vote->vote, vote->{
				return voteRepository.findByUserIdAndPollId(currentUser.getId(), vote);
			},(address1, address2) -> {
                System.out.println("duplicate key found!");
                return address1;
            }));
		}
		return pollUserVoteMap;
	}

	Map<Long, User> getPollCreatorMap(List<Poll> polls) {
		// Get Poll Creator details of the given list of polls
		List<Long> creatorIds = polls.stream().map(Poll::getCreatedBy).distinct().collect(Collectors.toList());

		List<User> creators = userRepository.findByIdIn(creatorIds);
		Map<Long, User> creatorMap = creators.stream().collect(Collectors.toMap(User::getId, Function.identity()));

		return creatorMap;
	}
}
